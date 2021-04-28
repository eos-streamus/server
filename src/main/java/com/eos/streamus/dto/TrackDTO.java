package com.eos.streamus.dto;

public class TrackDTO {
  /** Track number. */
  private int trackNumber;
  /** {@link com.eos.streamus.models.Song} id. */
  private int songId;

  /** @return Track number. */
  public int getTrackNumber() {
    return trackNumber;
  }

  /** @return {@link com.eos.streamus.models.Song} id. */
  public int getSongId() {
    return songId;
  }

}
