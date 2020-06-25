package com.eos.streamus.controllers;

import com.eos.streamus.models.Band;
import com.eos.streamus.models.Musician;
import com.eos.streamus.models.Person;
import com.eos.streamus.writers.JsonBandWriter;
import com.eos.streamus.writers.JsonMusicianWriter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.sql.Connection;
import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
}
