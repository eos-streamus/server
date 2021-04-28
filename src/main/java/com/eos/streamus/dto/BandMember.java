package com.eos.streamus.dto;

import javax.validation.constraints.NotNull;
import java.sql.Date;

public class BandMember {
  /** Id of Musician. */
  private Integer musicianId;
  /** Musician DTO representing member. */
  private MusicianDTO musician;

  /** From date. */
  @NotNull
  private Date from;

  /** To date. */
  private Date to;

  /** @return From date. */
  public Date getFrom() {
    return from;
  }

  /**
   * Set from date.
   *
   * @param from date to set.
   */
  public void setFrom(final Date from) {
    this.from = from;
  }

  /** @return To date. */
  public Date getTo() {
    return to;
  }

  /** @return Musician id. */
  public Integer getMusicianId() {
    return musicianId;
  }

  /** @return Band member's {@link MusicianDTO}. */
  public MusicianDTO getMusician() {
    return musician;
  }

}
