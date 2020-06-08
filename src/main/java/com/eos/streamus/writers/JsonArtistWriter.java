package com.eos.streamus.writers;

import com.eos.streamus.models.Artist;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

abstract class JsonArtistWriter extends JsonObjectWriter {
  private final Artist artist;

  public JsonArtistWriter(final Artist artist) {
    this.artist = artist;
  }

  @Override
  protected JsonNode getSpecificJson(final ObjectNode objectNode) {
    objectNode.put("id", artist.getId());
    if (artist.getName() != null) {
      objectNode.put("name", artist.getName());
    }
    return addSpecificArtistJson(objectNode);
  }

  protected abstract JsonNode addSpecificArtistJson(final ObjectNode objectNode);

}
