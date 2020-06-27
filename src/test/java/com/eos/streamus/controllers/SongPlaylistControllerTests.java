package com.eos.streamus.controllers;

import com.eos.streamus.models.Song;
import com.eos.streamus.models.User;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.sql.Connection;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SongPlaylistControllerTests extends ControllerTests {

  @Test
  void creatingASongPlaylistWithANonExistingUserShouldReturnBadRequest() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      User user = new User(
          "John",
          "Doe",
          date("2000-01-01"),
          UUID.randomUUID().toString() + "@streamus.com",
          UUID.randomUUID().toString()
      );
      user.save(connection);
      user.delete(connection);
      ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
      objectNode.put("name", "Test playlist");
      objectNode.put("userId", user.getId());

      RequestBuilder builder = MockMvcRequestBuilders.post("/songplaylist")
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .content(objectNode.toPrettyString());

      mockMvc.perform(builder).andExpect(status().is(400));
    }
  }

  @Test
  void creatingASongPlaylistWithNonContinuousTrackNumbersShouldReturnBadRequest() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      User user = new User(
          "John",
          "Doe",
          date("2000-01-01"),
          UUID.randomUUID().toString() + "@streamus.com",
          UUID.randomUUID().toString()
      );
      user.save(connection);

      Song song1 = new Song(UUID.randomUUID().toString(), "Test song 1", 27);
      song1.save(connection);
      Song song2 = new Song(UUID.randomUUID().toString(), "Test song 2", 27);
      song2.save(connection);

      ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
      objectNode.put("name", "Test SongPlaylist");
      objectNode.put("userId", user.getId());

      ArrayNode tracks = objectNode.putArray("tracks");

      ObjectNode song1Node = tracks.addObject();
      song1Node.put("songId", song1.getId());
      song1Node.put("trackNumber", 1);

      ObjectNode song2Node = tracks.addObject();
      song2Node.put("songId", song2.getId());
      song2Node.put("trackNumber", 3);

      RequestBuilder builder = MockMvcRequestBuilders.post("/songplaylist")
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .content(objectNode.toPrettyString());

      mockMvc.perform(builder).andExpect(status().is(400));
    }
  }

  @Test
  void creatingASongPlaylistWithNonExistingSongsShouldReturnBadRequest() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      User user = new User(
          "John",
          "Doe",
          date("2000-01-01"),
          UUID.randomUUID().toString() + "@streamus.com",
          UUID.randomUUID().toString()
      );
      user.save(connection);

      Song song1 = new Song(UUID.randomUUID().toString(), "Test song 1", 27);
      song1.save(connection);
      song1.delete(connection);

      ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
      objectNode.put("name", "Test SongPlaylist");
      objectNode.put("userId", user.getId());

      ArrayNode tracks = objectNode.putArray("tracks");

      ObjectNode song1Node = tracks.addObject();
      song1Node.put("songId", song1.getId());
      song1Node.put("trackNumber", 1);

      RequestBuilder builder = MockMvcRequestBuilders.post("/songplaylist")
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .content(objectNode.toPrettyString());

      mockMvc.perform(builder).andExpect(status().is(400));
    }
  }

  @Test
  void creatingASongPlaylistWithASingleTrackWithNumberGreaterThanOneShouldReturnBadRequest() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      User user = new User(
          "John",
          "Doe",
          date("2000-01-01"),
          UUID.randomUUID().toString() + "@streamus.com",
          UUID.randomUUID().toString()
      );
      user.save(connection);

      Song song1 = new Song(UUID.randomUUID().toString(), "Test song 1", 27);
      song1.save(connection);
      song1.delete(connection);

      ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
      objectNode.put("name", "Test SongPlaylist");
      objectNode.put("userId", user.getId());

      ArrayNode tracks = objectNode.putArray("tracks");

      ObjectNode song1Node = tracks.addObject();
      song1Node.put("songId", song1.getId());
      song1Node.put("trackNumber", 3);

      RequestBuilder builder = MockMvcRequestBuilders.post("/songplaylist")
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .content(objectNode.toPrettyString());

      mockMvc.perform(builder).andExpect(status().is(400));
    }
  }

  @Test
  void creatingASongPlaylistWithASingleTrackWithNumberLessThanOneShouldReturnBadRequest() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      User user = new User(
          "John",
          "Doe",
          date("2000-01-01"),
          UUID.randomUUID().toString() + "@streamus.com",
          UUID.randomUUID().toString()
      );
      user.save(connection);

      Song song1 = new Song(UUID.randomUUID().toString(), "Test song 1", 27);
      song1.save(connection);
      song1.delete(connection);

      ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
      objectNode.put("name", "Test SongPlaylist");
      objectNode.put("userId", user.getId());

      ArrayNode tracks = objectNode.putArray("tracks");

      ObjectNode song1Node = tracks.addObject();
      song1Node.put("songId", song1.getId());
      song1Node.put("trackNumber", 0);

      RequestBuilder builder = MockMvcRequestBuilders.post("/songplaylist")
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .content(objectNode.toPrettyString());

      mockMvc.perform(builder).andExpect(status().is(400));
    }
  }

}
