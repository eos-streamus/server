package com.eos.streamus;

import com.eos.streamus.models.Person;
import com.eos.streamus.models.User;
import com.eos.streamus.utils.TestDatabaseConnection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

import static java.sql.Date.valueOf;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
public abstract class DatabaseTests {

  private final Random random = new Random();

  @Autowired
  protected TestDatabaseConnection databaseConnection;

  public Connection getConnection() throws SQLException {
    return databaseConnection.getConnection();
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
    return new Person(randomString(), randomString(), randomDate());
  }

  protected User randomUser() {
    return new User(randomString(), randomString(), randomDate(), randomString() + "@" + randomString(),
                    randomString());
  }

  @Test
  void connectToDatabase() {
    assertDoesNotThrow(() -> {
      Connection connection = databaseConnection.getConnection();
      connection.close();
    });
  }

}
