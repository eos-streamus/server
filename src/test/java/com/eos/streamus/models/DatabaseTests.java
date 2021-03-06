package com.eos.streamus.models;

import com.eos.streamus.utils.IDatabaseConnector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.sql.Date.valueOf;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ContextConfiguration(locations={"file:src/test/resources/test-context.xml"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class DatabaseTests {

  private final Random random = new Random();

  @Autowired
  protected IDatabaseConnector databaseConnector;

  public Connection getConnection() throws SQLException {
    return databaseConnector.getConnection();
  }

  protected String randomString() {
    return "randomString" + random.nextDouble();
  }

  protected java.sql.Date randomDate() {
    int year = random.nextInt() % 70 + 1940;
    int month = (random.nextInt() & Integer.MAX_VALUE) % 12 + 1;
    return valueOf(String.format("%d-%s-01", year, (month < 10 ? "0" + month : month)));
  }

  protected Person randomPerson() {
    return new PersonBuilder(randomString(), randomString(), randomDate()).build();
  }

  protected User randomUser() {
    return (User) new PersonBuilder(randomString(), randomString(), randomDate())
        .asUser(randomString() + "@" + randomString(), randomString())
        .build();
  }

  protected Random getRandom() {
    return random;
  }

  @Test
  void connectToDatabase() {
    assertDoesNotThrow(() -> {
      Connection connection = databaseConnector.getConnection();
      connection.close();
    });
  }

  @AfterAll
  void emptyDatabase() throws SQLException, IOException {
    try (Connection connection = databaseConnector.getConnection()) {
      // Delete all Resources
      List<String> pathStrings = new ArrayList<>();
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format("select distinct %s from %s", Resource.PATH_COLUMN, Resource.TABLE_NAME)
      )) {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          while (resultSet.next()) {
            pathStrings.add(resultSet.getString(1));
          }
        }
      }
      for (final String pathString : pathStrings) {
        Files.delete(Paths.get(pathString));
      }

      try (PreparedStatement preparedStatement = connection
          .prepareStatement("truncate table " + Resource.TABLE_NAME + " cascade")) {
        preparedStatement.execute();
      }

      // Delete all Persons
      try (PreparedStatement preparedStatement = connection
          .prepareStatement("truncate table " + Person.TABLE_NAME + " cascade")) {
        preparedStatement.execute();
      }

      // Delete all Artists
      try (PreparedStatement preparedStatement = connection
          .prepareStatement("truncate table " + Artist.TABLE_NAME + " cascade")) {
        preparedStatement.execute();
      }

      // Delete all Collections
      try (PreparedStatement preparedStatement = connection
          .prepareStatement("truncate table " + Collection.TABLE_NAME + " cascade")) {
        preparedStatement.execute();
      }

      // Delete all Activities
      try (PreparedStatement preparedStatement = connection
          .prepareStatement("truncate table " + Activity.TABLE_NAME + " cascade")) {
        preparedStatement.execute();
      }
    }
  }

}
