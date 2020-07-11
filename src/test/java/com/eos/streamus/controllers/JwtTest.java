package com.eos.streamus.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JwtTest extends JwtSetupControllerTests {
  @Test
  void accessingAJwtProtectedEndpointWithAnInvalidTokenShouldReturnForbidden() throws Exception {
    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/films");
    builder.header("Authorization", "Bearer wrongToken");
    mockMvc.perform(builder).andExpect(status().is(403));
  }
}
