package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Film;
import com.eos.streamus.utils.FileInfo;
import com.eos.streamus.utils.IDatabaseConnector;
import com.eos.streamus.utils.IResourcePathResolver;
import com.eos.streamus.utils.ShellUtils;
import com.eos.streamus.writers.JsonFilmListWriter;
import com.eos.streamus.writers.JsonFilmWriter;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

@RestController
public final class FilmController implements CommonResponses {
  /** Max Video chunk size to return at a time during stream. */
  private static final long MAX_VIDEO_CHUNK_SIZE = (long) 1024 * 1024;
  /** Accepted Video Mime types. */
  private static final String[] VIDEO_MIME_TYPES = {
      "video/x-flv", "video/mp4", "video/MP2T", "video/3gpp", "video/quicktime", "video/x-msvideo", "video/x-ms-wmv"
  };

  /** {@link com.eos.streamus.utils.IResourcePathResolver} to use. */
  @Autowired
  private IResourcePathResolver resourcePathResolver;
  /** {@link com.eos.streamus.utils.IDatabaseConnector} to use. */
  @Autowired
  private IDatabaseConnector databaseConnector;

  @GetMapping("/films")
  public ResponseEntity<JsonNode> allFilms() {
    try (Connection connection = databaseConnector.getConnection()) {
      return ResponseEntity.ok(new JsonFilmListWriter(Film.all(connection)).getJson());
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    }
  }

  @PostMapping("/film")
  public ResponseEntity<JsonNode> postFilm(@RequestParam("file") final MultipartFile file,
                                           @RequestParam("name") final String name) {
    if (file.getContentType() == null) {
      return badRequest("No specified mime type");
    }
    boolean acceptableMimeType = false;
    for (String type : VIDEO_MIME_TYPES) {
      if (file.getContentType().equals(type)) {
        acceptableMimeType = true;
        break;
      }
    }
    if (!acceptableMimeType) {
      return badRequest(String.format("Invalid mime type : %s", file.getContentType()));
    }

    String path = String.format(
        "%s%s.%s",
        resourcePathResolver.getVideoDir(),
        UUID.randomUUID().toString(),
        FilenameUtils.getExtension(file.getOriginalFilename())
    );

    File storedFile = new File(path);

    try (Connection connection = databaseConnector.getConnection()) {
      file.transferTo(storedFile);
      FileInfo fileInfo = ShellUtils.getResourceInfo(storedFile.getPath());
      if (!fileInfo.isVideo()) {
        return badRequest("File is not video");
      }
      Film film = new Film(path, name, fileInfo.getDuration());
      film.save(connection);
      return ResponseEntity.ok(new JsonFilmWriter(film).getJson());
    } catch (IOException | SQLException e) {
      logException(e);
      try {
        java.nio.file.Files.delete(storedFile.toPath());
      } catch (IOException ioException) {
        logException(e);
      }
      return internalServerError();
    }
  }

  @GetMapping("/film/{id}")
  public ResponseEntity<JsonNode> getFilm(@PathVariable("id") final int id) {
    try (Connection connection = databaseConnector.getConnection()) {
      return ResponseEntity.ok().body(new JsonFilmWriter(Film.findById(id, connection)).getJson());
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    } catch (NoResultException noResultException) {
      return notFound();
    }
  }

  @GetMapping("/film/{id}/stream")
  public ResponseEntity<ResourceRegion> streamFilm(@RequestHeader final HttpHeaders headers,
                                                   @PathVariable("id") final int id) {
    try (Connection connection = databaseConnector.getConnection()) {
      return streamResource(Film.findById(id, connection), headers.getRange(), MAX_VIDEO_CHUNK_SIZE);
    } catch (NoResultException noResultException) {
      return notFound();
    } catch (SQLException | IOException exception) {
      logException(exception);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @DeleteMapping("/film/{id}")
  public ResponseEntity<JsonNode> deleteFilm(@PathVariable final int id) {
    try (Connection connection = databaseConnector.getConnection()) {
      return deleteFileAndResource(Film.findById(id, connection), connection);
    } catch (SQLException sqlException) {
      logException(sqlException);
      return internalServerError();
    } catch (NoResultException noResultException) {
      return notFound();
    }
  }

}
