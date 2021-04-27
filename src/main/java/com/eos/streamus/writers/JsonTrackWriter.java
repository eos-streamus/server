package com.eos.streamus.writers;

import com.eos.streamus.models.SongCollection;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonTrackWriter extends JsonResourceWriter {
  /**
   * {@link com.eos.streamus.models.SongCollection.Track} to write.
   */
  private final SongCollection.Track track;

  public JsonTrackWriter(final SongCollection.Track track) {
    super(track.getValue());
    this.track = track;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ObjectNode addSpecificJsonAttributes(final ObjectNode objectNode) {
    objectNode.put("trackNumber", track.getKey());
    return objectNode;
  }

}
