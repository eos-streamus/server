package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Artist;
import com.eos.streamus.models.ArtistDAO;
import com.eos.streamus.models.Band;
import com.eos.streamus.models.Musician;
import com.eos.streamus.utils.DatabaseConnection;
import com.eos.streamus.writers.JsonArtistListWriter;
import com.eos.streamus.writers.JsonBandWriter;
import com.eos.streamus.writers.JsonMusicianWriter;
import com.eos.streamus.writers.JsonWriter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import static com.eos.streamus.controllers.CommonResponses.internalServerError;

@RestController
public class ArtistController {
  private final DatabaseConnection databaseConnection;

  public ArtistController(@Autowired DatabaseConnection databaseConnection) {
    this.databaseConnection = databaseConnection;
  }

  @GetMapping("/artists")
  public JsonNode allArtists() throws SQLException {
    List<Artist> allArtists;
    try (Connection connection = databaseConnection.getConnection()) {
      allArtists = ArtistDAO.all(connection);
      for (Artist artist : allArtists) {
        artist.fetchAlbums(connection);
      }
    }
    return new JsonArtistListWriter(allArtists).getJson();
  }

  @GetMapping("/artist/{id}")
  public ResponseEntity<JsonNode> getArtist(@PathVariable int id) throws SQLException {
    try (Connection connection = databaseConnection.getConnection()) {
      Artist artist = ArtistDAO.findById(id, connection);
      artist.fetchAlbums(connection);
      JsonWriter writer = artist instanceof Band ?
          new JsonBandWriter((Band) artist) :
          new JsonMusicianWriter((Musician) artist);
      return ResponseEntity.ok().body(writer.getJson());
    } catch (NoResultException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/band")
  public ResponseEntity<JsonNode> createBand(@Valid @RequestBody com.eos.streamus.payloadmodels.Band bandData) {
    try (Connection connection = databaseConnection.getConnection()) {
      Band band = new Band(bandData.getName());
      band.save(connection);
      return ResponseEntity.ok(new JsonBandWriter(band).getJson());
    } catch (SQLException sqlException) {
      Logger.getLogger(getClass().getName()).severe(sqlException.getMessage());
      return internalServerError();
    }
  }

}
