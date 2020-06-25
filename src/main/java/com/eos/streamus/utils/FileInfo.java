package com.eos.streamus.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Class that contains FFProbe information about a file. For now, used only for duration.
 */
public class FileInfo {
  private final int duration;
  private boolean isVideo;
  private boolean isAudio;

  FileInfo(ObjectNode jsonData) {
    this.duration = jsonData.get("format").get("duration").asInt();
    for (final JsonNode stream : jsonData.get("streams")) {
      if (stream.get("codec_type").asText().equals("audio")) {
        this.isAudio = true;
      } else if (stream.get("codec_type").asText().equals("video")) {
        this.isVideo = true;
      }
    }
  }

  public final int getDuration() {
    return duration;
  }

  public final boolean isAudio() {return isAudio;}

  public final boolean isVideo() {return isVideo;}

  public final boolean isAudioOnly() {
    return isAudio && !isVideo;
  }

}
