package com.eos.streamus.controllers;

import com.eos.streamus.models.PersonBuilder;
import com.eos.streamus.models.User;
import com.eos.streamus.utils.JwtService;
import com.eos.streamus.writers.JsonUserWriter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.sql.Connection;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTests extends ControllerTests {
  @Value("${minPasswordLength}")
  private int minPasswordLength;
  @Value("${minUsernameLength}")
  private int minUsernameLength;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtService jwtService;

  private static final String ACCEPTABLE_CHARACTERS = "ABCEDFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";

  private String randomStringOfLength(final int length) {
    StringBuilder stringBuilder = new StringBuilder();
    Random random = new Random();
    for (int i = 0; i < length; i++) {
      stringBuilder.append(ACCEPTABLE_CHARACTERS.charAt(Math.abs(random.nextInt()) % ACCEPTABLE_CHARACTERS.length()));
    }
    return stringBuilder.toString();
  }

  @Test
  void signingUpWithValidUserDataShouldWork() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID() + "@streamus.com";
    objectNode.put("email", email);
    objectNode.put("username", randomStringOfLength(minUsernameLength));
    objectNode.put("firstName", "John");
    objectNode.put("lastName", "Doe");
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", randomStringOfLength(minPasswordLength));
    MockHttpServletRequestBuilder builder = post("/users")
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
    final String email = UUID.randomUUID() + "@streamus.com";

    try (Connection connection = databaseConnector.getConnection()) {
      new PersonBuilder("John", "Doe", Date.valueOf("2000-01-01"))
          .asUser(email, "john_doe").build()
          .save(connection);
    }

    objectNode.put("email", email);
    objectNode.put("username", randomStringOfLength(minUsernameLength));
    objectNode.put("firstName", "John");
    objectNode.put("lastName", "Doe");
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", randomStringOfLength(minPasswordLength));
    MockHttpServletRequestBuilder builder = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithMissingEmailShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("username", randomStringOfLength(minUsernameLength));
    objectNode.put("firstName", "John");
    objectNode.put("lastName", "Doe");
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", randomStringOfLength(minPasswordLength));
    MockHttpServletRequestBuilder builder = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithMissingUsernameShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID() + "@streamus.com";
    objectNode.put("email", email);
    objectNode.put("firstName", "John");
    objectNode.put("lastName", "Doe");
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", randomStringOfLength(minPasswordLength));
    MockHttpServletRequestBuilder builder = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithMissingFirstNameShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID() + "@streamus.com";
    objectNode.put("email", email);
    objectNode.put("username", randomStringOfLength(minUsernameLength));
    objectNode.put("lastName", "Doe");
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", randomStringOfLength(minPasswordLength));
    MockHttpServletRequestBuilder builder = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithMissingLastNameShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID() + "@streamus.com";
    objectNode.put("email", email);
    objectNode.put("username", randomStringOfLength(minUsernameLength));
    objectNode.put("firstName", "John");
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", randomStringOfLength(minPasswordLength));
    MockHttpServletRequestBuilder builder = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithMissingDateOfBirthShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID() + "@streamus.com";
    objectNode.put("email", email);
    objectNode.put("username", randomStringOfLength(minUsernameLength));
    objectNode.put("firstName", "John");
    objectNode.put("lastName", "Doe");
    objectNode.put("password", randomStringOfLength(minPasswordLength));
    MockHttpServletRequestBuilder builder = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithMissingPasswordShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID() + "@streamus.com";
    objectNode.put("email", email);
    objectNode.put("username", randomStringOfLength(minUsernameLength));
    objectNode.put("firstName", "John");
    objectNode.put("lastName", "Doe");
    objectNode.put("dateOfBirth", "2000-01-01");
    MockHttpServletRequestBuilder builder = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithPasswordTooShortShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID() + "@streamus.com";
    objectNode.put("email", email);
    objectNode.put("username", randomStringOfLength(minUsernameLength));
    objectNode.put("firstName", "John");
    objectNode.put("lastName", "Doe");
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", randomStringOfLength(minPasswordLength - 1));
    MockHttpServletRequestBuilder builder = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithUsernameTooShortShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID() + "@streamus.com";
    objectNode.put("email", email);
    objectNode.put("username", randomStringOfLength(minUsernameLength));
    objectNode.put("firstName", "John");
    objectNode.put("lastName", "Doe");
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", randomStringOfLength(minUsernameLength - 1));
    MockHttpServletRequestBuilder builder = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithInvalidEmailShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID().toString();
    objectNode.put("email", email);
    objectNode.put("username", randomStringOfLength(minUsernameLength));
    objectNode.put("firstName", "John");
    objectNode.put("lastName", "Doe");
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", randomStringOfLength(minPasswordLength));
    MockHttpServletRequestBuilder builder = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithInvalidFirstNameShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID() + "@streamus.com";
    objectNode.put("email", email);
    objectNode.put("username", randomStringOfLength(minUsernameLength));
    objectNode.put("firstName", "");
    objectNode.put("lastName", "Doe");
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", randomStringOfLength(minPasswordLength));
    MockHttpServletRequestBuilder builder = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithInvalidLastNameShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID() + "@streamus.com";
    objectNode.put("email", email);
    objectNode.put("username", randomStringOfLength(minUsernameLength));
    objectNode.put("firstName", "John");
    objectNode.put("lastName", "");
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", randomStringOfLength(minPasswordLength));
    MockHttpServletRequestBuilder builder = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithANonDateDateOfBirthShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID() + "@streamus.com";
    objectNode.put("username", randomStringOfLength(minUsernameLength));
    objectNode.put("firstName", "John");
    objectNode.put("email", email);
    objectNode.put("lastName", "Doe");
    objectNode.put("dateOfBirth", "shouldFail");
    objectNode.put("password", randomStringOfLength(minPasswordLength));
    MockHttpServletRequestBuilder builder = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingUpWithDateOfBirthInTheFutureShouldReturnBadRequest() throws Exception {
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String email = UUID.randomUUID() + "@streamus.com";
    objectNode.put("email", email);
    objectNode.put("username", randomStringOfLength(minUsernameLength));
    objectNode.put("firstName", "John");
    objectNode.put("lastName", "Doe");
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String dateString = dateFormat.format(new java.sql.Date(new java.util.Date().getTime() + 24 * 60 * 60 * 1000));
    objectNode.put("dateOfBirth", dateString);
    objectNode.put("password", randomStringOfLength(minPasswordLength));
    MockHttpServletRequestBuilder builder = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn().getResponse();
  }

  @Test
  void signingInWithValidUserDataShouldReturnAValidJWT() throws Exception {
    User user;
    String password;
    try (Connection connection = databaseConnector.getConnection()) {
      user = (User) new PersonBuilder("John", "Doe", Date.valueOf("2000-01-01"))
          .asUser(randomStringOfLength(5) + "@streamus.com", randomStringOfLength(minUsernameLength)).build();
      user.save(connection);
      password = randomStringOfLength(minPasswordLength);
      user.upsertPassword(passwordEncoder.encode(password), connection);
    }

    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("email", user.getEmail());
    objectNode.put("password", password);
    MockHttpServletRequestBuilder builder = post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectNode.toPrettyString());
    MockHttpServletResponse response = perform(builder).andExpect(status().is(200)).andReturn().getResponse();
    final String sessionToken = new ObjectMapper(new JsonFactory()).readTree(response.getContentAsString())
                                                             .get("sessionToken").asText();
    final Jws<Claims> claimsJws = jwtService.decode(sessionToken);
    assertEquals(claimsJws.getBody().get("email", String.class), user.getEmail());
  }

  @Test
  void signingInWithInvalidUserDataShouldReturnBadRequest() throws Exception {
    User user;
    String password;
    try (Connection connection = databaseConnector.getConnection()) {
      user = (User) new PersonBuilder("John", "Doe", Date.valueOf("2000-01-01"))
          .asUser(randomStringOfLength(5) + "@streamus.com", randomStringOfLength(minUsernameLength)).build();
      user.save(connection);
      password = randomStringOfLength(minPasswordLength);
      user.upsertPassword(passwordEncoder.encode(password), connection);
    }

    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("email", user.getEmail());
    objectNode.put("password", randomStringOfLength(minPasswordLength));
    MockHttpServletRequestBuilder builder = post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn();
  }

  @Test
  void signingInWithInvalidEmailShouldReturnBadRequest() throws Exception {
    User user;
    String password;
    try (Connection connection = databaseConnector.getConnection()) {
      user = (User) new PersonBuilder("John", "Doe", Date.valueOf("2000-01-01"))
          .asUser(randomStringOfLength(5) + "@streamus.com", randomStringOfLength(minUsernameLength)).build();
      user.save(connection);
      password = randomStringOfLength(minPasswordLength);
      user.upsertPassword(passwordEncoder.encode(password), connection);
    }

    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("email", randomStringOfLength(5) + "@streamus.com");
    objectNode.put("password", password);
    MockHttpServletRequestBuilder builder = post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn();
  }

  @Test
  void signingInWithMissingEmailShouldReturnBadRequest() throws Exception {
    User user;
    String password;
    try (Connection connection = databaseConnector.getConnection()) {
      user = (User) new PersonBuilder("John", "Doe", Date.valueOf("2000-01-01"))
          .asUser(randomStringOfLength(5) + "@streamus.com", randomStringOfLength(minUsernameLength)).build();
      user.save(connection);
      password = randomStringOfLength(minPasswordLength);
      user.upsertPassword(passwordEncoder.encode(password), connection);
    }

    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("password", password);
    MockHttpServletRequestBuilder builder = post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn();
  }

  @Test
  void signingInWithMissingPasswordShouldReturnBadRequest() throws Exception {
    User user;
    String password;
    try (Connection connection = databaseConnector.getConnection()) {
      user = (User) new PersonBuilder("John", "Doe", Date.valueOf("2000-01-01"))
          .asUser(randomStringOfLength(5) + "@streamus.com", randomStringOfLength(minUsernameLength)).build();
      user.save(connection);
      password = randomStringOfLength(minPasswordLength);
      user.upsertPassword(passwordEncoder.encode(password), connection);
    }

    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("email", user.getEmail());
    MockHttpServletRequestBuilder builder = post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn();
  }

  @Test
  void updatingPasswordShouldWork() throws Exception {
    User user;
    String password;
    try (Connection connection = databaseConnector.getConnection()) {
      user = (User) new PersonBuilder("John", "Doe", date("2000-01-01"))
          .asUser(randomStringOfLength(10) + "@streamus.com", randomStringOfLength(minUsernameLength)).build();
      user.save(connection);
      password = randomStringOfLength(minPasswordLength);
      user.upsertPassword(passwordEncoder.encode(password), connection);
    }
    MockHttpServletRequestBuilder builder = post(String.format("/user/%d/password", user.getId()));
    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("password", password);
    String newPassword = randomStringOfLength(minPasswordLength);
    objectNode.put("updatedPassword", newPassword);

    builder.contentType(MediaType.APPLICATION_JSON);
    builder.content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(200)).andReturn();

    objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("email", user.getEmail());
    objectNode.put("password", newPassword);

    builder = post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(200)).andReturn().getResponse();
  }

  @Test
  void updatingAUserProfileWithNewDataShouldWork() throws Exception {
    User user;
    String password;
    try (Connection connection = databaseConnector.getConnection()) {
      user = (User) new PersonBuilder("John", "Doe", date("2000-01-01"))
          .asUser(randomStringOfLength(10) + "@streamus.com", randomStringOfLength(minUsernameLength)).build();
      user.save(connection);
      password = randomStringOfLength(minPasswordLength);
      user.upsertPassword(passwordEncoder.encode(password), connection);
    }

    MockHttpServletRequestBuilder builder = put("/user/" + user.getId());

    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    final String newEmail = randomStringOfLength(10) + "@streamus.com";
    objectNode.put("email", newEmail);
    final String newUsername = randomStringOfLength(minUsernameLength);
    objectNode.put("username", newUsername);
    final String newFirstName = "Jonathan";
    objectNode.put("firstName", newFirstName);
    final String newLastName = "Deer";
    objectNode.put("lastName", newLastName);
    final String newDateOfBirth = "2001-01-01";
    objectNode.put("dateOfBirth", newDateOfBirth);
    objectNode.put("password", password);

    builder.contentType(MediaType.APPLICATION_JSON);
    builder.content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(200)).andReturn();

    try (Connection connection = databaseConnector.getConnection()) {
      User fetchedUser = User.findById(user.getId(), connection);
      assertEquals(newEmail, fetchedUser.getEmail());
      assertEquals(newFirstName, fetchedUser.getFirstName());
      assertEquals(newLastName, fetchedUser.getLastName());
      assertEquals(date(newDateOfBirth), fetchedUser.getDateOfBirth());
    }
  }

  @Test
  void updatingAUserProfileWithUsedEmailShouldReturnBadRequest() throws Exception {
    User user;
    String password;
    String email = randomStringOfLength(10) + "@streamus.com";
    try (Connection connection = databaseConnector.getConnection()) {
      user = (User) new PersonBuilder("John", "Doe", date("2000-01-01"))
          .asUser(randomStringOfLength(10) + "@streamus.com", randomStringOfLength(minUsernameLength)).build();
      user.save(connection);
      password = randomStringOfLength(minPasswordLength);
      user.upsertPassword(passwordEncoder.encode(password), connection);

      // Used email user
      new PersonBuilder(
          "John",
          "Doe",
          date("2000-01-01")
      ).asUser(
          email,
          randomStringOfLength(minUsernameLength)
      ).build().save(connection);
    }

    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("email", email);
    objectNode.put("username", user.getUsername());
    objectNode.put("firstName", user.getFirstName());
    objectNode.put("lastName", user.getLastName());
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", password);

    MockHttpServletRequestBuilder builder = put("/user/" + user.getId());
    builder.contentType(MediaType.APPLICATION_JSON);
    builder.content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn();
  }

  @Test
  void updatingAUserProfileWithATooShortPasswordShouldReturnBadRequest() throws Exception {
    User user;
    String password;
    try (Connection connection = databaseConnector.getConnection()) {
      user = (User) new PersonBuilder("John", "Doe", date("2000-01-01"))
          .asUser(randomStringOfLength(10) + "@streamus.com", randomStringOfLength(minUsernameLength)).build();
      user.save(connection);
      password = randomStringOfLength(minPasswordLength);
      user.upsertPassword(passwordEncoder.encode(password), connection);
    }

    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("password", password);
    objectNode.put("updatedPassword", randomStringOfLength(minPasswordLength - 1));

    MockHttpServletRequestBuilder builder = post(String.format("/user/%d/password", user.getId()));
    builder.contentType(MediaType.APPLICATION_JSON);
    builder.content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn();
  }

  @Test
  void updatingAUserProfileWithMissingEmailShouldReturnBadRequest() throws Exception {
    User user;
    String password;
    try (Connection connection = databaseConnector.getConnection()) {
      user = (User) new PersonBuilder("John", "Doe", date("2000-01-01"))
          .asUser(randomStringOfLength(10) + "@streamus.com", randomStringOfLength(minUsernameLength)).build();
      user.save(connection);
      password = randomStringOfLength(minPasswordLength);
      user.upsertPassword(passwordEncoder.encode(password), connection);
    }

    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("username", user.getUsername());
    objectNode.put("firstName", user.getFirstName());
    objectNode.put("lastName", user.getLastName());
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", password);

    MockHttpServletRequestBuilder builder = put("/user/" + user.getId());
    builder.contentType(MediaType.APPLICATION_JSON);
    builder.content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn();
  }

  @Test
  void updatingAUserProfileWithMissingUsernameShouldReturnBadRequest() throws Exception {
    User user;
    String password;
    try (Connection connection = databaseConnector.getConnection()) {
      user = (User) new PersonBuilder("John", "Doe", date("2000-01-01"))
          .asUser(randomStringOfLength(10) + "@streamus.com", randomStringOfLength(minUsernameLength)).build();
      user.save(connection);
      password = randomStringOfLength(minPasswordLength);
      user.upsertPassword(passwordEncoder.encode(password), connection);
    }

    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("email", user.getEmail());
    objectNode.put("firstName", user.getFirstName());
    objectNode.put("lastName", user.getLastName());
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", password);

    MockHttpServletRequestBuilder builder = put("/user/" + user.getId());
    builder.contentType(MediaType.APPLICATION_JSON);
    builder.content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn();
  }

  @Test
  void updatingAUserProfileWithMissingFirstNameShouldReturnBadRequest() throws Exception {
    User user;
    String password;
    try (Connection connection = databaseConnector.getConnection()) {
      user = (User) new PersonBuilder("John", "Doe", date("2000-01-01"))
          .asUser(randomStringOfLength(10) + "@streamus.com", randomStringOfLength(minUsernameLength)).build();
      user.save(connection);
      password = randomStringOfLength(minPasswordLength);
      user.upsertPassword(passwordEncoder.encode(password), connection);
    }

    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("email", user.getEmail());
    objectNode.put("username", user.getUsername());
    objectNode.put("lastName", user.getLastName());
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", password);

    MockHttpServletRequestBuilder builder = put("/user/" + user.getId());
    builder.contentType(MediaType.APPLICATION_JSON);
    builder.content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn();
  }

  @Test
  void updatingAUserProfileWithMissingLastNameShouldReturnBadRequest() throws Exception {
    User user;
    String password;
    try (Connection connection = databaseConnector.getConnection()) {
      user = (User) new PersonBuilder("John", "Doe", date("2000-01-01"))
          .asUser(randomStringOfLength(10) + "@streamus.com", randomStringOfLength(minUsernameLength)).build();
      user.save(connection);
      password = randomStringOfLength(minPasswordLength);
      user.upsertPassword(passwordEncoder.encode(password), connection);
    }

    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("email", user.getEmail());
    objectNode.put("username", user.getUsername());
    objectNode.put("firstName", user.getFirstName());
    objectNode.put("dateOfBirth", "2000-01-01");
    objectNode.put("password", password);

    MockHttpServletRequestBuilder builder = put("/user/" + user.getId());
    builder.contentType(MediaType.APPLICATION_JSON);
    builder.content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn();
  }

  @Test
  void updatingAUserProfileWithMissingDateOfBirthShouldReturnBadRequest() throws Exception {
    User user;
    String password;
    try (Connection connection = databaseConnector.getConnection()) {
      user = (User) new PersonBuilder("John", "Doe", date("2000-01-01"))
          .asUser(randomStringOfLength(10) + "@streamus.com", randomStringOfLength(minUsernameLength)).build();
      user.save(connection);
      password = randomStringOfLength(minPasswordLength);
      user.upsertPassword(passwordEncoder.encode(password), connection);
    }

    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("email", user.getEmail());
    objectNode.put("username", user.getUsername());
    objectNode.put("firstName", user.getFirstName());
    objectNode.put("lastName", user.getLastName());
    objectNode.put("password", password);

    MockHttpServletRequestBuilder builder = put("/user/" + user.getId());
    builder.contentType(MediaType.APPLICATION_JSON);
    builder.content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn();
  }

  @Test
  void updatingAUserProfileWithMissingPasswordShouldReturnBadRequest() throws Exception {
    User user;
    String password;
    try (Connection connection = databaseConnector.getConnection()) {
      user = (User) new PersonBuilder("John", "Doe", date("2000-01-01"))
          .asUser(randomStringOfLength(10) + "@streamus.com", randomStringOfLength(minUsernameLength)).build();
      user.save(connection);
      password = randomStringOfLength(minPasswordLength);
      user.upsertPassword(passwordEncoder.encode(password), connection);
    }

    ObjectNode objectNode = new ObjectNode(new TestJsonFactory());
    objectNode.put("email", user.getEmail());
    objectNode.put("username", user.getUsername());
    objectNode.put("firstName", user.getFirstName());
    objectNode.put("lastName", user.getLastName());
    objectNode.put("dateOfBirth", "2000-01-01");

    MockHttpServletRequestBuilder builder = put("/user/" + user.getId());
    builder.contentType(MediaType.APPLICATION_JSON);
    builder.content(objectNode.toPrettyString());
    perform(builder).andExpect(status().is(400)).andReturn();
  }

}
