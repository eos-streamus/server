package com.eos.streamus.writers;

import com.eos.streamus.models.Person;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonPersonWriter extends JsonObjectWriter {
  private final Person person;

  public JsonPersonWriter(final Person person) {
    this.person = person;
  }

  @Override
  protected JsonNode getSpecificJson(final ObjectNode objectNode) {
    objectNode.put("id", person.getId())
              .put("firstName", person.getFirstName())
              .put("lastName", person.getLastName());
    objectNode.put("dateOfBirth", person.getDateOfBirth() == null ? null : person.getDateOfBirth().getTime());
    objectNode.put("createdAt", person.getCreatedAt().getTime());
    objectNode.put("updatedAt", person.getUpdatedAt().getTime());
    return objectNode;
  }

}
