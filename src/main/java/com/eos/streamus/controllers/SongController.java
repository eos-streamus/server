package com.eos.streamus.controllers;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Song;
import com.eos.streamus.utils.DatabaseConnection;
import com.eos.streamus.utils.FileInfo;
import com.eos.streamus.utils.ResourcePathResolver;
import com.eos.streamus.utils.ShellUtils;
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

import static com.eos.streamus.controllers.CommonResponses.badRequest;
import static com.eos.streamus.controllers.CommonResponses.streamResource;

@RestController
public class SongController {
  private static final long MAX_AUDIO_CHUNK_SIZE = (long) 1024 * 1024;

  private static final String[] AUDIO_MIME_TYPES = {
      "audio/wav", "audio/mpeg", "audio/mp4", "audio/aac", "audio/aacp", "audio/ogg", "audio/webm", "audio/ogg",
      "audio/webm", "audio/flac", "audio/og"
  };

  private final ResourcePathResolver resourcePathResolver;
  private final DatabaseConnection databaseConnection;

  public SongController(@Autowired final ResourcePathResolver resourcePathResolver,
                        @Autowired final DatabaseConnection databaseConnection) {
    this.resourcePathResolver = resourcePathResolver;
    this.databaseConnection = databaseConnection;
  }

  @PostMapping("/song")
  public ResponseEntity<Object> postSong(@RequestParam("file") MultipartFile file,
                                         @RequestParam("name") String name) throws IOException {

    if (file.getContentType() == null) {
      return badRequest("No specified mime type");
    }
    boolean acceptableMimeType = false;
    for (String type : AUDIO_MIME_TYPES) {
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

}
