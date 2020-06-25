package com.eos.streamus.utils;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDatabaseConnector {
  Connection getConnection() throws SQLException;
}
