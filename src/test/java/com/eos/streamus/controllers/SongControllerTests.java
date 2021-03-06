package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Song;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.servlet.http.Cookie;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SongControllerTests extends JwtSetupControllerTests {

  //#region Get song
  @Test
  void gettingAnExistingSongShouldReturnPartialContent() throws Exception {
    Path path = Files.copy(
        SAMPLE_AUDIO_PATH,
        Paths.get(resourcePathResolver.getAudioDir() + "sample-audio-" + UUID.randomUUID() + ".mp3")
    );

    Song song = new Song(path.toString(), "sample audio", 27);
    try (Connection connection = databaseConnector.getConnection()) {
      song.save(connection);

      // Get Song
      MockHttpServletRequestBuilder builder =
          MockMvcRequestBuilders
              .get(String.format("/song/%d/stream", song.getId()))
              .cookie(new Cookie("streamusSessionToken", getToken().getSessionToken()));
      MvcResult result = perform(builder)
          .andExpect(status().is(206))
          .andReturn();
      assertTrue("Result length > 0", result.getResponse().getContentAsByteArray().length > 0);

      Files.delete(path);
      song.delete(connection);
    }
  }

  @Test
  void gettingANonExistingSongShouldReturn404() throws Exception {
    Song song = new Song(SAMPLE_AUDIO_PATH.toString(), "sample audio", 27);
    try (Connection connection = databaseConnector.getConnection()) {
      song.save(connection);
      song.delete(connection);

      // Get Song
      MockHttpServletRequestBuilder builder =
          MockMvcRequestBuilders
              .get(String.format("/song/%d", song.getId()))
              .contentType(MediaType.APPLICATION_JSON);
      perform(builder)
          .andExpect(status().is(404));
    }
  }
  //#endregion Get song

  //#region Delete song
  @Test
  void deletingAnExistingSongShouldBeSuccessful() throws Exception {
    Path path = Files.copy(
        SAMPLE_AUDIO_PATH,
        Paths.get(resourcePathResolver.getAudioDir() + "sample-audio-" + UUID.randomUUID() + ".mp3")
    );

    Song song = new Song(path.toString(), "sample audio", 27);
    try (Connection connection = databaseConnector.getConnection()) {
      song.save(connection);

      // Delete Song
      MockHttpServletRequestBuilder builder =
          MockMvcRequestBuilders
              .delete(String.format("/song/%d", song.getId()))
              .contentType(MediaType.APPLICATION_JSON);
      perform(builder)
          .andExpect(status().is(200));

      assertThrows(NoResultException.class, () -> Song.findById(song.getId(), connection));
      assertFalse(Files.exists(path));
    }
  }

  @Test
  void aDeleteRequestShouldReturnNotFoundIfSongDoesNotExist() throws Exception {

    try (Connection connection = databaseConnector.getConnection()) {
      Song song = new Song("randomPath", "randomName", 27);
      song.save(connection);
      song.delete(connection);
      MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
          .get(String.format("/song/%d", song.getId()))
          .contentType(MediaType.APPLICATION_JSON);
      perform(builder)
          .andExpect(status().is(404));
    }
  }
  //#endregion Delete song

  //#region Post song
  @Test
  void postingASongWithCorrectDataShouldReturnOk() throws Exception {
    MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
        .multipart("/song");

    MockMultipartFile mockMultipartFile = new MockMultipartFile(
        "file",
        "sample-audio.mp3",
        "audio/mp4",
        new FileInputStream(SAMPLE_AUDIO_PATH.toFile())
    );
    requestBuilder
        .file(mockMultipartFile)
        .param("name", "sample-audio.mp3");
    MockHttpServletResponse response = perform(requestBuilder)
        .andExpect(status().is(200)).andReturn()
        .getResponse();
    JsonNode json = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString());
    assertNotNull(json.get("id"));
    assertNotNull(json.get("name"));
    assertEquals("sample-audio.mp3", json.get("name").asText());
    assertNotNull(json.get("duration"));
  }

  @Test
  void postingASongWithNoFileShouldReturnBadRequest() throws Exception {
    MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
        .multipart("/song");

    requestBuilder
        .param("name", "sample-audio.mp3");
    perform(requestBuilder)
        .andExpect(status().is(400));
  }

  @Test
  void postingASongWithNoNameShouldReturnBadRequest() throws Exception {
    MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
        .multipart("/song");

    MockMultipartFile mockMultipartFile = new MockMultipartFile(
        "file",
        "sample-audio.mp3",
        "audio/mp4",
        new FileInputStream(SAMPLE_AUDIO_PATH.toFile())
    );
    requestBuilder
        .file(mockMultipartFile);

    perform(requestBuilder)
        .andExpect(status().is(400));
  }

  @Test
  void postingASongWithNoDataAtAllShouldReturnBadRequest() throws Exception {
    MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
        .multipart("/song");
    perform(requestBuilder)
        .andExpect(status().is(400));

  }

  @Test
  void postingASongWithInvalidContentTypeShouldReturnBadRequest() throws Exception {

    MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
        .multipart("/song");

    MockMultipartFile mockMultipartFile = new MockMultipartFile(
        "file",
        "sample-audio.mp3",
        "fail",
        new FileInputStream(SAMPLE_AUDIO_PATH.toFile())
    );
    requestBuilder
        .file(mockMultipartFile)
        .param("name", "sample-audio.mp3");

    perform(requestBuilder)
        .andExpect(status().is(400));
  }

  @Test
  void postingASongWithVideoContentShouldReturnBadRequest() throws Exception {

    MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
        .multipart("/song");

    MockMultipartFile mockMultipartFile = new MockMultipartFile(
        "file",
        "sample-audio.mp3",
        "audio/mp4",
        new FileInputStream(SAMPLE_VIDEO_PATH.toFile())
    );
    requestBuilder
        .file(mockMultipartFile)
        .param("name", "sample-audio.mp3");

    perform(requestBuilder)
        .andExpect(status().is(400));
  }
  //#endregion Post song

}
