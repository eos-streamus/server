package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.Connection;
import java.sql.SQLException;

public class ResourceDAO {
  private ResourceDAO() {}

  public static Resource findById(Integer id, Connection connection) throws SQLException, NoResultException {
    try {
      return VideoDAO.findById(id, connection);
    } catch (NoResultException e) {
      // Ignore, could be Song
    }
    return Song.findById(id, connection);
  }

}
