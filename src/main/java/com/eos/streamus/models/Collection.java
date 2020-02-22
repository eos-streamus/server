package com.eos.streamus.models;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

abstract class Collection implements SavableEntity, Entity {
  //#region Static attributes
  protected static final String TABLE_NAME = "Collection";
  protected static final String PRIMARY_KEY_NAME = "id";
  protected static final String NAME_COLUMN = "name";
  protected static final String CREATED_AT_COLUMN = "createdAt";
  protected static final String UPDATED_AT_COLUMN = "updatedAt";
  //#endregion Static attributes

  //#region Instance attributes
  private Integer id;
  private String name;
  private Timestamp createdAt;
  private Timestamp updatedAt;
  //#endregion Instance attributes

  //#region Constructors
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

  //#region Database operations
  @Override
  public void save(Connection connection) throws SQLException {
    // TODO
  }
  //#endregion Database operations
}
