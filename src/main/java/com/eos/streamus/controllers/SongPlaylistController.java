package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Song;
import com.eos.streamus.models.SongPlaylist;
import com.eos.streamus.models.User;
import com.eos.streamus.payloadmodels.Track;
import com.eos.streamus.payloadmodels.validators.SongPlaylistValidator;
import com.eos.streamus.utils.DatabaseConnection;
import com.eos.streamus.writers.JsonSongPlaylistWriter;
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
import java.sql.SQLException;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class SongPlaylistController implements CommonResponses {

  private final DatabaseConnection databaseConnection;
  private final SongPlaylistValidator songPlaylistValidator;

  public SongPlaylistController(@Autowired DatabaseConnection databaseConnection,
                                @Autowired SongPlaylistValidator songPlaylistValidator) {
    this.databaseConnection = databaseConnection;
    this.songPlaylistValidator = songPlaylistValidator;
  }

  @GetMapping("/songplaylist/{id}")
  public ResponseEntity<JsonNode> getSongPlaylistById(@PathVariable final int id) {
    try (Connection connection = databaseConnection.getConnection()) {
      return ok(new JsonSongPlaylistWriter(SongPlaylist.findById(id, connection)).getJson());
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    } catch (NoResultException noResultException) {
      return notFound();
    }
  }

  @PostMapping("/songplaylist")
  public ResponseEntity<JsonNode> createSongPlaylist(
      @Valid @RequestBody final com.eos.streamus.payloadmodels.SongPlaylist songPlaylistData,
      BindingResult result
  ) {
    songPlaylistValidator.validate(songPlaylistData, result);
    if (result.hasErrors()) {
      return badRequest(result.toString());
    }
    try (Connection connection = databaseConnection.getConnection()) {
      connection.setAutoCommit(false);
      SongPlaylist songPlaylist = new SongPlaylist(
          songPlaylistData.getName(),
          User.findById(songPlaylistData.getUserId(), connection)
      );
      for (Track track : songPlaylistData.getTracks()) {
        songPlaylist.addTrack(
            songPlaylist.new Track(track.getTrackNumber(), Song.findById(track.getSongId(), connection))
        );
      }
      songPlaylist.save(connection);
      connection.commit();
      return ok(new JsonSongPlaylistWriter(songPlaylist).getJson());
    } catch (NoResultException noResultException) {
      // Should not happen
      return badRequest("Invalid ids");
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    }
  }

  @PostMapping("/songplaylist/{songPlaylistId}/{songId}")
  public ResponseEntity<JsonNode> addSongToPlaylist(@PathVariable final int songPlaylistId,
                                                    @PathVariable final int songId) {
    try (Connection connection = databaseConnection.getConnection()) {
      SongPlaylist songPlaylist = SongPlaylist.findById(songPlaylistId, connection);
      songPlaylist.addSong(Song.findById(songId, connection));
      songPlaylist.save(connection);
      return ok(new JsonSongPlaylistWriter(songPlaylist).getJson());
    } catch (NoResultException noResultException) {
      return notFound();
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    }
  }

  @DeleteMapping("/songplaylist/{id}")
  public ResponseEntity<String> deleteSongPlaylist(@PathVariable final int id) {
    try (Connection connection = databaseConnection.getConnection()) {
      SongPlaylist.findById(id, connection).delete(connection);
      return ok("SongPlaylist deleted");
    } catch (NoResultException noResultException) {
      return notFound();
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerErrorString();
    }
  }

}
