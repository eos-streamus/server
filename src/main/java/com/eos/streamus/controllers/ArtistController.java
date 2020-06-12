package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Artist;
import com.eos.streamus.models.ArtistDAO;
import com.eos.streamus.models.Band;
import com.eos.streamus.models.Musician;
import com.eos.streamus.models.Person;
import com.eos.streamus.payloadmodels.BandMember;
import com.eos.streamus.payloadmodels.BandMemberValidator;
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
  private final BandMemberValidator bandMemberValidator;

  public ArtistController(@Autowired DatabaseConnection databaseConnection,
                          @Autowired MusicianValidator musicianValidator,
                          @Autowired BandMemberValidator bandMemberValidator) {
    this.databaseConnection = databaseConnection;
    this.musicianValidator = musicianValidator;
    this.bandMemberValidator = bandMemberValidator;
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
  public ResponseEntity<JsonNode> getArtist(@PathVariable int id) {
    try (Connection connection = databaseConnection.getConnection()) {
      Artist artist = ArtistDAO.findById(id, connection);
      artist.fetchAlbums(connection);
      JsonWriter writer = artist instanceof Band ?
          new JsonBandWriter((Band) artist) :
          new JsonMusicianWriter((Musician) artist);
      return ResponseEntity.ok().body(writer.getJson());
    } catch (NoResultException e) {
      return ResponseEntity.notFound().build();
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
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

  @PostMapping("/band/{bandId}/members")
  public ResponseEntity<JsonNode> addMemberToBand(@PathVariable int bandId,
                                                  @Valid @RequestBody BandMember member,
                                                  BindingResult result) {
    bandMemberValidator.validate(member, result);
    if (result.hasErrors()) {
      return badRequest(result.toString());
    }
    try (Connection connection = databaseConnection.getConnection()) {
      connection.setAutoCommit(false);
      Band band = Band.findById(bandId, connection);
      Musician musician = getMusicianFromBandMemberData(member, connection);
      band.addMember(musician, member.getFrom(), member.getTo());
      band.save(connection);
      connection.commit();
      return ResponseEntity.ok(new JsonBandWriter(band).getJson());
    } catch (NoResultException noResultException) {
      return badRequest("Invalid data");
    } catch (SQLException sqlException) {
      if (sqlException.getSQLState().equals("40002")) {
        return badRequest("Overlapping band memberships for musician");
      } else {
        logException(sqlException);
        return internalServerError();
      }
    }

  }

  private Musician getMusicianFromBandMemberData(BandMember member, Connection connection) throws SQLException, NoResultException {
    Musician musician;
    if (member.getMusicianId() != null) {
      musician = Musician.findById(member.getMusicianId(), connection);
    } else if (member.getMusician().getId() != null) {
      musician = Musician.findById(member.getMusician().getId(), connection);
    } else {
      if (member.getMusician().getPerson().getId() != null) {
        musician = member.getMusician().getName() == null ?
            new Musician(Person.findById(member.getMusician().getPerson().getId(), connection))
            :
            new Musician(
                member.getMusician().getName(),
                Person.findById(member.getMusician().getPerson().getId(), connection)
            );
      } else {
        com.eos.streamus.payloadmodels.Person payloadPerson = member.getMusician().getPerson();
        Person person = new Person(
            payloadPerson.getFirstName(),
            payloadPerson.getLastName(),
            new Date(payloadPerson.getDateOfBirth())
        );
        musician = member.getMusician().getName() == null ?
            new Musician(person)
            :
            new Musician(member.getMusician().getName(), person);
      }
      musician.save(connection);
    }
    return musician;
  }

}
