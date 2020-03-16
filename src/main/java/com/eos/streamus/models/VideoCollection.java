package com.eos.streamus.models;

import java.sql.Timestamp;

public abstract class VideoCollection extends Collection {
  //#region Static attributes
  protected static final String TABLE_NAME = "VideoCollection";
  protected static final String PRIMARY_KEY_NAME = "idCollection";
  //#endregion Static attributes

  //#region Constructors
  protected VideoCollection(Integer id, String name, Timestamp createdAt, Timestamp updatedAt) {
    super(id, name, createdAt, updatedAt);
  }

  protected VideoCollection(String name) {
    super(name);
  }
  //#endregion Constructors

  //#region Getters and Setters
  @Override
  public String tableName() {
    return TABLE_NAME;
  }

  @Override
  public String primaryKeyName() {
    return PRIMARY_KEY_NAME;
  }
  //#endregion Getters and Setters
}
