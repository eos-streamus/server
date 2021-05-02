package com.eos.streamus.writers;

import com.eos.streamus.models.Collection;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

abstract class JsonCollectionWriter extends JsonObjectWriter {
  /** {@link Collection} to write. */
  private final Collection collection;

  protected JsonCollectionWriter(final Collection collection) {
    this.collection = collection;
  }

  /** {@inheritDoc} */
  @Override
  protected JsonNode getSpecificJson(final ObjectNode objectNode) {
    return getSpecificCollectionJson(objectNode.put("id", collection.getId())
        .put("name", collection.getName())
        .put("createdAt", collection.getCreatedAt().getTime())
        .put("updatedAt", collection.getUpdatedAt().getTime())
    );
  }

  protected abstract JsonNode getSpecificCollectionJson(ObjectNode objectNode);

}
