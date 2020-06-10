package com.eos.streamus.payloadmodels;

import javax.validation.constraints.NotNull;

public class BandMember {
  private Integer musicianId;

  private Musician musician;

  @NotNull
  private Long from;

  private Long to;

  public Long getFrom() {
    return from;
  }

  public void setFrom(Long from) {
    this.from = from;
  }

  public Long getTo() {
    return to;
  }

  public Integer getMusicianId() {
    return musicianId;
  }

  public Musician getMusician() {
    return musician;
  }

}
