package com.eos.streamus.dto;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

public class AlbumDTO extends SongCollectionDTO {
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
