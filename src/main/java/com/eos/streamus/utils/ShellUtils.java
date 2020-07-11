package com.eos.streamus.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class ShellUtils {
  private ShellUtils() {}
  /** Command to be used by FFProbe to get file information. */
  private static final String FFPROBE_COMMAND = "ffprobe -v quiet -print_format json -show_format -show_streams";

  public static FileInfo getResourceInfo(final String path) throws IOException {
    Process process = Runtime.getRuntime().exec(String.format("%s \"%s\"", FFPROBE_COMMAND, path));
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

    String line;
    StringBuilder output = new StringBuilder();

    while ((line = reader.readLine()) != null) {
      output.append(line);
    }
    return new FileInfo(new ObjectMapper().readValue(output.toString(), ObjectNode.class));
  }

}
