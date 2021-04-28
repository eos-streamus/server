package com.eos.streamus.controllers;

import com.eos.streamus.filters.JwtFilter;
import com.eos.streamus.models.PersonBuilder;
import com.eos.streamus.models.User;
import com.eos.streamus.utils.JwtService;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;

abstract class JwtSetupControllerTests extends ControllerTests {

  @Autowired
  private JwtService jwtService;

  @Autowired
  private JwtFilter jwtFilter;

  @Override
  @BeforeAll
  void setupMockMvc() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilter(jwtFilter).build();
  }

  private String token;

  @BeforeAll
  void createAndLoginUser() throws SQLException, ParseException {
    try (Connection connection = databaseConnector.getConnection()) {
      User user = (User) new PersonBuilder("John", "Doe", date("2000-01-01"))
          .asUser("john.doe@gmail.com", "john_doe").build();
      user.save(connection);
      token = jwtService.createToken(user);
    }
  }

  @Override
  protected final ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
    return mockMvc.perform(builder.header("Authorization", "Bearer " + token));
  }

}
