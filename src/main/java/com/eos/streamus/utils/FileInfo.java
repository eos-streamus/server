package com.eos.streamus.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Class that contains FFProbe information about a file. For now, used only for duration.
 */
public final class FileInfo {
  /** Duration of media file. */
  private final int duration;
  /** If File is video file. */
  private boolean isVideo;
  /** If file is audio file (caution: Video files are often also audio files). */
  private boolean isAudio;

  FileInfo(final ObjectNode jsonData) {
    this.duration = jsonData.get("format").get("duration").asInt();
    for (final JsonNode stream : jsonData.get("streams")) {
      if (stream.get("codec_type").asText().equals("audio")) {
        this.isAudio = true;
      } else if (stream.get("codec_type").asText().equals("video")) {
        this.isVideo = true;
      }
    }
  }

  public int getDuration() {
    return duration;
  }

  public boolean isAudio() { return isAudio; }

  public boolean isVideo() { return isVideo; }

  public boolean isAudioOnly() {
    return isAudio && !isVideo;
  }

}
