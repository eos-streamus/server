package com.eos.streamus.controllers;

import com.eos.streamus.dto.LoginDTO;
import com.eos.streamus.dto.UserDTO;
import com.eos.streamus.dto.validators.UserDTOValidator;
import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.PersonBuilder;
import com.eos.streamus.models.User;
import com.eos.streamus.utils.IDatabaseConnector;
import com.eos.streamus.utils.JwtService;
import com.eos.streamus.writers.JsonUserWriter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;

@RestController
public class UserController implements CommonResponses {

  /** {@link IDatabaseConnector} to use. */
  @Autowired
  private IDatabaseConnector databaseConnector;

  /** {@link UserDTOValidator} to use. */
  @Autowired
  private UserDTOValidator userDTOValidator;

  /** {@link PasswordEncoder} to use. */
  @Autowired
  private PasswordEncoder passwordEncoder;

  /** {@link JwtService} to use. */
  @Autowired
  private JwtService jwtService;

  /**
   * Register a new User.
   *
   * @param userDTO User data.
   * @param result  Validation BindingResult.
   * @return Created User data in JSON.
   */
  @PostMapping("/users")
  public ResponseEntity<JsonNode> register(@RequestBody @Valid final UserDTO userDTO, final BindingResult result) {
    if (result.hasErrors()) {
      return badRequest(result.toString());
    }
    userDTOValidator.validate(userDTO, result);
    if (result.hasErrors()) {
      return badRequest(result.toString());
    }

    try (Connection connection = databaseConnector.getConnection()) {
      if (User.findByEmail(userDTO.getEmail(), connection) != null) {
        return badRequest("Invalid email");
      }

      User user = (User) new PersonBuilder(
          userDTO.getFirstName(),
          userDTO.getLastName(),
          Date.valueOf(userDTO.getDateOfBirth())
      ).asUser(
          userDTO.getEmail(),
          userDTO.getUsername()
      ).build();
      connection.setAutoCommit(false);
      String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
      user.save(connection);
      user.upsertPassword(encodedPassword, connection);
      connection.commit();
      return ResponseEntity.ok(new JsonUserWriter(user).getJson());
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    }
  }

  /**
   * Login a User.
   *
   * @param loginDTO Login credentials.
   * @param result   BindingResult to validate credentials with.
   * @return Token to use for future requests if successful login.
   */
  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestBody @Valid final LoginDTO loginDTO, final BindingResult result) {
    if (result.hasErrors()) {
      return ResponseEntity.badRequest().body(result.toString());
    }
    try (Connection connection = databaseConnector.getConnection()) {
      User user = User.findByEmail(loginDTO.getEmail(), connection);
      if (user == null) {
        return ResponseEntity.badRequest().body("Invalid email or password");
      }
      String password = user.getPassword(connection);
      if (passwordEncoder.matches(loginDTO.getPassword(), password)) {
        return ResponseEntity.ok(jwtService.createToken(user));
      } else {
        return ResponseEntity.badRequest().body("Invalid email or password");
      }
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerErrorString();
    } catch (NoResultException noResultException) {
      return notFound();
    }
  }

  /**
   * Delete an User by id.
   *
   * @param id Id of User to delete.
   * @return Confirmation message.
   */
  @DeleteMapping("/user/{id}")
  public ResponseEntity<String> deleteUser(@PathVariable final int id) {
    try (Connection connection = databaseConnector.getConnection()) {
      User.findById(id, connection).delete(connection);
      return ResponseEntity.ok("User deleted");
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerErrorString();
    } catch (NoResultException noResultException) {
      return notFound();
    }
  }

  /**
   * Update an User by id.
   *
   * @param id      Id of User to update.
   * @param userDTO Updated User data.
   * @param result  BindingResult to validate data with.
   * @return Updated User data in JSON format.
   */
  @PutMapping("/user/{id}")
  public ResponseEntity<JsonNode> updateUser(@PathVariable final int id,
                                             @RequestBody @Valid final UserDTO userDTO,
                                             final BindingResult result) {
    if (result.hasErrors()) {
      return badRequest(result.toString());
    }
    userDTOValidator.validate(userDTO, result);
    if (result.hasErrors()) {
      return badRequest(result.toString());
    }

    try (Connection connection = databaseConnector.getConnection()) {
      User user = User.findById(id, connection);
      String currentPassword = user.getPassword(connection);
      if (!passwordEncoder.matches(userDTO.getPassword(), currentPassword)) {
        return badRequest("Invalid password");
      }
      User userByEmail = User.findByEmail(userDTO.getEmail(), connection);
      if (userByEmail != null && !userByEmail.getId().equals(user.getId())) {
        return badRequest("Invalid email");
      }
      user.setEmail(userDTO.getEmail());
      user.setFirstName(userDTO.getFirstName());
      user.setLastName(userDTO.getLastName());
      user.setDateOfBirth(Date.valueOf(userDTO.getDateOfBirth()));
      user.setUsername(userDTO.getUsername());
      user.save(connection);
      if (userDTO.getUpdatedPassword() != null) {
        user.upsertPassword(passwordEncoder.encode(userDTO.getUpdatedPassword()), connection);
      }
      return ResponseEntity.ok(new JsonUserWriter(user).getJson());
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    } catch (NoResultException noResultException) {
      return notFound();
    }
  }

}
