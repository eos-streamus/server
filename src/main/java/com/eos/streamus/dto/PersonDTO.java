package com.eos.streamus.dto;

import javax.validation.constraints.NotNull;

public class PersonDTO {
  /** Id of person. */
  private Integer id;

  /** First name of Person. */
  @NotNull
  private String firstName;

  /** Last name of Person. */
  @NotNull
  private String lastName;

  /** Date of birth of person as String (should be in format yyyy-MM-dd). */
  private String dateOfBirth;

  public final Integer getId() {
    return id;
  }

  public final String getFirstName() {
    return firstName;
  }

  public final String getLastName() {
    return lastName;
  }

  public final String getDateOfBirth() {
    return dateOfBirth;
  }

}
