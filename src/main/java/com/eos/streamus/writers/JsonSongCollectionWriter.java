package com.eos.streamus.writers;

import com.eos.streamus.models.SongCollection;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class JsonSongCollectionWriter extends JsonCollectionWriter {
  /** {@link com.eos.streamus.models.SongCollection} to write. */
  private final SongCollection songCollection;

  protected JsonSongCollectionWriter(final SongCollection songCollection) {
    super(songCollection);
    this.songCollection = songCollection;
  }

  /**
   * Adds specific properties to given {@link ObjectNode}.
   *
   * @param objectNode ObjectNode to update
   * @return Updated ObjectNode
   */
  @Override
  protected JsonNode getSpecificCollectionJson(final ObjectNode objectNode) {
    ArrayNode tracks = objectNode.putArray("tracks");
    for (SongCollection.Track track : songCollection.getTracks()) {
      tracks.add(new JsonTrackWriter(track).getJson());
    }
    return getSpecificSongCollectionJson(objectNode);
  }

  /**
   * Add Specific {@link SongCollection} data to result ObjectNode.
   *
   * @param objectNode ObjectNode to write to.
   * @return The updated ObjectNode.
   */
  protected abstract ObjectNode getSpecificSongCollectionJson(ObjectNode objectNode);

}
