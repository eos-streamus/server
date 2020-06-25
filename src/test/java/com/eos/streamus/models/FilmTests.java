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
public class FilmTests extends DatabaseTests {

  @Test
  void testFilmCRUD() throws SQLException, NoResultException {
    try (Connection connection = databaseConnector.getConnection()) {
      // Create
      Film film = new Film(randomString(), randomString(), 100);
      film.save(connection);

      // Read
      Film retrievedFilm = Film.findById(film.getId(), connection);
      assertEquals(film, retrievedFilm);

      // Update
      film.setName(randomString());
      film.setPath(randomString());
      film.setDuration(101);
      film.save(connection);

      retrievedFilm = Film.findById(film.getId(), connection);
      assertEquals(film, retrievedFilm);

      // Delete
      film.delete(connection);
      assertThrows(NoResultException.class, () -> Film.findById(film.getId(), connection));
    }
  }
}
