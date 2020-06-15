package com.eos.streamus.payloadmodels;

import javax.validation.constraints.NotNull;
import java.util.List;

public abstract class SongCollection {
  @NotNull
  private String name;
  @NotNull
  private List<Track> tracks;

  public String getName() {
    return name;
  }

  public List<Track> getTracks() {
    return tracks;
  }
}
