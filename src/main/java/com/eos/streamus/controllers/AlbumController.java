package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Album;
import com.eos.streamus.models.ArtistDAO;
import com.eos.streamus.models.Song;
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

  @PostMapping("/albums")
  public ResponseEntity<JsonNode> createAlbum(@Valid @RequestBody final com.eos.streamus.payloadmodels.Album albumData,
                                              BindingResult result) {
    albumValidator.validate(albumData, result);
    if (result.hasErrors()) {
      return badRequest(result.toString());
    }
    try (Connection connection = databaseConnector.getConnection()) {
      connection.setAutoCommit(false);
      Album album = new Album(albumData.getName(), new java.sql.Date(albumData.getReleaseDate().getTime()));
      for (int artistId : albumData.getArtistIds()) {
        album.addArtist(ArtistDAO.findById(artistId, connection));
      }
      for (Track track : albumData.getTracks()) {
        album.addTrack(album.new Track(track.getTrackNumber(), Song.findById(track.getSongId(), connection)));
      }
      album.save(connection);
      connection.commit();
      return ResponseEntity.ok(new JsonAlbumWriter(album).getJson());
    } catch (NoResultException noResultException) {
      // Should not happen
      return badRequest("Invalid ids");
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    }
  }

  @PostMapping("/album/{albumId}/{songId}")
  public ResponseEntity<JsonNode> addSongToPlaylist(@PathVariable final int albumId,
                                                    @PathVariable final int songId) {
    return addSongToCollection(albumId, songId);
  }

  @PutMapping("/album/{id}")
  public ResponseEntity<JsonNode> addOrMoveTrackInAlbum(@PathVariable final int id,
                                                        @Valid @RequestBody final Track trackData) {
    return addOrMoveTrackInSongCollection(id, trackData);
  }

  @Override
  protected SongCollectionValidator getSongCollectionValidator() {
    return albumValidator;
  }

  @Override
  protected JsonSongCollectionWriter jsonSongCollectionWriter(final SongCollection songCollection) {
    return new JsonAlbumWriter((Album) songCollection);
  }

}
