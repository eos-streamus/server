package com.eos.streamus.utils;

import org.springframework.beans.factory.annotation.Value;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(value = "singleton")
public class DatabaseConnection implements IDatabaseConnection {
  @Value("${jdbc.url}")
  private String url;

  @Value("${jdbc.host}")
  private String host;

  @Value("${jdbc.port}")
  private int port;

  @Value("${jdbc.databaseName}")
  private String databaseName;

  @Value("${database.user}")
  private String user;

  @Value("${database.password}")
  private String password;

  @Override
  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(String.format("%s%s:%d/%s", url, host, port, databaseName), user, password);
  }
}




























