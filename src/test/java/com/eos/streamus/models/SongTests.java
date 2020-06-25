package com.eos.streamus.models;

import com.eos.streamus.DatabaseTests;
import com.eos.streamus.exceptions.NoResultException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class SongTests extends DatabaseTests {

  @Test
  void testSongCRUD() throws SQLException, NoResultException {
    try (Connection connection = databaseConnector.getConnection()) {
      // Create
      Song song = new Song(String.format("test%d.mp3", new Date().getTime()), randomString(), 100);
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
}
