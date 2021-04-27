package com.eos.streamus.writers;

import com.eos.streamus.models.Film;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonFilmWriter extends JsonResourceWriter {

  public JsonFilmWriter(final Film film) {
    super(film);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ObjectNode addSpecificJsonAttributes(final ObjectNode objectNode) {
    return objectNode;
  }

}
