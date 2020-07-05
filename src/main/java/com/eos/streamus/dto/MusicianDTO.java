package com.eos.streamus.dto;

import com.eos.streamus.payloadmodels.Person;

public class MusicianDTO {
  private Integer id;
  private String name;
  private Person person;

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Person getPerson() {
    return person;
  }

}
