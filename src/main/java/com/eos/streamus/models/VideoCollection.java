package com.eos.streamus.models;

import java.sql.Timestamp;

public abstract class VideoCollection extends Collection {
  //#region Static attributes
  /** Table name in database. */
  protected static final String TABLE_NAME = "VideoCollection";
  /** Primary key column name in database. */
  protected static final String PRIMARY_KEY_NAME = "idCollection";
  //#endregion Static attributes

  //#region Constructors
  protected VideoCollection(final Integer id, final String name, final Timestamp createdAt, final Timestamp updatedAt) {
    super(id, name, createdAt, updatedAt);
  }

  protected VideoCollection(final String name) {
    super(name);
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
  //#endregion Getters and Setters
}
