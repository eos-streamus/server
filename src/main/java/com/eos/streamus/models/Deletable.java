package com.eos.streamus.models;

import java.sql.Connection;
import java.sql.SQLException;

public interface Deletable {
  void delete(Connection connection) throws SQLException;

}
