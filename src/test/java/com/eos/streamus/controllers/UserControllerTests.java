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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

  @Test
  void signingUpWithAnAlreadyUsedEmailShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID().toString() + "@streamus.com";

    try (Connection connection = databaseConnector.getConnection()) {
      new User("John", "Doe", java.sql.Date.valueOf("2000-01-01"), email, "john_doe")
          .save(connection);
    }

    objectNode.put("email", email);
    objectNode.put("username", "johnDoe");
    objectNode.put("firstName", "John");
    objectNode.put("lastName", "Doe");
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", "JohnDoe-password");
    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/users")
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithMissingEmailShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID().toString() + "@streamus.com";
    objectNode.put("username", "johnDoe");
    objectNode.put("firstName", "John");
    objectNode.put("lastName", "Doe");
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", "JohnDoe-password");
    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/users")
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithMissingUsernameShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID().toString() + "@streamus.com";
    objectNode.put("email", email);
    objectNode.put("firstName", "John");
    objectNode.put("lastName", "Doe");
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", "JohnDoe-password");
    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/users")
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithMissingFirstNameShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID().toString() + "@streamus.com";
    objectNode.put("email", email);
    objectNode.put("username", "johnDoe");
    objectNode.put("lastName", "Doe");
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", "JohnDoe-password");
    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/users")
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithMissingLastNameShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID().toString() + "@streamus.com";
    objectNode.put("email", email);
    objectNode.put("username", "johnDoe");
    objectNode.put("firstName", "John");
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", "JohnDoe-password");
    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/users")
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithMissingDateOfBirthShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID().toString() + "@streamus.com";
    objectNode.put("email", email);
    objectNode.put("username", "johnDoe");
    objectNode.put("firstName", "John");
    objectNode.put("lastName", "Doe");
    objectNode.put("password", "JohnDoe-password");
    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/users")
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithMissingPasswordShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID().toString() + "@streamus.com";
    objectNode.put("email", email);
    objectNode.put("username", "johnDoe");
    objectNode.put("firstName", "John");
    objectNode.put("lastName", "Doe");
    objectNode.put("dateOfBirth", "2000-01-01");
    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/users")
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithInvalidEmailShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID().toString();
    objectNode.put("username", "johnDoe");
    objectNode.put("firstName", "John");
    objectNode.put("lastName", "Doe");
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", "JohnDoe-password");
    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/users")
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithInvalidFirstNameShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID().toString() + "@streamus.com";
    objectNode.put("username", "johnDoe");
    objectNode.put("firstName", "");
    objectNode.put("lastName", "Doe");
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", "JohnDoe-password");
    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/users")
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithInvalidLastNameShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID().toString() + "@streamus.com";
    objectNode.put("username", "johnDoe");
    objectNode.put("firstName", "John");
    objectNode.put("lastName", "");
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", "JohnDoe-password");
    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/users")
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithANonDateDateOfBirthShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID().toString() + "@streamus.com";
    objectNode.put("username", "johnDoe");
    objectNode.put("firstName", "John");
    objectNode.put("lastName", "Doe");
    objectNode.put("dateOfBirth", "shouldFail");
    objectNode.put("password", "JohnDoe-password");
    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/users")
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithDateOfBirthInTheFutureShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID().toString() + "@streamus.com";
    objectNode.put("username", "johnDoe");
    objectNode.put("firstName", "John");
    objectNode.put("lastName", "Doe");
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String dateString = dateFormat.format(new java.sql.Date(new java.util.Date().getTime() + 24 * 60 * 60 * 1000));
    objectNode.put("dateOfBirth", dateString);
    objectNode.put("password", "JohnDoe-password");
    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/users")
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

}
