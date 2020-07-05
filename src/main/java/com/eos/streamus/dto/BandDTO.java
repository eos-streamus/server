package com.eos.streamus.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class BandDTO {
  @NotNull
  @Size(min = 1)
  private String name;

  public String getName() {
    return name;
  }

}
