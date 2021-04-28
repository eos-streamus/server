package com.eos.streamus.dto;

import javax.validation.constraints.NotNull;

public class LoginDTO {
  /** Login email. */
  @NotNull
  private String email;
  /** Login password. */
  @NotNull
  private String password;

  /** @return Email of login data. */
  public String getEmail() {
    return email;
  }

  /** @return Password of login data. */
  public String getPassword() {
    return password;
  }

}
