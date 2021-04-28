package com.eos.streamus.writers;

import com.eos.streamus.models.Album;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

public class JsonAlbumListWriter extends JsonArrayWriter {
  /**
   * List of {@link Album}s to write.
   */
  private final List<Album> albums;

  public JsonAlbumListWriter(final List<Album> albums) {
    this.albums = albums;
  }

  /** {@inheritDoc} */
  @Override
  protected JsonNode getSpecificArrayJson(final ArrayNode arrayNode) {
    for (Album album : albums) {
      arrayNode.add(new JsonAlbumWriter(album).getJson());
    }
    return arrayNode;
  }

}
