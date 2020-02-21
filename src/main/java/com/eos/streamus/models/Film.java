package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;

public class Film extends Video {
  private Film(Integer id, String path, String name, Timestamp createdAt, Integer duration) {
    super(id, path, name, createdAt, duration);
  }

  public Film(String path, String name, Integer duration) {
    super(path, name, duration);
  }

  @Override
  public String creationFunctionName() {
    return "createFilm";
  }

  public static Film findById(int id, Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement statement = connection.prepareStatement("select * from vfilm where id = ?")) {
      statement.setInt(1, id);
      try (ResultSet rs = statement.executeQuery()) {
        if (!rs.next()) {
          throw new NoResultException();
        }
        return new Film(
          rs.getInt("id"),
          rs.getString("path"),
          rs.getString("name"),
          rs.getTimestamp("createdAt"),
          rs.getInt("duration")
        );
      }
    }
  }
}
