package com.eos.streamus.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public final class UserDTO extends PersonDTO {
  /** Email. */
  @NotNull
  @Email
  private String email;

  /** Username. */
  @NotNull
  private String username;

  /** Password. */
  @NotNull
  private String password;

  /** Update password if provided. */
  private String updatedPassword;

  public String getUpdatedPassword() {
    return updatedPassword;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

}
