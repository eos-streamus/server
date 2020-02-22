package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;

public class Song extends Resource implements SavableDeletableEntity {
  //#region Static attributes
  private static final String TABLE_NAME = "Song";
  private static final String PRIMARY_KEY_NAME = "idResource";
  private static final String CREATION_FUNCTION_NAME = "createSong";
  //#endregion Static attributs

  //#region Constructors
  private Song(Integer id, String path, String name, Timestamp createdAt, int duration) {
    super(id, path, name, createdAt, duration);
  }

  public Song(String path, String name, int duration) {
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

  @Override
  public String getCreationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }
  //#endregion Getters and Setters

  //#region Database operations
  public static Song findById(int id, Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement statement = connection.prepareStatement("select * from vsong where id = ?")) {
      statement.setInt(1, id);
      try (ResultSet rs = statement.executeQuery()) {
        if (!rs.next()) {
          throw new NoResultException();
        }
        return new Song(
          rs.getInt("id"),
          rs.getString("path"),
          rs.getString("name"),
          rs.getTimestamp("createdAt"),
          rs.getInt("duration")
        );
      }
    }
  }
  //#endregion Database operations
}
