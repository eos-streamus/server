package com.eos.streamus.writers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

abstract class JsonArrayWriter implements JsonWriter {
  @Override
  public final JsonNode getJson() {
    ArrayNode arrayNode = new ArrayNode(new JsonWriterNodeFactory());
    return getSpecificArrayJson(arrayNode);
  }

  /**
   * Add specific properties to given {@link ArrayNode}.
   *
   * @param arrayNode ArrayNode to update.
   * @return Updated ArrayNode.
   */
  protected abstract JsonNode getSpecificArrayJson(ArrayNode arrayNode);

}
