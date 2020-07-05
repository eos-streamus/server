package com.eos.streamus.controllers;

import com.eos.streamus.dto.AlbumDTO;
import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Album;
import com.eos.streamus.models.ArtistDAO;
import com.eos.streamus.models.SongCollection;
import com.eos.streamus.payloadmodels.validators.AlbumValidator;
import com.eos.streamus.payloadmodels.Track;
import com.eos.streamus.payloadmodels.validators.SongCollectionValidator;
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
public class AlbumController extends SongCollectionController {

  @Autowired
  private AlbumValidator albumValidator;

  @GetMapping("/album/{id}")
  public ResponseEntity<JsonNode> getAlbumById(@PathVariable final int id) {
    return getSongCollectionById(id);
  }

  @PostMapping("/albums")
  public ResponseEntity<JsonNode> createAlbum(
      @Valid @RequestBody final AlbumDTO albumDTO,
      BindingResult result
  ) {
    return createSongCollection(albumDTO, result);
  }

  @PostMapping("/album/{albumId}/{songId}")
  public ResponseEntity<JsonNode> addSongToAlbum(@PathVariable final int albumId, @PathVariable final int songId) {
    return addSongToCollection(albumId, songId);
  }

  @PutMapping("/album/{id}")
  public ResponseEntity<JsonNode> addOrMoveTrackInAlbum(@PathVariable final int id,
                                                        @Valid @RequestBody final Track trackData) {
    return addOrMoveTrackInSongCollection(id, trackData);
  }

  @DeleteMapping("/album/{id}")
  public ResponseEntity<String> deleteAlbum(@PathVariable final int id) {
    return deleteSongCollection(id);
  }

  @Override
  protected SongCollectionValidator getSongCollectionValidator() {
    return albumValidator;
  }

  @Override
  protected JsonSongCollectionWriter jsonSongCollectionWriter(final SongCollection songCollection) {
    return new JsonAlbumWriter((Album) songCollection);
  }

  @Override
  protected SongCollection createSpecificCollection(
      final com.eos.streamus.payloadmodels.SongCollection songCollectionData,
      final Connection connection) throws SQLException, NoResultException {
    AlbumDTO albumDTO = (AlbumDTO) songCollectionData;
    Album album = new Album(albumDTO.getName(), new java.sql.Date(albumDTO.getReleaseDate().getTime()));
    for (int artistId : albumDTO.getArtistIds()) {
      album.addArtist(ArtistDAO.findById(artistId, connection));
    }
    return album;
  }

}
