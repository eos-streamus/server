package com.eos.streamus.controllers;

import com.eos.streamus.models.Album;
import com.eos.streamus.models.Band;
import com.eos.streamus.models.Song;
import com.eos.streamus.writers.JsonAlbumWriter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.sql.Connection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AlbumControllerTests extends ControllerTests {

  @Test
  void gettingAnExistingAlbumShouldReturnOkWithJsonRepresentation() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      Album album = new Album("Test album", date("2020-01-01"));
      album.save(connection);

      RequestBuilder builder = MockMvcRequestBuilders.get(String.format("/songcollection/%d", album.getId()));
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

      RequestBuilder builder = MockMvcRequestBuilders.get(String.format("/songcollection/%d", album.getId()));
      mockMvc.perform(builder)
             .andExpect(status().is(404))
             .andReturn();
    }
  }

  @Test
  void creatingAnAlbumWithANonExistingArtistShouldReturnBadRequest() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      Band band = new Band("Test band");
      band.save(connection);
      band.delete(connection);
      ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
      objectNode.put("name", "Test album");
      objectNode.put("releaseDate", "2000-01-01");
      ArrayNode artists = objectNode.putArray("artistIds");
      artists.add(band.getId());

      RequestBuilder builder = MockMvcRequestBuilders.post("/albums")
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .content(objectNode.toPrettyString());

      mockMvc.perform(builder).andExpect(status().is(400));
    }
  }

  @Test
  void creatingAnAlbumWithNonContinuousTrackNumbersShouldReturnBadRequest() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      Band band = new Band("Test band");
      band.save(connection);

      Song song1 = new Song(UUID.randomUUID().toString(), "Test song 1", 27);
      song1.save(connection);
      Song song2 = new Song(UUID.randomUUID().toString(), "Test song 2", 27);
      song2.save(connection);

      ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
      objectNode.put("name", "Test album");
      objectNode.put("releaseDate", "2000-01-01");

      ArrayNode artists = objectNode.putArray("artistIds");
      artists.add(band.getId());

      ArrayNode tracks = objectNode.putArray("tracks");

      ObjectNode song1Node = tracks.addObject();
      song1Node.put("songId", song1.getId());
      song1Node.put("trackNumber", 1);

      ObjectNode song2Node = tracks.addObject();
      song2Node.put("songId", song2.getId());
      song2Node.put("trackNumber", 3);

      RequestBuilder builder = MockMvcRequestBuilders.post("/albums")
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .content(objectNode.toPrettyString());

      mockMvc.perform(builder).andExpect(status().is(400));
    }
  }

  @Test
  void creatingAnAlbumWithNonExistingSongsShouldReturnBadRequest() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      Band band = new Band("Test band");
      band.save(connection);

      Song song1 = new Song(UUID.randomUUID().toString(), "Test song 1", 27);
      song1.save(connection);
      song1.delete(connection);

      ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
      objectNode.put("name", "Test album");
      objectNode.put("releaseDate", "2000-01-01");

      ArrayNode artists = objectNode.putArray("artistIds");
      artists.add(band.getId());

      ArrayNode tracks = objectNode.putArray("tracks");

      ObjectNode song1Node = tracks.addObject();
      song1Node.put("songId", song1.getId());
      song1Node.put("trackNumber", 1);

      RequestBuilder builder = MockMvcRequestBuilders.post("/albums")
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .content(objectNode.toPrettyString());

      mockMvc.perform(builder).andExpect(status().is(400));
    }
  }

  @Test
  void creatingAnAlbumWithASingleTrackWithNumberGreaterThanOneShouldReturnBadRequest() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      Band band = new Band("Test band");
      band.save(connection);

      Song song1 = new Song(UUID.randomUUID().toString(), "Test song 1", 27);
      song1.save(connection);
      song1.delete(connection);

      ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
      objectNode.put("name", "Test album");
      objectNode.put("releaseDate", "2000-01-01");

      ArrayNode artists = objectNode.putArray("artistIds");
      artists.add(band.getId());

      ArrayNode tracks = objectNode.putArray("tracks");

      ObjectNode song1Node = tracks.addObject();
      song1Node.put("songId", song1.getId());
      song1Node.put("trackNumber", 3);

      RequestBuilder builder = MockMvcRequestBuilders.post("/albums")
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .content(objectNode.toPrettyString());

      mockMvc.perform(builder).andExpect(status().is(400));
    }
  }

  @Test
  void creatingAnAlbumWithASingleTrackWithNumberLessThanOneShouldReturnBadRequest() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      Band band = new Band("Test band");
      band.save(connection);

      Song song1 = new Song(UUID.randomUUID().toString(), "Test song 1", 27);
      song1.save(connection);
      song1.delete(connection);

      ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
      objectNode.put("name", "Test album");
      objectNode.put("releaseDate", "2000-01-01");

      ArrayNode artists = objectNode.putArray("artistIds");
      artists.add(band.getId());

      ArrayNode tracks = objectNode.putArray("tracks");

      ObjectNode song1Node = tracks.addObject();
      song1Node.put("songId", song1.getId());
      song1Node.put("trackNumber", 0);

      RequestBuilder builder = MockMvcRequestBuilders.post("/albums")
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .content(objectNode.toPrettyString());

      mockMvc.perform(builder).andExpect(status().is(400));
    }
  }

}
