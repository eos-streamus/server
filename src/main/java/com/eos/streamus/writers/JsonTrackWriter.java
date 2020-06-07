package com.eos.streamus.writers;

import com.eos.streamus.models.SongCollection;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonTrackWriter extends JsonSongWriter {
  private final SongCollection.Track track;
  public JsonTrackWriter(final SongCollection.Track track) {
    super(track.getValue());
    this.track = track;
  }

  @Override
  protected ObjectNode addSpecificJsonAttributes(final ObjectNode objectNode) {
    super.addSpecificJsonAttributes(objectNode);
    objectNode.put("trackNumber", track.getKey());
    return objectNode;
  }

}
