package com.eos.streamus.models;

import com.eos.streamus.utils.Pair;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;

abstract class Collection implements SavableDeletableEntity {
  //#region Static attributes
  protected static final String TABLE_NAME = "Collection";
  protected static final String PRIMARY_KEY_NAME = "id";
  protected static final String NAME_COLUMN = "name";
  protected static final String CREATED_AT_COLUMN = "createdAt";
  protected static final String UPDATED_AT_COLUMN = "updatedAt";
  //#endregion Static attributes

  //#region Instance attributes
  private Integer id;
  private String name;
  private Timestamp createdAt;
  private Timestamp updatedAt;
  //#endregion Instance attributes

  //#region Constructors
  protected Collection(Integer id, String name, Timestamp createdAt, Timestamp updatedAt) {
    this.id = id;
    this.name = name;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  protected Collection(String name) {
    this.name = name;
  }
  //#endregion Constructors

  //#region Getters and Setters
  @Override
  public String getTableName() {
    return TABLE_NAME;
  }

  @Override
  public String getPrimaryKeyName() {
    return PRIMARY_KEY_NAME;
  }

  @Override
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
  }

  public Timestamp getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Timestamp updatedAt) {
    this.updatedAt = updatedAt;
  }

  public final List<Pair<Integer, Resource>> getContent() {
    List<Pair<Integer, Resource>> content = getSpecificContent();
    content.sort(Comparator.comparing(Pair::getKey));
    return content;
  }

  protected abstract List<Pair<Integer, Resource>> getSpecificContent();

  //#endregion Getters and Setters

  //#region Database operations
  @Override
  public void save(Connection connection) throws SQLException {
    if (this.id == null) {
      throw new NullPointerException("Collection#save can only be called on update");
    }
    try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("update %s set %s = ? where %s = ? returning %s;", TABLE_NAME, NAME_COLUMN, PRIMARY_KEY_NAME, UPDATED_AT_COLUMN))) {
      preparedStatement.setString(1, name);
      preparedStatement.setInt(2, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        resultSet.next();
        this.updatedAt = resultSet.getTimestamp(1);
      }
    }
  }
  //#endregion Database operations

  //#region String representations
  @Override
  public String getFieldNamesAndValuesString() {
    DateFormat timestampFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");
    return String.format(
      "%s: %d, %s: %s, %s: %s, %s, %s",
      PRIMARY_KEY_NAME,
      id,
      NAME_COLUMN,
      name,
      CREATED_AT_COLUMN,
      timestampFormat.format(createdAt),
      UPDATED_AT_COLUMN,
      timestampFormat.format(updatedAt)
    );
  }

  @Override
  public String toString() {
    return String.format("{%s}", getFieldNamesAndValuesString());
  }
  //#endregion String representations

  //#region Equals
  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || o.getClass() != this.getClass()) {
      return false;
    }
    Collection collection = (Collection) o;
    return
      collection.id.equals(id) &&
        collection.name.equals(name) &&
        collection.createdAt.equals(createdAt) &&
        collection.updatedAt.equals(updatedAt);
  }
  //#endregion Equals
}
