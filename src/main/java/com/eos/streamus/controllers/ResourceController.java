package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Film;
import com.eos.streamus.models.Resource;
import com.eos.streamus.models.Song;
import com.eos.streamus.utils.FileInfo;
import com.eos.streamus.utils.ResourcePathResolver;
import com.eos.streamus.utils.ShellUtils;
import com.eos.streamus.utils.TestDatabaseConnection;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
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
import java.util.List;
import java.util.UUID;

class ErrorObjectNodeFactory extends JsonNodeFactory {
  private static final long serialVersionUID = -5301230341871379657L;

}

@RestController
public class ResourceController {
  //#region Static attributes
  private static final long MAX_VIDEO_CHUNK_SIZE = (long) 1024 * 1024;
  private static final long MAX_AUDIO_CHUNK_SIZE = (long) 1024 * 1024;

  private static final String[] AUDIO_MIME_TYPES = {
      "audio/wav", "audio/mpeg", "audio/mp4", "audio/aac", "audio/aacp", "audio/ogg", "audio/webm", "audio/ogg",
      "audio/webm", "audio/flac", "audio/og"
  };

  private static final String[] VIDEO_MIME_TYPES = {
      "video/x-flv", "video/mp4", "video/MP2T", "video/3gpp", "video/quicktime", "video/x-msvideo", "video/x-ms-wmv"
  };
  //#endregion Static attributes

  //#region Attributes
  @Autowired
  protected TestDatabaseConnection databaseConnection = null;

  private final ResourcePathResolver resourcePathResolver;
  //#endregion Attributes

  //#region Constructor
  public ResourceController(@Autowired final ResourcePathResolver resourcePathResolver) {
    this.resourcePathResolver = resourcePathResolver;
  }
  //#endregion Constructor

  //#region Songs
  @GetMapping("/song/{id}")
  public ResponseEntity<ResourceRegion> getAudio(@RequestHeader HttpHeaders headers,
                                                 @PathVariable("id") int id) throws IOException, SQLException {
    Song song;
    try (Connection connection = databaseConnection.getConnection()) {
      song = Song.findById(id, connection);
    } catch (NoResultException e) {
      return ResponseEntity.notFound().build();
    }
    return streamResource(song, headers.getRange(), MAX_AUDIO_CHUNK_SIZE);
  }

  @PostMapping("/song")
  public ResponseEntity<Object> postSong(@RequestParam("file") MultipartFile file,
                                         @RequestParam("name") String name) throws IOException {

    if (file.getContentType() == null) {
      return error("No specified mime type");
    }
    boolean acceptableMimeType = false;
    for (String type : AUDIO_MIME_TYPES) {
      if (file.getContentType().equals(type)) {
        acceptableMimeType = true;
        break;
      }
    }
    if (!acceptableMimeType) {
      return error(String.format("Invalid mime type : %s", file.getContentType()));
    }

    String path = String.format(
        "%s%s.%s",
        resourcePathResolver.getAudioDir(),
        UUID.randomUUID().toString(),
        FilenameUtils.getExtension(file.getOriginalFilename())
    );

    File storedFile = new File(path);
    file.transferTo(storedFile);

    FileInfo fileInfo = ShellUtils.getResourceInfo(storedFile.getPath());

    Song song = new Song(path, name, fileInfo.getDuration());
    try (Connection connection = databaseConnection.getConnection()) {
      song.save(connection);
    } catch (Exception e) {
      java.nio.file.Files.delete(storedFile.toPath());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
    return ResponseEntity.ok(song);
  }
  //#endregion Songs

  //#region Films
  @GetMapping("/films")
  public List<Film> getResources() throws SQLException {
    try (Connection connection = databaseConnection.getConnection()) {
      return Film.all(connection);
    }
  }

  @GetMapping("/video/{id}")
  public ResponseEntity<ResourceRegion> getVideo(@RequestHeader HttpHeaders headers,
                                                 @PathVariable("id") int id) throws IOException, SQLException {
    Film film;
    try (Connection connection = databaseConnection.getConnection()) {
      film = Film.findById(id, connection);
    } catch (NoResultException e) {
      return ResponseEntity.notFound().build();
    }
    return streamResource(film, headers.getRange(), MAX_VIDEO_CHUNK_SIZE);
  }

  @PostMapping("/film")
  public ResponseEntity<Object> postFilm(@RequestParam("file") MultipartFile file,
                                         @RequestParam("name") String name) throws IOException {
    if (file.getContentType() == null) {
      return error("No specified mime type");
    }
    boolean acceptableMimeType = false;
    for (String type : VIDEO_MIME_TYPES) {
      if (file.getContentType().equals(type)) {
        acceptableMimeType = true;
        break;
      }
    }
    if (!acceptableMimeType) {
      return error(String.format("Invalid mime type : %s", file.getContentType()));
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
  //#endregion Films

  //#region Private methods
  private ResponseEntity<ResourceRegion> streamResource(final Resource resource, final List<HttpRange> range,
                                                        final long maxChunkSize) throws IOException {
    UrlResource urlResource = new UrlResource(String.format("file:%s", resource.getPath()));
    long contentLength = urlResource.contentLength();
    ResourceRegion region;
    long start;
    long rangeLength;
    if (!range.isEmpty()) {
      start = range.get(0).getRangeStart(contentLength);
      long end = range.get(0).getRangeEnd(contentLength);
      rangeLength = Math.min(maxChunkSize, end - start + 1);
    } else {
      start = 0;
      rangeLength = Math.min(maxChunkSize, contentLength);
    }
    region = new ResourceRegion(urlResource, start, rangeLength);
    return ResponseEntity
        .status(HttpStatus.PARTIAL_CONTENT)
        .contentType(
            MediaTypeFactory
                .getMediaType(urlResource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM)
        )
        .body(region);
  }

  private ResponseEntity<Object> error(final String reason) {
    ObjectNode errorResponse = new ObjectNode(new ErrorObjectNodeFactory());
    errorResponse.put("reason", reason);
    return ResponseEntity.badRequest().body(errorResponse);
  }
  //#endregion Private methods
}
