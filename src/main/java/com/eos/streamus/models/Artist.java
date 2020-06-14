package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class Artist implements SavableDeletableEntity {
  //#region Static attributes
  public static final String TABLE_NAME = "Artist";
  public static final String PRIMARY_KEY_NAME = "id";
  public static final String NAME_COLUMN = "name";
  public static final String ALBUM_ARTIST_TABLE_NAME = "AlbumArtist";
  public static final String ALBUM_ARTIST_ALBUM_ID = "idAlbum";
  public static final String ALBUM_ARTIST_ARTIST_ID = "idArtist";
  //#endregion Static attributes

  //#region Instance attributes
  private Integer id;
  private String name;
  private final List<Album> albums = new ArrayList<>();
  //#endregion Instance attributes

  //#region Constructors
  protected Artist(Integer id, String name) {
    this.id = id;
    this.name = name;
  }

  protected Artist(String name) {
    this.name = name;
  }
  //#endregion Constructors

  //#region Getters and Setters
  @Override
  public Integer getId() {
    return id;
  }

  protected void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String tableName() {
    return TABLE_NAME;
  }

  @Override
  public String primaryKeyName() {
    return PRIMARY_KEY_NAME;
  }

  public final List<Album> getAlbums() {
    return albums;
  }
  //#endregion Getters and Setters

  //#region Database operations
  @Override
  public void save(Connection connection) throws SQLException {
    if (this.getId() == null) {
      throw new NullPointerException("Artist#save can only be called on update");
    }
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "update %s set %s = ? where %s = ?", TABLE_NAME, NAME_COLUMN, PRIMARY_KEY_NAME
        )
    )) {
      preparedStatement.setString(1, name);
      preparedStatement.setInt(2, id);
      preparedStatement.execute();
    }
  }

  public void fetchAlbums(Connection connection) throws SQLException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select * from %s where %s = ?", ALBUM_ARTIST_TABLE_NAME, ALBUM_ARTIST_ARTIST_ID
        )
    )) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          try {
            albums.add(Album.findById(resultSet.getInt(ALBUM_ARTIST_ALBUM_ID), connection));
          } catch (NoResultException e) {
            // Should never happen
            Logger.getLogger(Artist.class.getName()).severe(e.getMessage());
          }
        }
      }
    }
  }
  //#endregion Database operations

  //#region String representations
  @Override
  public String toString() {
    return String.format(
        "%s[%s=%d]",
        getClass().getName(),
        primaryKeyName(),
        id
    );
  }
  //#endregion

  //#region Equals
  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj.getClass() != this.getClass()) {
      return false;
    }
    Artist artist = (Artist) obj;
    if (name == null && artist.name != null || name != null && artist.name == null) {
      return false;
    }
    return this.id.equals(artist.id) && (name == null || name.equals(artist.name));
  }
  //#endregion Equals
}
