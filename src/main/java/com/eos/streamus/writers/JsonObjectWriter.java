package com.eos.streamus.writers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

abstract class JsonObjectWriter implements JsonWriter {
  /** {@inheritDoc} */
  @Override
  public final JsonNode getJson() {
    final ObjectNode objectNode = new ObjectNode(new JsonWriterNodeFactory());
    return getSpecificJson(objectNode);
  }

  /**
   * Adds specific properties to the given {@link ObjectNode}.
   *
   * @param objectNode ObjectNode to add specific properties to.
   * @return Updated ObjectNode
   */
  protected abstract JsonNode getSpecificJson(ObjectNode objectNode);
}
