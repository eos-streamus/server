package com.eos.streamus.controllers;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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
}
