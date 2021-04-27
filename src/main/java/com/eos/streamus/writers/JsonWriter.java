package com.eos.streamus.writers;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonWriter {
  /**
   * @return {@link JsonNode} representation.
   */
  JsonNode getJson();
}
