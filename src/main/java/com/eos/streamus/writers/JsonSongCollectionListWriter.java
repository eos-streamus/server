package com.eos.streamus.writers;

import com.eos.streamus.models.SongCollection;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

public class JsonSongCollectionListWriter extends JsonArrayWriter {
  /** {@link SongCollection} subclass instance to write. */
  private final List<? extends SongCollection> collections;

  public JsonSongCollectionListWriter(final List<? extends SongCollection> collections) {
    this.collections = collections;
  }

  /** {@inheritDoc} */
  @Override
  protected JsonNode getSpecificArrayJson(final ArrayNode arrayNode) {
    for (SongCollection collection : collections) {
      arrayNode.add(new JsonSongCollectionWriter(collection).getJson());
    }
    return arrayNode;
  }

}
