package com.eos.streamus.dto;

import javax.validation.constraints.NotNull;

public class SongPlaylistDTO extends SongCollectionDTO {
  @NotNull
  private Integer userId;

  public Integer getUserId() {
    return userId;
  }

}
