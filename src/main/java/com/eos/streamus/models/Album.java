package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Album extends SongCollection {
  //#region Static attributes
  private static final String CREATION_FUNCTION_NAME = "createAlbum";
  private static final String TABLE_NAME = "Album";
  private static final String PRIMARY_KEY_NAME = "idSongCollection";
  private static final String RELEASE_DATE_COLUMN = "releaseDate";
  private static final String ALBUM_ARTIST_TABLE_NAME = "AlbumArtist";
  private static final String ALBUM_ARTIST_ARTIST_ID_COLUMN = "idArtist";
  private static final String ALBUM_ARTIST_ALBUM_ID_COLUMN = "idAlbum";
  private static final String VIEW_NAME = "vAlbum";
  private static final String VIEW_ID = "id";
  private static final String VIEW_SONG_ID = "idSong";
  private static final String TRACK_NUMBER_COLUMN = "trackNumber";
  private static final String VIEW_SONG_NAME_COLUMN = "songName";
  private static final String SONG_CREATED_AT_COLUMN = "songCreatedAt";
  //#endregion Static attributes

  //#region Instance attributes
  private List<Artist> artists = new ArrayList<>();
  private Date releaseDate;
  //#endregion Instance attributes

  //#region Constructors
  private Album(Integer id, String name, Date releaseDate, Timestamp createdAt, Timestamp updatedAt, Track... tracks) {
    super(id, name, createdAt, updatedAt, tracks);
    this.releaseDate = releaseDate;
  }

  public Album(String name, Date releaseDate, Track... tracks) {
    super(name, tracks);
    this.releaseDate = releaseDate;
  }

  @Override
  public String getCreationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }

  @Override
  public String getTableName() {
    return TABLE_NAME;
  }
  //#endregion Constructors

  //#region Accessors
  public void addArtist(Artist artist) {
    if (artist == null) {
      throw new NullPointerException();
    }
    artists.add(artist);
  }

  public List<Artist> getArtists() {
    return artists;
  }

  @Override
  public String getPrimaryKeyName() {
    return PRIMARY_KEY_NAME;
  }
  //#endregion Accessors

  //#region Database operations
  @Override
  public void save(Connection connection) throws SQLException {
    if (this.getId() == null) {
      StringBuilder artistIds = new StringBuilder();
      boolean first = true;
      for (Artist artist : artists) {
        if (artist.getId() == null) {
          artist.save(connection);
        }
        if (first) {
          first = false;
        } else {
          artistIds.append(", ");
        }
        artistIds.append(artist.getId());
      }
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s(?, ?%s);", CREATION_FUNCTION_NAME, artistIds))) {
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

  public static Album findById(Integer id, Connection connection) throws SQLException, NoResultException {
    Album album = null;
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
    try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s where %s = ?;", ALBUM_ARTIST_TABLE_NAME, ALBUM_ARTIST_ALBUM_ID_COLUMN))) {
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

  //#region String representations
  @Override
  public String toString() {
    return String.format("{%s}", getFieldNamesAndValuesString());
  }

  @Override
  public String getFieldNamesAndValuesString() {
    return String.format(
      "%s, %s : %s",
      super.getFieldNamesAndValuesString(),
      RELEASE_DATE_COLUMN,
      releaseDate
    );
  }
  //#endregion String representations

  //#region Equals
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object o) {
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
