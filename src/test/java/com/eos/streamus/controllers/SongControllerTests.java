package com.eos.streamus.controllers;

import com.eos.streamus.StreamusTestConfiguration;
import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Song;
import com.eos.streamus.utils.IDatabaseConnection;
import com.eos.streamus.utils.IResourcePathResolver;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SongController.class)
@ContextConfiguration(classes = StreamusTestConfiguration.class)
@AutoConfigureMockMvc
public class SongControllerTests {
  static class JsonSongFactory extends JsonNodeFactory {
    private static final long serialVersionUID = 6068382117192685166L;

  }

  private static final Path SAMPLE_AUDIO_PATH = Paths.get(
      String.format(
          "src%stest%sresources%ssample-audio.mp3",
          File.separator,
          File.separator,
          File.separator
      )
  );

  private static final Path SAMPLE_VIDEO_PATH = Paths.get(
      String.format(
          "src%stest%sresources%ssample-video.mp4",
          File.separator,
          File.separator,
          File.separator
      )
  );

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private IResourcePathResolver resourcePathResolver;

  @Autowired
  private IDatabaseConnection databaseConnection;

  //#region Get song
  @Test
  void gettingAnExistingSongShouldReturnPartialContent() throws Exception {
    Path path = Files.copy(
        SAMPLE_AUDIO_PATH,
        Paths.get(resourcePathResolver.getAudioDir() + "sample-audio-" + UUID.randomUUID() + ".mp3")
    );

    Song song = new Song(path.toString(), "sample audio", 27);
    try (Connection connection = databaseConnection.getConnection()) {
      song.save(connection);

      // Get Song
      RequestBuilder builder =
          MockMvcRequestBuilders
              .get(String.format("/song/%d", song.getId()))
              .contentType(MediaType.APPLICATION_JSON);
      MvcResult result = mockMvc.perform(builder)
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
    try (Connection connection = databaseConnection.getConnection()) {
      song.save(connection);
      song.delete(connection);

      // Get Song
      RequestBuilder builder =
          MockMvcRequestBuilders
              .get(String.format("/song/%d", song.getId()))
              .contentType(MediaType.APPLICATION_JSON);
      mockMvc
          .perform(builder)
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
    try (Connection connection = databaseConnection.getConnection()) {
      song.save(connection);

      // Delete Song
      RequestBuilder builder =
          MockMvcRequestBuilders
              .delete(String.format("/song/%d", song.getId()))
              .contentType(MediaType.APPLICATION_JSON);
      mockMvc
          .perform(builder)
          .andExpect(status().is(200));

      assertThrows(NoResultException.class, () -> Song.findById(song.getId(), connection));
      assertFalse(Files.exists(path));
    }
  }

  @Test
  void aDeleteRequestShouldReturnNotFoundIfSongDoesNotExist() throws Exception {

    try (Connection connection = databaseConnection.getConnection()) {
      Song song = new Song("randomPath", "randomName", 27);
      song.save(connection);
      song.delete(connection);
      RequestBuilder builder = MockMvcRequestBuilders
          .get(String.format("/song/%d", song.getId()))
          .contentType(MediaType.APPLICATION_JSON);
      mockMvc
          .perform(builder)
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
    MockHttpServletResponse response = mockMvc
        .perform(requestBuilder)
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
    mockMvc
        .perform(requestBuilder)
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

    mockMvc
        .perform(requestBuilder)
        .andExpect(status().is(400));
  }

  @Test
  void postingASongWithNoDataAtAllShouldReturnBadRequest() throws Exception {
    MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
        .multipart("/song");
    mockMvc
        .perform(requestBuilder)
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

    mockMvc
        .perform(requestBuilder)
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

    mockMvc
        .perform(requestBuilder)
        .andExpect(status().is(400));
  }
  //#endregion Post song

}
