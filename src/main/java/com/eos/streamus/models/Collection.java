package com.eos.streamus.models;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

abstract class Collection implements SavableEntity, Entity {
  private Integer id;
  private String name;
  private Timestamp createdAt;
  private Timestamp updatedAt;

  protected Collection(Integer id, String name, Timestamp createdAt, Timestamp updatedAt) {
    this.id = id;
    this.name = name;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  protected Collection(String name, Timestamp createdAt, Timestamp updatedAt) {
    this.name = name;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  @Override
  public void save(Connection connection) throws SQLException {
    // TODO
  }

  @Override
  public String tableName() {
    return "collection";
  }

  @Override
  public String primaryKeyName() {
    return "id";
  }
}
