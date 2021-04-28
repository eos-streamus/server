package com.eos.streamus.writers;

import com.eos.streamus.models.Album;
import com.eos.streamus.models.Artist;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

abstract class JsonArtistWriter extends JsonObjectWriter {
  /** {@link Artist} to write. */
  private final Artist artist;

  JsonArtistWriter(final Artist artist) {
    this.artist = artist;
  }

  /** {@inheritDoc} */
  @Override
  protected JsonNode getSpecificJson(final ObjectNode objectNode) {
    objectNode.put("id", artist.getId());
    if (artist.getName() != null) {
      objectNode.put("name", artist.getName());
    }
    ArrayNode albums = objectNode.putArray("albums");
    for (Album album : artist.getAlbums()) {
      albums.add(album.getId());
    }
    return addSpecificArtistJson(objectNode);
  }

  /**
   * Write specific {@link Artist} attributes to given ObjectNode.
   *
   * @param objectNode ObjectNode to update.
   * @return updated ObjectNode.
   */
  protected abstract JsonNode addSpecificArtistJson(ObjectNode objectNode);

}
