package com.eos.streamus.dto;

import javax.validation.constraints.NotNull;
import java.sql.Date;

public class BandMember {
  private Integer musicianId;

  private MusicianDTO musician;

  @NotNull
  private Date from;

  private Date to;

  public Date getFrom() {
    return from;
  }

  public void setFrom(Date from) {
    this.from = from;
  }

  public Date getTo() {
    return to;
  }

  public Integer getMusicianId() {
    return musicianId;
  }

  public MusicianDTO getMusician() {
    return musician;
  }

}
