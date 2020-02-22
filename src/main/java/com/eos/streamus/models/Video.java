package com.eos.streamus.models;

import java.sql.Timestamp;

abstract public class Video extends Resource {
  //#region Static attributes
  private static final String TABLE_NAME = "Video";
  private static final String PRIMARY_KEY_NAME = "idResource";
  //#endregion Static attributes

  //#region Constructors
  protected Video(Integer id, String path, String name, Timestamp createdAt, Integer duration) {
    super(id, path, name, createdAt, duration);
  }

  protected Video(String path, String name, Integer duration) {
    super(path, name, duration);
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
  //#endregion Getters and Setters
}
