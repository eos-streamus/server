package com.eos.streamus.writers;

import com.eos.streamus.models.Person;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class JsonPersonWriter extends JsonObjectWriter {
  /** {@link Person} to write. */
  private final Person person;

  public JsonPersonWriter(final Person person) {
    this.person = person;
  }

  /** {@inheritDoc} */
  @Override
  protected JsonNode getSpecificJson(final ObjectNode objectNode) {
    DateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    return objectNode.put("id", person.getId())
        .put("firstName", person.getFirstName())
        .put("lastName", person.getLastName())
        .put("dateOfBirth", person.getDateOfBirth() == null ? null : dateFormat.format(person.getDateOfBirth()))
        .put("createdAt", timestampFormat.format(person.getCreatedAt()))
        .put("updatedAt", timestampFormat.format(person.getUpdatedAt()));
  }

}
