package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Activity;
import com.eos.streamus.models.Collection;
import com.eos.streamus.models.CollectionActivity;
import com.eos.streamus.models.CollectionDAO;
import com.eos.streamus.models.Resource;
import com.eos.streamus.models.ResourceActivity;
import com.eos.streamus.models.ResourceDAO;
import com.eos.streamus.models.User;
import com.eos.streamus.utils.IDatabaseConnector;
import com.eos.streamus.utils.JwtService;
import com.eos.streamus.writers.JsonCollectionActivityWriter;
import com.eos.streamus.writers.JsonResourceActivityWriter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

@RestController
public final class ActivityController implements CommonResponses {
  /** JWT Token offset in Bearer string. */
  private static final int TOKEN_OFFSET = 7;
  /** Authorization header name. */
  private static final String AUTHORIZATION = "Authorization";
  /** User id. */
  private static final String USER_ID = "userId";
  /** {@link com.eos.streamus.utils.IDatabaseConnector} to use. */
  @Autowired
  private IDatabaseConnector databaseConnector;

  /** {@link com.eos.streamus.utils.JwtService} to use. */
  @Autowired
  private JwtService jwtService;

  @GetMapping("/activity/{resourceId}")
  public ResponseEntity<JsonNode> getOrCreateActivity(@RequestHeader final HttpHeaders headers,
                                                      @PathVariable("resourceId") final int resourceId) {
    try (Connection connection = databaseConnector.getConnection()) {
      String token = Objects.requireNonNull(headers.getFirst(AUTHORIZATION)).substring(TOKEN_OFFSET);
      User user = User.findById(this.jwtService.decode(token).getBody().get(USER_ID, Integer.class), connection);
      Resource resource = ResourceDAO.findById(resourceId, connection);
      ResourceActivity resourceActivity = ResourceActivity.findByUserAndResourceIds(
          user.getId(),
          resource.getId(),
          connection
      );
      if (resourceActivity == null || resourceActivity.getPausedAt() >= resource.getDuration()) {
        resourceActivity = new ResourceActivity(resource, user);
        resourceActivity.save(connection);
        resourceActivity.start();
        resourceActivity.save(connection);
      }
      return ResponseEntity.ok(new JsonResourceActivityWriter(resourceActivity).getJson());
    } catch (SQLException | NoResultException sqlException) {
      logException(sqlException);
      return internalServerError();
    }
  }

  @GetMapping("/collectionActivity/{collectionId}")
  public ResponseEntity<JsonNode> getOrCreateCollectionActivity(@RequestHeader final HttpHeaders headers,
                                                                @PathVariable("collectionId") final int collectionId) {
    try (Connection connection = databaseConnector.getConnection()) {
      final String token = Objects.requireNonNull(headers.getFirst(AUTHORIZATION)).substring(TOKEN_OFFSET);
      final User user = User.findById(this.jwtService.decode(token).getBody().get(USER_ID, Integer.class), connection);
      final Collection collection = CollectionDAO.findById(collectionId, connection);
      CollectionActivity collectionActivity = CollectionActivity.findByUserAndCollectionIds(
          user.getId(),
          collection.getId(),
          connection
      );
      if (collectionActivity == null) {
        collectionActivity = new CollectionActivity(user, collection);
        collectionActivity.save(connection);
        collectionActivity.continueOrNext(connection);
      }
      return ResponseEntity.ok(new JsonCollectionActivityWriter(collectionActivity).getJson());
    } catch (NoResultException noResultException) {
      return notFound();
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    }
  }

  @PostMapping("/activity/{id}/pause/{time}")
  public ResponseEntity<JsonNode> pauseActivity(@RequestHeader final HttpHeaders headers,
                                                @PathVariable final int id,
                                                @PathVariable final int time) {
    try (Connection connection = databaseConnector.getConnection()) {
      final String token = Objects.requireNonNull(headers.getFirst(AUTHORIZATION)).substring(TOKEN_OFFSET);
      final User user = User.findById(this.jwtService.decode(token).getBody().get(USER_ID, Integer.class), connection);
      final ResourceActivity resourceActivity = ResourceActivity.findById(id, connection);
      if (resourceActivity == null) {
        return notFound();
      }
      boolean containedAndIsOwner = false;
      for (Activity.UserActivity userActivity : resourceActivity.getUsers()) {
        if (user.equals(userActivity.getUser()) && userActivity.isManager() != null && userActivity.isManager()) {
          containedAndIsOwner = true;
          break;
        }
      }
      if (!containedAndIsOwner) {
        return badRequest("Unauthorized");
      }
      resourceActivity.setPausedAt(time);
      resourceActivity.save(connection);
      return ResponseEntity.ok(new JsonResourceActivityWriter(resourceActivity).getJson());
    } catch (SQLException | NoResultException sqlException) {
      logException(sqlException);
      return internalServerError();
    }
  }

}
