package com.eos.streamus.writers;

import com.eos.streamus.models.SongCollection;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonSongCollectionWriter extends JsonCollectionWriter {
  /** {@link com.eos.streamus.models.SongCollection} to write as Json. */
  private final SongCollection songCollection;

  protected JsonSongCollectionWriter(final SongCollection songCollection) {
    super(songCollection);
    this.songCollection = songCollection;
  }

  /**
   * Write {@link SongCollection.Track}s to json.
   * @param objectNode ObjectNode to write tracks to.
   * @return Json node.
   */
  @Override
  protected JsonNode getSpecificCollectionJson(final ObjectNode objectNode) {
    ArrayNode tracks = objectNode.putArray("tracks");
    for (SongCollection.Track track : songCollection.getTracks()) {
      tracks.add(new JsonTrackWriter(track).getJson());
    }
    return objectNode;
  }

}
