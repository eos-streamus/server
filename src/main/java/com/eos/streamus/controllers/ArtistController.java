package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Artist;
import com.eos.streamus.models.ArtistDAO;
import com.eos.streamus.models.Band;
import com.eos.streamus.models.Musician;
import com.eos.streamus.models.Person;
import com.eos.streamus.payloadmodels.MusicianValidator;
import com.eos.streamus.utils.DatabaseConnection;
import com.eos.streamus.writers.JsonArtistListWriter;
import com.eos.streamus.writers.JsonBandWriter;
import com.eos.streamus.writers.JsonMusicianWriter;
import com.eos.streamus.writers.JsonWriter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

@RestController
public class ArtistController implements CommonResponses {
  private final DatabaseConnection databaseConnection;
  private final MusicianValidator musicianValidator;

  public ArtistController(@Autowired DatabaseConnection databaseConnection,
                          @Autowired MusicianValidator musicianValidator) {
    this.databaseConnection = databaseConnection;
    this.musicianValidator = musicianValidator;
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
      logException(sqlException);
      return internalServerError();
    }
  }

  @PostMapping("/musician")
  public ResponseEntity<JsonNode> createMusician(@Valid @RequestBody com.eos.streamus.payloadmodels.Musician data,
                                                 BindingResult result) {
    musicianValidator.validate(data, result);
    if (result.hasErrors()) {
      return badRequest(result.toString());
    }
    try (Connection connection = databaseConnection.getConnection()) {
      connection.setAutoCommit(false);
      Musician musician;
      if (data.getPerson() != null) {
        Person person;
        com.eos.streamus.payloadmodels.Person dataPerson = data.getPerson();
        if (dataPerson.getId() != null) {
          person = Person.findById(dataPerson.getId(), connection);
        } else {
          person = new Person(
              dataPerson.getFirstName(),
              dataPerson.getLastName(),
              dataPerson.getDateOfBirth() == null ? null : new Date(dataPerson.getDateOfBirth())
          );
          person.save(connection);
        }
        musician = data.getName() != null ? new Musician(data.getName(), person) : new Musician(person);
      } else {
        musician = new Musician(data.getName());
      }
      musician.save(connection);
      connection.commit();
      return ResponseEntity.ok(new JsonMusicianWriter(musician).getJson());
    } catch (NoResultException noResultException) {
      return badRequest("Invalid person id");
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    }
  }

}
