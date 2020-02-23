package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Band extends Artist {
  //#region Static attributes
  public static final String TABLE_NAME = "Band";
  public static final String PRIMARY_KEY_NAME = "idArtist";
  public static final String CREATION_FUNCTION_NAME = "createBand";
  public static final String VIEW_NAME = "vBand";
  public static final String VIEW_ID_COLUMN = "id";
  public static final String VIEW_NAME_COLUMN = "name";
  //#endregion Static attributes

  //#region Instance attributes

  //#endregion Instance attributes

  //#region Constructors
  private Band(Integer id, String name) {
    super(id, name);
  }

  public Band(String name) {
    super(name);
    if (name == null) {
      throw new IllegalArgumentException("Band name cannot be null");
    }
  }
  //#endregion Constructors

  //#region Getters and Setters
  @Override
  public String getTableName() {
    return super.getTableName();
  }

  @Override
  public String getPrimaryKeyName() {
    return super.getPrimaryKeyName();
  }

  @Override
  public String getCreationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }
  //#endregion Getters and Setters

  //#region Database operations
  @Override
  public void save(Connection connection) throws SQLException {
    if (this.getId() == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s(?);", CREATION_FUNCTION_NAME))) {
        preparedStatement.setString(1, getName());
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          resultSet.next();
          this.setId(resultSet.getInt(Artist.PRIMARY_KEY_NAME));
        }
      }
    } else {
      super.save(connection);
    }
  }

  public static Band findById(Integer id, Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s where %s = ?", VIEW_NAME, VIEW_ID_COLUMN))) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (!resultSet.next()) {
          throw new NoResultException();
        }
        return new Band(
          id,
          resultSet.getString(VIEW_NAME_COLUMN)
        );
      }
    }
  }
  //#endregion Database operations

  //#region String representations
  @Override
  public String toString() {
    return super.toString();
  }

  @Override
  public String getFieldNamesAndValuesString() {
    return super.getFieldNamesAndValuesString();
  }
  //#endregion String representations

  //#region Equals
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }
  //#endregion Equals
}
