package com.eos.streamus.models;

import com.eos.streamus.DatabaseTests;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class VideoPlaylistTests extends DatabaseTests {

  @Test
  void testEmptyVideoPlaylistCRUD() throws SQLException, NoResultException, ParseException {
    try (Connection connection = databaseConnector.getConnection()) {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      java.sql.Date sqlDate = new java.sql.Date(dateFormat.parse("1970-08-01").getTime());
      User user = new User(
          "John",
          "Doe",
          sqlDate,
          String.format("john.doe%d@email.com", getRandom().nextInt()),
          "johndoe"
      );
      user.save(connection);

      // Create
      VideoPlaylist videoPlaylist = new VideoPlaylist("Test playlist", user);
      videoPlaylist.save(connection);

      // Read
      VideoPlaylist retrievedVideoPlaylist = VideoPlaylist.findById(videoPlaylist.getId(), connection);
      assertEquals(videoPlaylist, retrievedVideoPlaylist);

      // Update
      videoPlaylist.setName(randomString());
      videoPlaylist.save(connection);

      retrievedVideoPlaylist = VideoPlaylist.findById(videoPlaylist.getId(), connection);
      assertEquals(videoPlaylist, retrievedVideoPlaylist);

      // Delete
      videoPlaylist.delete(connection);
      assertThrows(NoResultException.class, () -> VideoPlaylist.findById(videoPlaylist.getId(), connection));
      user.delete(connection);
    }
  }

  @Test
  void testPopulatedVideoPlaylistCRUD() throws SQLException, NoResultException {
    try (Connection connection = databaseConnector.getConnection()) {
      User user = randomUser();
      user.save(connection);
      VideoPlaylist videoPlaylist = new VideoPlaylist("Test video playlist", user);
      videoPlaylist.save(connection);
      List<Video> testVideos = new ArrayList<>();
      for (int i = 0; i < 10; i++) {
        Video video = new Film(
            String.format("test%d%d.mp4", new Date().getTime(), getRandom().nextInt()),
            "Test video",
            100
        );
        video.save(connection);
        testVideos.add(video);
        videoPlaylist.addVideo(video);
      }
      videoPlaylist.save(connection);

      VideoPlaylist retrievedVideoPlaylist = VideoPlaylist.findById(videoPlaylist.getId(), connection);
      assertEquals(videoPlaylist, retrievedVideoPlaylist);

      // Update
      videoPlaylist.setName(randomString());
      for (int i = 0; i < 10; i++) {
        Video video = new Film(
            String.format("test%d%d.mp4", new Date().getTime(), getRandom().nextInt()),
            "Test video",
            100
        );
        video.save(connection);
        testVideos.add(video);
        videoPlaylist.addVideo(video);
      }
      videoPlaylist.save(connection);

      retrievedVideoPlaylist = VideoPlaylist.findById(videoPlaylist.getId(), connection);
      assertEquals(videoPlaylist, retrievedVideoPlaylist);

      // Delete
      videoPlaylist.delete(connection);
      assertThrows(NoResultException.class, () -> SongPlaylist.findById(videoPlaylist.getId(), connection));

      user.delete(connection);
      for (Video video : testVideos) {
        video.delete(connection);
      }
    }
  }

}
