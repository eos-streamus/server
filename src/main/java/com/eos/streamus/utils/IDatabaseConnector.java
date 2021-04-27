package com.eos.streamus.utils;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDatabaseConnector {
  /**
   * Get {@link Connection} to database.
   *
   * @return Connection.
   * @throws SQLException if an error occurred.
   */
  Connection getConnection() throws SQLException;
}
