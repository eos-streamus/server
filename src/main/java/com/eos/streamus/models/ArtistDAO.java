package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ArtistDAO {
  private ArtistDAO() {}

  public static Artist findById(Integer id, Connection connection) throws SQLException, NoResultException {
    try {
      return Musician.findById(id, connection);
    } catch (NoResultException e) {
      // Do nothing, could be Band
    }
    return Band.findById(id, connection);
  }

  public static List<Artist> all(Connection connection) throws SQLException {
    List<Artist> artists = new ArrayList<>();
    try (PreparedStatement preparedStatement = connection.prepareStatement("select distinct id from " + Artist.TABLE_NAME)) {
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          try {
            artists.add(findById(resultSet.getInt("id"), connection));
          } catch (NoResultException e) {
            Logger.getLogger(ArtistDAO.class.getName()).severe(e.getMessage());
          }
        }
      }
    }
    return artists;
  }
}
