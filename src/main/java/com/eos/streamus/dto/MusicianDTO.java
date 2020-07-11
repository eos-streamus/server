package com.eos.streamus.dto;

public final class MusicianDTO {
  /** Musician id. */
  private Integer id;
  /** Musician name. */
  private String name;
  /** Musician person. */
  private PersonDTO person;

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public PersonDTO getPerson() {
    return person;
  }

}
