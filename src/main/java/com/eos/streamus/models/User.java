package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;

public class User extends Person {
  //#region Static attributes
  /** Table name in database. */
  private static final String TABLE_NAME = "StreamUsUser";
  /** Email column name. */
  protected static final String EMAIL_COLUMN = "email";
  /** Username column name. */
  protected static final String USERNAME_COLUMN = "username";
  /** View name. */
  private static final String VIEW_NAME = "vUser";
  /** User password column name. */
  private static final String PASSWORD_TABLE_NAME = "UserPassword";
  /** User id column name in password table. */
  private static final String PASSWORD_TABLE_USER_ID_COLUMN = "idUser";
  /** Password column name in password table. */
  private static final String PASSWORD_TABLE_PASSWORD_COLUMN = "password";
  //#endregion Static attributes

  //#region Instance attributes
  /** Email of this User. */
  private String email;
  /** Username of this User. */
  private String username;
  //#endregion Instance attributes

  //#region Constructors
  protected User(final Integer id, final String firstName, final String lastName, final Date dateOfBirth, // NOSONAR
                 final Timestamp createdAt, final Timestamp updatedAt, final String email, final String username) {
    super(id, firstName, lastName, dateOfBirth, createdAt, updatedAt);
    this.email = email;
    this.username = username;
  }

  public User(final String firstName, final String lastName, final Date dateOfBirth,
              final String email, final String username) {
    super(firstName, lastName, dateOfBirth);
    this.email = email;
    this.username = username;
  }
  //#endregion Constructors

  //#region Getters and Setters
  public final String getEmail() {
    return email;
  }

  public final void setEmail(final String email) {
    this.email = email;
  }

  public final String getUsername() {
    return username;
  }

  public final void setUsername(final String username) {
    this.username = username;
  }

  /** @return Table name. */
  @Override
  public String tableName() {
    return TABLE_NAME;
  }

  /** @return Creation function name. */
  @Override
  public String creationFunctionName() {
    return "createUser";
  }

  /** @return Primary key name. */
  @Override
  public String primaryKeyName() {
    return "idPerson";
  }
  //#endregion Getters and Setters

  //#region Database operations

  /**
   * Save this instance to database.
   *
   * @param connection {@link Connection} to use to save.
   * @throws SQLException if an error occurs.
   */
  @Override
  public void save(final Connection connection) throws SQLException {
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

  public static User findById(final Integer id, final Connection connection) throws SQLException, NoResultException {
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

  public static User findByEmail(final String email, final Connection connection) throws SQLException {
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

  public final void upsertPassword(final String password, final Connection connection) throws SQLException {
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

  public final String getPassword(final Connection connection) throws SQLException, NoResultException {
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
  /** @return hashcode of this instance. */
  @Override
  public int hashCode() {
    return getId();
  }

  /**
   * Tests if given object is equal to this instance.
   *
   * @param o Object to test.
   * @return If the test passes.
   */
  @Override
  public boolean equals(final Object o) {
    return super.equals(o) && ((User) o).email.equals(email);
  }
  //#endregion Equals
}
