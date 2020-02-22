package com.eos.streamus.models;

import java.sql.Timestamp;

public abstract class VideoCollection extends Collection {

  protected static final String TABLE_NAME = "VideoCollection";
  protected static final String PRIMARY_KEY_NAME = "idCollection";

  protected VideoCollection(Integer id, String name, Timestamp createdAt, Timestamp updatedAt) {
    super(id, name, createdAt, updatedAt);
  }

  protected VideoCollection(String name) {
    super(name);
  }

  @Override
  public String getTableName() {
    return TABLE_NAME;
  }

  @Override
  public String getPrimaryKeyName() {
    return PRIMARY_KEY_NAME;
  }
}
