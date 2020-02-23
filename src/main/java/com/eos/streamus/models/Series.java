package com.eos.streamus.models;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Series extends VideoCollection {
  //#region Static attributes
  public static final String TABLE_NAME = "Series";
  public static final String PRIMARY_KEY_NAME = "idVideoCollection";
  public static final String VIEW_NAME = "vSeries";
  public static final String CREATION_FUNCTION_NAME = "createSeries";
  //#endregion

  protected Series(Integer id, String name, Timestamp createdAt, Timestamp updatedAt) {
    super(id, name, createdAt, updatedAt);
  }

  protected Series(String name) {
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

  @Override
  public void save(Connection connection) throws SQLException {
    super.save(connection);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public String getCreationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }
}
