package com.eos.streamus.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class BandDTO {
  /** Name of the Band. */
  @NotNull
  @Size(min = 1)
  private String name;

  /** @return Name of the Band */
  public String getName() {
    return name;
  }

}
