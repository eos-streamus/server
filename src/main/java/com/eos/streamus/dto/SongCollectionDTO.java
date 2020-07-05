package com.eos.streamus.dto;

import com.eos.streamus.payloadmodels.Track;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public abstract class SongCollectionDTO {
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
