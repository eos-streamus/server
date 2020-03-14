package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.Connection;
import java.sql.SQLException;

public class ArtistDAO {
  private ArtistDAO() {}

  public static Artist findById(Integer id, Connection connection) throws SQLException, NoResultException {
    try {
      Musician.findById(id, connection);
    } catch (NoResultException e) {
      // Do nothing, could be Band
    }
    return Band.findById(id, connection);
  }
}
