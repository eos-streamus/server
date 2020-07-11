package com.eos.streamus.models;

import java.sql.Connection;
import java.sql.SQLException;

public interface Savable {
  /**
   * Save this entity to database.
   *
   * @param connection {@link Connection} to use to save.
   * @throws SQLException if an error occurs during operation.
   */
  void save(Connection connection) throws SQLException;

}
