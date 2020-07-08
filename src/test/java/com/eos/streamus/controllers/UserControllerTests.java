package com.eos.streamus.controllers;

import com.eos.streamus.models.User;
import com.eos.streamus.writers.JsonUserWriter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.sql.Connection;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTests extends ControllerTests {
  @Test
  void signingUpWithValidUserDataShouldWork() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID().toString() + "@streamus.com";
    objectNode.put("email", email);
    objectNode.put("username", "johnDoe");
    objectNode.put("firstName", "John");
    objectNode.put("lastName", "Doe");
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", "JohnDoe-password");
    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/users")
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .content(objectNode.toPrettyString());
    MockHttpServletResponse response = perform(builder).andExpect(status().is(200)).andReturn().getResponse();
    try (Connection connection = databaseConnector.getConnection()) {
      User user = User.findByEmail(email, connection);
      assertEquals(new JsonUserWriter(user).getJson(),
                   new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString()));
    }
  }

}
