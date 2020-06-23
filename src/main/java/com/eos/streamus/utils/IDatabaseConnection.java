package com.eos.streamus.utils;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDatabaseConnection {
  Connection getConnection() throws SQLException;
}
