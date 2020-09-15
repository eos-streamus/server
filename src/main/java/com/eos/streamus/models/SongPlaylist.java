package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class SongPlaylist extends SongCollection {
  //#region Static attributes
  /** Creation function name. */
  private static final String CREATION_FUNCTION_NAME = "createSongPlaylist";
  /** View name. */
  private static final String VIEW_NAME = "vSongPlaylist";
  /** User id column name. */
  private static final String USER_ID_COLUMN = "idUser";
  //#endregion Static attributes

  //#region Instance attributes
  /** User this playlist is owned by. */
  private final User user;
  //#endregion Instance attributes

  //#region Constructor
  private SongPlaylist(final Integer id, final String name, final Timestamp createdAt,
                       final Timestamp updatedAt, final User user, final Track... tracks) {
    super(id, name, createdAt, updatedAt, tracks);
    this.user = user;
  }

  public SongPlaylist(final String name, final User user, final Track... tracks) {
    super(name, tracks);
    this.user = user;
  }
  //#endregion Constructors

  //#region Getters and Setters
  @Override
  public String creationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }

  public User getUser() {
    return user;
  }
  //#endregion Getters and Setters

  //#region Database operations
  @Override
  public void save(final Connection connection) throws SQLException {
    if (this.getId() == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format(
              "select * from %s(?::varchar(200), ?);",
              CREATION_FUNCTION_NAME
          )
      )) {
        preparedStatement.setString(1, getName());
        preparedStatement.setInt(2, user.getId());
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          resultSet.next();
          this.setId(resultSet.getInt("id"));
          this.setCreatedAt(resultSet.getTimestamp(Collection.CREATED_AT_COLUMN));
          this.setUpdatedAt(resultSet.getTimestamp(Collection.UPDATED_AT_COLUMN));
        }
        if (!getTracks().isEmpty()) {
          this.save(connection);
        }
      }
    } else {
      super.save(connection);
    }
  }

  public static SongPlaylist findById(final Integer id, final Connection connection)
      throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select * from %s where %s = ?;",
            VIEW_NAME,
            Collection.PRIMARY_KEY_NAME
        )
    )) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (!resultSet.next()) {
          throw new NoResultException();
        }
        // Collection attributes
        String name = resultSet.getString(Collection.NAME_COLUMN);
        Timestamp createdAt = resultSet.getTimestamp(Collection.CREATED_AT_COLUMN);
        Timestamp updatedAt = resultSet.getTimestamp(Collection.UPDATED_AT_COLUMN);

        // User
        Integer userId = resultSet.getInt(USER_ID_COLUMN);
        User user = User.findById(userId, connection);

        SongPlaylist songPlaylist = new SongPlaylist(
            id,
            name,
            createdAt,
            updatedAt,
            user
        );

        // Songs
        int firstTrackNumber = resultSet.getInt(Track.TRACK_NUMBER_COLUMN);
        if (!resultSet.wasNull()) {
          songPlaylist.addTrack(
              songPlaylist.new Track(
                  firstTrackNumber,
                  Song.findById(resultSet.getInt(Track.ID_SONG_COLUMN), connection)
              )
          );
          while (resultSet.next()) {
            songPlaylist.addTrack(
                songPlaylist.new Track(
                    resultSet.getInt(Track.TRACK_NUMBER_COLUMN),
                    Song.findById(resultSet.getInt(Track.ID_SONG_COLUMN), connection)
                )
            );
          }
        }
        return songPlaylist;
      }
    }
  }

  public static List<SongPlaylist> all(final Connection connection) throws SQLException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(
      String.format(
        "select distinct %s from %s",
        "idSongCollection",
        "songplaylist"
      )
    )) {
      List<SongPlaylist> playlists = new ArrayList<>();
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          try {
            playlists.add(findById(resultSet.getInt(1), connection));
          } catch (NoResultException e) {
            // Won't happen
          }
        }
      }
      return playlists;
    }
  }
  //#endregion Database operations

  //#region Equals
  @Override
  public int hashCode() {
    return getId();
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    return super.equals(o) && ((SongPlaylist) o).user.equals(user);
  }
  //#endregion Equals
}
