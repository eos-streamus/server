package com.eos.streamus.models;

import com.eos.streamus.utils.TestDatabaseConnection;
import com.eos.streamus.exceptions.NoResultException;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransactionRollbackException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static java.sql.Date.valueOf;

@SpringBootTest
class StreamUsApplicationTests {
  @Autowired
  protected TestDatabaseConnection databaseConnection = null;

  private final Random random = new Random();

  @Test
  void testAdmin() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      Admin admin = new Admin("Test", "Admin", valueOf("1990-01-01"), String.format("test%d@admin.com", random.nextInt()), "test_admin");
      admin.save(connection);
      assertNotNull(admin.getId());
      assertNotNull(admin.getCreatedAt());
      assertNotNull(admin.getUpdatedAt());

      assertEquals(admin, Admin.findById(admin.getId(), connection));
      admin.delete(connection);
      assertThrows(NoResultException.class, () -> Admin.findById(admin.getId(), connection));
    }
  }

  @Test
  void testResourceActivity() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      Song song = new Song(randomString(), randomString(), 100);
      song.save(connection);

      User user = new User(randomString(), randomString(), randomDate(), randomString() + "@" + randomString(), randomString());
      user.save(connection);

      ResourceActivity resourceActivity = new ResourceActivity(song, user);
      resourceActivity.save(connection);
      assertEquals(resourceActivity, ResourceActivity.findById(resourceActivity.getId(), connection));

      // Update
      resourceActivity.start();
      resourceActivity.save(connection);
      assertEquals(resourceActivity, ResourceActivity.findById(resourceActivity.getId(), connection));
      resourceActivity.setPausedAt(1);
      resourceActivity.save(connection);
      assertEquals(resourceActivity, ResourceActivity.findById(resourceActivity.getId(), connection));

      // Delete
      resourceActivity.delete(connection);
      assertNull(ResourceActivity.findById(resourceActivity.getId(), connection));

      user.delete(connection);
      song.delete(connection);
    }
  }

  @Test
  void testCollectionActivity() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      Series theExpanse = new Series("The Expanse");
      short season1 = 1;
      theExpanse.new Episode(randomString(), "Dulcinea", 2700, season1);
      theExpanse.new Episode(randomString(), "The Big Empty", 2700, season1);
      theExpanse.new Episode(randomString(), "Remember the Cant", 2700, season1);
      theExpanse.new Episode(randomString(), "CQB (Close Quarter Battle)", 2700, season1);
      theExpanse.new Episode(randomString(), "Back to the Butcher", 2700, season1);
      theExpanse.new Episode(randomString(), "Rock Bottom", 2700, season1);
      theExpanse.new Episode(randomString(), "Windmills", 2700, season1);
      theExpanse.new Episode(randomString(), "Salvage", 2700, season1);
      theExpanse.new Episode(randomString(), "Critical Mass", 2700, season1);
      theExpanse.new Episode(randomString(), "Leviathan Wakes", 2700, season1);

      short season2 = 2;
      theExpanse.new Episode(randomString(), "Safe", 2700, season2);
      theExpanse.new Episode(randomString(), "Doors & Corners", 2700, season2);
      theExpanse.new Episode(randomString(), "Static", 2700, season2);
      theExpanse.new Episode(randomString(), "Godspeed", 2700, season2);
      theExpanse.new Episode(randomString(), "Home", 2700, season2);
      theExpanse.new Episode(randomString(), "Paradigm Shift", 2700, season2);
      theExpanse.new Episode(randomString(), "The Seventh Man", 2700, season2);
      theExpanse.new Episode(randomString(), "Pyre", 2700, season2);
      theExpanse.new Episode(randomString(), "The Weeping Somnambulist", 2700, season2);
      theExpanse.new Episode(randomString(), "Cascade", 2700, season2);
      theExpanse.new Episode(randomString(), "Here There be Dragons", 2700, season2);
      theExpanse.new Episode(randomString(), "The Monster and the Rocket", 2700, season2);
      theExpanse.new Episode(randomString(), "Caliban's War", 2700, season2);

      theExpanse.save(connection);

      assertEquals(theExpanse, Series.findById(theExpanse.getId(), connection));

      User user = randomUser();
      user.save(connection);
      CollectionActivity collectionActivity = new CollectionActivity(user, theExpanse);
      collectionActivity.save(connection);
      assertEquals(collectionActivity, CollectionActivity.findById(collectionActivity.getId(), connection));
      int i = 0;
      ResourceActivity resourceActivity;
      do {
        assertEquals(i, collectionActivity.getContent().stream().reduce(0, (subtotal, entry) -> subtotal + (entry.getValue().getValue() == null ? 0 : 1), Integer::sum));
        resourceActivity = collectionActivity.continueOrNext(connection);
        if (resourceActivity != null) {
          resourceActivity.setPausedAt(resourceActivity.getResource().getDuration());
          resourceActivity.save(connection);
          i++;
        }
      } while (resourceActivity != null);
      assertEquals(collectionActivity, CollectionActivity.findById(collectionActivity.getId(), connection));

      theExpanse.delete(connection);
      user.delete(connection);
    }
  }

  @Test
  void testActivityMessages() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      Film film = new Film(randomString(), randomString(), (random.nextInt() & Integer.MAX_VALUE));
      film.save(connection);

      User user = randomUser();
      user.save(connection);

      ResourceActivity activity = new ResourceActivity(film, user);
      activity.save(connection);

      Activity.ActivityMessage message = activity.new ActivityMessage(user, "Test message");
      message.save(connection);

      assertEquals(1, activity.getMessages().size());
      assertEquals(activity, ResourceActivity.findById(activity.getId(), connection));

      for (int i = 0; i < 10; i++) {
        message = activity.new ActivityMessage(user, randomString());
        message.save(connection);
      }
      ResourceActivity fetchedActivity = ResourceActivity.findById(activity.getId(), connection);
      if (fetchedActivity == null) {
        fail("Null fetchedActivity");
      } else {
        assertEquals(activity.getMessages().size(), fetchedActivity.getMessages().size());
        assertTrue(activity.getMessages().containsAll(fetchedActivity.getMessages()));
      }
      film.delete(connection);
      assertNull(ResourceActivity.findById(activity.getId(), connection));
      user.delete(connection);
    }
  }

  @Test
  void testAlbum() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      Album album = new Album(randomString(), randomDate());

      Song[] songs = new Song[10];
      for (int i = 0; i < 10; i++) {
        songs[i] = new Song(randomString(), randomString(), 100);
        songs[i].save(connection);
        album.addSong(songs[i]);
      }
      album.save(connection);
      Album fetchedAlbum = Album.findById(album.getId(), connection);
      assertEquals(album.getContent().size(), fetchedAlbum.getContent().size());
      assertEquals(album, fetchedAlbum);

      album.delete(connection);
      for (Song song : songs) {
        song.delete(connection);
      }
    }
  }

  @Test
  void testAlbumWithArtists() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      Band band = new Band("test band");
      band.save(connection);

      Musician musician = new Musician(randomPerson());
      musician.save(connection);

      Album album = new Album(randomString(), randomDate());

      album.addArtist(band);
      album.addArtist(musician);

      Song[] songs = new Song[10];
      for (int i = 0; i < 10; i++) {
        songs[i] = new Song(randomString(), randomString(), 100);
        songs[i].save(connection);
        album.addSong(songs[i]);
      }
      album.save(connection);
      Album fetchedAlbum = Album.findById(album.getId(), connection);
      assertEquals(album.getContent().size(), fetchedAlbum.getContent().size());
      assertEquals(album, fetchedAlbum);

      album.delete(connection);
      for (Song song : songs) {
        song.delete(connection);
      }
      musician.getPerson().delete(connection);
      musician.delete(connection);
      band.delete(connection);
    }
  }

  private String randomString() {
    return "randomString" + random.nextDouble();
  }

  private java.sql.Date randomDate() {
    int year = random.nextInt() % 70 + 1940;
    int month = (random.nextInt() & Integer.MAX_VALUE) % 12 + 1;
    return valueOf(String.format("%d-%s-01", year, (month < 10 ? "0" + month : month)));
  }

  private Person randomPerson() {
    return new Person(randomString(), randomString(), randomDate());
  }

  private User randomUser() {
    return new User(randomString(), randomString(), randomDate(), randomString() + "@" + randomString(), randomString());
  }
}
