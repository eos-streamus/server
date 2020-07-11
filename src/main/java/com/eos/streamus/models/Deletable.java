package com.eos.streamus.models;

import java.sql.Connection;
import java.sql.SQLException;

public interface Deletable {
  /**
   * Delete this entity from database.
   *
   * @param connection {@link Connection} to use to delete.
   * @throws SQLException if an error occurs during operation.
   */
  void delete(Connection connection) throws SQLException;

}
