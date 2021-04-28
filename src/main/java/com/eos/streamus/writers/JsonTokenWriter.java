package com.eos.streamus.writers;

import com.eos.streamus.dto.TokensDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class JsonTokenWriter extends JsonObjectWriter {
  /** Session JWT to write. */
  private final TokensDTO tokensDTO;

  public JsonTokenWriter(final TokensDTO tokensDTO) {
    this.tokensDTO = tokensDTO;
  }

  @Override
  protected JsonNode getSpecificJson(final ObjectNode objectNode) {
    return objectNode
        .put("sessionToken", tokensDTO.getSessionToken())
        .put("refreshToken", tokensDTO.getRefreshToken());
  }

}
