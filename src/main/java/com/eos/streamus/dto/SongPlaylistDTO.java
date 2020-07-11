package com.eos.streamus.dto;

import javax.validation.constraints.NotNull;

public final class SongPlaylistDTO extends SongCollectionDTO {
  /** User id. */
  @NotNull
  private Integer userId;

  public Integer getUserId() {
    return userId;
  }

}
