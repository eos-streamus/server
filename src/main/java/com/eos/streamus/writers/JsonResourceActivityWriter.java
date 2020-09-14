package com.eos.streamus.writers;

import com.eos.streamus.models.ResourceActivity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonResourceActivityWriter extends JsonObjectWriter {
  private final ResourceActivity resourceActivity;

  public JsonResourceActivityWriter(final ResourceActivity resourceActivity) {
    this.resourceActivity = resourceActivity;
  }

  @Override
  protected JsonNode getSpecificJson(final ObjectNode objectNode) {
    return objectNode.put("pausedAt", resourceActivity.getPausedAt());
  }

}
