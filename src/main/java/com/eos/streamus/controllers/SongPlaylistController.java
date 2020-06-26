package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.SongCollection;
import com.eos.streamus.models.SongPlaylist;
import com.eos.streamus.models.User;
import com.eos.streamus.payloadmodels.validators.SongCollectionValidator;
import com.eos.streamus.payloadmodels.validators.SongPlaylistValidator;
import com.eos.streamus.writers.JsonSongCollectionWriter;
import com.eos.streamus.writers.JsonSongPlaylistWriter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.sql.Connection;
import java.sql.SQLException;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class SongPlaylistController extends SongCollectionController {

  @Autowired
  private SongPlaylistValidator songPlaylistValidator;

  @PostMapping("/songplaylist")
  public ResponseEntity<JsonNode> createSongPlaylist(
      @Valid @RequestBody final com.eos.streamus.payloadmodels.SongPlaylist songPlaylistData,
      BindingResult result
  ) {
    return createSongCollection(songPlaylistData, result);
  }

  @Override
  protected SongCollectionValidator getSongCollectionValidator() {
    return songPlaylistValidator;
  }

  @Override
  protected JsonSongCollectionWriter jsonSongCollectionWriter(final SongCollection songCollection) {
    return new JsonSongPlaylistWriter((SongPlaylist) songCollection);
  }

  @Override
  protected SongCollection createSpecificCollection(
      final com.eos.streamus.payloadmodels.SongCollection songCollectionData,
      final Connection connection
  ) throws SQLException, NoResultException {
    return new SongPlaylist(
        songCollectionData.getName(),
        User.findById(
            ((com.eos.streamus.payloadmodels.SongPlaylist) songCollectionData).getUserId(),
            connection
        )
    );
  }

}
