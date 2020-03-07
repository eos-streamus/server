package com.eos.streamus.utils;

public class Pair<K, V> {
  private K key;
  private V value;

  public Pair(K key, V value) {
    this.key = key;
    this.value = value;
  }

  public K getKey() {
    return key;
  }

  public V getValue() {
    return value;
  }

  public void setKey(K key) {
    this.key = key;
  }

  public void setValue(V value) {
    this.value = value;
  }

  @Override
  public int hashCode() {
    return key.hashCode() * 31 + value.hashCode();
  }

  public boolean equals(Object obj) {
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

  public String toString() {
    return String.format("<%s, %s>", key, value);
  }
}
