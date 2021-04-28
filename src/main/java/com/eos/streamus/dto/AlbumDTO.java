package com.eos.streamus.dto;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

public class AlbumDTO extends SongCollectionDTO {
  /** Ids of contributing {@link com.eos.streamus.models.Artist}s. */
  @NotNull
  private List<Integer> artistIds;
  /** Release date of {@link com.eos.streamus.models.Album}. */
  @NotNull
  private Date releaseDate;

  /** @return List of Artist ids of the Album. */
  public List<Integer> getArtistIds() {
    return artistIds;
  }

  /** @return Release date of Album. */
  public Date getReleaseDate() {
    return releaseDate;
  }

}
