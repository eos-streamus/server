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
public class PersonTests extends DatabaseTests {
  @Test
  void testPersonCRUD() throws SQLException, NoResultException {
    try (Connection connection = databaseConnector.getConnection()) {
      // Create
      Person person = new Person("John", "Doe", randomDate());
      person.save(connection);

      // Read
      Person retrievedPerson = Person.findById(person.getId(), connection);
      assertEquals(person, retrievedPerson);

      // Update
      person.setFirstName("Jane");
      person.setLastName("Donut");
      person.setDateOfBirth(randomDate());
      person.save(connection);

      retrievedPerson = Person.findById(person.getId(), connection);
      assertEquals(person, retrievedPerson);

      // Delete
      person.delete(connection);
      assertThrows(NoResultException.class, () -> Person.findById(person.getId(), connection));
    }
  }

}
