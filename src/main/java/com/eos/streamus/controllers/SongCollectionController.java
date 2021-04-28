package com.eos.streamus.controllers;

import com.eos.streamus.dto.SongCollectionDTO;
import com.eos.streamus.dto.TrackDTO;
import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Song;
import com.eos.streamus.models.SongCollection;
import com.eos.streamus.models.SongCollectionDAO;
import com.eos.streamus.dto.validators.SongCollectionDTOValidator;
import com.eos.streamus.utils.IDatabaseConnector;
import com.eos.streamus.writers.JsonSongCollectionWriter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.ok;

public abstract class SongCollectionController implements CommonResponses {
  /**
   * {@link IDatabaseConnector} to use.
   */
  @Autowired
  private IDatabaseConnector databaseConnector;

  protected abstract SongCollectionDTOValidator getSongCollectionDTOValidator();

  /**
   * Get an instance of {@link JsonSongCollectionWriter} to use to write results.
   *
   * @param songCollection {@link SongCollection} to write.
   * @return Created writer.
   */
  protected abstract JsonSongCollectionWriter jsonSongCollectionWriter(SongCollection songCollection);

  /**
   * Get a SongCollection by id.
   *
   * @param id Id of song collection.
   * @return SongCollection data in JSON format.
   */
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

  /**
   * Add a song to a SongCollection.
   *
   * @param songCollectionId SongCollection to add Song to.
   * @param songId           Id of Song to add.
   * @return Updated SongCollection data in JSON format.
   */
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

  /**
   * Add or move a Track in a SongCollection.
   *
   * @param songCollectionId        Id of SongCollection.
   * @param trackData Data of Track (new or existing)
   * @return Updated SongCollection data in JSON.
   */
  public ResponseEntity<JsonNode> addOrMoveTrackInSongCollection(final int songCollectionId, final TrackDTO trackData) {
    try (Connection connection = databaseConnector.getConnection()) {

      SongCollection songCollection = SongCollectionDAO.findById(songCollectionId, connection);

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

  /**
   * Delete a SongCollection by id.
   *
   * @param id Id of SongCollection to delete.
   * @return Confirmation message.
   */
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

  /**
   * Remove a Song from a SongCollection.
   *
   * @param songCollectionId SongCollection id.
   * @param songId           Song id.
   * @return Updated SongCollection data in JSON.
   */
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

  protected abstract SongCollection createSpecificCollection(SongCollectionDTO songCollectionData, Connection conn)
      throws SQLException, NoResultException;

  /**
   * Create a SongCollection.
   *
   * @param songCollectionData Song collection data.
   * @param result             To add validation errors to.
   * @return Created SongCollection in JSON format.
   */
  protected ResponseEntity<JsonNode> createSongCollection(final SongCollectionDTO songCollectionData,
                                                          final BindingResult result) {
    getSongCollectionDTOValidator().validate(songCollectionData, result);
    if (result.hasErrors()) {
      return badRequest(result.toString());
    }
    try (Connection connection = databaseConnector.getConnection()) {
      connection.setAutoCommit(false);

      SongCollection collection = createSpecificCollection(songCollectionData, connection);
      for (TrackDTO track : songCollectionData.getTracks()) {
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
