package com.eos.streamus.utils;

import java.util.Objects;

public class Pair<K, V> {
  /**
   * Key of Pair.
   */
  private K key;
  /**
   * Value of Pair.
   */
  private V value;

  public Pair(final K key, final V value) {
    this.key = key;
    this.value = value;
  }

  /**
   * @return Key of Pair.
   */
  public K getKey() {
    return key;
  }

  /**
   * @return Value of Pair.
   */
  public V getValue() {
    return value;
  }

  /**
   * Set key of Pair.
   *
   * @param key Key to set.
   */
  public void setKey(final K key) {
    this.key = key;
  }

  /**
   * Set Value of Pair.
   *
   * @param value Value to set.
   */
  public void setValue(final V value) {
    this.value = value;
  }

  /**
   * @return HashCode combined from key hashCode and value hashCode.
   */
  @Override
  public int hashCode() {
    return Objects.hash(key.hashCode(), value.hashCode());
  }

  /**
   * Returns whether the given Object is equal.
   * Equal if:
   * - Not null
   * - Same class
   * - Same value (both null or equal)
   * - Same key (both null or equal)
   *
   * @param obj Object to compare.
   * @return True if all conditions are met.
   */
  public boolean equals(final Object obj) {
    if (obj == null || obj.getClass() != getClass()) {
      return false;
    }
    Pair<?, ?> pair = (Pair<?, ?>) obj;
    if (pair.value == null && value != null || pair.value != null && value == null) {
      return false;
    }
    if (value == null) {
      return key.equals(pair.key);
    }
    return key.equals(pair.key) && value.equals(pair.value);
  }

  /**
   * {@inheritDoc}
   */
  public String toString() {
    return String.format("<%s, %s>", key, value);
  }
}
