package com.eos.streamus.controllers;

import com.eos.streamus.dto.AlbumDTO;
import com.eos.streamus.dto.TrackDTO;
import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Album;
import com.eos.streamus.models.ArtistDAO;
import com.eos.streamus.models.SongCollection;
import com.eos.streamus.dto.validators.AlbumDTOValidator;
import com.eos.streamus.dto.validators.SongCollectionDTOValidator;
import com.eos.streamus.writers.JsonAlbumWriter;
import com.eos.streamus.writers.JsonSongCollectionWriter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.sql.Connection;
import java.sql.SQLException;

@Controller
public final class AlbumController extends SongCollectionController {
  /** {@link AlbumDTOValidator} to use to validate incoming requests. */
  @Autowired
  private AlbumDTOValidator albumDTOValidator;

  /**
   * Get an album by id.
   *
   * @param id Id of album to get.
   * @return Album in Json format.
   */
  @GetMapping("/album/{id}")
  public ResponseEntity<JsonNode> getAlbumById(@PathVariable final int id) {
    return getSongCollectionById(id);
  }

  /**
   * Create an Album.
   *
   * @param albumDTO Album data.
   * @param result   BindingResult for validation.
   * @return Created album in Json format.
   */
  @PostMapping("/albums")
  public ResponseEntity<JsonNode> createAlbum(@Valid @RequestBody final AlbumDTO albumDTO,
                                              final BindingResult result) {
    return createSongCollection(albumDTO, result);
  }

  /**
   * Add a song to an album.
   *
   * @param albumId Id of album to add song to.
   * @param songId  Id of Song to add to album.
   * @return Updated Album in JSON format.
   */
  @PostMapping("/album/{albumId}/{songId}")
  public ResponseEntity<JsonNode> addSongToAlbum(@PathVariable final int albumId, @PathVariable final int songId) {
    return addSongToCollection(albumId, songId);
  }

  /**
   * Add or move a track in Album.
   *
   * @param albumId  Id of Album to add track to.
   * @param trackDTO Data of track.
   * @return Updated Album in JSON format.
   */
  @PutMapping("/album/{albumId}")
  public ResponseEntity<JsonNode> addOrMoveTrackInAlbum(@PathVariable final int albumId,
                                                        @Valid @RequestBody final TrackDTO trackDTO) {
    return addOrMoveTrackInSongCollection(albumId, trackDTO);
  }

  /**
   * Delete an album by id.
   *
   * @param id Id of album to delete.
   * @return Confirmation message.
   */
  @DeleteMapping("/album/{id}")
  public ResponseEntity<JsonNode> deleteAlbum(@PathVariable final int id) {
    return deleteSongCollection(id);
  }

  /** @return {@link com.eos.streamus.dto.validators.SongCollectionDTOValidator}. */
  @Override
  protected SongCollectionDTOValidator getSongCollectionDTOValidator() {
    return albumDTOValidator;
  }

  /** {@inheritDoc} */
  @Override
  protected JsonSongCollectionWriter jsonSongCollectionWriter(final SongCollection songCollection) {
    return new JsonAlbumWriter((Album) songCollection);
  }

  /** {@inheritDoc} */
  @Override
  protected SongCollection createSpecificCollection(
      final com.eos.streamus.dto.SongCollectionDTO songCollectionDTO,
      final Connection connection) throws SQLException, NoResultException {
    AlbumDTO albumDTO = (AlbumDTO) songCollectionDTO;
    Album album = new Album(albumDTO.getName(), new java.sql.Date(albumDTO.getReleaseDate().getTime()));
    for (int artistId : albumDTO.getArtistIds()) {
      album.addArtist(ArtistDAO.findById(artistId, connection));
    }
    return album;
  }

}
