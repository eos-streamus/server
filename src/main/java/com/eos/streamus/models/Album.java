package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class Album extends SongCollection {
  //#region Static attributes
  /** Creation_function_name in the database. */
  private static final String CREATION_FUNCTION_NAME = "createAlbum";
  /** Table name in the database. */
  private static final String TABLE_NAME = "Album";
  /** Primary key name in the database. */
  private static final String PRIMARY_KEY_NAME = "idSongCollection";
  /** Release date column in the database. */
  private static final String RELEASE_DATE_COLUMN = "releaseDate";
  /** Album artist table name in the database. */
  private static final String ALBUM_ARTIST_TABLE_NAME = "AlbumArtist";
  /** Album artist artist id column in the database. */
  private static final String ALBUM_ARTIST_ARTIST_ID_COLUMN = "idArtist";
  /** Album artist album id column in the database. */
  private static final String ALBUM_ARTIST_ALBUM_ID_COLUMN = "idAlbum";
  /** View name in the database. */
  private static final String VIEW_NAME = "vAlbum";
  /** View id in the database. */
  private static final String VIEW_ID = "id";
  /** View song id in the database. */
  private static final String VIEW_SONG_ID = "idSong";
  /** Track number column in the database. */
  private static final String TRACK_NUMBER_COLUMN = "trackNumber";
  /** View song name column in the database. */
  private static final String VIEW_SONG_NAME_COLUMN = "songName";
  /** Song created at column in the database. */
  private static final String SONG_CREATED_AT_COLUMN = "songCreatedAt";
  //#endregion Static attributes

  //#region Instance attributes
  /** List of contributing {@link Artist}s of this Album. */
  private final List<Artist> artists = new ArrayList<>();
  /** Release date of this Album. */
  private final Date releaseDate;
  //#endregion Instance attributes

  //#region Constructors
  public Album(final String name, final Date releaseDate) {
    super(name);
    this.releaseDate = releaseDate;
  }
  //#endregion Constructors

  /** {@inheritDoc} */
  @Override
  public String creationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }

  /** {@inheritDoc} */
  @Override
  public String tableName() {
    return TABLE_NAME;
  }
  //#endregion Constructors

  //#region Accessors

  /**
   * Add an {@link Artist} as contributor to the Album.
   *
   * @param artist {@link Artist} to add.
   */
  public void addArtist(final Artist artist) {
    if (artist == null) {
      throw new NullPointerException();
    }
    artists.add(artist);
  }

  /** @return List of contributing {@link Artist}s of this Album. */
  public List<Artist> getArtists() {
    return artists;
  }

  /** {@inheritDoc} */
  @Override
  public String primaryKeyName() {
    return PRIMARY_KEY_NAME;
  }
  //#endregion Accessors

  //#region Database operations

  /** {@inheritDoc} */
  @Override
  public void save(final Connection connection) throws SQLException {
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

  /**
   * Finds an Album by id in the database.
   *
   * @param id         Id of the Album to find.
   * @param connection {@link Connection} to use to perform the operation.
   * @return Found Album.
   * @throws SQLException      If an error occurred while performing the database operation.
   * @throws NoResultException If no Album was found
   */
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
        album = new Album(resultSet.getString(Collection.NAME_COLUMN), resultSet.getDate(RELEASE_DATE_COLUMN));
        album.setId(id);
        album.setCreatedAt(resultSet.getTimestamp(Collection.CREATED_AT_COLUMN));
        album.setUpdatedAt(resultSet.getTimestamp(Collection.UPDATED_AT_COLUMN));
        do {
          if (resultSet.getInt(VIEW_SONG_ID) != 0) {
            Song song = new Song(
                resultSet.getString(Resource.PATH_COLUMN),
                resultSet.getString(VIEW_SONG_NAME_COLUMN),
                resultSet.getInt(Resource.DURATION_COLUMN)
            );
            song.setId(resultSet.getInt(VIEW_SONG_ID));
            song.setCreatedAt(resultSet.getTimestamp(SONG_CREATED_AT_COLUMN));
            album.addTrack(album.new Track(resultSet.getInt(TRACK_NUMBER_COLUMN), song));
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

  /** @return This Album's hashCode, i.e. its id. */
  @Override
  public int hashCode() {
    return getId();
  }

  /**
   * Returns whether the given object is equal to this Album.
   * Will be equal if:
   * - All of {@link SongCollection}'s equality conditions are met
   * - Same release data
   * - Same {@link Artist}s
   *
   * @param o Object to compare
   * @return True if all conditions are met.
   */
  @Override
  public boolean equals(final Object o) {
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
