package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;

public class Song extends Resource implements SavableEntity {
  private Song(Integer id, String path, String name, Timestamp createdAt, int duration) {
    super(id, path, name, createdAt, duration);
  }

  public Song(String path, String name, int duration) {
    super(path, name, duration);
  }

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

  @Override
  public String tableName() {
    return "Song";
  }

  @Override
  public String primaryKeyName() {
    return "idResource";
  }

  @Override
  public String creationFunctionName() {
    return "createSong";
  }
}
