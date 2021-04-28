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
public class SongPlaylistController extends SongCollectionController {

  /** {@link SongPlaylistDTOValidator} to use. */
  @Autowired
  private SongPlaylistDTOValidator songPlaylistDTOValidator;

  /**
   * Get a SongPlaylist by id.
   *
   * @param id Id of SongPlaylist to get.
   * @return Song playlist in JSON format.
   */
  @GetMapping("/songplaylist/{id}")
  public ResponseEntity<JsonNode> getSongPlaylist(@PathVariable final int id) {
    return getSongCollectionById(id);
  }

  /**
   * Create a SongPlaylist.
   *
   * @param songPlaylistDTO SongPlaylist data.
   * @param result          BindingResult to add validation results to.
   * @return Created SongPlaylist data in JSON format.
   */
  @PostMapping("/songplaylist")
  public ResponseEntity<JsonNode> createSongPlaylist(@Valid @RequestBody final SongPlaylistDTO songPlaylistDTO,
                                                     final BindingResult result) {
    return createSongCollection(songPlaylistDTO, result);
  }

  /**
   * Delete a SongPlaylist by id.
   *
   * @param id Id of SongPlaylist to delete.
   * @return Confirmation message.
   */
  @DeleteMapping("/songplaylist/{id}")
  public ResponseEntity<String> deleteSongPlaylist(@PathVariable final int id) {
    return deleteSongCollection(id);
  }

  /**
   * Delete a song from a SongCollection.
   *
   * @param songCollectionId Id of SongCollection.
   * @param songId           Id of Song.
   * @return Updated SongCollection data in JSON.
   */
  @DeleteMapping("/songplaylist/{songCollectionId}/{songId}")
  public ResponseEntity<JsonNode> deleteSongFromSongPlaylist(@PathVariable final int songCollectionId,
                                                             @PathVariable final int songId) {
    return deleteSongFromSongCollection(songCollectionId, songId);
  }

  /**
   * Add or move a Track in a SongPlaylist.
   *
   * @param songPlaylistId Id
   * @param trackData      Track data.
   * @return Updated SongPlaylist data in JSON.
   */
  @PutMapping("/songplaylist/{songPlaylistId}/tracks")
  public ResponseEntity<JsonNode> addOrMoveTrackInSongPlaylist(@PathVariable final int songPlaylistId,
                                                               @Valid @RequestBody final TrackDTO trackData) {
    return addOrMoveTrackInSongCollection(songPlaylistId, trackData);
  }

  /**
   * Add a Song to a SongPlaylist.
   *
   * @param songPlaylistId SongPlaylist id.
   * @param songId         Song id.
   * @return Updated SongPlaylist in JSON format.
   */
  @PostMapping("/songplaylist/{songPlaylistId}/{songId}")
  public ResponseEntity<JsonNode> addSongToSongPlaylist(@PathVariable final int songPlaylistId,
                                                        @PathVariable final int songId) {
    return addSongToCollection(songPlaylistId, songId);
  }

  /** {@inheritDoc} */
  @Override
  protected SongCollectionDTOValidator getSongCollectionDTOValidator() {
    return songPlaylistDTOValidator;
  }

  /** {@inheritDoc} */
  @Override
  protected JsonSongCollectionWriter jsonSongCollectionWriter(final SongCollection songCollection) {
    return new JsonSongPlaylistWriter((SongPlaylist) songCollection);
  }

  /** {@inheritDoc} */
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
