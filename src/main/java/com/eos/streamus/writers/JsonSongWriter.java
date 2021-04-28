package com.eos.streamus.writers;

import com.eos.streamus.models.Resource;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonSongWriter extends JsonResourceWriter {
  public JsonSongWriter(final Resource resource) {
    super(resource);
  }

  /** {@inheritDoc} */
  @Override
  protected ObjectNode addSpecificJsonAttributes(final ObjectNode objectNode) {
    return objectNode;
  }

}
