package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;

public class User extends Person {
  //#region Static attributes
  private static final String TABLE_NAME = "StreamUsUser";
  protected static final String EMAIL_COLUMN = "email";
  protected static final String USERNAME_COLUMN = "username";
  private static final String VIEW_NAME = "vuser";
  private static final String PASSWORD_TABLE_NAME = "UserPassword";
  private static final String PASSWORD_TABLE_USER_ID_COLUMN = "idUser";
  private static final String PASSWORD_TABLE_PASSWORD_COLUMN = "password";
  //#endregion Static attributes

  //#region Instance attributes
  private String email;
  private String username;
  //#endregion Instance attributes

  //#region Constructors
  protected User(Integer id, String firstName, String lastName, Date dateOfBirth, // NOSONAR
                 Timestamp createdAt, Timestamp updatedAt, String email, String username) {
    super(id, firstName, lastName, dateOfBirth, createdAt, updatedAt);
    this.email = email;
    this.username = username;
  }

  public User(String firstName, String lastName, Date dateOfBirth, String email, String username) {
    super(firstName, lastName, dateOfBirth);
    this.email = email;
    this.username = username;
  }
  //#endregion Constructors

  //#region Getters and Setters
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public String tableName() {
    return TABLE_NAME;
  }

  @Override
  public String creationFunctionName() {
    return "createUser";
  }

  @Override
  public String primaryKeyName() {
    return "idPerson";
  }
  //#endregion Getters and Setters

  //#region Database operations
  @Override
  public void save(Connection connection) throws SQLException {
    if (this.getId() == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format(
              "select * from %s(?::varchar(200), ?::varchar(200), ?, ?::varchar(255), ?::varchar(50));",
              creationFunctionName()
          )
      )) {
        preparedStatement.setString(1, getFirstName());
        preparedStatement.setString(2, getLastName());
        preparedStatement.setDate(3, getDateOfBirth());
        preparedStatement.setString(4, email);
        preparedStatement.setString(5, username);
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          if (!resultSet.next()) {
            throw new SQLException("Could not execute statement");
          }
          this.setId(resultSet.getInt("id"));
          this.setCreatedAt(resultSet.getTimestamp(CREATED_AT_COLUMN));
          this.setUpdatedAt(resultSet.getTimestamp(UPDATED_AT_COLUMN));
        }
      }
    } else {
      super.save(connection);
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format(
              "update %s set %s = ?, %s = ? where %s = ?;", tableName(), EMAIL_COLUMN, USERNAME_COLUMN,
              primaryKeyName()
          )
      )) {
        preparedStatement.setString(1, email);
        preparedStatement.setString(2, username);
        preparedStatement.setInt(3, getId());
        preparedStatement.execute();
      }
    }
  }

  public static User findById(Integer id, Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select * from %s where id = ?",
            VIEW_NAME
        )
    )) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (!resultSet.next()) {
          throw new NoResultException();
        }
        return new User(
            resultSet.getInt(ID_COLUMN),
            resultSet.getString(FIRST_NAME_COLUMN),
            resultSet.getString(LAST_NAME_COLUMN),
            resultSet.getDate(DATE_OF_BIRTH_COLUMN),
            resultSet.getTimestamp(CREATED_AT_COLUMN),
            resultSet.getTimestamp(UPDATED_AT_COLUMN),
            resultSet.getString(EMAIL_COLUMN),
            resultSet.getString(USERNAME_COLUMN)
        );
      }
    }
  }

  public static User findByEmail(final String email, Connection connection) throws SQLException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select * from %s where %s = ?",
            VIEW_NAME,
            EMAIL_COLUMN
        )
    )) {
      preparedStatement.setString(1, email);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          return new User(
              resultSet.getInt(ID_COLUMN),
              resultSet.getString(FIRST_NAME_COLUMN),
              resultSet.getString(LAST_NAME_COLUMN),
              resultSet.getDate(DATE_OF_BIRTH_COLUMN),
              resultSet.getTimestamp(CREATED_AT_COLUMN),
              resultSet.getTimestamp(UPDATED_AT_COLUMN),
              resultSet.getString(EMAIL_COLUMN),
              resultSet.getString(USERNAME_COLUMN)
          );
        }
        return null;
      }
    }
  }

  public void updatePassword(String password, Connection connection) throws SQLException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "INSERT INTO %s(%s, %s) VALUES (?, ?) ON CONFLICT (%s) DO UPDATE SET %s = excluded.%s;",
            PASSWORD_TABLE_NAME,
            PASSWORD_TABLE_USER_ID_COLUMN,
            PASSWORD_TABLE_PASSWORD_COLUMN,
            PASSWORD_TABLE_USER_ID_COLUMN,
            PASSWORD_TABLE_PASSWORD_COLUMN,
            PASSWORD_TABLE_PASSWORD_COLUMN
        )
    )) {
      preparedStatement.setInt(1, getId());
      preparedStatement.setString(2, password);
      preparedStatement.execute();
    }
  }

  public String getPassword(Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select %s from %s where %s = ?",
            PASSWORD_TABLE_PASSWORD_COLUMN,
            PASSWORD_TABLE_NAME,
            PASSWORD_TABLE_USER_ID_COLUMN
        )
    )) {
      preparedStatement.setInt(1, getId());
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          return resultSet.getString(1);
        } else {
          throw new NoResultException();
        }
      }
    }
  }
  //#endregion Database operations

  //#region Equals
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o) && ((User) o).email.equals(email);
  }
  //#endregion Equals
}
