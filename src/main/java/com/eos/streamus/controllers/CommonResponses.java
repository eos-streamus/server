package com.eos.streamus.controllers;

import com.eos.streamus.models.Resource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

interface CommonResponses {

  default ResponseEntity<JsonNode> badRequest(final String reason) {
    ObjectNode errorResponse = new ObjectNode(new ControllerObjectNodeFactory());
    errorResponse.put("reason", reason);
    return ResponseEntity.badRequest().body(errorResponse);
  }

  default ResponseEntity<JsonNode> internalServerError() {
    ObjectNode errorResponse = new ObjectNode(new ControllerObjectNodeFactory());
    errorResponse.put("reason", "Something went wrong");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  default ResponseEntity<String> internalServerErrorString() {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
  }

  default ResponseEntity<ResourceRegion> streamResource(final Resource resource,
                                                        final List<HttpRange> range,
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

  default void logException(final Exception exception) {
    Logger.getLogger(getClass().getName()).severe(exception.getMessage());
  }

  default <T> ResponseEntity<T> notFound() {
    return ResponseEntity.notFound().build();
  }

  default ResponseEntity<String> deleteFileAndResource(final Resource resource,
                                                       final Connection connection) throws SQLException {
    try {
      Files.delete(FileSystems.getDefault().getPath(resource.getPath()));
    } catch (IOException e) {
      logException(e);
      return internalServerErrorString();
    }
    resource.delete(connection);
    return ResponseEntity.ok("Resource deleted");
  }

}
