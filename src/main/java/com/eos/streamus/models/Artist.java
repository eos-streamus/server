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
  /** Table name in the database. */
  public static final String TABLE_NAME = "Artist";
  /** Primary key name in the database. */
  public static final String PRIMARY_KEY_NAME = "id";
  /** Name column in the database. */
  public static final String NAME_COLUMN = "name";
  /** Album artist table name in the database. */
  public static final String ALBUM_ARTIST_TABLE_NAME = "AlbumArtist";
  /** Album artist album id in the database. */
  public static final String ALBUM_ARTIST_ALBUM_ID = "idAlbum";
  /** Album artist artist id in the database. */
  public static final String ALBUM_ARTIST_ARTIST_ID = "idArtist";
  //#endregion Static attributes

  //#region Instance attributes
  /** Id of this Artist. */
  private Integer id;
  /** Name of this Artist. */
  private String name;
  /** List of contributing {@link Album}s. */
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

  /** {@inheritDoc} */
  @Override
  public Integer getId() {
    return id;
  }

  /**
   * Set the Id of this Artist.
   *
   * @param id Id to set.
   */
  protected void setId(final Integer id) {
    this.id = id;
  }

  /** @return Artist's name. */
  public String getName() {
    return name;
  }

  /**
   * Set the name of this Artist.
   *
   * @param name Name of the Artist.
   */
  public void setName(final String name) {
    this.name = name;
  }

  /** {@inheritDoc} */
  @Override
  public String tableName() {
    return TABLE_NAME;
  }

  /** {@inheritDoc} */
  @Override
  public String primaryKeyName() {
    return PRIMARY_KEY_NAME;
  }

  public final List<Album> getAlbums() {
    return albums;
  }
  //#endregion Getters and Setters

  //#region Database operations

  /** {@inheritDoc} */
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

  /**
   * Fetches the Artist's {@link Album}s from the database.
   *
   * @param connection {@link Connection} to use to perform the operation.
   * @throws SQLException If an error occurred while performing the database operation.
   */
  public void fetchAlbums(final Connection connection) throws SQLException {
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
  /** {@inheritDoc} */
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
  /** @return This Artist's hashCode, i.e. its id. */
  @Override
  public int hashCode() {
    return id;
  }

  /**
   * Returns whether the given Object is equal.
   * Will be equal if:
   * - Not null
   * - Same class
   * - Same name (equal or both null)
   * - Same ids
   * @param obj Object to compare.
   * @return True if all conditions are met.
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
