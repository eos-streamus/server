package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Song;
import com.eos.streamus.models.SongCollection;
import com.eos.streamus.models.SongCollectionDAO;
import com.eos.streamus.payloadmodels.Track;
import com.eos.streamus.payloadmodels.validators.SongCollectionValidator;
import com.eos.streamus.utils.IDatabaseConnector;
import com.eos.streamus.writers.JsonSongCollectionWriter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.ok;

public abstract class SongCollectionController implements CommonResponses {

  @Autowired
  protected IDatabaseConnector databaseConnector;

  protected abstract SongCollectionValidator getSongCollectionValidator();

  protected abstract JsonSongCollectionWriter jsonSongCollectionWriter(final SongCollection songCollection);

  public ResponseEntity<JsonNode> getSongCollectionById(@PathVariable final int id) {
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

  public ResponseEntity<String> deleteSongCollection(final int id) {
    try (Connection connection = databaseConnector.getConnection()) {
      SongCollectionDAO.findById(id, connection).delete(connection);
      return ok("SongPlaylist deleted");
    } catch (NoResultException noResultException) {
      return notFound();
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerErrorString();
    }
  }

  public ResponseEntity<JsonNode> deleteSongFromSongCollection(final int songCollectionId, final int songId) {
    try (Connection connection = databaseConnector.getConnection()) {
      SongCollection songCollection = SongCollectionDAO.findById(songCollectionId, connection);
      Optional<SongCollection.Track> existingTrack = songCollection.getTracks().stream().filter(
          track -> track.getSong().getId() == songId
      ).findFirst();
      if (existingTrack.isEmpty()) {
        return notFound();
      } else {
        SongCollection.Track track = existingTrack.get();
        songCollection.moveTrack(track, songCollection.getTracks().size(), connection);
        track.delete(connection);
        songCollection.removeTrack(track);
        return ok(jsonSongCollectionWriter(songCollection).getJson());
      }
    } catch (NoResultException noResultException) {
      return notFound();
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    }
  }

  protected abstract SongCollection createSpecificCollection(
      com.eos.streamus.payloadmodels.SongCollection songCollectionData,
      Connection connection) throws SQLException, NoResultException;

  protected ResponseEntity<JsonNode> createSongCollection(
      com.eos.streamus.payloadmodels.SongCollection songCollectionData,
      BindingResult result
  ) {
    getSongCollectionValidator().validate(songCollectionData, result);
    if (result.hasErrors()) {
      return badRequest(result.toString());
    }
    try (Connection connection = databaseConnector.getConnection()) {
      connection.setAutoCommit(false);

      SongCollection collection = createSpecificCollection(songCollectionData, connection);
      for (Track track : songCollectionData.getTracks()) {
        collection.addTrack(
            collection.new Track(track.getTrackNumber(), Song.findById(track.getSongId(), connection))
        );
      }
      collection.save(connection);
      connection.commit();
      return ok(jsonSongCollectionWriter(collection).getJson());
    } catch (NoResultException noResultException) {
      // Should not happen
      return badRequest("Invalid ids");
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    }
  }

}
