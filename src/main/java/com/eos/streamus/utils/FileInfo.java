package com.eos.streamus.utils;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Class that contains ffprobe information about a file.
 * For now, used only for duration.
 */
public class FileInfo {
  private final int duration;

  FileInfo(ObjectNode jsonData) {
    this.duration = jsonData.get("format").get("duration").asInt();
  }

  public final int getDuration() {
    return duration;
  }
}
