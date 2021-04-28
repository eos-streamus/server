package com.eos.streamus.models;

import java.sql.Timestamp;

public abstract class Video extends Resource {
  //#region Static attributes
  /** Table name in database. */
  private static final String TABLE_NAME = "Video";
  /** Primary key column name in database. */
  private static final String PRIMARY_KEY_NAME = "idResource";
  //#endregion Static attributes

  //#region Constructors
  protected Video(final Integer id, final String path, final String name,
                  final Timestamp createdAt, final Integer duration) {
    super(id, path, name, createdAt, duration);
  }

  protected Video(final String path, final String name, final Integer duration) {
    super(path, name, duration);
  }
  //#endregion Constructors_

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
