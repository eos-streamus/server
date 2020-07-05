package com.eos.streamus.dto;

import com.eos.streamus.payloadmodels.SongCollection;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

public class AlbumDTO extends SongCollection {
  @NotNull
  private List<Integer> artistIds;
  @NotNull
  private Date releaseDate;

  public List<Integer> getArtistIds() {
    return artistIds;
  }

  public Date getReleaseDate() {
    return releaseDate;
  }

}
