package com.eos.streamus.dto;

public class MusicianDTO {
  /** Id of Musician. */
  private Integer id;
  /** Name of Musician. */
  private String name;
  /** Person data of Musician. */
  private PersonDTO person;

  /** @return Id of Musician. */
  public Integer getId() {
    return id;
  }

  /** @return Name of Musician. */
  public String getName() {
    return name;
  }

  /** @return {@link PersonDTO} of Musician. */
  public PersonDTO getPerson() {
    return person;
  }

}
