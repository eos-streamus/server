package com.eos.streamus.models;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

abstract class Resource implements Entity, SavableEntity {
  //#region Static attributes
  protected static final String TABLE_NAME = "Resource";
  protected static final String ID_COLUMN = "id";
  protected static final String PATH_COLUMN = "path";
  protected static final String NAME_COLUMN = "name";
  protected static final String DURATION_COLUMN = "duration";
  protected static final String CREATED_AT_COLUMN = "createdAt";
  //#endregion Static attributes

  //#region Instance attributes
  private Integer id;
  private String path;
  private String name;
  private Timestamp createdAt;
  private Integer duration;
  //#endregion Instance attributes

  //#region Constructors
  protected Resource(Integer id, String path, String name, Timestamp createdAt, Integer duration) {
    this(path, name, duration);
    this.createdAt = createdAt;
    this.id = id;
  }

  protected Resource(String path, String name, Integer duration) {
    this.path = path;
    this.name = name;
    this.duration = duration;
  }
  //#endregion Constructors

  //#region Getters and Setters
  public Integer getId() {
    return this.id;
  }

  protected void setId(Integer id) {
    this.id = id;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
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

  public void setCreatedAt(Timestamp timestamp) {
    this.createdAt = timestamp;
  }

  public Integer getDuration() {
    return duration;
  }

  public void setDuration(Integer duration) {
    this.duration = duration;
  }

  @Override
  public String getPrimaryKeyName() {
    return ID_COLUMN;
  }

  @Override
  public String getTableName() {
    return TABLE_NAME;
  }
  //#endregion Getters and Setters

  //#region Database operations
  @Override
  public void save(Connection connection) throws SQLException {
    if (this.id == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s(?::varchar(1041), ?::varchar(200), ?)", getCreationFunctionName()))) {
        preparedStatement.setString(1, getPath());
        preparedStatement.setString(2, getName());
        preparedStatement.setInt(3, getDuration());
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          if (!resultSet.next()) {
            throw new SQLException("Could not execute statement");
          }
          this.setId(resultSet.getInt(ID_COLUMN));
          this.setCreatedAt(resultSet.getTimestamp(CREATED_AT_COLUMN));
        }
      }
    } else {
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("update %s set %s = ?, %s = ?, %s = ? where %s = ?;", TABLE_NAME, PATH_COLUMN, NAME_COLUMN, DURATION_COLUMN, ID_COLUMN))) {
        preparedStatement.setString(1, path);
        preparedStatement.setString(2, name);
        preparedStatement.setLong(3, duration);
        preparedStatement.setInt(4, id);
        preparedStatement.execute();
      }
    }
  }
  //#endregion Database operations

  //#region String representations
  @Override
  public String toString() {
    return String.format("{%s}", getFieldNamesAndValuesString());
  }

  public String getFieldNamesAndValuesString() {
    DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");
    return String.format("%s: %d, %s: %s, %s: %s, %s: %s, %s: %s",
      ID_COLUMN,
      id,
      PATH_COLUMN,
      path,
      NAME_COLUMN,
      name,
      DURATION_COLUMN,
      duration,
      CREATED_AT_COLUMN,
      dateFormat.format(createdAt));
  }
  //#endregion String representations

  //#region Equals
  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || o.getClass() != getClass()) {
      return false;
    }
    Resource oResource = (Resource) o;
    return
      oResource.id.equals(id) &&
        oResource.path.equals(path) &&
        oResource.name.equals(name) &&
        oResource.createdAt.equals(createdAt) &&
        oResource.duration.equals(duration);
  }
  //#endregion Equals
}
