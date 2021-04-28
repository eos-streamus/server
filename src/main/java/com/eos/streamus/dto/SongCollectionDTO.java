package com.eos.streamus.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public abstract class SongCollectionDTO {
  /**
   * Name of the SongCollection.
   */
  @NotNull
  @Size(min = 1)
  private String name;
  /**
   * {@link TrackDTO}s of the SongCollection.
   */
  @NotNull
  private List<TrackDTO> tracks;

  /**
   * @return Name of SongCollection.
   */
  public String getName() {
    return name;
  }

  /**
   * @return Tracks of SongCollection.
   */
  public List<TrackDTO> getTracks() {
    return tracks;
  }
}
