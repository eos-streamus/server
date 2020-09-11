package com.eos.streamus.controllers;

import com.eos.streamus.dto.LoginDTO;
import com.eos.streamus.dto.TokensDTO;
import com.eos.streamus.dto.UserDTO;
import com.eos.streamus.dto.validators.UserDTOValidator;
import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Admin;
import com.eos.streamus.models.User;
import com.eos.streamus.utils.IDatabaseConnector;
import com.eos.streamus.utils.JwtService;
import com.eos.streamus.writers.JsonErrorListWriter;
import com.eos.streamus.writers.JsonTokenWriter;
import com.eos.streamus.writers.JsonUserWriter;
import com.fasterxml.jackson.databind.JsonNode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
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

@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
@RestController
public final class UserController implements CommonResponses {
  /** User id claim name. */
  private static final String USER_ID = "userId";

  /** {@link com.eos.streamus.utils.IDatabaseConnector} to use. */
  @Autowired
  private IDatabaseConnector databaseConnector;

  /** {@link com.eos.streamus.dto.validators.UserDTOValidator} to use. */
  @Autowired
  private UserDTOValidator userDTOValidator;

  /** {@link org.springframework.security.crypto.password.PasswordEncoder} to use. */
  @Autowired
  private PasswordEncoder passwordEncoder;

  /** {@link com.eos.streamus.utils.JwtService} to use. */
  @Autowired
  private JwtService jwtService;

  @PostMapping("/users")
  public ResponseEntity<JsonNode> register(@RequestBody @Valid final UserDTO userDTO, final BindingResult result) {
    if (result.hasErrors()) {
      return ResponseEntity.badRequest().body(new JsonErrorListWriter(result).getJson());
    }
    userDTOValidator.validate(userDTO, result);
    if (result.hasErrors()) {
      return ResponseEntity.badRequest().body(new JsonErrorListWriter(result).getJson());
    }

    try (Connection connection = databaseConnector.getConnection()) {
      if (User.findByEmail(userDTO.getEmail(), connection) != null) {
        return badRequest("Invalid email");
      }

      User user = new User(
          userDTO.getFirstName(),
          userDTO.getLastName(),
          Date.valueOf(userDTO.getDateOfBirth()),
          userDTO.getEmail(),
          userDTO.getUsername()
      );
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

  @PostMapping("/login")
  public ResponseEntity<JsonNode> login(@RequestBody @Valid final LoginDTO loginDTO, final BindingResult result) {
    if (result.hasErrors()) {
      return ResponseEntity.badRequest().body(new JsonErrorListWriter(result).getJson());
    }
    try (Connection connection = databaseConnector.getConnection()) {
      User user = Admin.findByEmail(loginDTO.getEmail(), connection);
      if (user == null) {
        user = User.findByEmail(loginDTO.getEmail(), connection);
      }
      if (user == null) {
        return badRequest("Invalid email or password");
      }
      String password = user.getPassword(connection);
      if (passwordEncoder.matches(loginDTO.getPassword(), password)) {
        return ResponseEntity.ok(new JsonTokenWriter(jwtService.createToken(user)).getJson());
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

  @PostMapping("refresh")
  public ResponseEntity<JsonNode> refreshTokens(@RequestBody @Valid final TokensDTO tokensDTO,
                                                final BindingResult result) {
    if (result.hasErrors()) {
      return ResponseEntity.badRequest().body(new JsonErrorListWriter(result).getJson());
    }
    final Claims refreshClaims;
    try {
      refreshClaims = jwtService.decode(tokensDTO.getRefreshToken()).getBody();
    } catch (JwtException e) {
      System.out.println(e.getMessage());
      return badRequest("Invalid tokens");
    }

    try (Connection connection = databaseConnector.getConnection()) {
      User user;
      try {
        user = Admin.findById(refreshClaims.get(USER_ID, Integer.class), connection);
      } catch (NoResultException noResultException) {
       user = User.findById(refreshClaims.get(USER_ID, Integer.class), connection);
      }
      return ResponseEntity.ok(new JsonTokenWriter(jwtService.createToken(user)).getJson());
    } catch (SQLException e) {
      return internalServerError();
    } catch (NoResultException e) {
      return badRequest("Invalid user");
    }
  }

  @DeleteMapping("/user/{id}")
  public ResponseEntity<JsonNode> deleteUser(@PathVariable final int id) {
    try (Connection connection = databaseConnector.getConnection()) {
      User.findById(id, connection).delete(connection);
      return simpleOk("User deleted");
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    } catch (NoResultException noResultException) {
      return notFound();
    }
  }

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
