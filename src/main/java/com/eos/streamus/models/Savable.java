package com.eos.streamus.models;

import java.sql.Connection;
import java.sql.SQLException;

public interface Savable {
  void save(Connection connection) throws SQLException;
}
