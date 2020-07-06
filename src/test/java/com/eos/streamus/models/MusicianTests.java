package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class MusicianTests extends DatabaseTests {

  @Test
  void testNamedMusicianWithNoPerson() throws SQLException, NoResultException {
    try (Connection connection = databaseConnector.getConnection()) {
      Musician musician = new Musician("Test");
      musician.save(connection);

      Musician retrievedMusician = Musician.findById(musician.getId(), connection);
      assertEquals(musician, retrievedMusician);

      musician.setName("Test updated");
      musician.save(connection);

      retrievedMusician = Musician.findById(musician.getId(), connection);
      assertEquals(musician, retrievedMusician);

      musician.delete(connection);
      assertThrows(NoResultException.class, () -> Musician.findById(musician.getId(), connection));
    }
  }

  @Test
  void testNotNamedMusicianWithPerson() throws SQLException, NoResultException {
    try (Connection connection = databaseConnector.getConnection()) {
      Person person = new Person("Test firstname", "Test lastname", java.sql.Date.valueOf("1980-01-01"));
      person.save(connection);
      Musician musician = new Musician(person);
      musician.save(connection);

      Musician retrievedMusician = Musician.findById(musician.getId(), connection);
      assertEquals(musician, retrievedMusician);

      person.delete(connection);
      assertNotEquals(musician, Musician.findById(musician.getId(), connection));

      musician.delete(connection);
      assertThrows(NoResultException.class, () -> Musician.findById(musician.getId(), connection));
    }
  }

  @Test
  void testNamedMusicianWithPerson() throws SQLException, NoResultException {
    try (Connection connection = databaseConnector.getConnection()) {
      Person person = new Person("Test firstname", "Test lastname", java.sql.Date.valueOf("1980-01-01"));
      person.save(connection);
      Musician musician = new Musician("Test musician", person);
      musician.save(connection);

      Musician retrievedMusician = Musician.findById(musician.getId(), connection);
      assertEquals(musician, retrievedMusician);

      musician.setName("Test musician updated");
      musician.save(connection);
      retrievedMusician = Musician.findById(musician.getId(), connection);
      assertEquals(musician, retrievedMusician);

      person.delete(connection);
      assertNotEquals(musician, Musician.findById(musician.getId(), connection));

      musician.delete(connection);
      assertThrows(NoResultException.class, () -> Musician.findById(musician.getId(), connection));
    }
  }

  @Test
  void testNotNamedMusicianWithNoPerson() {
    assertThrows(IllegalArgumentException.class, () -> new Musician((String) null));
    assertThrows(IllegalArgumentException.class, () -> new Musician((Person) null));
  }
}
