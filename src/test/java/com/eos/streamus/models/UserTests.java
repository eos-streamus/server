package com.eos.streamus.models;

import com.eos.streamus.DatabaseTests;
import com.eos.streamus.exceptions.NoResultException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class UserTests extends DatabaseTests {

  @Test
  void testUserCRUD() throws SQLException, NoResultException {
    try (Connection connection = databaseConnector.getConnection()) {
      // Create
      User user = randomUser();
      user.save(connection);

      // Read
      User retrievedUser = User.findById(user.getId(), connection);
      assertEquals(user, retrievedUser);

      // Update
      user.setFirstName("Jane");
      user.setLastName("Donut");
      user.setDateOfBirth(randomDate());
      user.setEmail(String.format("jane.donut%d@email.com", getRandom().nextInt()));
      user.setUsername("janedonut");
      user.save(connection);

      retrievedUser = User.findById(user.getId(), connection);
      assertEquals(user, retrievedUser);

      // Delete
      user.delete(connection);
      assertThrows(NoResultException.class, () -> User.findById(user.getId(), connection));
    }
  }
}
