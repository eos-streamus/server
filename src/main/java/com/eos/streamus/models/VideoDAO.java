package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.Connection;
import java.sql.SQLException;

public class VideoDAO {
  public static Video findById(Integer id, Connection connection) throws NoResultException, SQLException {
    Video toReturn = null;
    // Ignore NoResultException later
    toReturn = Film.findById(id, connection);
    // Add Episode handling
    return toReturn;
  }
}
