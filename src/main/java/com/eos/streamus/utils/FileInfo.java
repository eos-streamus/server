package com.eos.streamus.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Class that contains ffprobe information about a file.
 * For now, used only for duration.
 */
public class FileInfo {
  private final int duration;
  private final int bitrate;
  private final long size;

  FileInfo(ObjectNode jsonData) {
    JsonNode format = jsonData.get("format");
    this.duration = format.get("duration").asInt();
    this.bitrate = format.get("bit_rate").asInt();
    this.size = format.get("size").asLong();
  }

  public final int getDuration() {
    return duration;
  }

  public final int getBitrate() {
    return bitrate;
  }

  public final long getSize() {
    return size;
  }
}
