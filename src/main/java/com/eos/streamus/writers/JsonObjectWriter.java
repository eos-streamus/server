package com.eos.streamus.writers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

abstract class JsonObjectWriter implements JsonWriter {
  @Override
  public final JsonNode getJson() {
    final ObjectNode objectNode = new ObjectNode(new JsonWriterNodeFactory());
    return getSpecificJson(objectNode);
  }

  protected abstract JsonNode getSpecificJson(ObjectNode objectNode);
}
