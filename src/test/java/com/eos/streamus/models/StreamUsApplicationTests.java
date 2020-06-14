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
