package com.eos.streamus.dto;

import javax.validation.constraints.NotNull;

public class PersonDTO {
  private Integer id;

  @NotNull
  private String firstName;

  @NotNull
  private String lastName;

  private Long dateOfBirth;

  public Integer getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public Long getDateOfBirth() {
    return dateOfBirth;
  }

}
