package com.eos.streamus.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public final class TokensDTO {
  /** Refresh JWT. */
  @NotNull
  @NotBlank
  private final String refreshToken;
  /** Session JWT. */
  @NotNull
  @NotBlank
  private final String sessionToken;

  public TokensDTO(final String refreshToken, final String sessionToken) {
    this.refreshToken = refreshToken;
    this.sessionToken = sessionToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public String getSessionToken() {
    return sessionToken;
  }

}
