package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;

public class Series extends VideoCollection {
  //#region Static attributes
  public static final String TABLE_NAME = "Series";
  public static final String PRIMARY_KEY_NAME = "idVideoCollection";
  public static final String VIEW_NAME = "vSeries";
  public static final String CREATION_FUNCTION_NAME = "createSeries";
  //#endregion

  private Series(Integer id, String name, Timestamp createdAt, Timestamp updatedAt) {
    super(id, name, createdAt, updatedAt);
  }

  public Series(String name) {
    super(name);
  }

  @Override
  public String getTableName() {
    return TABLE_NAME;
  }

  @Override
  public String getPrimaryKeyName() {
    return PRIMARY_KEY_NAME;
  }

  @Override
  public void save(Connection connection) throws SQLException {
    if (this.getId() == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s(?::varchar(200))", CREATION_FUNCTION_NAME))) {
        preparedStatement.setString(1, getName());
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          resultSet.next();
          this.setId(resultSet.getInt(Collection.PRIMARY_KEY_NAME));
          this.setCreatedAt(resultSet.getTimestamp(Collection.CREATED_AT_COLUMN));
          this.setUpdatedAt(resultSet.getTimestamp(Collection.UPDATED_AT_COLUMN));
        }
      }
    } else {
      super.save(connection);
    }
  }

  public static Series findById(int id, Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s where %s = ?;", VIEW_NAME, Collection.PRIMARY_KEY_NAME))) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (!resultSet.next()) {
          throw new NoResultException();
        }
        Series series = new Series(
          resultSet.getInt(Collection.PRIMARY_KEY_NAME),
          resultSet.getString(Collection.NAME_COLUMN),
          resultSet.getTimestamp(Collection.CREATED_AT_COLUMN),
          resultSet.getTimestamp(Collection.UPDATED_AT_COLUMN)
        );

        // Handle episodes
        // TODO

        return series;
      }
    }
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public String getCreationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }
}
