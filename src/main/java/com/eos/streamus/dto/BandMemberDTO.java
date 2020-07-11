package com.eos.streamus.dto;

import javax.validation.constraints.NotNull;
import java.sql.Date;

public final class BandMemberDTO {
  /** Id of musician. */
  private Integer musicianId;
  /** {@link com.eos.streamus.dto.MusicianDTO} of this member. */
  private MusicianDTO musician;

  /** Membership from date. */
  @NotNull
  private Date from;

  /** Membership to date. */
  private Date to;

  public Date getFrom() {
    return from;
  }

  public void setFrom(final Date from) {
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
