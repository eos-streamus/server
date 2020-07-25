package com.eos.streamus.writers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class JsonTokenWriter extends JsonObjectWriter {
  /** JWT Token to write. */
  private final String token;
  public JsonTokenWriter(final String token) {
    this.token = token;
  }

  @Override
  protected JsonNode getSpecificJson(final ObjectNode objectNode) {
    return objectNode.put("token", token);
  }

}
