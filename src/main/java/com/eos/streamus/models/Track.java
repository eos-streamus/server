package com.eos.streamus.models;

import com.eos.streamus.utils.Pair;

public class Track extends Pair<Integer, Song> {
  public Track(Integer trackNumber, Song song) {
    super(trackNumber, song);
  }

  @Override
  public int hashCode() {
    return getKey() * 31 + getValue().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || o.getClass() != getClass()) {
      return false;
    }
    Track track = (Track) o;
    return track.getValue().equals(getValue()) && track.getKey().equals(getKey());
  }
}
