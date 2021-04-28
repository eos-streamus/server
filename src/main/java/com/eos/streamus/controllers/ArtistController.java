package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.*;
import com.eos.streamus.dto.BandMember;
import com.eos.streamus.dto.validators.BandMemberDTOValidator;
import com.eos.streamus.dto.validators.MusicianDTOValidator;
import com.eos.streamus.utils.IDatabaseConnector;
import com.eos.streamus.writers.JsonAlbumListWriter;
import com.eos.streamus.writers.JsonArtistListWriter;
import com.eos.streamus.writers.JsonBandWriter;
import com.eos.streamus.writers.JsonMusicianWriter;
import com.eos.streamus.writers.JsonWriter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
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
  /** {@link IDatabaseConnector} to use. */
  @Autowired
  private IDatabaseConnector databaseConnector;
  /** {@link MusicianDTOValidator} to use. */
  @Autowired
  private MusicianDTOValidator musicianDTOValidator;
  /** {@link BandMemberDTOValidator} to use. */
  @Autowired
  private BandMemberDTOValidator bandMemberDTOValidator;

  /** @return All artists in Json format. */
  @GetMapping("/artists")
  public ResponseEntity<JsonNode> allArtists() {
    List<Artist> allArtists;
    try (Connection connection = databaseConnector.getConnection()) {
      allArtists = ArtistDAO.all(connection);
      for (Artist artist : allArtists) {
        artist.fetchAlbums(connection);
      }
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    }
    return ResponseEntity.ok(new JsonArtistListWriter(allArtists).getJson());
  }

  /**
   * Get an artist by id.
   *
   * @param id Id of artist to get.
   * @return Artist in Json format.
   */
  @GetMapping("/artist/{id}")
  public ResponseEntity<JsonNode> getArtist(@PathVariable final int id) {
    try (Connection connection = databaseConnector.getConnection()) {
      Artist artist = ArtistDAO.findById(id, connection);
      artist.fetchAlbums(connection);
      JsonWriter writer = artist instanceof Band ?
          new JsonBandWriter((Band) artist) :
          new JsonMusicianWriter((Musician) artist);
      return ResponseEntity.ok().body(writer.getJson());
    } catch (NoResultException e) {
      return notFound();
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    }
  }

  /**
   * Get an Artist's discography.
   *
   * @param id Id of Artist whose discography should be retrieved.
   * @return Json formatted discography of artist.
   */
  @GetMapping("/artist/{id}/discography")
  public ResponseEntity<JsonNode> getArtistDiscography(@PathVariable final int id) {
    try (Connection connection = databaseConnector.getConnection()) {
      Artist artist = ArtistDAO.findById(id, connection);
      artist.fetchAlbums(connection);
      return ResponseEntity.ok(new JsonAlbumListWriter(artist.getAlbums()).getJson());
    } catch (NoResultException noResultException) {
      return notFound();
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    }
  }

  /**
   * Create a Band.
   *
   * @param bandData Band data.
   * @return Json formatted created band.
   */
  @PostMapping("/band")
  public ResponseEntity<JsonNode> createBand(@Valid @RequestBody final com.eos.streamus.dto.BandDTO bandData) {
    try (Connection connection = databaseConnector.getConnection()) {
      Band band = new Band(bandData.getName());
      band.save(connection);
      return ResponseEntity.ok(new JsonBandWriter(band).getJson());
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    }
  }

  /**
   * Create a new Musician.
   *
   * @param data   Sent MusicianDTO.
   * @param result BindingResult to store validation errors in.
   * @return Created Musician data in Json format.
   */
  @PostMapping("/musician")
  public ResponseEntity<JsonNode> createMusician(@Valid @RequestBody final com.eos.streamus.dto.MusicianDTO data,
                                                 final BindingResult result) {
    musicianDTOValidator.validate(data, result);
    if (result.hasErrors()) {
      return badRequest(result.toString());
    }
    try (Connection connection = databaseConnector.getConnection()) {
      connection.setAutoCommit(false);
      Musician musician;
      if (data.getPerson() != null) {
        Person person;
        com.eos.streamus.dto.PersonDTO dataPerson = data.getPerson();
        if (dataPerson.getId() != null) {
          person = Person.findById(dataPerson.getId(), connection);
        } else {
          person = new PersonBuilder(
              dataPerson.getFirstName(),
              dataPerson.getLastName(),
              dataPerson.getDateOfBirth() == null ? null : Date.valueOf(dataPerson.getDateOfBirth())
          ).build();
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

  /**
   * Create a new Band.Member.
   *
   * @param bandId Band to add Member to.
   * @param member {@link BandMember} data.
   * @param result BindingResult to store validation errors in.
   * @return Band Member data in Json format.
   */
  @PostMapping("/band/{bandId}/members")
  public ResponseEntity<JsonNode> addMemberToBand(@PathVariable final int bandId,
                                                  @Valid @RequestBody final BandMember member,
                                                  final BindingResult result) {
    bandMemberDTOValidator.validate(member, result);
    if (result.hasErrors()) {
      return badRequest(result.toString());
    }
    try (Connection connection = databaseConnector.getConnection()) {
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
        return badRequest(sqlException.getMessage());
      } else {
        logException(sqlException);
        return internalServerError();
      }
    }

  }

  /**
   * Delete an Artist by id.
   *
   * @param id Id of Artist to delete.
   * @return Confirmation message.
   */
  @DeleteMapping("/artist/{id}")
  public ResponseEntity<JsonNode> deleteArtist(@PathVariable final int id) {
    try (Connection connection = databaseConnector.getConnection()) {
      ArtistDAO.findById(id, connection).delete(connection);
      return simpleOk("Artist deleted");
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    } catch (NoResultException noResultException) {
      return notFound();
    }
  }

  private Musician getMusicianFromBandMemberData(final BandMember member, final Connection connection)
      throws SQLException, NoResultException {
    Musician musician;
    if (member.getMusicianId() != null) {
      musician = Musician.findById(member.getMusicianId(), connection);
    } else if (member.getMusician().getId() != null) {
      musician = Musician.findById(member.getMusician().getId(), connection);
    } else {
      if (member.getMusician().getPerson() == null) {
        musician = new Musician(member.getMusician().getName());
      } else if (member.getMusician().getPerson().getId() != null) {
        musician = member.getMusician().getName() == null ?
            new Musician(Person.findById(member.getMusician().getPerson().getId(), connection))
            :
            new Musician(
                member.getMusician().getName(),
                Person.findById(member.getMusician().getPerson().getId(), connection)
            );
      } else {
        com.eos.streamus.dto.PersonDTO personDTO = member.getMusician().getPerson();
        Person person = new Person(
            new PersonBuilder(
                personDTO.getFirstName(),
                personDTO.getLastName(),
                Date.valueOf(personDTO.getDateOfBirth())
            )
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
