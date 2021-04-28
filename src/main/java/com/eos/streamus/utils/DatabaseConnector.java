package com.eos.streamus.utils;

import org.springframework.beans.factory.annotation.Value;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(value = "singleton")
public class DatabaseConnector implements IDatabaseConnector {
  /** Jdbc url. */
  @Value("${jdbc.url}")
  private String url;

  /** Jdbc host. */
  @Value("${jdbc.host}")
  private String host;

  /** Jdbc port. */
  @Value("${jdbc.port}")
  private int port;

  /** Jdbc databaseName. */
  @Value("${jdbc.databaseName}")
  private String databaseName;

  /** Database user. */
  @Value("${database.user}")
  private String user;

  /** Database password. */
  @Value("${database.password}")
  private String password;

  /** {@inheritDoc} */
  @Override
  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(String.format("%s%s:%d/%s", url, host, port, databaseName), user, password);
  }

}




























