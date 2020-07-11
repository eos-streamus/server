package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Album;
import com.eos.streamus.models.ArtistDAO;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ArtistControllerTests extends JwtSetupControllerTests {

  @Test
  void gettingArtistsShouldReturnOkWithArray() throws Exception {
    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/artists")
                                                                  .contentType(MediaType.APPLICATION_JSON);
    MockHttpServletResponse response =
        perform(builder)
            .andExpect(status().is(200))
            .andReturn()
            .getResponse();
    JsonNode json = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
    assertNotNull(json.iterator());
  }

  @Test
  void gettingAnExistingBandShouldReturnOkWithCorrectJson() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      Band band = new Band("Test band");
      band.save(connection);

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(String.format("/artist/%d", band.getId()))
                                                                    .contentType(MediaType.APPLICATION_JSON);

      MockHttpServletResponse response =
          perform(builder)
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
    try (Connection connection = databaseConnector.getConnection()) {
      Musician musician = new Musician("Test musician");
      musician.save(connection);

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(String.format("/artist/%d", musician.getId()))
                                                                    .contentType(MediaType.APPLICATION_JSON);

      MockHttpServletResponse response =
          perform(builder)
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
    try (Connection connection = databaseConnector.getConnection()) {
      Musician musician = new Musician(
          new Person("John", "Doe", new Date(dateFormatter.parse("2000-01-01").getTime())));
      musician.save(connection);

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(String.format("/artist/%d", musician.getId()))
                                                                    .contentType(MediaType.APPLICATION_JSON);

      MockHttpServletResponse response =
          perform(builder)
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
    try (Connection connection = databaseConnector.getConnection()) {
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

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(String.format("/artist/%d", band.getId()))
                                                                    .contentType(MediaType.APPLICATION_JSON);

      MockHttpServletResponse response =
          perform(builder)
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
    try (Connection connection = databaseConnector.getConnection()) {
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

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(String.format("/artist/%d", band.getId()))
                                                                    .contentType(MediaType.APPLICATION_JSON);

      MockHttpServletResponse response =
          perform(builder)
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
    try (Connection connection = databaseConnector.getConnection()) {
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

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
          .get(String.format("/artist/%d/discography", band.getId()))
          .contentType(MediaType.APPLICATION_JSON);

      MockHttpServletResponse response =
          perform(builder)
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
    try (Connection connection = databaseConnector.getConnection()) {
      Musician musician = new Musician("Test musician");
      musician.save(connection);
      musician.delete(connection);

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(String.format("/artist/%d", musician.getId()))
                                                                    .contentType(MediaType.APPLICATION_JSON);

      perform(builder)
          .andExpect(status().is(404))
          .andReturn();
    }
  }

  @Test
  void gettingANonExistentArtistsDiscographyShouldReturnNotFound() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      Band band = new Band("Test band");
      band.save(connection);
      band.delete(connection);

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
          .get(String.format("/artist/%d/discography", band.getId()))
          .contentType(MediaType.APPLICATION_JSON);

      perform(builder)
          .andExpect(status().is(404))
          .andReturn();
    }
  }

  @Test
  void creatingABandWithCorrectDataShouldReturnCreatedBand() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("name", "Test band");

    MockHttpServletRequestBuilder builder =
        MockMvcRequestBuilders
            .post("/band")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectNode.toPrettyString());

    MockHttpServletResponse response = perform(builder).andExpect(status().is(200)).andReturn().getResponse();
    JsonNode json = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
    assertTrue(json.has("id"));

    try (Connection connection = databaseConnector.getConnection()) {
      Band band = Band.findById(json.get("id").asInt(), connection);
      assertEquals(new JsonBandWriter(band).getJson(), json);
      band.delete(connection);
    }
  }

  @Test
  void creatingABandWithIncorrectDataShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("name", "");

    MockHttpServletRequestBuilder builder =
        MockMvcRequestBuilders
            .post("/band")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectNode.toPrettyString());

    perform(builder).andExpect(status().is(400)).andReturn();

    objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("fail", "failValue");

    builder =
        MockMvcRequestBuilders
            .post("/band")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectNode.toPrettyString());

    perform(builder).andExpect(status().is(400)).andReturn();
  }

  @Test
  void creatingAMusicianWithANameShouldReturnOkWithCorrectJson() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("name", "test musician");

    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/musician")
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .content(objectNode.toPrettyString());

    MockHttpServletResponse response = perform(builder)
        .andExpect(status().is(200))
        .andReturn()
        .getResponse();
    JsonNode jsonNode = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
    assertTrue(jsonNode.has("id"));
    try (Connection connection = databaseConnector.getConnection()) {
      Musician musician = Musician.findById(jsonNode.get("id").asInt(), connection);
      assertEquals(new JsonMusicianWriter(musician).getJson(), jsonNode);
      musician.delete(connection);
    }
  }

  @Test
  void creatingAMusicianWithAPersonShouldReturnOkWithCorrectJson() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    ObjectNode personObjectNode = objectNode.putObject("person");
    personObjectNode.put("firstName", "John");
    personObjectNode.put("lastName", "Doe");
    personObjectNode.put("dateOfBirth", "2000-01-01");

    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/musician")
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .content(objectNode.toPrettyString());

    MockHttpServletResponse response = perform(builder)
        .andExpect(status().is(200))
        .andReturn()
        .getResponse();
    JsonNode jsonNode = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
    assertTrue(jsonNode.has("id"));
    try (Connection connection = databaseConnector.getConnection()) {
      Musician musician = Musician.findById(jsonNode.get("id").asInt(), connection);
      assertEquals(new JsonMusicianWriter(musician).getJson(), jsonNode);
      musician.delete(connection);
    }
  }

  @Test
  void creatingAMusicianWithAPersonAndNameShouldReturnOkWithCorrectJson() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("name", "test name");
    ObjectNode personObjectNode = objectNode.putObject("person");
    personObjectNode.put("firstName", "John");
    personObjectNode.put("lastName", "Doe");
    personObjectNode.put("dateOfBirth", "2000-01-01");

    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/musician")
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .content(objectNode.toPrettyString());

    MockHttpServletResponse response = perform(builder)
        .andExpect(status().is(200))
        .andReturn()
        .getResponse();
    JsonNode jsonNode = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
    assertTrue(jsonNode.has("id"));
    try (Connection connection = databaseConnector.getConnection()) {
      Musician musician = Musician.findById(jsonNode.get("id").asInt(), connection);
      assertEquals(new JsonMusicianWriter(musician).getJson(), jsonNode);
      musician.delete(connection);
    }
  }

  @Test
  void creatingAMusicianWithNoPersonAndNoNameShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());

    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/musician")
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .content(objectNode.toPrettyString());

    perform(builder)
        .andExpect(status().is(400));
  }

  @Test
  void creatingAMusicianWithNoPersonAndEmptyNameShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("name", "");

    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/musician")
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .content(objectNode.toPrettyString());

    perform(builder)
        .andExpect(status().is(400));
  }

  @Test
  void creatingAMusicianWithPersonAndEmptyNameShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("name", "");
    ObjectNode personObjectNode = objectNode.putObject("person");
    personObjectNode.put("firstName", "John");
    personObjectNode.put("lastName", "Doe");
    personObjectNode.put("dateOfBirth", "2000-01-01");
    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/musician")
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .content(objectNode.toPrettyString());

    perform(builder)
        .andExpect(status().is(400));
  }

  @Test
  void creatingAMusicianWithInvalidPersonAndValidNameShouldReturnBadRequest() throws Exception {

    // Test with empty first name
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("name", "test name");
    ObjectNode personObjectNode = objectNode.putObject("person");
    personObjectNode.put("firstName", "");
    personObjectNode.put("lastName", "Doe");
    personObjectNode.put("dateOfBirth", "2000-01-01");
    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/musician")
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .content(objectNode.toPrettyString());

    perform(builder)
        .andExpect(status().is(400));

    // Test with empty last name
    objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("name", "test name");
    personObjectNode = objectNode.putObject("person");
    personObjectNode.put("firstName", "John");
    personObjectNode.put("lastName", "");
    personObjectNode.put("dateOfBirth", "2000-01-01");
    builder = MockMvcRequestBuilders.post("/musician")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectNode.toPrettyString());

    perform(builder)
        .andExpect(status().is(400));

    // Test with date of birth in the future
    objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("name", "test name");
    personObjectNode = objectNode.putObject("person");
    personObjectNode.put("firstName", "John");
    personObjectNode.put("lastName", "Doe");
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new java.util.Date());
    calendar.add(Calendar.DATE, 1);
    personObjectNode
        .put("dateOfBirth",
             String.format(
                 "%s-%s-%s",
                 calendar.get(Calendar.YEAR),
                 calendar.get(Calendar.MONTH) + 1,
                 calendar.get(Calendar.DATE)
             )
        );
    builder = MockMvcRequestBuilders.post("/musician")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectNode.toPrettyString());

    perform(builder)
        .andExpect(status().is(400));

    // Test with no last name
    objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("name", "test name");
    personObjectNode = objectNode.putObject("person");
    personObjectNode.put("firstName", "John");
    personObjectNode.put("dateOfBirth", "2000-01-01");
    builder = MockMvcRequestBuilders.post("/musician")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectNode.toPrettyString());

    perform(builder)
        .andExpect(status().is(400));
  }

  @Test
  void addingABandMemberWithMusicianIdShouldReturnOkWithJson() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      Band band = new Band("Test name");
      band.save(connection);

      Musician musician = new Musician("Test musician");
      musician.save(connection);

      ObjectNode bandMemberData = new ObjectNode(new TestJsonFactory());
      bandMemberData.put("musicianId", musician.getId());
      bandMemberData.put("from", "2000-01-01");

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
          .post(String.format("/band/%d/members", band.getId()))
          .contentType(MediaType.APPLICATION_JSON)
          .content(bandMemberData.toPrettyString());
      MockHttpServletResponse response = perform(builder).andExpect(status().is(200)).andReturn().getResponse();
      JsonNode result = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
      band.fetchMembers(connection);
      assertEquals(new JsonBandWriter(band).getJson(), result);
      musician.delete(connection);
      band.delete(connection);
    }
  }

  @Test
  void addingABandMemberWithANewNameMusicianShouldReturnOkWithJson() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      Band band = new Band("Test name");
      band.save(connection);

      ObjectNode bandMemberData = new ObjectNode(new TestJsonFactory());
      ObjectNode musicianNode = bandMemberData.putObject("musician");
      musicianNode.put("name", "Test musician");
      bandMemberData.put("from", "2000-01-01");

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
          .post(String.format("/band/%d/members", band.getId()))
          .contentType(MediaType.APPLICATION_JSON)
          .content(bandMemberData.toPrettyString());
      MockHttpServletResponse response = perform(builder).andExpect(status().is(200)).andReturn().getResponse();
      JsonNode result = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
      band.fetchMembers(connection);
      assertEquals(new JsonBandWriter(band).getJson(), result);
      band.delete(connection);
    }
  }

  @Test
  void addingABandMemberWithANewPersonMusicianShouldReturnOkWithJson() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      Band band = new Band("Test name");
      band.save(connection);

      ObjectNode bandMemberData = new ObjectNode(new TestJsonFactory());
      ObjectNode musicianNode = bandMemberData.putObject("musician");
      ObjectNode personNode = musicianNode.putObject("person");
      personNode.put("firstName", "John");
      personNode.put("lastName", "Doe");
      personNode.put("dateOfBirth", "2000-01-01");
      bandMemberData.put("from", "2000-01-01");

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
          .post(String.format("/band/%d/members", band.getId()))
          .contentType(MediaType.APPLICATION_JSON)
          .content(bandMemberData.toPrettyString());
      MockHttpServletResponse response = perform(builder).andExpect(status().is(200)).andReturn().getResponse();
      JsonNode result = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
      band.fetchMembers(connection);
      assertEquals(new JsonBandWriter(band).getJson(), result);
      band.delete(connection);
    }
  }

  @Test
  void addingABandMemberWithANewNameAndPersonMusicianShouldReturnOkWithJson() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      Band band = new Band("Test name");
      band.save(connection);

      ObjectNode bandMemberData = new ObjectNode(new TestJsonFactory());
      ObjectNode musicianNode = bandMemberData.putObject("musician");
      musicianNode.put("name", "Test name");
      ObjectNode personNode = musicianNode.putObject("person");
      personNode.put("firstName", "John");
      personNode.put("lastName", "Doe");
      personNode.put("dateOfBirth", "2000-01-01");
      bandMemberData.put("from", "2000-01-01");

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
          .post(String.format("/band/%d/members", band.getId()))
          .contentType(MediaType.APPLICATION_JSON)
          .content(bandMemberData.toPrettyString());
      MockHttpServletResponse response = perform(builder).andExpect(status().is(200)).andReturn()
                                                         .getResponse();
      JsonNode result = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
      band.fetchMembers(connection);
      assertEquals(new JsonBandWriter(band).getJson(), result);
      band.delete(connection);
    }
  }

  @Test
  void addingABandMemberWithFromGreaterThanToShouldReturnBadRequest() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      Band band = new Band("Test name");
      band.save(connection);

      ObjectNode bandMemberData = new ObjectNode(new TestJsonFactory());
      ObjectNode musicianNode = bandMemberData.putObject("musician");
      musicianNode.put("name", "Test name");
      ObjectNode personNode = musicianNode.putObject("person");
      personNode.put("firstName", "John");
      personNode.put("lastName", "Doe");
      personNode.put("dateOfBirth", "2000-01-01");
      bandMemberData.put("from", "2000-01-01");
      bandMemberData.put("to", "1999-01-01");

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
          .post(String.format("/band/%d/members", band.getId()))
          .contentType(MediaType.APPLICATION_JSON)
          .content(bandMemberData.toPrettyString());
      perform(builder)
          .andExpect(status().is(400))
          .andReturn();
    }
  }

  @Test
  void addingABandMemberWithOverlappingMembershipsToSameBandShouldReturnBadRequest() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      Band band = new Band("Test name");
      band.save(connection);

      Musician musician = new Musician("Test musician");
      musician.save(connection);
      band.addMember(musician, date("2000-01-01"), date("2010-01-01"));
      band.save(connection);

      ObjectNode bandMemberData = new ObjectNode(new TestJsonFactory());
      bandMemberData.put("musicianId", musician.getId());
      bandMemberData.put("from", "2005-01-01");

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
          .post(String.format("/band/%d/members", band.getId()))
          .contentType(MediaType.APPLICATION_JSON)
          .content(bandMemberData.toPrettyString());
      perform(builder)
          .andExpect(status().is(400))
          .andReturn();
    }
  }

  @Test
  void addingABandMemberWithoutFromDateShouldReturnBadRequest() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      Band band = new Band("Test name");
      band.save(connection);

      Musician musician = new Musician("Test musician");
      musician.save(connection);

      ObjectNode bandMemberData = new ObjectNode(new TestJsonFactory());
      bandMemberData.put("musicianId", musician.getId());

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
          .post(String.format("/band/%d/members", band.getId()))
          .contentType(MediaType.APPLICATION_JSON)
          .content(bandMemberData.toPrettyString());
      perform(builder)
          .andExpect(status().is(400))
          .andReturn();
    }
  }

  // Delete tests
  @Test
  void deletingAnArtistShouldBeSuccessful() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      Band band = new Band("Test band");
      band.save(connection);

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.delete(String.format("/artist/%d", band.getId()));
      perform(builder).andExpect(status().is(200));

      assertThrows(NoResultException.class, () -> ArtistDAO.findById(band.getId(), connection));
    }
  }

  @Test
  void deletingANonExistingArtistShouldReturnNotFound() throws Exception {
    try (Connection connection = databaseConnector.getConnection()) {
      Band band = new Band("Test band");
      band.save(connection);
      band.delete(connection);

      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.delete(String.format("/artist/%d", band.getId()));
      perform(builder).andExpect(status().is(404));
    }
  }

}
