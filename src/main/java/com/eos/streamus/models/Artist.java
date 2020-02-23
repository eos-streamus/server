package com.eos.streamus.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class Artist implements SavableDeletableEntity {
  //#region Static attributes
  public static final String TABLE_NAME = "Artist";
  public static final String PRIMARY_KEY_NAME = "id";
  public static final String NAME_COLUMN = "name";
  //#endregion Static attributes

  //#region Instance attributes
  private Integer id;
  private String name;
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
  public String getTableName() {
    return TABLE_NAME;
  }

  @Override
  public String getPrimaryKeyName() {
    return PRIMARY_KEY_NAME;
  }
  //#endregion Getters and Setters

  //#region Database operations
  @Override
  public void save(Connection connection) throws SQLException {
    if (this.getId() == null) {
      throw new NullPointerException("Artist#save can only be called on update");
    }
    try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("update %s set %s = ? where %s = ?", TABLE_NAME, NAME_COLUMN, PRIMARY_KEY_NAME))) {
      preparedStatement.setString(1, name);
      preparedStatement.setInt(2, id);
      preparedStatement.execute();
    }
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
      "%s: %d, %s: %s",
      PRIMARY_KEY_NAME,
      id,
      NAME_COLUMN,
      name
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
