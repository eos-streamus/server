package com.eos.streamus.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Class that contains FFProbe information about a file. For now, used only for duration.
 */
public class FileInfo {
  /**
   * Duration.
   */
  private final int duration;
  /**
   * Has video track.
   */
  private boolean isVideo;
  /**
   * Has audio track.
   */
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

  /**
   * @return File duration.
   */
  public final int getDuration() {
    return duration;
  }

  /**
   * @return If file has audio content.
   */
  public final boolean isAudio() {
    return isAudio;
  }

  /**
   * @return If file is a video.
   */
  public final boolean isVideo() {
    return isVideo;
  }

  /**
   * @return If file is exclusively audio.
   */
  public final boolean isAudioOnly() {
    return isAudio && !isVideo;
  }

}
