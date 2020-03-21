package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;

public class Admin extends User {
  //#region Static Attributes
  private static final String TABLE_NAME = "Admin";
  private static final String PRIMARY_KEY_NAME = "idUser";
  private static final String CREATION_FUNCTION_NAME = "createAdmin";
  private static final String VIEW_NAME = "vadmin";
  //#endregion Static Attributes

  //#region Constructors
  private Admin(Integer id, String firstName, String lastName, Date dateOfBirth, Timestamp createdAt, Timestamp updatedAt, String email, String username) { // NOSONAR
    super(id, firstName, lastName, dateOfBirth, createdAt, updatedAt, email, username);
  }

  public Admin(String firstName, String lastName, Date dateOfBirth, String email, String username) {
    super(firstName, lastName, dateOfBirth, email, username);
  }
  //#endregion Constructors

  //#region Getters and Setters

  @Override
  public String getTableName() {
    return TABLE_NAME;
  }

  @Override
  public String getCreationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }

  @Override
  public String getPrimaryKeyName() {
    return PRIMARY_KEY_NAME;
  }

  @Override
  public void save(Connection connection) throws SQLException {
    if (getId() == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s(?::varchar(200), ?::varchar(200), ?, ?::varchar(255), ?::varchar(50));", CREATION_FUNCTION_NAME))) {
        preparedStatement.setString(1, getFirstName());
        preparedStatement.setString(2, getLastName());
        preparedStatement.setDate(3, getDateOfBirth());
        preparedStatement.setString(4, getEmail());
        preparedStatement.setString(5, getUsername());
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          resultSet.next();
          setId(resultSet.getInt(Person.ID_COLUMN));
          setCreatedAt(resultSet.getTimestamp(Person.CREATED_AT_COLUMN));
          setUpdatedAt(resultSet.getTimestamp(Person.UPDATED_AT_COLUMN));
        }
      }
    } else {
      super.save(connection);
    }
  }

  public static Admin findById(Integer id, Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s where %s = ?;", VIEW_NAME, Person.ID_COLUMN))) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (!resultSet.next()) {
          throw new NoResultException();
        }
        return new Admin(
          id,
          resultSet.getString(Person.FIRST_NAME_COLUMN),
          resultSet.getString(Person.LAST_NAME_COLUMN),
          resultSet.getDate(Person.DATE_OF_BIRTH_COLUMN),
          resultSet.getTimestamp(Person.CREATED_AT_COLUMN),
          resultSet.getTimestamp(Person.UPDATED_AT_COLUMN),
          resultSet.getString(User.EMAIL_COLUMN),
          resultSet.getString(User.USERNAME_COLUMN)
        );
      }
    }
  }
}
