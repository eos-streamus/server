package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Song;
import com.eos.streamus.models.SongCollection;
import com.eos.streamus.models.SongCollectionDAO;
import com.eos.streamus.payloadmodels.validators.SongCollectionValidator;
import com.eos.streamus.utils.IDatabaseConnector;
import com.eos.streamus.writers.JsonSongCollectionWriter;
import com.eos.streamus.writers.JsonSongPlaylistWriter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.sql.Connection;
import java.sql.SQLException;

import static org.springframework.http.ResponseEntity.ok;

public abstract class SongCollectionController implements CommonResponses {

  @Autowired
  protected IDatabaseConnector databaseConnector;

  protected abstract SongCollectionValidator getSongCollectionValidator();

  protected abstract JsonSongCollectionWriter jsonSongCollectionWriter(final SongCollection songCollection);

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
}
