package com.eos.streamus.models;

import java.sql.Timestamp;

abstract public class Video extends Resource {
  protected Video(Integer id, String path, String name, Timestamp createdAt, Integer duration) {
    super(id, path, name, createdAt, duration);
  }

  protected Video(String path, String name, Integer duration) {
    super(path, name, duration);
  }
}
