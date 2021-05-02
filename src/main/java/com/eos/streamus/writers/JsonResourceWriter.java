package com.eos.streamus.writers;

import com.eos.streamus.models.Resource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

abstract class JsonResourceWriter extends JsonObjectWriter {
  /** {@link Resource} to write. */
  private final Resource resource;

  JsonResourceWriter(final Resource resource) {
    this.resource = resource;
  }

  /** {@inheritDoc} */
  @Override
  protected final JsonNode getSpecificJson(final ObjectNode objectNode) {
    return addSpecificJsonAttributes(objectNode.put("id", resource.getId())
        .put("name", resource.getName())
        .put("duration", resource.getDuration()));
  }

  /** {@inheritDoc} */
  protected abstract ObjectNode addSpecificJsonAttributes(ObjectNode objectNode);

}
