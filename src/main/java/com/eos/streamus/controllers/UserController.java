package com.eos.streamus.controllers;

import com.eos.streamus.dto.LoginDTO;
import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.User;
import com.eos.streamus.payloadmodels.UserData;
import com.eos.streamus.payloadmodels.validators.UserValidator;
import com.eos.streamus.utils.IDatabaseConnector;
import com.eos.streamus.writers.JsonUserWriter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Date;

@RestController
public class UserController implements CommonResponses {

  @Autowired
  private IDatabaseConnector databaseConnector;

  @Autowired
  private UserValidator userValidator;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @PostMapping("/users")
  public ResponseEntity<JsonNode> register(@RequestBody @Valid final UserData userData, BindingResult result) {
    userValidator.validate(userData, result);
    if (result.hasErrors()) {
      return badRequest(result.toString());
    }

    try (Connection connection = databaseConnector.getConnection()) {
      if (User.findByEmail(userData.getEmail(), connection) != null) {
        return badRequest("Invalid email");
      }

      User user = new User(
          userData.getFirstName(),
          userData.getLastName(),
          new Date(userData.getDateOfBirth()),
          userData.getEmail(),
          userData.getUsername()
      );
      connection.setAutoCommit(false);
      String encodedPassword = passwordEncoder.encode(userData.getPassword());
      user.save(connection);
      user.updatePassword(encodedPassword, connection);
      connection.commit();
      return ResponseEntity.ok(new JsonUserWriter(user).getJson());
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    }
  }

  @PostMapping("/login")
  public ResponseEntity<JsonNode> login(@RequestBody @Valid final LoginDTO loginDTO) {
    try (Connection connection = databaseConnector.getConnection()) {
      User user = User.findByEmail(loginDTO.getEmail(), connection);
      String password = user.getPassword(connection);
      if (passwordEncoder.matches(loginDTO.getPassword(), password)) {
        return ResponseEntity.ok(new JsonUserWriter(user).getJson());
      } else {
        return badRequest("Invalid email or password");
      }
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    } catch (NoResultException noResultException) {
      return notFound();
    }
  }

}
