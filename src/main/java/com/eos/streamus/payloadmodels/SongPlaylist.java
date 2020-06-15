package com.eos.streamus.payloadmodels;

import javax.validation.constraints.NotNull;

public class SongPlaylist extends SongCollection {
  @NotNull
  private Integer userId;

  public Integer getUserId() {
    return userId;
  }

}
