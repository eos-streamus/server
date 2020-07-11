package com.eos.streamus.dto;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

public final class AlbumDTO extends SongCollectionDTO {
  /** Artist ids. */
  @NotNull
  private List<Integer> artistIds;
  /** Release date of album. */
  @NotNull
  private Date releaseDate;

  public List<Integer> getArtistIds() {
    return artistIds;
  }

  public Date getReleaseDate() {
    return releaseDate;
  }

}
