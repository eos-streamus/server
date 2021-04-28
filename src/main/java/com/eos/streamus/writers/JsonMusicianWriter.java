package com.eos.streamus.writers;

import com.eos.streamus.models.Musician;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonMusicianWriter extends JsonArtistWriter {
  /**
   * {@link Musician} to write.
   */
  private final Musician musician;

  public JsonMusicianWriter(final Musician musician) {
    super(musician);
    this.musician = musician;
  }

  /** {@inheritDoc} */
  @Override
  protected JsonNode addSpecificArtistJson(final ObjectNode objectNode) {
    if (musician.getPerson() != null) {
      objectNode.set("person", new JsonPersonWriter(musician.getPerson()).getJson());
    }
    return objectNode;
  }

}
