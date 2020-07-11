package com.eos.streamus.writers;

import com.eos.streamus.models.SongCollection;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

public class JsonSongCollectionListWriter extends JsonArrayWriter {
  /** List of {@link com.eos.streamus.models.SongCollection}s to write as Json. */
  private final List<? extends SongCollection> collections;

  public JsonSongCollectionListWriter(final List<? extends SongCollection> collections) {
    this.collections = collections;
  }

  /**
   * Writes the list of {@link SongCollection}s to json.
   * @param arrayNode node to write collections to.
   * @return Json node.
   */
  @Override
  protected JsonNode getSpecificArrayJson(final ArrayNode arrayNode) {
    for (SongCollection collection : collections) {
      arrayNode.add(new JsonSongCollectionWriter(collection).getJson());
    }
    return arrayNode;
  }

}
