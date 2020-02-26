package com.eos.streamus;

import com.eos.streamus.utils.DatabaseConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Service
@Scope(value = "singleton")
public class TestDatabaseConnection {
  @Value("${jdbc.url}")
  protected String url;

  @Value("${jdbc.host}")
  protected String host;

  @Value("${jdbc.port}")
  protected int port;

  @Value("${jdbc.testDatabaseName}")
  protected String databaseName;

  @Value("${database.user}")
  protected String user;

  @Value("${database.password}")
  protected String password;
  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(String.format("%s%s:%d/%s", url, host, port, databaseName), user, password);
  }
}
