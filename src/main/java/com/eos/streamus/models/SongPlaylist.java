package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;

public final class SongPlaylist extends SongCollection {
  //#region Static attributes
  /** Creation function name in the database. */
  private static final String CREATION_FUNCTION_NAME = "createSongPlaylist";
  /** View name in the database. */
  private static final String VIEW_NAME = "vSongPlaylist";
  /** User id column in the database. */
  private static final String USER_ID_COLUMN = "idUser";
  //#endregion Static attributes

  //#region Instance attributes
  /** Owner {@link User} of this SongPlaylist. */
  private final User user;
  //#endregion Instance attributes

  //#region Constructors
  public SongPlaylist(final String name, final User user) {
    super(name);
    this.user = user;
  }
  //#endregion Constructors

  //#region Getters and Setters

  /** {@inheritDoc} */
  @Override
  public String creationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }

  /** @return {@link User} that owns this SongPlaylist. */
  public User getUser() {
    return user;
  }
  //#endregion Getters and Setters

  //#region Database operations

  /** {@inheritDoc} */
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

  /**
   * Finds a SongPlaylist by id in the database.
   *
   * @param id         Id of the SongPlaylist to find.
   * @param connection {@link Connection} to use to perform the operation.
   * @return Found SongPlaylist.
   * @throws NoResultException If no SongPlaylist by this id was found.
   * @throws SQLException      If an error occurred while performing the database operation.
   */
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

        SongPlaylist songPlaylist = new SongPlaylist(name, user);
        songPlaylist.setId(id);
        songPlaylist.setCreatedAt(createdAt);
        songPlaylist.setUpdatedAt(updatedAt);

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
  //#endregion Database operations

  //#region Equals

  /**
   * @return HashCode of this SongPlaylist, i.e. its id.
   */
  @Override
  public int hashCode() {
    return getId();
  }

  /**
   * Returns whether the given Object is equal to this SongPlaylist.
   * Will be equal if:
   * - All {@link SongCollection}'s equality conditions are met.
   * - Equal Owner {@link User}
   *
   * @param o Object to compare
   * @return True if all conditions are met.
   */
  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    return super.equals(o) && ((SongPlaylist) o).user.equals(user);
  }
  //#endregion Equals
}
