package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Film;
import com.eos.streamus.utils.DatabaseConnection;
import com.eos.streamus.utils.FileInfo;
import com.eos.streamus.utils.ResourcePathResolver;
import com.eos.streamus.utils.ShellUtils;
import com.eos.streamus.writers.JsonFilmListWriter;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import java.util.logging.Logger;

import static com.eos.streamus.controllers.CommonResponses.badRequest;
import static com.eos.streamus.controllers.CommonResponses.streamResource;

@RestController
public class FilmController {
  private static final long MAX_VIDEO_CHUNK_SIZE = (long) 1024 * 1024;
  private static final String[] VIDEO_MIME_TYPES = {
      "video/x-flv", "video/mp4", "video/MP2T", "video/3gpp", "video/quicktime", "video/x-msvideo", "video/x-ms-wmv"
  };

  private final ResourcePathResolver resourcePathResolver;
  private final DatabaseConnection databaseConnection;

  public FilmController(@Autowired final ResourcePathResolver resourcePathResolver,
                        @Autowired final DatabaseConnection databaseConnection) {
    this.resourcePathResolver = resourcePathResolver;
    this.databaseConnection = databaseConnection;
  }

  @GetMapping("/films")
  public JsonNode allFilms() throws SQLException {
    try (Connection connection = databaseConnection.getConnection()) {
      return new JsonFilmListWriter(Film.all(connection)).getJson();
    }
  }

  @PostMapping("/film")
  public ResponseEntity<Object> postFilm(@RequestParam("file") MultipartFile file,
                                         @RequestParam("name") String name) throws IOException {
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
    file.transferTo(storedFile);

    FileInfo fileInfo = ShellUtils.getResourceInfo(storedFile.getPath());

    Film film = new Film(path, name, fileInfo.getDuration());
    try (Connection connection = databaseConnection.getConnection()) {
      film.save(connection);
    } catch (Exception e) {
      java.nio.file.Files.delete(storedFile.toPath());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
    return ResponseEntity.ok(film);
  }

  @GetMapping("/film/{id}")
  public ResponseEntity<ResourceRegion> getFilm(@RequestHeader HttpHeaders headers,
                                                 @PathVariable("id") int id) {
    try (Connection connection = databaseConnection.getConnection()) {
      return streamResource(Film.findById(id, connection), headers.getRange(), MAX_VIDEO_CHUNK_SIZE);
    } catch (NoResultException noResultException) {
      return ResponseEntity.notFound().build();
    } catch (SQLException | IOException exception) {
      Logger.getLogger(getClass().getName()).severe(exception.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

}
