package com.eos.streamus.controllers;

import com.eos.streamus.models.Band;
import com.eos.streamus.models.Song;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.sql.Connection;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AlbumControllerTests extends JwtSetupControllerTests {

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

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/albums")
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .content(objectNode.toPrettyString());

      perform(builder).andExpect(status().is(400));
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

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/albums")
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .content(objectNode.toPrettyString());

      perform(builder).andExpect(status().is(400));
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

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/albums")
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .content(objectNode.toPrettyString());

      perform(builder).andExpect(status().is(400));
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

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/albums")
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .content(objectNode.toPrettyString());

      perform(builder).andExpect(status().is(400));
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

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/albums")
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .content(objectNode.toPrettyString());

      perform(builder).andExpect(status().is(400));
    }
  }

}
