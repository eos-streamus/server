package com.eos.streamus.writers;

import com.eos.streamus.models.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonUserWriter extends JsonPersonWriter {
  private final User user;
  public JsonUserWriter(final User user) {
    super(user);
    this.user = user;
  }

  @Override
  protected JsonNode getSpecificJson(final ObjectNode objectNode) {
    super.getSpecificJson(objectNode);
    return objectNode.put("email", user.getEmail()).put("username", user.getUsername());
  }

}
