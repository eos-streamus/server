package com.eos.streamus.models;

import java.sql.*;

public abstract class Resource implements SavableDeletableEntity {
  //#region Static attributes
  /** Table name in database. */
  public static final String TABLE_NAME = "Resource";
  /** id column name. */
  protected static final String ID_COLUMN = "id";
  /** path column name. */
  public static final String PATH_COLUMN = "path";
  /** name column name. */
  protected static final String NAME_COLUMN = "name";
  /** duration column name. */
  protected static final String DURATION_COLUMN = "duration";
  /** createdAt column name. */
  protected static final String CREATED_AT_COLUMN = "createdAt";
  //#endregion Static attributes

  //#region Instance attributes
  /** id of this instance. */
  private Integer id;
  /** path of this instance. */
  private String path;
  /** name of this instance. */
  private String name;
  /** createdAt of this instance. */
  private Timestamp createdAt;
  /** duration of this instance. */
  private Integer duration;
  //#endregion Instance attributes

  //#region Constructors
  protected Resource(final Integer id, final String path, final String name,
                     final Timestamp createdAt, final Integer duration) {
    this(path, name, duration);
    this.createdAt = createdAt;
    this.id = id;
  }

  protected Resource(final String path, final String name, final Integer duration) {
    this.path = path;
    this.name = name;
    this.duration = duration;
  }
  //#endregion Constructors

  //#region Getters and Setters
  public final Integer getId() {
    return this.id;
  }

  protected final void setId(final Integer id) {
    this.id = id;
  }

  public final String getPath() {
    return path;
  }

  public final void setPath(final String path) {
    this.path = path;
  }

  public final String getName() {
    return name;
  }

  public final void setName(final String name) {
    this.name = name;
  }

  public final Timestamp getCreatedAt() {
    return createdAt;
  }

  public final void setCreatedAt(final Timestamp timestamp) {
    this.createdAt = timestamp;
  }

  public final Integer getDuration() {
    return duration;
  }

  public final void setDuration(final Integer duration) {
    this.duration = duration;
  }

  /** @return Primary key name. */
  @Override
  public String primaryKeyName() {
    return ID_COLUMN;
  }

  /** @return Table name. */
  @Override
  public String tableName() {
    return TABLE_NAME;
  }
  //#endregion Getters and Setters

  //#region Database operations

  /**
   * Save this resource to database.
   *
   * @param connection {@link Connection} to use to save.
   * @throws SQLException if an error occurs.
   */
  @Override
  public void save(final Connection connection) throws SQLException {
    if (this.id == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format(
              "select * from %s(?::varchar(1041), ?::varchar(200), ?)",
              creationFunctionName()
          )
      )) {
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
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format(
              "update %s set %s = ?, %s = ?, %s = ? where %s = ?;",
              TABLE_NAME,
              PATH_COLUMN,
              NAME_COLUMN,
              DURATION_COLUMN,
              ID_COLUMN
          )
      )) {
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
  /** @return String representation of this instance. */
  @Override
  public String toString() {
    return defaultToString();
  }
  //#endregion String representations

  //#region Equals
  /** @return hashcode. */
  @Override
  public int hashCode() {
    return id;
  }

  /**
   * Test if this instance is equal to given object.
   *
   * @param o Object to test.
   * @return If they are equal.
   */
  @Override
  public boolean equals(final Object o) {
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
