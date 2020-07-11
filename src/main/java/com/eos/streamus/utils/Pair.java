package com.eos.streamus.utils;

import java.util.Objects;

public class Pair<K, V> {
  /** Key of pair. */
  private K key;
  /** Value of pair. */
  private V value;

  public Pair(final K key, final V value) {
    this.key = key;
    this.value = value;
  }

  public final K getKey() {
    return key;
  }

  public final V getValue() {
    return value;
  }

  public final void setKey(final K key) {
    this.key = key;
  }

  public final void setValue(final V value) {
    this.value = value;
  }

  /** @return hashcode of this Pair. */
  @Override
  public int hashCode() {
    return Objects.hash(key.hashCode(), value.hashCode());
  }

  /**
   * Tests if other object is equal to this instance.
   * @param obj Object test.
   * @return If they are equal.
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

  /** @return String representation of this pair. */
  public String toString() {
    return String.format("<%s, %s>", key, value);
  }
}
