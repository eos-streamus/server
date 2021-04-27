package com.eos.streamus.writers;

import com.eos.streamus.models.Artist;
import com.eos.streamus.models.Band;
import com.eos.streamus.models.Musician;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

public class JsonArtistListWriter extends JsonArrayWriter {
  private final List<Artist> artists;

  public JsonArtistListWriter(final List<Artist> artists) {
    this.artists = artists;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected JsonNode getSpecificArrayJson(final ArrayNode arrayNode) {
    for (Artist artist : artists) {
      if (artist instanceof Band) {
        arrayNode.add(new JsonBandWriter((Band) artist).getJson());
      } else {
        arrayNode.add(new JsonMusicianWriter((Musician) artist).getJson());
      }
    }
    return arrayNode;
  }

}
