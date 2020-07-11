package com.eos.streamus.utils;

import org.springframework.beans.factory.annotation.Value;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(value = "singleton")
public final class DatabaseConnector implements IDatabaseConnector {
  /** Database url. */
  @Value("${jdbc.url}")
  private String url;

  /** Database host. */
  @Value("${jdbc.host}")
  private String host;

  /** Database application port. */
  @Value("${jdbc.port}")
  private int port;

  /** Database name. */
  @Value("${jdbc.databaseName}")
  private String databaseName;

  /** Database connection user. */
  @Value("${database.user}")
  private String user;

  /** Database connection password. */
  @Value("${database.password}")
  private String password;

  @Override
  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(String.format("%s%s:%d/%s", url, host, port, databaseName), user, password);
  }

}
