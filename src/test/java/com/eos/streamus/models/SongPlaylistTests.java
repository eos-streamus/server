package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SongPlaylistTests extends DatabaseTests {

  @Test
  void testEmptySongPlaylistCRUD() throws SQLException, NoResultException, ParseException {
    try (Connection connection = databaseConnector.getConnection()) {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      java.sql.Date sqlDate = new java.sql.Date(dateFormat.parse("1970-08-01").getTime());
      User user = (User) new PersonBuilder(
          "John",
          "Doe",
          sqlDate
      ).asUser(
          String.format("john.doe%d@email.com", getRandom().nextInt()),
          "johndoe"
      ).build();
      user.save(connection);

      // Create
      SongPlaylist songPlaylist = new SongPlaylist(randomString(), user);
      songPlaylist.save(connection);

      // Read
      SongPlaylist retrievedSongPlaylist = SongPlaylist.findById(songPlaylist.getId(), connection);
      assertEquals(songPlaylist, retrievedSongPlaylist);

      // Update
      songPlaylist.setName(randomString());
      songPlaylist.save(connection);

      retrievedSongPlaylist = SongPlaylist.findById(songPlaylist.getId(), connection);
      assertEquals(songPlaylist, retrievedSongPlaylist);

      // Delete
      songPlaylist.delete(connection);
      assertThrows(NoResultException.class, () -> SongPlaylist.findById(songPlaylist.getId(), connection));

      user.delete(connection);
    }
  }

  @Test
  void testPopulatedSongPlaylistCRUD() throws SQLException, NoResultException {
    try (Connection connection = databaseConnector.getConnection()) {
      User user = randomUser();
      user.save(connection);

      // Create
      SongPlaylist songPlaylist = new SongPlaylist("Test playlist", user);
      songPlaylist.save(connection);
      List<Song> testSongs = new ArrayList<>(); // Used to keep track of songs to delete
      for (int i = 0; i < 10; i++) {
        Song song = new Song(String.format("test%d%d.mp3", new Date().getTime(), getRandom().nextInt()), randomString(),
            100);
        song.save(connection);
        testSongs.add(song);
        songPlaylist.addSong(song);
      }
      songPlaylist.save(connection);

      // Read
      SongPlaylist retrievedSongPlaylist = SongPlaylist.findById(songPlaylist.getId(), connection);
      assertEquals(songPlaylist, retrievedSongPlaylist);

      // Update
      songPlaylist.setName(randomString());
      for (int i = 0; i < 10; i++) {
        Song song = new Song(String.format("test%d%d.mp3", new Date().getTime(), getRandom().nextInt()), randomString(),
            100);
        song.save(connection);
        testSongs.add(song);
        songPlaylist.addSong(song);
      }
      songPlaylist.save(connection);

      retrievedSongPlaylist = SongPlaylist.findById(songPlaylist.getId(), connection);
      assertEquals(songPlaylist, retrievedSongPlaylist);

      // Delete
      songPlaylist.delete(connection);
      assertThrows(NoResultException.class, () -> SongPlaylist.findById(songPlaylist.getId(), connection));

      // Clean up Songs
      testSongs.forEach(song -> {
        try {
          song.delete(connection);
        } catch (SQLException e) {
          fail(String.format("Song %s could not be deleted", song));
        }
      });
      user.delete(connection);
    }
  }

}
