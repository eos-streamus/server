package com.eos.streamus.writers;

import com.eos.streamus.models.Film;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

public class JsonFilmListWriter extends JsonArrayWriter {
  /** List of {@link com.eos.streamus.models.Film} to write as Json. */
  private final List<Film> films;

  public JsonFilmListWriter(final List<Film> films) {
    this.films = films;
  }

  @Override
  protected final JsonNode getSpecificArrayJson(final ArrayNode arrayNode) {
    for (Film film : films) {
      arrayNode.add(new JsonFilmWriter(film).getJson());
    }
    return arrayNode;
  }

}
