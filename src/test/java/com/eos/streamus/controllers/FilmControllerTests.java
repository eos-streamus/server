package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Film;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FilmControllerTests extends ControllerTests {

  //#region Get film
  @Test
  void gettingAnExistingFilmShouldReturnContent() throws Exception {
    Path path = Files.copy(
        SAMPLE_VIDEO_PATH,
        Paths.get(resourcePathResolver.getVideoDir() + "sample-video-" + UUID.randomUUID() + ".mp3")
    );

    Film film = new Film(path.toString(), "sample film", 27);
    try (Connection connection = databaseConnector.getConnection()) {
      film.save(connection);

      // Get Film
      RequestBuilder builder =
          MockMvcRequestBuilders
              .get(String.format("/film/%d", film.getId()))
              .contentType(MediaType.APPLICATION_JSON);
      MockHttpServletResponse response = mockMvc.perform(builder)
                                                .andExpect(status().is(200))
                                                .andReturn()
                                                .getResponse();

      JsonNode json = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());

      assertTrue("Result has id", json.has("id"));
      assertTrue("Result has name", json.has("name"));
      assertTrue("Result has duration", json.has("duration"));

      assertEquals(film.getId(), json.get("id").asInt());
      assertEquals(film.getName(), json.get("name").asText());
      assertEquals(film.getDuration(), json.get("duration").asInt());

      Files.delete(path);
      film.delete(connection);
    }
  }

  @Test
  void gettingANonExistingFilmShouldReturnNotFound() throws Exception {
    Path path = Files.copy(
        SAMPLE_VIDEO_PATH,
        Paths.get(resourcePathResolver.getVideoDir() + "sample-video-" + UUID.randomUUID() + ".mp3")
    );

    Film film = new Film(path.toString(), "sample film", 27);
    try (Connection connection = databaseConnector.getConnection()) {
      film.save(connection);

      // Get Film
      RequestBuilder builder =
          MockMvcRequestBuilders
              .get(String.format("/film/%d", film.getId()))
              .contentType(MediaType.APPLICATION_JSON);

      Files.delete(path);
      film.delete(connection);

      mockMvc.perform(builder)
             .andExpect(status().is(404))
             .andReturn();
    }
  }

  @Test
  void gettingAnExistingFilmStreamShouldReturnPartialContent() throws Exception {
    Path path = Files.copy(
        SAMPLE_VIDEO_PATH,
        Paths.get(resourcePathResolver.getVideoDir() + "sample-video-" + UUID.randomUUID() + ".mp3")
    );

    Film film = new Film(path.toString(), "sample film", 27);
    try (Connection connection = databaseConnector.getConnection()) {
      film.save(connection);

      // Get Film
      RequestBuilder builder =
          MockMvcRequestBuilders
              .get(String.format("/film/%d/stream", film.getId()));

      mockMvc.perform(builder)
             .andExpect(status().is(206))
             .andReturn();

      Files.delete(path);
      film.delete(connection);
    }
  }

  @Test
  void gettingANonExistingFilmStreamShouldReturnNotFound() throws Exception {
    Path path = Files.copy(
        SAMPLE_VIDEO_PATH,
        Paths.get(resourcePathResolver.getVideoDir() + "sample-video-" + UUID.randomUUID() + ".mp3")
    );

    Film film = new Film(path.toString(), "sample film", 27);
    try (Connection connection = databaseConnector.getConnection()) {
      film.save(connection);
      film.delete(connection);
      Files.delete(path);

      // Get Film
      RequestBuilder builder =
          MockMvcRequestBuilders
              .get(String.format("/film/%d/stream", film.getId()));

      mockMvc.perform(builder)
             .andExpect(status().is(404))
             .andReturn();

    }
  }
  //#endregion Get film

  //#region Delete film
  @Test
  void deletingAFilmShouldBeSuccessful() throws Exception {
    Path path = Files.copy(
        SAMPLE_VIDEO_PATH,
        Paths.get(resourcePathResolver.getVideoDir() + "sample-video-" + UUID.randomUUID() + ".mp3")
    );

    Film film = new Film(path.toString(), "sample video", 27);
    try (Connection connection = databaseConnector.getConnection()) {
      film.save(connection);

      // Delete Film
      RequestBuilder builder =
          MockMvcRequestBuilders
              .delete(String.format("/film/%d", film.getId()))
              .contentType(MediaType.APPLICATION_JSON);
      mockMvc
          .perform(builder)
          .andExpect(status().is(200));

      assertThrows(NoResultException.class, () -> Film.findById(film.getId(), connection));
      assertFalse(Files.exists(path));
    }
  }

  @Test
  void deletingANonExistentFilmShouldReturnNotFound() throws Exception {
    Path path = Files.copy(
        SAMPLE_VIDEO_PATH,
        Paths.get(resourcePathResolver.getVideoDir() + "sample-video-" + UUID.randomUUID() + ".mp3")
    );

    Film film = new Film(path.toString(), "sample video", 27);
    try (Connection connection = databaseConnector.getConnection()) {
      film.save(connection);
      film.delete(connection);
      Files.delete(path);

      // Delete Film
      RequestBuilder builder =
          MockMvcRequestBuilders
              .delete(String.format("/film/%d", film.getId()))
              .contentType(MediaType.APPLICATION_JSON);
      mockMvc
          .perform(builder)
          .andExpect(status().is(404));
    }
  }
  //#endregion Delete film

  //#region Post film
  @Test
  void postingAFilmWithCorrectDataShouldReturnOk() throws Exception {
    MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
        .multipart("/film");

    MockMultipartFile mockMultipartFile = new MockMultipartFile(
        "file",
        "sample-video.mp4",
        "video/mp4",
        new FileInputStream(SAMPLE_VIDEO_PATH.toFile())
    );
    requestBuilder
        .file(mockMultipartFile)
        .param("name", "sample-video.mp4");
    MockHttpServletResponse response = mockMvc
        .perform(requestBuilder)
        .andExpect(status().is(200)).andReturn()
        .getResponse();
    JsonNode json = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
    assertNotNull(json.get("id"));
    assertNotNull(json.get("name"));
    assertEquals("sample-video.mp4", json.get("name").asText());
    assertNotNull(json.get("duration"));
  }

  @Test
  void postingAFilmWithNoFileShouldReturnBadRequest() throws Exception {
    MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
        .multipart("/film");

    requestBuilder
        .param("name", "sample-video.mp4");
    mockMvc
        .perform(requestBuilder)
        .andExpect(status().is(400));
  }

  @Test
  void postingAFilmWithNoNameShouldReturnBadRequest() throws Exception {
    MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
        .multipart("/film");

    MockMultipartFile mockMultipartFile = new MockMultipartFile(
        "file",
        "sample-video.mp4",
        "video/mp4",
        new FileInputStream(SAMPLE_AUDIO_PATH.toFile())
    );
    requestBuilder
        .file(mockMultipartFile);

    mockMvc
        .perform(requestBuilder)
        .andExpect(status().is(400));
  }

  @Test
  void postingAFilmWithNoDataAtAllShouldReturnBadRequest() throws Exception {
    MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
        .multipart("/film");
    mockMvc
        .perform(requestBuilder)
        .andExpect(status().is(400));

  }

  @Test
  void postingAFilmWithInvalidContentTypeShouldReturnBadRequest() throws Exception {
    MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
        .multipart("/film");

    MockMultipartFile mockMultipartFile = new MockMultipartFile(
        "file",
        "sample-video.mp4",
        "fail",
        new FileInputStream(SAMPLE_AUDIO_PATH.toFile())
    );
    requestBuilder
        .file(mockMultipartFile)
        .param("name", "sample-video.mp4");

    mockMvc
        .perform(requestBuilder)
        .andExpect(status().is(400));
  }

  @Test
  void postingAFilmWithAudioContentShouldReturnBadRequest() throws Exception {
    MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
        .multipart("/film");

    MockMultipartFile mockMultipartFile = new MockMultipartFile(
        "file",
        "sample-video.mp4",
        "video/mp4",
        new FileInputStream(SAMPLE_AUDIO_PATH.toFile())
    );
    requestBuilder
        .file(mockMultipartFile)
        .param("name", "sample-video.mp4");

    mockMvc
        .perform(requestBuilder)
        .andExpect(status().is(400));
  }
  //#endregion Post film
}
