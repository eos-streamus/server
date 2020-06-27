package com.eos.streamus.controllers;

import com.eos.streamus.models.Album;
import com.eos.streamus.models.Song;
import com.eos.streamus.models.SongCollection;
import com.eos.streamus.models.SongPlaylist;
import com.eos.streamus.models.User;
import com.eos.streamus.writers.JsonAlbumWriter;
import com.eos.streamus.writers.JsonSongPlaylistWriter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SongCollectionTests extends ControllerTests {

  private User user;

  @BeforeAll
  private void setup() throws SQLException, ParseException {
    user = new User("John", "Doe", date("2000-01-01"), "john.doe@streamus.com", "johndoe");
    try (Connection connection = databaseConnector.getConnection()) {
      user.save(connection);
    }
  }

  @Test
  void gettingAnExistingAlbumShouldReturnOkWithJsonRepresentation() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      Album album = new Album("Test album", date("2020-01-01"));
      album.save(connection);

      RequestBuilder builder = MockMvcRequestBuilders.get(String.format("/album/%d", album.getId()));
      MockHttpServletResponse response = mockMvc.perform(builder)
                                                .andExpect(status().is(200))
                                                .andReturn()
                                                .getResponse();

      JsonNode json = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
      assertEquals(new JsonAlbumWriter(album).getJson(), json);
    }
  }

  @Test
  void gettingANonExistingAlbumShouldReturnNotFound() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      Album album = new Album("Test album", date("2020-01-01"));
      album.save(connection);
      album.delete(connection);

      RequestBuilder builder = MockMvcRequestBuilders.get(String.format("/album/%d", album.getId()));
      mockMvc.perform(builder)
             .andExpect(status().is(404))
             .andReturn();
    }
  }

  @Test
  void gettingAnExistingSongPlaylistShouldReturnOkWithJsonRepresentation() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      SongPlaylist songPlaylist = new SongPlaylist("Test songPlaylist", user);
      songPlaylist.save(connection);

      RequestBuilder builder = MockMvcRequestBuilders.get(String.format("/songplaylist/%d", songPlaylist.getId()));
      MockHttpServletResponse response = mockMvc.perform(builder)
                                                .andExpect(status().is(200))
                                                .andReturn()
                                                .getResponse();

      JsonNode json = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
      assertEquals(new JsonSongPlaylistWriter(songPlaylist).getJson(), json);
    }
  }

  @Test
  void gettingANonExistingSongPlaylistShouldReturnNotFound() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      SongPlaylist songPlaylist = new SongPlaylist("Test songPlaylist", user);
      songPlaylist.save(connection);
      songPlaylist.delete(connection);

      RequestBuilder builder = MockMvcRequestBuilders.get(String.format("/songplaylist/%d", songPlaylist.getId()));
      mockMvc.perform(builder)
             .andExpect(status().is(404))
             .andReturn();
    }
  }

  @Test
  void movingATrackInASongCollectionShouldReturnOkWithCorrectJson() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      Album album = new Album("Test album", date("2000-01-01"));
      album.save(connection);
      List<SongCollection.Track> tracks = new ArrayList<>();
      for (int i = 0; i < 10; i++) {
        Song song = new Song(UUID.randomUUID().toString(), UUID.randomUUID().toString(), 100);
        song.save(connection);
        tracks.add(album.addSong(song));
      }
      album.save(connection);

      SongCollection.Track trackToMove = tracks.get(2);
      int newTrackNumber = 4;
      ObjectNode trackData = new ObjectNode(new TestJsonFactory());
      trackData.put("songId", trackToMove.getSong().getId());
      trackData.put("trackNumber", newTrackNumber);
      RequestBuilder builder = MockMvcRequestBuilders.put(String.format("/album/%d", album.getId()))
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .content(trackData.toPrettyString());

      MockHttpServletResponse response = mockMvc.perform(builder).andExpect(status().is(200)).andReturn().getResponse();
      JsonNode json = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());

      for (JsonNode trackNode : json.get("tracks")) {
        if (trackNode.get("id").asInt() == trackToMove.getSong().getId()) {
          assertEquals(trackNode.get("trackNumber").asInt(), newTrackNumber);
        }
      }
    }
  }

  @Test
  void movingATrackInASongCollectionWithAnOutOfBoundTrackNumberReturnBadRequest() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      Album album = new Album("Test album", date("2000-01-01"));
      album.save(connection);
      List<SongCollection.Track> tracks = new ArrayList<>();
      for (int i = 0; i < 10; i++) {
        Song song = new Song(UUID.randomUUID().toString(), UUID.randomUUID().toString(), 100);
        song.save(connection);
        tracks.add(album.addSong(song));
      }
      album.save(connection);

      SongCollection.Track trackToMove = tracks.get(2);
      int newTrackNumber = 13;
      ObjectNode trackData = new ObjectNode(new TestJsonFactory());
      trackData.put("songId", trackToMove.getSong().getId());
      trackData.put("trackNumber", newTrackNumber);
      RequestBuilder builder = MockMvcRequestBuilders.put(String.format("/album/%d", album.getId()))
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .content(trackData.toPrettyString());

      mockMvc.perform(builder).andExpect(status().is(400)).andReturn();

    }
  }

}
