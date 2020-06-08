package com.eos.streamus.controllers;

import com.eos.streamus.models.Artist;
import com.eos.streamus.models.ArtistDAO;
import com.eos.streamus.utils.TestDatabaseConnection;
import com.eos.streamus.writers.JsonArtistListWriter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@RestController
public class ArtistController {
  private final TestDatabaseConnection databaseConnection;

  public ArtistController(@Autowired TestDatabaseConnection databaseConnection) {
    this.databaseConnection = databaseConnection;
  }

  @GetMapping("/artists")
  public JsonNode allArtists() throws SQLException {
    List<Artist> allArtists;
    try (Connection connection = databaseConnection.getConnection()) {
      allArtists = ArtistDAO.all(connection);
    }
    return new JsonArtistListWriter(allArtists).getJson();
  }
}
