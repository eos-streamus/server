package com.eos.streamus.dto;

import javax.validation.constraints.NotNull;

public class PersonDTO {
  /**
   * Id of Person.
   */
  private Integer id;

  /**
   * First name of Person.
   */
  @NotNull
  private String firstName;

  /**
   * Last name of Person.
   */
  @NotNull
  private String lastName;

  /**
   * Date of birth of Person.
   */
  private String dateOfBirth;

  /**
   * @return Id of Person.
   */
  public Integer getId() {
    return id;
  }

  /**
   * @return First name of Person.
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * @return Last name of person.
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * @return Date of birth of Person.
   */
  public String getDateOfBirth() {
    return dateOfBirth;
  }

}
