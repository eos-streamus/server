package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Song;
import com.eos.streamus.models.SongCollection;
import com.eos.streamus.models.SongCollectionDAO;
import com.eos.streamus.models.SongPlaylist;
import com.eos.streamus.payloadmodels.Track;
import com.eos.streamus.payloadmodels.validators.SongCollectionValidator;
import com.eos.streamus.utils.IDatabaseConnector;
import com.eos.streamus.writers.JsonSongCollectionWriter;
import com.eos.streamus.writers.JsonSongPlaylistWriter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.ok;

public abstract class SongCollectionController implements CommonResponses {

  @Autowired
  protected IDatabaseConnector databaseConnector;

  protected abstract SongCollectionValidator getSongCollectionValidator();

  protected abstract JsonSongCollectionWriter jsonSongCollectionWriter(final SongCollection songCollection);

  public ResponseEntity<JsonNode> getSongCollectionById(final int id) {
    try (Connection connection = databaseConnector.getConnection()) {
      return ok(jsonSongCollectionWriter(SongCollectionDAO.findById(id, connection)).getJson());
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    } catch (NoResultException noResultException) {
      return notFound();
    }
  }

  public ResponseEntity<JsonNode> addSongToCollection(final int songCollectionId, final int songId) {
    try (Connection connection = databaseConnector.getConnection()) {
      SongCollection songCollection = SongCollectionDAO.findById(songCollectionId, connection);
      songCollection.addSong(Song.findById(songId, connection));
      songCollection.save(connection);
      return ok(jsonSongCollectionWriter(songCollection).getJson());
    } catch (NoResultException noResultException) {
      return notFound();
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    }
  }


  public ResponseEntity<JsonNode> addOrMoveTrackInSongCollection(final int id, final Track trackData) {
    try (Connection connection = databaseConnector.getConnection()) {

      SongCollection songCollection = SongCollectionDAO.findById(id, connection);

      Song song = Song.findById(trackData.getSongId(), connection);

      if (trackData.getTrackNumber() < 1 || trackData.getTrackNumber() > songCollection.getTracks().size()) {
        return badRequest("Track number out of bounds");
      }

      // If song is already in collection
      Optional<SongCollection.Track> existingTrack = songCollection.getTracks().stream().filter(
          track -> track.getSong().getId() == trackData.getSongId()
      ).findFirst();

      if (existingTrack.isPresent()) {
        songCollection.moveTrack(existingTrack.get(), trackData.getTrackNumber(), connection);
      } else {
        SongCollection.Track newTrack = songCollection.addSong(song);
        songCollection.save(connection);
        songCollection.moveTrack(newTrack, trackData.getTrackNumber(), connection);
      }

      return ok(jsonSongCollectionWriter(songCollection).getJson());

    } catch (NoResultException noResultException) {
      return notFound();
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    }
  }
}
