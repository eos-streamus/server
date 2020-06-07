package com.eos.streamus.writers;

import com.eos.streamus.models.Song;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonSongWriter extends JsonResourceWriter {

  public JsonSongWriter(final Song song) {
    super(song);
  }

  @Override
  protected ObjectNode addSpecificJsonAttributes(final ObjectNode objectNode) {
    return objectNode;
  }

}
