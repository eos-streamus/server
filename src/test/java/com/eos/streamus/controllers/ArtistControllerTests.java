package com.eos.streamus.controllers;

import com.eos.streamus.models.Band;
import com.eos.streamus.writers.JsonBandWriter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.sql.Connection;

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
}
