package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.SongPlaylist;
import com.eos.streamus.utils.DatabaseConnection;
import com.eos.streamus.writers.JsonSongPlaylistWriter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;

@RestController
public class SongPlaylistController implements CommonResponses {

  private final DatabaseConnection databaseConnection;

  public SongPlaylistController(@Autowired DatabaseConnection databaseConnection) {
    this.databaseConnection = databaseConnection;
  }

  @GetMapping("/songplaylist/{id}")
  public ResponseEntity<JsonNode> getSongPlaylistById(@PathVariable final int id) {
    try (Connection connection = databaseConnection.getConnection()) {
      return ResponseEntity.ok(new JsonSongPlaylistWriter(SongPlaylist.findById(id, connection)).getJson());
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    } catch (NoResultException noResultException) {
      return notFound();
    }
  }
}
