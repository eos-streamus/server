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

  /** {@link com.eos.streamus.dto.validators.AlbumDTOValidator} to use for validations. */
  @Autowired
  private AlbumDTOValidator albumDTOValidator;

  @GetMapping("/album/{id}")
  public ResponseEntity<JsonNode> getAlbumById(@PathVariable final int id) {
    return getSongCollectionById(id);
  }

  @PostMapping("/albums")
  public ResponseEntity<JsonNode> createAlbum(
      @Valid @RequestBody final AlbumDTO albumDTO,
      final BindingResult result
  ) {
    return createSongCollection(albumDTO, result);
  }

  @PostMapping("/album/{albumId}/{songId}")
  public ResponseEntity<JsonNode> addSongToAlbum(@PathVariable final int albumId, @PathVariable final int songId) {
    return addSongToCollection(albumId, songId);
  }

  @PutMapping("/album/{id}")
  public ResponseEntity<JsonNode> addOrMoveTrackInAlbum(@PathVariable final int id,
                                                        @Valid @RequestBody final TrackDTO trackDTO) {
    return addOrMoveTrackInSongCollection(id, trackDTO);
  }

  @DeleteMapping("/album/{id}")
  public ResponseEntity<JsonNode> deleteAlbum(@PathVariable final int id) {
    return deleteSongCollection(id);
  }

  @Override
  protected SongCollectionDTOValidator getSongCollectionDTOValidator() {
    return albumDTOValidator;
  }

  @Override
  protected JsonSongCollectionWriter jsonSongCollectionWriter(final SongCollection songCollection) {
    return new JsonAlbumWriter((Album) songCollection);
  }

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
