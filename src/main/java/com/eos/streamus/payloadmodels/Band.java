package com.eos.streamus.payloadmodels;

import javax.validation.constraints.NotNull;

public class Band {
  @NotNull
  private String name;

  public String getName() {
    return name;
  }

}
