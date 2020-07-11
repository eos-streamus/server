package com.eos.streamus.dto;

import javax.validation.constraints.NotNull;

public final class LoginDTO {
  /** Login email. */
  @NotNull
  private String email;
  /** Login password. */
  @NotNull
  private String password;

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

}
