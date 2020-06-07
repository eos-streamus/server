package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Film;
import com.eos.streamus.models.Resource;
import com.eos.streamus.utils.FileInfo;
import com.eos.streamus.utils.ResourcePathResolver;
import com.eos.streamus.utils.ShellUtils;
import com.eos.streamus.utils.TestDatabaseConnection;
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

@RestController
public class ResourceController {
  @Autowired
  protected TestDatabaseConnection databaseConnection = null;

  @Autowired
  private ResourcePathResolver resourcePathResolver = null;

  private static final long MAX_VIDEO_CHUNK_SIZE = (long) 1024 * 1024;
  private static final long MAX_AUDIO_CHUNK_SIZE = (long) 1024;

  @GetMapping("/videos")
  public List<Film> getResources() throws SQLException {
    try (Connection connection = databaseConnection.getConnection()) {
      return Film.all(connection);
    }
  }

  @GetMapping("/audio")
  public ResponseEntity<ResourceRegion> getAudio(@RequestHeader HttpHeaders headers) throws IOException {
    UrlResource urlResource = new UrlResource(
        String.format("file:%s%s", resourcePathResolver.getAudioDir(), "audio.mp3"));
    long contentLength = urlResource.contentLength();
    List<HttpRange> range = headers.getRange();
    ResourceRegion region;
    long start;
    long rangeLength;
    if (!range.isEmpty()) {
      start = range.get(0).getRangeStart(contentLength);
      long end = range.get(0).getRangeEnd(contentLength);
      rangeLength = Math.min(MAX_AUDIO_CHUNK_SIZE, end - start + 1);
    } else {
      start = 0;
      rangeLength = Math.min(MAX_AUDIO_CHUNK_SIZE, contentLength);
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

  @GetMapping("/video/{id}")
  public ResponseEntity<ResourceRegion> getVideo(@RequestHeader HttpHeaders headers,
                                                 @PathVariable("id") int id) throws IOException, SQLException {
    Film film;
    try (Connection connection = databaseConnection.getConnection()) {
      film = Film.findById(id, connection);
    } catch (NoResultException e) {
      return ResponseEntity.notFound().build();
    }
    return streamResource(film, headers.getRange());
  }

  private ResponseEntity<ResourceRegion> streamResource(Resource resource, List<HttpRange> range) throws IOException {
    UrlResource urlResource = new UrlResource(String.format("file:%s", resource.getPath()));
    long contentLength = urlResource.contentLength();
    ResourceRegion region;
    long start;
    long rangeLength;
    if (!range.isEmpty()) {
      start = range.get(0).getRangeStart(contentLength);
      long end = range.get(0).getRangeEnd(contentLength);
      rangeLength = Math.min(MAX_VIDEO_CHUNK_SIZE, end - start + 1);
    } else {
      start = 0;
      rangeLength = Math.min(MAX_VIDEO_CHUNK_SIZE, contentLength);
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

  @PostMapping("/film")
  public ResponseEntity<Film> postFilm(@RequestParam("file") MultipartFile file,
                                       @RequestParam("name") String name) throws IOException, SQLException {

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

}
