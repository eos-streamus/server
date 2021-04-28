package com.eos.streamus.models;

import java.sql.Connection;
import java.sql.SQLException;

public interface Deletable {
  /**
   * Deletes this entity from the database.
   *
   * @param connection {@link Connection} to use to perform the operation.
   * @throws SQLException If an error occurred while performing the database operation.
   */
  void delete(Connection connection) throws SQLException;

}
