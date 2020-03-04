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

  public boolean equals(Object obj) {
    if (obj == null || obj.getClass() != getClass()) {
      return false;
    }
    Pair pair = (Pair) obj;
    if (pair.key.getClass() != key.getClass() || pair.value.getClass() != value.getClass()) {
      return false;
    }
    return key.equals(pair.key) && value.equals(pair.value);
  }
}
