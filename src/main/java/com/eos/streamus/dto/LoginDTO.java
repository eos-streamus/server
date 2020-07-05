package com.eos.streamus.dto;

import javax.validation.constraints.NotNull;

public class LoginDTO {
  @NotNull
  private String email;
  @NotNull
  private String password;

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

}
