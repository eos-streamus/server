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
  /** Artist table name in database. */
  public static final String TABLE_NAME = "Artist";
  /** id column name in table. */
  public static final String PRIMARY_KEY_NAME = "id";
  /** name column name in table. */
  public static final String NAME_COLUMN = "name";
  /** AlbumArtist column name in table. */
  public static final String ALBUM_ARTIST_TABLE_NAME = "AlbumArtist";
  /** AlbumArtist association table name. */
  public static final String ALBUM_ARTIST_ALBUM_ID = "idAlbum";
  /** Artist id column name in AlbumArtist. */
  public static final String ALBUM_ARTIST_ARTIST_ID = "idArtist";
  //#endregion Static attributes

  //#region Instance attributes
  /** Id of Artist. */
  private Integer id;
  /** Name of the Artist. */
  private String name;
  /** List of albums of Artist. */
  private final List<Album> albums = new ArrayList<>();
  //#endregion Instance attributes

  //#region Constructors
  protected Artist(final Integer id, final String name) {
    this.id = id;
    this.name = name;
  }

  protected Artist(final String name) {
    this.name = name;
  }
  //#endregion Constructors

  //#region Getters and Setters
  @Override
  public final Integer getId() {
    return id;
  }

  protected final void setId(final Integer id) {
    this.id = id;
  }

  public final String getName() {
    return name;
  }

  public final void setName(final String name) {
    this.name = name;
  }

  /** @return Table name in database. */
  @Override
  public String tableName() {
    return TABLE_NAME;
  }

  /** @return Primary key column name in database. */
  @Override
  public String primaryKeyName() {
    return PRIMARY_KEY_NAME;
  }

  public final List<Album> getAlbums() {
    return albums;
  }
  //#endregion Getters and Setters

  //#region Database operations

  /**
   * Save this Artist in database.
   *
   * @param connection {@link Connection} to use to save.
   * @throws SQLException if an error occurs.
   */
  @Override
  public void save(final Connection connection) throws SQLException {
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

  public final void fetchAlbums(final Connection connection) throws SQLException {
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
  public final String toString() {
    return String.format(
        "%s[%s=%d]",
        getClass().getName(),
        primaryKeyName(),
        id
    );
  }
  //#endregion

  //#region Equals

  /** @return hashcode of instance. */
  @Override
  public int hashCode() {
    return id;
  }

  /**
   * Checks if this instance is equal to given one.
   * @param obj Object to test.
   * @return If the instances are equal.
   */
  @Override
  public boolean equals(final Object obj) {
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
