package com.eos.streamus.writers;

import com.eos.streamus.models.Person;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class JsonPersonWriter extends JsonObjectWriter {
  /** {@link com.eos.streamus.models.Person} to write as Json. */
  private final Person person;

  public JsonPersonWriter(final Person person) {
    this.person = person;
  }

  /**
   * Adds Person specific properties in ObjectNode.
   * @param objectNode ObjectNode to write properties to.
   * @return Json node.
   */
  @Override
  protected JsonNode getSpecificJson(final ObjectNode objectNode) {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    objectNode.put("id", person.getId())
              .put("firstName", person.getFirstName())
              .put("lastName", person.getLastName());
    objectNode.put("dateOfBirth", person.getDateOfBirth() == null ? null : person.getDateOfBirth().getTime());
    objectNode.put("createdAt", dateFormat.format(person.getCreatedAt()));
    objectNode.put("updatedAt", dateFormat.format(person.getUpdatedAt()));
    return objectNode;
  }

}
