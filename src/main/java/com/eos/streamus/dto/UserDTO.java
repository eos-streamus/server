package com.eos.streamus.dto;

import javax.annotation.Nullable;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public final class UserDTO extends PersonDTO {
  /** Email of User. */
  @NotNull
  private String email;

  /** Username of User. */
  @NotNull
  private String username;

  /** Password of User. */
  @NotNull
  private String password;

  /** Potential updated password. */
  private String updatedPassword;

  /** @return Updated password. */
  @Nullable
  public String getUpdatedPassword() {
    return updatedPassword;
  }

  /** @return Email of User. */
  public String getEmail() {
    return email;
  }

  /**
   * Set email of user.
   *
   * @param email Email to set.
   */
  public void setEmail(final String email) {
    this.email = email;
  }

  /** @return Username of User. */
  @NotNull
  public String getUsername() {
    return username;
  }

  /**
   * Set username.
   *
   * @param username Username to set.
   */
  public void setUsername(final String username) {
    this.username = username;
  }

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

}
