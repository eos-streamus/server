package com.eos.streamus.models;

import com.eos.streamus.utils.Pair;

import java.sql.*;
import java.util.Comparator;
import java.util.List;

public abstract class Collection implements SavableDeletableEntity {
  //#region Static attributes
  /** Table name in database. */
  public static final String TABLE_NAME = "Collection";
  /** id column name. */
  protected static final String PRIMARY_KEY_NAME = "id";
  /** name column name. */
  protected static final String NAME_COLUMN = "name";
  /** createdAt column name. */
  protected static final String CREATED_AT_COLUMN = "createdAt";
  /** updatedAt column name. */
  protected static final String UPDATED_AT_COLUMN = "updatedAt";
  //#endregion Static attributes

  //#region Instance attributes
  /** Id of instance. */
  private Integer id;
  /** Name of Collection. */
  private String name;
  /** Creation timestamp of collection. */
  private Timestamp createdAt;
  /** Update timestamp of collection. */
  private Timestamp updatedAt;
  //#endregion Instance attributes

  //#region Constructors
  protected Collection(final Integer id, final String name, final Timestamp createdAt, final Timestamp updatedAt) {
    this.id = id;
    this.name = name;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  protected Collection(final String name) {
    this.name = name;
  }
  //#endregion Constructors

  //#region Getters and Setters

  /** @return Name of table in database. */
  @Override
  public String tableName() {
    return TABLE_NAME;
  }

  /** @return Primary key column name. */
  @Override
  public String primaryKeyName() {
    return PRIMARY_KEY_NAME;
  }

  @Override
  public final Integer getId() {
    return id;
  }

  public final void setId(final Integer id) {
    this.id = id;
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

  public final void setCreatedAt(final Timestamp createdAt) {
    this.createdAt = createdAt;
  }

  public final Timestamp getUpdatedAt() {
    return updatedAt;
  }

  public final void setUpdatedAt(final Timestamp updatedAt) {
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

  /**
   * Save this collection to database.
   *
   * @param connection {@link Connection} to use to save.
   * @throws SQLException If an error occurs.
   */
  @Override
  public void save(final Connection connection) throws SQLException {
    if (this.id == null) {
      throw new NullPointerException("Collection#save can only be called on update");
    }
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "update %s set %s = ? where %s = ? returning %s;",
            TABLE_NAME,
            NAME_COLUMN,
            PRIMARY_KEY_NAME,
            UPDATED_AT_COLUMN
        )
    )) {
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

  /** @return String representation of this Collection. */
  @Override
  public String toString() {
    return defaultToString();
  }
  //#endregion String representations

  //#region Equals

  /** @return Hashcode of this instance. */
  @Override
  public int hashCode() {
    return id;
  }

  /**
   * Tests equality.
   *
   * @param o object to test.
   * @return If the object is equal to this instance.
   */
  @Override
  public boolean equals(final Object o) {
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
