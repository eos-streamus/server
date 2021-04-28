package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.SQLException;

import static java.sql.Date.valueOf;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AdminTests extends DatabaseTests {

  @Test
  void testAdmin() throws SQLException, NoResultException {
    try (Connection connection = databaseConnector.getConnection()) {
      Admin admin = (Admin) new PersonBuilder(
          "Test", "Admin", valueOf("1990-01-01")
      ).asAdmin(
          String.format("test%d@admin.com", getRandom().nextInt()),
          "test_admin"
      ).build();
      admin.save(connection);
      assertNotNull(admin.getId());
      assertNotNull(admin.getCreatedAt());
      assertNotNull(admin.getUpdatedAt());

      assertEquals(admin, Admin.findById(admin.getId(), connection));
      admin.delete(connection);
      assertThrows(NoResultException.class, () -> Admin.findById(admin.getId(), connection));
    }
  }

}
