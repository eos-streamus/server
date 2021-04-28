package com.eos.streamus.models;

import java.sql.*;

public abstract class Resource implements SavableDeletableEntity {
  //#region Static attributes
  /** Table name in database. */
  public static final String TABLE_NAME = "Resource";
  /** Id column name in database. */
  protected static final String ID_COLUMN = "id";
  /** Path column name in database. */
  public static final String PATH_COLUMN = "path";
  /** Name column name in database. */
  protected static final String NAME_COLUMN = "name";
  /** Duration column name in database. */
  protected static final String DURATION_COLUMN = "duration";
  /** Created at column name in database. */
  protected static final String CREATED_AT_COLUMN = "createdAt";
  //#endregion Static attributes

  //#region Instance attributes
  /** Id of the Resource. */
  private Integer id;
  /** Path of the Resource. */
  private String path;
  /** Name of the Resource. */
  private String name;
  /** CreatedAt of the Resource. */
  private Timestamp createdAt;
  /** Duration of the Resource. */
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

  /** {@inheritDoc} */
  public Integer getId() {
    return this.id;
  }

  /**
   * Set resource id.
   *
   * @param id id to set.
   */
  protected void setId(final Integer id) {
    this.id = id;
  }

  /** @return Path of resource. */
  public String getPath() {
    return path;
  }

  /**
   * Set path of resource.
   *
   * @param path Path to set.
   */
  public void setPath(final String path) {
    this.path = path;
  }

  /** @return Name of resource. */
  public String getName() {
    return name;
  }

  /**
   * Set name of resource.
   *
   * @param name Name to set.
   */
  public void setName(final String name) {
    this.name = name;
  }

  /** @return Created at timestamp. */
  public Timestamp getCreatedAt() {
    return createdAt;
  }

  /**
   * Set resource created at timestamp.
   *
   * @param timestamp Timestamp to set.
   */
  public void setCreatedAt(final Timestamp timestamp) {
    this.createdAt = timestamp;
  }

  /** @return Duration of the resource. */
  public Integer getDuration() {
    return duration;
  }

  /**
   * Set duration of resource.
   *
   * @param duration Duration to set.
   */
  public void setDuration(final Integer duration) {
    this.duration = duration;
  }

  /** {@inheritDoc} */
  @Override
  public String primaryKeyName() {
    return ID_COLUMN;
  }

  /** {@inheritDoc} */
  @Override
  public String tableName() {
    return TABLE_NAME;
  }
  //#endregion Getters and Setters

  //#region Database operations

  /** {@inheritDoc} */
  @Override
  public void save(final Connection connection) throws SQLException {
    if (this.id == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format(
              "select * from %s(?::varchar(1041), ?::varchar(200), ?)",
              creationFunctionName()
          )
      )) {
        int columnNumber = 0;
        preparedStatement.setString(++columnNumber, getPath());
        preparedStatement.setString(++columnNumber, getName());
        preparedStatement.setInt(++columnNumber, getDuration());
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
        int columnNumber = 0;
        preparedStatement.setString(++columnNumber, path);
        preparedStatement.setString(++columnNumber, name);
        preparedStatement.setLong(++columnNumber, duration);
        preparedStatement.setInt(++columnNumber, id);
        preparedStatement.execute();
      }
    }
  }
  //#endregion Database operations

  //#region String representations

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return defaultToString();
  }
  //#endregion String representations

  //#region Equals

  /** @return instance hashcode, i.e. its id. */
  @Override
  public int hashCode() {
    return id;
  }

  /**
   * Returns whether the given object is equal to this Resource.
   * Will be equal if:
   * - Not null
   * - Same class
   * - Same id
   * - Same path
   * - Same name
   * - Same duration
   * - Same created at
   *
   * @param o Object to compare.
   * @return True if all conditions above are met.
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
