package com.eos.streamus.models;

import java.sql.Timestamp;

public class Series extends VideoCollection {

  protected Series(Integer id, String name, Timestamp createdAt, Timestamp updatedAt) {
    super(id, name, createdAt, updatedAt);
  }

  protected Series(String name) {
    super(name);
  }

  @Override
  public String getCreationFunctionName() {
    return null;
  }
}
