package com.eos.streamus.models;

public interface Builder<T> {
  /**
   * @return An instance of T based off given values.
   */
  T build();
}
