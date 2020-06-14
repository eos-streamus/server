package com.eos.streamus.payloadmodels.validators;

import com.eos.streamus.payloadmodels.Musician;

import javax.validation.constraints.NotNull;
import java.sql.Date;

public class BandMember {
  private Integer musicianId;

  private Musician musician;

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

  public Musician getMusician() {
    return musician;
  }

}
