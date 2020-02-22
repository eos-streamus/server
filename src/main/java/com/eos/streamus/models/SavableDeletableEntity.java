package com.eos.streamus.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface SavableDeletableEntity extends SavableEntity, DeletableEntity {
  default void delete(Connection connection) throws SQLException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("delete from %s where %s = ?;", getTableName(), getPrimaryKeyName()))) {
      preparedStatement.setInt(1, getId());
      preparedStatement.execute();
    }
  }
}
