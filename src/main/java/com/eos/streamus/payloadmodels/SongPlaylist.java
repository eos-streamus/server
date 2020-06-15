package com.eos.streamus.payloadmodels;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

public class SongPlaylist {
  @NotNull
  private Integer userId;
  @NotNull
  private String name;
  @NotNull
  private List<Track> tracks;

  public Integer getUserId() {
    return userId;
  }

  public String getName() {
    return name;
  }

  public List<Track> getTracks() {
    return tracks;
  }
}
