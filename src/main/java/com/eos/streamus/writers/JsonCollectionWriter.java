package com.eos.streamus.writers;

import com.eos.streamus.models.Collection;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

abstract class JsonCollectionWriter extends JsonObjectWriter {
  private final Collection collection;

  protected JsonCollectionWriter(final Collection collection) {
    this.collection = collection;
  }

  @Override
  protected JsonNode getSpecificJson(final ObjectNode objectNode) {
    objectNode.put("id", collection.getId());
    objectNode.put("name", collection.getName());
    objectNode.put("createdAt", collection.getCreatedAt().getTime());
    objectNode.put("updatedAt", collection.getUpdatedAt().getTime());
    return getSpecificCollectionJson(objectNode);
  }

  protected abstract JsonNode getSpecificCollectionJson(final ObjectNode objectNode);

}
