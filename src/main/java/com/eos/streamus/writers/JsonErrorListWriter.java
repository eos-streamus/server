package com.eos.streamus.writers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

public final class JsonErrorListWriter extends JsonArrayWriter {
  /** Errors to write. */
  private final BindingResult result;

  public JsonErrorListWriter(final BindingResult result) {
    this.result = result;
  }

  @Override
  protected JsonNode getSpecificArrayJson(final ArrayNode arrayNode) {
    for (final FieldError error : result.getFieldErrors()) {
      arrayNode.add(new JsonErrorWriter(error).getJson());
    }
    for (final ObjectError error : result.getAllErrors()) {
      arrayNode.add(new JsonErrorWriter(error).getJson());
    }
    return arrayNode;
  }

}

final class JsonErrorWriter extends JsonObjectWriter {
  /** Error to write. */
  private final ObjectError error;

  JsonErrorWriter(final ObjectError error) {
    this.error = error;
  }

  @Override
  protected JsonNode getSpecificJson(final ObjectNode objectNode) {
    return objectNode.put("fieldName", error.getObjectName()).put("error", error.getDefaultMessage());
  }

}
