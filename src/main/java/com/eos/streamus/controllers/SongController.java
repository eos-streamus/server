package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Song;
import com.eos.streamus.utils.FileInfo;
import com.eos.streamus.utils.IDatabaseConnector;
import com.eos.streamus.utils.IResourcePathResolver;
import com.eos.streamus.utils.ShellUtils;
import com.eos.streamus.writers.JsonSongWriter;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

@RestController
public class SongController implements CommonResponses {
  /** Max Audio chunk size to stream (1GB). */
  private static final long MAX_AUDIO_CHUNK_SIZE = (long) 1024 * 1024;

  /** Accepted Mime types for Audio files. */
  private static final String[] AUDIO_MIME_TYPES = {
      "audio/wav", "audio/mpeg", "audio/mp4", "audio/aac", "audio/aacp", "audio/ogg", "audio/webm", "audio/ogg",
      "audio/webm", "audio/flac", "audio/og"
  };

  /** {@link IResourcePathResolver} to use. */
  @Autowired
  private IResourcePathResolver resourcePathResolver;

  /** {@link IDatabaseConnector} to use. */
  @Autowired
  private IDatabaseConnector databaseConnector;

  /**
   * Save a new {@link Song}.
   *
   * @param multipartFile Audio file of song to create.
   * @param name          Name of the song.
   * @return response (bad request, ok, internal server error).
   */
  @PostMapping("/song")
  public ResponseEntity<JsonNode> postSong(@RequestParam("file") final MultipartFile multipartFile,
                                           @RequestParam("name") final String name) {

    if (multipartFile.getContentType() == null) {
      return badRequest("No specified mime type");
    }

    if (Arrays.stream(AUDIO_MIME_TYPES).noneMatch(type -> type.equals(multipartFile.getContentType()))) {
      return badRequest(String.format("Invalid mime type : %s", multipartFile.getContentType()));
    }

    String path = String.format(
        "%s%s.%s",
        resourcePathResolver.getAudioDir(),
        UUID.randomUUID(),
        FilenameUtils.getExtension(multipartFile.getOriginalFilename())
    );

    File storedFile = new File(path);
    try (Connection connection = databaseConnector.getConnection()) {
      multipartFile.transferTo(storedFile);
      FileInfo fileInfo = ShellUtils.getResourceInfo(storedFile.getPath());
      if (!fileInfo.isAudioOnly()) {
        Files.delete(storedFile.toPath());
        return badRequest("file is not audio");
      }
      Song song = new Song(path, name, fileInfo.getDuration());
      song.save(connection);
      return ResponseEntity.ok(new JsonSongWriter(song).getJson());
    } catch (IOException | SQLException e) {
      logException(e);
      try {
        java.nio.file.Files.delete(storedFile.toPath());
      } catch (IOException ioException) {
        logException(ioException);
      }
      return internalServerError();
    }

  }

  /**
   * Get a Song file to stream.
   *
   * @param headers HttpHeaders to get range from.
   * @param id      Id of song.
   * @return Stream ResourceRegion.
   */
  @GetMapping("/song/{id}")
  public ResponseEntity<ResourceRegion> getAudio(@RequestHeader final HttpHeaders headers,
                                                 @PathVariable("id") final int id) {
    try (Connection connection = databaseConnector.getConnection()) {
      return streamResource(Song.findById(id, connection), headers.getRange(), MAX_AUDIO_CHUNK_SIZE);
    } catch (NoResultException noResultException) {
      return notFound();
    } catch (SQLException | IOException exception) {
      logException(exception);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Delete a Song by id.
   *
   * @param id Id of Song to delete.
   * @return Confirmation message.
   */
  @DeleteMapping("/song/{id}")
  public ResponseEntity<String> deleteSong(@PathVariable("id") final int id) {
    try (Connection connection = databaseConnector.getConnection()) {
      return deleteFileAndResource(Song.findById(id, connection), connection);
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerErrorString();
    } catch (NoResultException noResultException) {
      return notFound();
    }
  }

}
