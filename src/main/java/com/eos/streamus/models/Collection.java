package com.eos.streamus.models;

import com.eos.streamus.utils.Pair;

import java.sql.*;
import java.util.Comparator;
import java.util.List;

public abstract class Collection implements SavableDeletableEntity {
  //#region Static attributes
  /** Table name in database. */
  public static final String TABLE_NAME = "Collection";
  /** Primary key name in database. */
  protected static final String PRIMARY_KEY_NAME = "id";
  /** Name column in database. */
  protected static final String NAME_COLUMN = "name";
  /** Created at column in database. */
  protected static final String CREATED_AT_COLUMN = "createdAt";
  /** Updated at column in database. */
  protected static final String UPDATED_AT_COLUMN = "updatedAt";
  //#endregion Static attributes

  //#region Instance attributes
  /** Id of the Collection. */
  private Integer id;
  /** Name of the Collection. */
  private String name;
  /** Created at timestamp of this Collection. */
  private Timestamp createdAt;
  /** Updated at timestamp of this Collection. */
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

  /** {@inheritDoc} */
  @Override
  public final Integer getId() {
    return id;
  }

  /**
   * Sets an id to this Collection.
   *
   * @param id Id to set.
   */
  public void setId(final Integer id) {
    this.id = id;
  }

  /** @return Name of this Collection. */
  public String getName() {
    return name;
  }

  /**
   * Set name of this Collection.
   *
   * @param name Name to set.
   */
  public void setName(final String name) {
    this.name = name;
  }

  /** @return The created at timestamp of this Collection. */
  public Timestamp getCreatedAt() {
    return createdAt;
  }

  /**
   * Set the created at timestamp of this Collection.
   *
   * @param createdAt Created at to set.
   */
  public void setCreatedAt(final Timestamp createdAt) {
    this.createdAt = createdAt;
  }

  /** @return The updated at timestamp of this Collection. */
  public Timestamp getUpdatedAt() {
    return updatedAt;
  }

  /**
   * Set the updated at timestamp of this Collection.
   *
   * @param updatedAt Updated at to set.
   */
  public void setUpdatedAt(final Timestamp updatedAt) {
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

  /** {@inheritDoc} */
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

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return defaultToString();
  }
  //#endregion String representations

  //#region Equals
  /** @return HashCode of this instance, i.e. its id. */
  @Override
  public int hashCode() {
    return id;
  }

  /**
   * Returns whether the given object is equal to this Collection.
   * Will be equal if:
   * - Not null
   * - Equal class
   * - Equal id
   * - Equal name
   * - Equal created at
   * - Equal updated at
   * @param o Object to compare
   * @return True if all conditions above are met.
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
