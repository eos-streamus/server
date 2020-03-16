package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;

public class Film extends Video {
  //#region Static attributes
  private static final String TABLE_NAME = "Film";
  private static final String VIEW_NAME = "vfilm";
  private static final String CREATION_FUNCTION_NAME = "createFilm";
  private static final String PRIMARY_KEY_NAME = "idVideo";
  //#endregion Static attributes

  //#region Constructors
  private Film(Integer id, String path, String name, Timestamp createdAt, Integer duration) {
    super(id, path, name, createdAt, duration);
  }

  public Film(String path, String name, Integer duration) {
    super(path, name, duration);
  }
  //#endregion Constructors

  //#region Getters and Setters
  @Override
  public String creationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }

  @Override
  public String tableName() {
    return TABLE_NAME;
  }

  @Override
  public String primaryKeyName() {
    return PRIMARY_KEY_NAME;
  }
  //#endregion Getters and Setters

  //#region Database operations
  public static Film findById(int id, Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement statement = connection.prepareStatement(String.format("select * from %s where %s = ?", VIEW_NAME, Resource.ID_COLUMN))) {
      statement.setInt(1, id);
      try (ResultSet rs = statement.executeQuery()) {
        if (!rs.next()) {
          throw new NoResultException();
        }
        return new Film(
          rs.getInt(Resource.ID_COLUMN),
          rs.getString(Resource.PATH_COLUMN),
          rs.getString(Resource.NAME_COLUMN),
          rs.getTimestamp(Resource.CREATED_AT_COLUMN),
          rs.getInt(Resource.DURATION_COLUMN)
        );
      }
    }
  }
  //#endregion Database operations
}
