package com.eos.streamus.dto;

import javax.validation.constraints.NotNull;

public class SongPlaylistDTO extends SongCollectionDTO {
  /** Owner User id. */
  @NotNull
  private Integer userId;

  /** @return Owner User id. */
  public Integer getUserId() {
    return userId;
  }

}
