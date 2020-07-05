package com.eos.streamus.payloadmodels;

import javax.validation.constraints.NotNull;

public class UserData extends Person {
  @NotNull
  private String email;
  @NotNull
  private String username;
  @NotNull
  private String password;

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
