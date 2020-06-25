package com.eos.streamus.controllers;

import com.eos.streamus.models.Album;
import com.eos.streamus.models.Band;
import com.eos.streamus.models.Musician;
import com.eos.streamus.models.Person;
import com.eos.streamus.models.Song;
import com.eos.streamus.models.SongCollection;
import com.eos.streamus.writers.JsonAlbumListWriter;
import com.eos.streamus.writers.JsonBandWriter;
import com.eos.streamus.writers.JsonMusicianWriter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ArtistControllerTests extends ControllerTests {

  @Test
  void gettingArtistsShouldReturnOkWithArray() throws Exception {
    RequestBuilder builder = MockMvcRequestBuilders.get("/artists")
                                                   .contentType(MediaType.APPLICATION_JSON);
    MockHttpServletResponse response =
        mockMvc
            .perform(builder)
            .andExpect(status().is(200))
            .andReturn()
            .getResponse();
    JsonNode json = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
    assertNotNull(json.iterator());
  }

  @Test
  void gettingAnExistingBandShouldReturnOkWithCorrectJson() throws Exception {
    try (Connection connection = databaseConnection.getConnection()) {
      Band band = new Band("Test band");
      band.save(connection);

      RequestBuilder builder = MockMvcRequestBuilders.get(String.format("/artist/%d", band.getId()))
                                                     .contentType(MediaType.APPLICATION_JSON);

      MockHttpServletResponse response =
          mockMvc
              .perform(builder)
              .andExpect(status().is(200))
              .andReturn()
              .getResponse();

      JsonNode json = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
      assertEquals(new JsonBandWriter(band).getJson(), json);
      band.delete(connection);
    }
  }

  @Test
  void gettingAnExistingMusicianShouldReturnOkWithCorrectJson() throws Exception {
    try (Connection connection = databaseConnection.getConnection()) {
      Musician musician = new Musician("Test musician");
      musician.save(connection);

      RequestBuilder builder = MockMvcRequestBuilders.get(String.format("/artist/%d", musician.getId()))
                                                     .contentType(MediaType.APPLICATION_JSON);

      MockHttpServletResponse response =
          mockMvc
              .perform(builder)
              .andExpect(status().is(200))
              .andReturn()
              .getResponse();

      JsonNode json = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
      assertEquals(new JsonMusicianWriter(musician).getJson(), json);
      musician.delete(connection);
    }
  }

  @Test
  void gettingAnExistingPersonMusicianShouldReturnOkWithCorrectJson() throws Exception {
    try (Connection connection = databaseConnection.getConnection()) {
      Musician musician = new Musician(
          new Person("John", "Doe", new Date(dateFormatter.parse("2000-01-01").getTime())));
      musician.save(connection);

      RequestBuilder builder = MockMvcRequestBuilders.get(String.format("/artist/%d", musician.getId()))
                                                     .contentType(MediaType.APPLICATION_JSON);

      MockHttpServletResponse response =
          mockMvc
              .perform(builder)
              .andExpect(status().is(200))
              .andReturn()
              .getResponse();

      JsonNode json = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
      assertEquals(new JsonMusicianWriter(musician).getJson(), json);
      musician.delete(connection);
      musician.getPerson().delete(connection);
    }
  }

  @Test
  void gettingAnExistingBandWithMembersShouldReturnOkWithCorrectJson() throws Exception {
    try (Connection connection = databaseConnection.getConnection()) {
      Band band = new Band("Test band");
      band.save(connection);

      Musician musician = new Musician("Test musician");
      musician.save(connection);

      band.addMember(
          musician,
          new Date(dateFormatter.parse("2000-01-01").getTime()),
          new Date(dateFormatter.parse("2001-01-01").getTime())
      );

      band.save(connection);

      RequestBuilder builder = MockMvcRequestBuilders.get(String.format("/artist/%d", band.getId()))
                                                     .contentType(MediaType.APPLICATION_JSON);

      MockHttpServletResponse response =
          mockMvc
              .perform(builder)
              .andExpect(status().is(200))
              .andReturn()
              .getResponse();

      JsonNode json = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
      assertEquals(new JsonBandWriter(band).getJson(), json);
      band.delete(connection);

    }
  }

  @Test
  void gettingAnExistingArtistWithAlbumsShouldReturnOkWithCorrectJson() throws Exception {
    try (Connection connection = databaseConnection.getConnection()) {
      Album album = new Album("Test album", date("2000-01-01"));
      List<Path> paths = new ArrayList<>();
      for (int i = 0; i < 10; i++) {
        Path path = Files.copy(
            SAMPLE_AUDIO_PATH,
            Paths.get(resourcePathResolver.getAudioDir() + "sample-audio-" + UUID.randomUUID() + ".mp3")
        );
        paths.add(path);
        Song song = new Song(path.toString(), "sample audio", 27);
        song.save(connection);
        album.addSong(song);
      }
      album.save(connection);

      Band band = new Band("Test band");
      band.save(connection);
      album.addArtist(band);
      album.save(connection);

      band.fetchAlbums(connection);

      RequestBuilder builder = MockMvcRequestBuilders.get(String.format("/artist/%d", band.getId()))
                                                     .contentType(MediaType.APPLICATION_JSON);

      MockHttpServletResponse response =
          mockMvc
              .perform(builder)
              .andExpect(status().is(200))
              .andReturn()
              .getResponse();
      JsonNode json = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
      assertEquals(new JsonBandWriter(band).getJson(), json);

      for (Path path : paths) {
        Files.delete(path);
      }
      for (SongCollection.Track track : album.getTracks()) {
        track.getSong().delete(connection);
      }
      album.delete(connection);
      band.delete(connection);
    }
  }

  @Test
  void gettingAnArtistsDiscographyShouldReturnOkWithCorrectJson() throws Exception {
    try (Connection connection = databaseConnection.getConnection()) {
      Album album = new Album("Test album", date("2000-01-01"));
      List<Path> paths = new ArrayList<>();
      for (int i = 0; i < 10; i++) {
        Path path = Files.copy(
            SAMPLE_AUDIO_PATH,
            Paths.get(resourcePathResolver.getAudioDir() + "sample-audio-" + UUID.randomUUID() + ".mp3")
        );
        paths.add(path);
        Song song = new Song(path.toString(), "sample audio", 27);
        song.save(connection);
        album.addSong(song);
      }
      album.save(connection);

      Band band = new Band("Test band");
      band.save(connection);
      album.addArtist(band);
      album.save(connection);

      band.fetchAlbums(connection);

      RequestBuilder builder = MockMvcRequestBuilders.get(String.format("/artist/%d/discography", band.getId()))
                                                     .contentType(MediaType.APPLICATION_JSON);

      MockHttpServletResponse response =
          mockMvc
              .perform(builder)
              .andExpect(status().is(200))
              .andReturn()
              .getResponse();
      JsonNode json = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
      assertEquals(new JsonAlbumListWriter(band.getAlbums()).getJson(), json);

      for (Path path : paths) {
        Files.delete(path);
      }
      for (SongCollection.Track track : album.getTracks()) {
        track.getSong().delete(connection);
      }
      album.delete(connection);
      band.delete(connection);
    }
  }

  @Test
  void gettingANonExistentArtistShouldReturnNotFound() throws Exception {
    try (Connection connection = databaseConnection.getConnection()) {
      Musician musician = new Musician("Test musician");
      musician.save(connection);
      musician.delete(connection);

      RequestBuilder builder = MockMvcRequestBuilders.get(String.format("/artist/%d", musician.getId()))
                                                     .contentType(MediaType.APPLICATION_JSON);

      mockMvc
          .perform(builder)
          .andExpect(status().is(404))
          .andReturn();
    }
  }

  @Test
  void gettingANonExistentArtistsDiscographyShouldReturnNotFound() throws Exception {
    try (Connection connection = databaseConnection.getConnection()) {
      Band band = new Band("Test band");
      band.save(connection);
      band.delete(connection);

      RequestBuilder builder = MockMvcRequestBuilders.get(String.format("/artist/%d/discography", band.getId()))
                                                     .contentType(MediaType.APPLICATION_JSON);

      mockMvc
          .perform(builder)
          .andExpect(status().is(404))
          .andReturn();
    }
  }

  @Test
  void creatingABandWithCorrectDataShouldReturnCreatedBand() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("name", "Test band");

    RequestBuilder builder =
        MockMvcRequestBuilders
            .post("/band")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectNode.toPrettyString());

    MockHttpServletResponse response = mockMvc.perform(builder).andExpect(status().is(200)).andReturn().getResponse();
    JsonNode json = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
    assertTrue(json.has("id"));

    try (Connection connection = databaseConnection.getConnection()) {
      Band band = Band.findById(json.get("id").asInt(), connection);
      assertEquals(new JsonBandWriter(band).getJson(), json);
      band.delete(connection);
    }
  }

  @Test
  void creatingABandWithIncorrectDataShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("name", "");

    RequestBuilder builder =
        MockMvcRequestBuilders
            .post("/band")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectNode.toPrettyString());

    mockMvc.perform(builder).andExpect(status().is(400)).andReturn();

    objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("fail", "failValue");

    builder =
        MockMvcRequestBuilders
            .post("/band")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectNode.toPrettyString());

    mockMvc.perform(builder).andExpect(status().is(400)).andReturn();
  }

  @Test
  void creatingAMusicianWithANameShouldReturnOkWithCorrectJson() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("name", "test musician");

    RequestBuilder builder = MockMvcRequestBuilders.post("/musician")
                                                   .contentType(MediaType.APPLICATION_JSON)
                                                   .content(objectNode.toPrettyString());

    MockHttpServletResponse response = mockMvc.perform(builder)
                                              .andExpect(status().is(200))
                                              .andReturn()
                                              .getResponse();
    JsonNode jsonNode = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
    assertTrue(jsonNode.has("id"));
    try (Connection connection = databaseConnection.getConnection()) {
      Musician musician = Musician.findById(jsonNode.get("id").asInt(), connection);
      assertEquals(new JsonMusicianWriter(musician).getJson(), jsonNode);
      musician.delete(connection);
    }
  }
}
