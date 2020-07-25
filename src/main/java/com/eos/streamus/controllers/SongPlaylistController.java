package com.eos.streamus.controllers;

import com.eos.streamus.dto.validators.SongCollectionDTOValidator;
import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.SongCollection;
import com.eos.streamus.models.SongPlaylist;
import com.eos.streamus.models.User;
import com.eos.streamus.dto.SongPlaylistDTO;
import com.eos.streamus.dto.TrackDTO;
import com.eos.streamus.dto.validators.SongPlaylistDTOValidator;
import com.eos.streamus.writers.JsonSongCollectionWriter;
import com.eos.streamus.writers.JsonSongPlaylistWriter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.sql.Connection;
import java.sql.SQLException;

@RestController
public final class SongPlaylistController extends SongCollectionController {

  /** {@link com.eos.streamus.dto.validators.SongPlaylistDTOValidator} to use. */
  @Autowired
  private SongPlaylistDTOValidator songPlaylistDTOValidator;

  @GetMapping("/songplaylist/{id}")
  public ResponseEntity<JsonNode> getSongPlaylist(@PathVariable final int id) {
    return getSongCollectionById(id);
  }

  @PostMapping("/songplaylist")
  public ResponseEntity<JsonNode> createSongPlaylist(
      @Valid @RequestBody final SongPlaylistDTO songPlaylistDTO,
      final BindingResult result
  ) {
    return createSongCollection(songPlaylistDTO, result);
  }

  @DeleteMapping("/songplaylist/{id}")
  public ResponseEntity<JsonNode> deleteSongPlaylist(@PathVariable final int id) {
    return deleteSongCollection(id);
  }

  @DeleteMapping("/songplaylist/{songCollectionId}/{songId}")
  public ResponseEntity<JsonNode> deleteSongFromSongPlaylist(@PathVariable final int songCollectionId,
                                                             @PathVariable final int songId) {
    return deleteSongFromSongCollection(songCollectionId, songId);
  }

  @PutMapping("/songplaylist/{id}/tracks")
  public ResponseEntity<JsonNode> addOrMoveTrackInSongPlaylist(@PathVariable final int id,
                                                               @Valid @RequestBody final TrackDTO trackData) {
    return addOrMoveTrackInSongCollection(id, trackData);
  }

  @PostMapping("/songplaylist/{songPlaylistId}/{songId}")
  public ResponseEntity<JsonNode> addSongToSongPlaylist(@PathVariable final int songPlaylistId,
                                                        @PathVariable final int songId) {
    return addSongToCollection(songPlaylistId, songId);
  }

  @Override
  protected SongCollectionDTOValidator getSongCollectionDTOValidator() {
    return songPlaylistDTOValidator;
  }

  @Override
  protected JsonSongCollectionWriter jsonSongCollectionWriter(final SongCollection songCollection) {
    return new JsonSongPlaylistWriter((SongPlaylist) songCollection);
  }

  @Override
  protected SongCollection createSpecificCollection(
      final com.eos.streamus.dto.SongCollectionDTO songCollectionDTO,
      final Connection connection
  ) throws SQLException, NoResultException {
    return new SongPlaylist(
        songCollectionDTO.getName(),
        User.findById(
            ((SongPlaylistDTO) songCollectionDTO).getUserId(),
            connection
        )
    );
  }

}
