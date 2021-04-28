package com.eos.streamus.writers;

import com.eos.streamus.models.ResourceActivity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonResourceActivityWriter extends JsonObjectWriter {
  /** {@link com.eos.streamus.models.ResourceActivity} to write. */
  private final ResourceActivity resourceActivity;

  public JsonResourceActivityWriter(final ResourceActivity resourceActivity) {
    this.resourceActivity = resourceActivity;
  }

  /** {@inheritDoc} */
  @Override
  protected JsonNode getSpecificJson(final ObjectNode objectNode) {
    return objectNode
        .put("id", resourceActivity.getId())
        .put("pausedAt", resourceActivity.getPausedAt());
  }

}
