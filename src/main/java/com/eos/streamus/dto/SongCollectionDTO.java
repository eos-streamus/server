package com.eos.streamus.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public abstract class SongCollectionDTO {
  /** Name of SongCollection. */
  @NotNull
  @Size(min = 1)
  private String name;
  /** {@link com.eos.streamus.dto.TrackDTO}s of collection. */
  @NotNull
  private List<TrackDTO> tracks;

  public final String getName() {
    return name;
  }

  public final List<TrackDTO> getTracks() {
    return tracks;
  }
}
