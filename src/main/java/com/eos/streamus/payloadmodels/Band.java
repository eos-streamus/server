package com.eos.streamus.payloadmodels;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Band {
  @NotNull
  @Size(min = 1)
  private String name;

  public String getName() {
    return name;
  }

}
