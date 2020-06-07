package com.eos.streamus.writers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public abstract class JsonArrayWriter implements JsonWriter {
  @Override
  public final JsonNode getJson() {
    ArrayNode arrayNode = new ArrayNode(new JsonWriterNodeFactory());
    return getSpecificArrayJson(arrayNode);
  }

  protected abstract JsonNode getSpecificArrayJson(final ArrayNode arrayNode);

}
