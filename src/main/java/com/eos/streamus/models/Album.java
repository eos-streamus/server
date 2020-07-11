package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Album extends SongCollection {
  //#region Static attributes
  /** Creation function name in database. */
  private static final String CREATION_FUNCTION_NAME = "createAlbum";
  /** Table name in database. */
  private static final String TABLE_NAME = "Album";
  /** Primary key column name in table. */
  private static final String PRIMARY_KEY_NAME = "idSongCollection";
  /** Release date column name. */
  private static final String RELEASE_DATE_COLUMN = "releaseDate";
  /** AlbumArtist association table name. */
  private static final String ALBUM_ARTIST_TABLE_NAME = "AlbumArtist";
  /** Artist id column name in AlbumArtist. */
  private static final String ALBUM_ARTIST_ARTIST_ID_COLUMN = "idArtist";
  /** Album id column name in AlbumArtist. */
  private static final String ALBUM_ARTIST_ALBUM_ID_COLUMN = "idAlbum";
  /** View name. */
  private static final String VIEW_NAME = "vAlbum";
  /** Id column name in view. */
  private static final String VIEW_ID = "id";
  /** Song id column name in view. */
  private static final String VIEW_SONG_ID = "idSong";
  /** Track number column name in view. */
  private static final String TRACK_NUMBER_COLUMN = "trackNumber";
  /** Song name column name in view. */
  private static final String VIEW_SONG_NAME_COLUMN = "songName";
  /** Song created at timestamp column name in view. */
  private static final String SONG_CREATED_AT_COLUMN = "songCreatedAt";
  //#endregion Static attributes

  //#region Instance attributes
  /** List of {@link Artist} of the Album. */
  private final List<Artist> artists = new ArrayList<>();
  /** Release date of the Album. */
  private final Date releaseDate;
  //#endregion Instance attributes

  //#region Constructors
  private Album(final Integer id, final String name, final Date releaseDate, final Timestamp createdAt,
                final Timestamp updatedAt, final Track... tracks) {
    super(id, name, createdAt, updatedAt, tracks);
    this.releaseDate = releaseDate;
  }

  public Album(final String name, final Date releaseDate, final Track... tracks) {
    super(name, tracks);
    this.releaseDate = releaseDate;
  }

  @Override
  public final String creationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }

  @Override
  public final String tableName() {
    return TABLE_NAME;
  }
  //#endregion Constructors

  //#region Accessors

  /**
   * Add an {@link Artist} to this Album.
   *
   * @param artist Artist to add.
   */
  public void addArtist(final Artist artist) {
    if (artist == null) {
      throw new NullPointerException();
    }
    artists.add(artist);
  }

  /** @return List of {@link Artist}s of this Album. */
  public List<Artist> getArtists() {
    return artists;
  }

  @Override
  public final String primaryKeyName() {
    return PRIMARY_KEY_NAME;
  }
  //#endregion Accessors

  //#region Database operations
  @Override
  public final void save(final Connection connection) throws SQLException {
    if (this.getId() == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format(
              "select * from %s(?, ?);", CREATION_FUNCTION_NAME
          )
      )) {
        preparedStatement.setString(1, getName());
        preparedStatement.setDate(2, releaseDate);
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          resultSet.next();
          setId(resultSet.getInt(Collection.PRIMARY_KEY_NAME));
          setCreatedAt(resultSet.getTimestamp(Collection.CREATED_AT_COLUMN));
          setUpdatedAt(resultSet.getTimestamp(Collection.UPDATED_AT_COLUMN));
        }
      }
    }
    for (Artist artist : artists) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format(
              "insert into %s(%s, %s) values (?, ?) on conflict (%s, %s) do nothing;",
              ALBUM_ARTIST_TABLE_NAME,
              ALBUM_ARTIST_ALBUM_ID_COLUMN,
              ALBUM_ARTIST_ARTIST_ID_COLUMN,
              ALBUM_ARTIST_ALBUM_ID_COLUMN,
              ALBUM_ARTIST_ARTIST_ID_COLUMN
          )
      )) {
        preparedStatement.setInt(1, getId());
        preparedStatement.setInt(2, artist.getId());
        preparedStatement.execute();
      }
    }
    super.save(connection);
  }

  public static Album findById(final Integer id, final Connection connection) throws SQLException, NoResultException {
    Album album;
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select * from %s where %s = ?;",
            VIEW_NAME,
            VIEW_ID
        )
    )) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (!resultSet.next()) {
          throw new NoResultException();
        }
        album = new Album(
            id,
            resultSet.getString(Collection.NAME_COLUMN),
            resultSet.getDate(RELEASE_DATE_COLUMN),
            resultSet.getTimestamp(Collection.CREATED_AT_COLUMN),
            resultSet.getTimestamp(Collection.UPDATED_AT_COLUMN)
        );
        do {
          if (resultSet.getInt(VIEW_SONG_ID) != 0) {
            album.addTrack(album.new Track(
                resultSet.getInt(TRACK_NUMBER_COLUMN),
                new Song(
                    resultSet.getInt(VIEW_SONG_ID),
                    resultSet.getString(Resource.PATH_COLUMN),
                    resultSet.getString(VIEW_SONG_NAME_COLUMN),
                    resultSet.getTimestamp(SONG_CREATED_AT_COLUMN),
                    resultSet.getInt(Resource.DURATION_COLUMN)
                )
            ));
          }
        } while (resultSet.next());
      }
    }
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select * from %s where %s = ?;", ALBUM_ARTIST_TABLE_NAME, ALBUM_ARTIST_ALBUM_ID_COLUMN
        )
    )) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          album.addArtist(ArtistDAO.findById(resultSet.getInt(ALBUM_ARTIST_ARTIST_ID_COLUMN), connection));
        }
      }
    }
    return album;
  }
  //#endregion Database operations

  //#region Equals
  @Override
  public final int hashCode() {
    return getId();
  }

  @Override
  public final boolean equals(final Object o) {
    if (!super.equals(o)) {
      return false;
    }
    Album album = (Album) o;
    if (!releaseDate.equals(((Album) o).releaseDate)) {
      return false;
    }
    if (artists.size() != album.artists.size()) {
      return false;
    }
    return artists.containsAll(album.artists);
  }
  //#endregion Equals
}
