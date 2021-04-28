package com.eos.streamus.writers;

import com.eos.streamus.models.CollectionActivity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonCollectionActivityWriter extends JsonObjectWriter {
  /** {@link CollectionActivity} to write. */
  private final CollectionActivity collectionActivity;

  public JsonCollectionActivityWriter(final CollectionActivity collectionActivity) {
    this.collectionActivity = collectionActivity;
  }

  /** {@inheritDoc} */
  @Override
  protected JsonNode getSpecificJson(final ObjectNode objectNode) {
    objectNode.put("id", collectionActivity.getId());
    objectNode.put("collectionId", collectionActivity.getCollection().getId());
    ArrayNode resourceActivities = objectNode.putArray("resourceActivities");
    for (var entry : collectionActivity.getContent()) {
      ObjectNode resourceActivityNode = new ObjectNode(new JsonWriterNodeFactory());
      resourceActivityNode.put("num", entry.getKey());
      JsonResourceWriterFactory.getWriterFor(entry.getValue().getKey())
          .getSpecificJson(resourceActivityNode.putObject("resource"));
      var resourceActivity = entry.getValue().getValue();
      resourceActivityNode.put("resourceActivityId", resourceActivity == null ? null : resourceActivity.getId());
      resourceActivityNode.put("pausedAt", resourceActivity == null ? null : resourceActivity.getPausedAt());
      resourceActivities.add(resourceActivityNode);
    }
    return objectNode;
  }

}
