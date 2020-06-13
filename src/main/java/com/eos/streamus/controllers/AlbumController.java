package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Album;
import com.eos.streamus.models.ArtistDAO;
import com.eos.streamus.models.Song;
import com.eos.streamus.payloadmodels.AlbumValidator;
import com.eos.streamus.payloadmodels.Track;
import com.eos.streamus.utils.DatabaseConnection;
import com.eos.streamus.writers.JsonAlbumWriter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.sql.Connection;
import java.sql.SQLException;

@Controller
public class AlbumController implements CommonResponses {

  private final DatabaseConnection databaseConnection;
  private final AlbumValidator albumValidator;

  public AlbumController(@Autowired DatabaseConnection databaseConnection, @Autowired AlbumValidator albumValidator) {
    this.databaseConnection = databaseConnection;
    this.albumValidator = albumValidator;
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

  @PostMapping("/albums")
  public ResponseEntity<JsonNode> createAlbum(
      @Valid @RequestBody final com.eos.streamus.payloadmodels.Album albumData,
      BindingResult result) {
    albumValidator.validate(albumData, result);
    if (result.hasErrors()) {
      return badRequest(result.toString());
    }
    try (Connection connection = databaseConnection.getConnection()) {
      connection.setAutoCommit(false);
      Album album = new Album(albumData.getName(), new java.sql.Date(albumData.getReleaseDate().getTime()));
      for (int artistId : albumData.getArtistIds()) {
        album.addArtist(ArtistDAO.findById(artistId, connection));
      }
      for (Track track : albumData.getTracks()) {
        album.addTrack(album.new Track(track.getTrackNumber(), Song.findById(track.getSongId(), connection)));
      }
      album.save(connection);
      connection.commit();
      return ResponseEntity.ok(new JsonAlbumWriter(album).getJson());
    } catch (NoResultException noResultException) {
      // Should not happen
      return badRequest("Invalid ids");
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    }
  }

}
