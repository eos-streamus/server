package com.eos.streamus.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface DeletableEntity extends Deletable, Entity {
  /** {@inheritDoc} */
  default void delete(Connection connection) throws SQLException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "delete from %s where %s = ?;",
            tableName(),
            primaryKeyName()
        )
    )) {
      preparedStatement.setInt(1, getId());
      preparedStatement.execute();
    }
  }

}
