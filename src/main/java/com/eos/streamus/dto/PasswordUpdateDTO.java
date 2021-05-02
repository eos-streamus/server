package com.eos.streamus.dto;

import javax.validation.constraints.NotNull;

public class PasswordUpdateDTO {

  /** Password of User. */
  @NotNull
  private String password;

  /** Potential updated password. */
  @NotNull
  private String updatedPassword;

  /** @return User password. */
  public String getPassword() {
    return password;
  }

  /**
   * Set user password.
   *
   * @param password Password to set.
   */
  public void setPassword(final String password) {
    this.password = password;
  }

  /** @return User password. */
  public String getUpdatedPassword() {
    return updatedPassword;
  }

  /**
   * Set user password.
   *
   * @param updatedPassword Updated password to set.
   */
  public void setUpdatedPassword(final String updatedPassword) {
    this.updatedPassword = updatedPassword;
  }
}
