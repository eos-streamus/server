package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class AlbumTests extends DatabaseTests {

  @Test
  void testAlbum() throws SQLException, NoResultException {
    try (Connection connection = databaseConnector.getConnection()) {
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
    try (Connection connection = databaseConnector.getConnection()) {
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

}
