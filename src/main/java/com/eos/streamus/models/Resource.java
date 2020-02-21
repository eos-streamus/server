package com.eos.streamus.models;

import com.eos.streamus.exceptions.NotPersistedException;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

abstract class Resource implements Entity, SavableEntity {
  private Integer id;
  private String path;
  private String name;
  private Timestamp createdAt;
  private Integer duration;

  protected Resource(Integer id, String path, String name, Timestamp createdAt, Integer duration) {
    this(path, name, duration);
    this.createdAt = createdAt;
    this.id = id;
  }

  protected Resource(String path, String name, Integer duration) {
    this.path = path;
    this.name = name;
    this.duration = duration;
  }

  @Override
  public String tableName() {
    return "Resource";
  }

  @Override
  public String primaryKeyName() {
    return "id";
  }

  public Integer getId() {
    return this.id;
  }

  protected void setId(Integer id) {
    this.id = id;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Timestamp timestamp) {
    this.createdAt = timestamp;
  }

  public Integer getDuration() {
    return duration;
  }

  public void setDuration(Integer duration) {
    this.duration = duration;
  }

  @Override
  public void save(Connection connection) throws SQLException {
    if (this.id == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s(?::varchar(1041), ?::varchar(200), ?)", creationFunctionName()))) {
        preparedStatement.setString(1, getPath());
        preparedStatement.setString(2, getName());
        preparedStatement.setInt(3, getDuration());
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          if (!resultSet.next()) {
            throw new SQLException("Could not execute statement");
          }
          this.setId(resultSet.getInt("id"));
          this.setCreatedAt(resultSet.getTimestamp("createdAt"));
        }
      }
    } else {
      try (PreparedStatement preparedStatement = connection.prepareStatement("update resource set path = ?, name = ?, duration = ? where id = ?;")) {
        preparedStatement.setString(1, path);
        preparedStatement.setString(2, name);
        preparedStatement.setLong(3, duration);
        preparedStatement.setInt(4, id);
        preparedStatement.execute();
      }
    }
  }

  @Override
  public String toString() {
    DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");
    return String.format("{id: %d, path: %s, name: %s, duration: %s, createdAt: %s}", id, path, name, duration, dateFormat.format(createdAt));
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || o.getClass() != getClass()) {
      return false;
    }
    Resource oResource = (Resource) o;
    return
      oResource.id.equals(id) &&
        oResource.path.equals(path) &&
        oResource.name.equals(name) &&
        oResource.createdAt.equals(createdAt) &&
        oResource.duration.equals(duration);
  }
}
