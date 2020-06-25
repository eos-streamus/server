package com.eos.streamus.models;

import com.eos.streamus.DatabaseTests;
import com.eos.streamus.exceptions.NoResultException;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class SeriesAndEpisodeTests extends DatabaseTests {

  @Test
  void testEmptySeriesCRUD() throws SQLException, NoResultException {
    try (Connection connection = databaseConnector.getConnection()) {
      // Create
      Series series = new Series(randomString());
      series.save(connection);
      assertNotNull(series.getId());

      // Read
      Series retrievedSeries = Series.findById(series.getId(), connection);
      assertEquals(series, retrievedSeries);

      // Update
      series.setName(String.format("Test series updated %d", new Date().getTime()));
      series.save(connection);
      retrievedSeries = Series.findById(series.getId(), connection);
      assertEquals(series, retrievedSeries);

      // Delete
      series.delete(connection);
      assertThrows(NoResultException.class, () -> Series.findById(series.getId(), connection));
    }
  }

  @Test
  void testEpisodeCRUD() throws SQLException, NoResultException {
    try (Connection connection = databaseConnector.getConnection()) {
      // Create
      Series series = new Series(String.format(randomString(), new Date().getTime()));
      series.save(connection);
      Series.Episode episode = series.new Episode(randomString(), randomString(), 100, (short) 1, (short) 1);
      episode.save(connection);
      assertNotNull(episode.getId());

      // Read
      Series.Episode retrievedEpisode = (Series.Episode) VideoDAO.findById(episode.getId(), connection);
      assertEquals(episode, retrievedEpisode);

      // Update
      episode.setName("Test episode updated");
      episode.save(connection);
      retrievedEpisode = (Series.Episode) VideoDAO.findById(episode.getId(), connection);
      assertEquals(episode, retrievedEpisode);

      // Delete
      episode.delete(connection);
      assertThrows(NoResultException.class, () -> VideoDAO.findById(episode.getId(), connection));
      series.delete(connection);
    }
  }

  @Test
  void testEpisodeDeleteCascade() throws SQLException {
    try (Connection connection = databaseConnector.getConnection()) {
      // Create
      Series series = new Series(String.format(randomString(), new Date().getTime()));
      series.save(connection);
      Series.Episode episode = series.new Episode(randomString(), randomString(), 100, (short) 1, (short) 1);
      episode.save(connection);
      assertNotNull(episode.getId());

      series.delete(connection);
      assertThrows(NoResultException.class, () -> VideoDAO.findById(episode.getId(), connection));
    }
  }

  @Test
  void testEpisodeWithInvalidSeasonAndEpisodeValues() throws SQLException {
    try (Connection connection = databaseConnector.getConnection()) {
      // Create
      Series series = new Series(String.format(randomString(), new Date().getTime()));
      series.save(connection);
      Series.Episode episode = series.new Episode(randomString(), randomString(), 100, (short) 2, (short) 2);
      try {
        episode.save(connection);
      } catch (PSQLException e) {
        if (!e.getMessage().contains("Invalid episode numbers")) {
          fail();
        }
      }
      series.delete(connection);
    }
  }

  @Test
  void testEpisodeWithAutomaticEpisodeNumber() throws SQLException {
    try (Connection connection = databaseConnector.getConnection()) {
      // Create
      Series series = new Series(String.format(randomString(), new Date().getTime()));
      series.save(connection);
      Series.Episode episode1 = series.new Episode(randomString(), randomString(), 100, (short) 1);
      assertEquals(1, episode1.getEpisodeNumber());
      Series.Episode episode2 = series.new Episode(randomString(), randomString(), 100, (short) 1);
      assertEquals(2, episode2.getEpisodeNumber());

      episode1.save(connection);
      episode2.save(connection);

      series.delete(connection);
    }
  }

  @Test
  void testPopulatedSeries() throws SQLException, NoResultException {
    try (Connection connection = databaseConnector.getConnection()) {
      Series series = new Series("Test series");
      series.save(connection);
      List<Series.Episode> episodes = new ArrayList<>();
      for (short i = 1; i <= 5; i++) {
        for (short j = 1; j <= 10; j++) {
          episodes.add(
              series.new Episode(
                  String.format("test_path_%d_%d", new Date().getTime(), getRandom().nextInt()),
                  String.format("Episode %d", j),
                  100,
                  i
              )
          );
        }
      }
      series.save(connection);

      Series retrievedSeries = Series.findById(series.getId(), connection);
      assertEquals(series, retrievedSeries);

      retrievedSeries.delete(connection);
      assertThrows(NoResultException.class, () -> Series.findById(series.getId(), connection));
      for (Series.Episode episode : episodes) {
        assertThrows(NoResultException.class, () -> VideoDAO.findById(episode.getId(), connection));
      }
    }
  }

}
