package com.eos.streamus.payloadmodels;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public abstract class SongCollection {
  @NotNull
  @Size(min = 1)
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
