package com.eos.streamus;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Film;
import com.eos.streamus.models.Person;
import com.eos.streamus.models.Song;
import com.eos.streamus.models.User;
import com.eos.streamus.utils.DatabaseConnection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StreamUsApplicationTests {
  @Autowired
  protected DatabaseConnection databaseConnection = null;

  @Test
  void connectToDatabase() {
    assertDoesNotThrow(() -> {
      Connection connection = databaseConnection.getConnection();
      connection.close();
    });
  }

  @Test
  void testSongCRUD() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      // Create
      Song song = new Song(String.format("test%d.mp3", new Date().getTime()), "Test song", 100);
      song.save(connection);

      // Read
      Song retrievedSong = Song.findById(song.getId(), connection);
      assertEquals(song, retrievedSong);

      // Update
      song.setName("Changed song name");
      song.setPath(String.format("test%d.mp3", new Date().getTime()));
      song.setDuration(101);
      song.save(connection);
      song.save(connection);

      retrievedSong = Song.findById(song.getId(), connection);
      assertEquals(song, retrievedSong);

      // Delete
      song.delete(connection);
      assertThrows(NoResultException.class, () -> Song.findById(song.getId(), connection));
    }
  }

  @Test
  void testFilmCRUD() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      // Create
      Film film = new Film(String.format("test%d.mp4", new Date().getTime()), "Test film", 100);
      film.save(connection);

      // Read
      Film retrievedFilm = Film.findById(film.getId(), connection);
      assertEquals(film, retrievedFilm);

      // Update
      film.setName("Changed film name");
      film.setPath(String.format("test%d.mp4", new Date().getTime()));
      film.setDuration(101);
      film.save(connection);

      retrievedFilm = Film.findById(film.getId(), connection);
      assertEquals(film, retrievedFilm);

      // Delete
      film.delete(connection);
      assertThrows(NoResultException.class, () -> Film.findById(film.getId(), connection));
    }
  }

  @Test
  void testPersonCRUD() throws SQLException, NoResultException, ParseException {
    try (Connection connection = databaseConnection.getConnection()) {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      java.sql.Date sqlDate = new java.sql.Date(dateFormat.parse("1970-08-01").getTime());

      // Create
      Person person = new Person("John", "Doe", sqlDate);
      person.save(connection);
      System.out.println(person);

      // Read
      Person retrievedPerson = Person.findById(person.getId(), connection);
      assertEquals(person, retrievedPerson);

      // Update
      person.setFirstName("Jane");
      person.setLastName("Donut");
      person.setDateOfBirth(new java.sql.Date(dateFormat.parse("1970-08-02").getTime()));
      person.save(connection);

      retrievedPerson = Person.findById(person.getId(), connection);
      assertEquals(person, retrievedPerson);

      // Delete
      person.delete(connection);
      assertThrows(NoResultException.class, () -> Person.findById(person.getId(), connection));
    }
  }

  @Test
  void testUserCRUD() throws SQLException, NoResultException, ParseException {
    try (Connection connection = databaseConnection.getConnection()) {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      java.sql.Date sqlDate = new java.sql.Date(dateFormat.parse("1970-08-01").getTime());

      // Create
      User user = new User("John", "Doe", sqlDate, String.format("john.doe%d@email.com", new Random().nextInt()), "johndoe");
      user.save(connection);
      System.out.println(user);

      // Read
      User retrievedUser = User.findById(user.getId(), connection);
      assertEquals(user, retrievedUser);

      // Update
      user.setFirstName("Jane");
      user.setLastName("Donut");
      user.setDateOfBirth(new java.sql.Date(dateFormat.parse("1970-08-02").getTime()));
      user.setEmail(String.format("jane.donut%d@email.com", new Random().nextInt()));
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
