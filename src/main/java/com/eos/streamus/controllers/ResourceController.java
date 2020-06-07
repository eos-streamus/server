package com.eos.streamus.controllers;

import com.eos.streamus.models.Film;
import com.eos.streamus.utils.ResourcePathResolver;
import com.eos.streamus.utils.TestDatabaseConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@RestController
public class ResourceController {
  @Autowired
  protected TestDatabaseConnection databaseConnection = null;

  @Autowired
  private ResourcePathResolver resourcePathResolver = null;

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
    var contentLength = urlResource.contentLength();
    var range = headers.getRange();
    if (!range.isEmpty()) {
      var start = range.get(0).getRangeStart(contentLength);
      var end = range.get(0).getRangeEnd(contentLength);
      var rangeLength = Math.min((long) 1024 * 1024, end - start + 1);
      var region = new ResourceRegion(urlResource, start, rangeLength);
      return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                           .contentType(MediaTypeFactory
                                            .getMediaType(urlResource)
                                            .orElse(MediaType.APPLICATION_OCTET_STREAM)).body(region);
    }
    var rangeLength = Math.min((long) 1024 * 1024, contentLength);
    var region = new ResourceRegion(urlResource, 0, rangeLength);
    return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                         .contentType(MediaTypeFactory
                                          .getMediaType(urlResource)
                                          .orElse(MediaType.APPLICATION_OCTET_STREAM)).body(region);

  }

  @GetMapping("/video")
  public ResponseEntity<ResourceRegion> getVideo(@RequestHeader HttpHeaders headers) throws IOException {
    UrlResource urlResource = new UrlResource(
        String.format("file:%s%s", resourcePathResolver.getVideoDir(), "film.mp4"));
    var contentLength = urlResource.contentLength();
    var range = headers.getRange();
    if (!range.isEmpty()) {
      var start = range.get(0).getRangeStart(contentLength);
      var end = range.get(0).getRangeEnd(contentLength);
      var rangeLength = Math.min((long) 1024 * 1024, end - start + 1);
      var region = new ResourceRegion(urlResource, start, rangeLength);
      return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                           .contentType(MediaTypeFactory
                                            .getMediaType(urlResource)
                                            .orElse(MediaType.APPLICATION_OCTET_STREAM)).body(region);
    }
    var rangeLength = Math.min((long) 1024 * 1024, contentLength);
    var region = new ResourceRegion(urlResource, 0, rangeLength);
    return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                         .contentType(MediaTypeFactory
                                          .getMediaType(urlResource)
                                          .orElse(MediaType.APPLICATION_OCTET_STREAM)).body(region);
  }

}
