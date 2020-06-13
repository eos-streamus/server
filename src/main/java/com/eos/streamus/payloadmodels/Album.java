package com.eos.streamus.payloadmodels;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

public class Album {
  @NotNull
  private List<Integer> artistIds;
  @NotNull
  private String name;
  @NotNull
  private Date releaseDate;
  @NotNull
  private List<Track> tracks;

  public List<Integer> getArtistIds() {
    return artistIds;
  }

  public String getName() {
    return name;
  }

  public Date getReleaseDate() {
    return releaseDate;
  }

  public List<Track> getTracks() {
    return tracks;
  }

}
