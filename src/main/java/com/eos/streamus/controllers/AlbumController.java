package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Album;
import com.eos.streamus.utils.DatabaseConnection;
import com.eos.streamus.writers.JsonAlbumWriter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.sql.Connection;
import java.sql.SQLException;

@Controller
public class AlbumController implements CommonResponses {

  private final DatabaseConnection databaseConnection;

  public AlbumController(@Autowired DatabaseConnection databaseConnection) {
    this.databaseConnection = databaseConnection;
  }

  @GetMapping("/album/{id}")
  public ResponseEntity<JsonNode> getAlbum(@PathVariable int id) {
    try (Connection connection = databaseConnection.getConnection()) {
      Album album = Album.findById(id, connection);
      return ResponseEntity.ok(new JsonAlbumWriter(album).getJson());
    } catch (NoResultException noResultException) {
      return ResponseEntity.notFound().build();
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    }
  }

}
