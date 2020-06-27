package com.eos.streamus.controllers;

import com.eos.streamus.models.Album;
import com.eos.streamus.models.SongPlaylist;
import com.eos.streamus.models.User;
import com.eos.streamus.writers.JsonAlbumWriter;
import com.eos.streamus.writers.JsonSongPlaylistWriter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;

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
}
