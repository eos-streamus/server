package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;
import java.util.Objects;

public class User extends Person {
  //#region Static attributes
  /**
   * Table name in database.
   */
  private static final String TABLE_NAME = "StreamUsUser";
  /**
   * Email column in database.
   */
  protected static final String EMAIL_COLUMN = "email";
  /**
   * Username column in database.
   */
  protected static final String USERNAME_COLUMN = "username";
  /**
   * View name in database.
   */
  private static final String VIEW_NAME = "vUser";
  /**
   * Password table name in database.
   */
  private static final String PASSWORD_TABLE_NAME = "UserPassword";
  /**
   * Password table user id column in database.
   */
  private static final String PASSWORD_TABLE_USER_ID_COLUMN = "idUser";
  /**
   * Password table password column in database.
   */
  private static final String PASSWORD_TABLE_PASSWORD_COLUMN = "password";
  //#endregion Static attributes

  //#region Instance attributes
  /**
   * Email of the User.
   */
  private String email;
  /**
   * Username of the User.
   */
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

  User(final PersonBuilder builder) {
    super(builder);
    Objects.requireNonNull(builder.getEmail());
    Objects.requireNonNull(builder.getUsername());
    this.email = builder.getEmail();
    this.username = builder.getUsername();
  }
  //#endregion Constructors

  //#region Getters and Setters

  /**
   * @return User's email.
   */
  public String getEmail() {
    return email;
  }

  /**
   * Set the User's email.
   *
   * @param email Email to set.
   */
  public void setEmail(final String email) {
    this.email = email;
  }

  /**
   * @return Username.
   */
  public String getUsername() {
    return username;
  }

  /**
   * Set the User's username.
   *
   * @param username Username to set.
   */
  public void setUsername(final String username) {
    this.username = username;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String tableName() {
    return TABLE_NAME;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String creationFunctionName() {
    return "createUser";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String primaryKeyName() {
    return "idPerson";
  }
  //#endregion Getters and Setters

  //#region Database operations

  /**
   * {@inheritDoc}
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
        int columnNumber = 0;
        preparedStatement.setString(++columnNumber, getFirstName());
        preparedStatement.setString(++columnNumber, getLastName());
        preparedStatement.setDate(++columnNumber, getDateOfBirth());
        preparedStatement.setString(++columnNumber, email);
        preparedStatement.setString(++columnNumber, username);
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
        int columnNumber = 0;
        preparedStatement.setString(++columnNumber, email);
        preparedStatement.setString(++columnNumber, username);
        preparedStatement.setInt(++columnNumber, getId());
        preparedStatement.execute();
      }
    }
  }

  /**
   * Find a User by id in the database.
   *
   * @param id         Id of the User to find.
   * @param connection Database connection to use to perform the operation.
   * @return Found User.
   * @throws SQLException      If an error occurred while performing the operation.
   * @throws NoResultException If no User by this id was found.
   */
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

  /**
   * Find a User by email in the database.
   *
   * @param email      Email of the User to find.
   * @param connection Database connection to use to perform the operation.
   * @return Found User.
   * @throws SQLException      If an error occurred while performing the operation.
   */
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

  /**
   * Update or set the password of the User.
   *
   * @param password Password to update or set.
   * @param connection {@link Connection} to use to perform the operation.
   * @throws SQLException If an error occurred while performing the database operation.
   */
  public void upsertPassword(final String password, final Connection connection) throws SQLException {
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

  /**
   * Get the password of the User.
   *
   * @param connection {@link Connection} to use to perform the operation.
   * @throws SQLException If an error occurred while performing the database operation.
   * @throws NoResultException If the User has no saved password.
   * @return The password of the User.
   */
  public String getPassword(final Connection connection) throws SQLException, NoResultException {
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
  /** @return hashCode of this User, which is the id. */
  @Override
  public int hashCode() {
    return getId();
  }

  /**
   * Returns whether the given Object is equal to this User.
   * Will be equal if:
   * - Equal by {@link Person}'s implementation.
   * - Same email.
   *
   * @param o Object to compare
   * @return True if all conditions are met.
   */
  @Override
  public boolean equals(final Object o) {
    return super.equals(o) && ((User) o).email.equals(email);
  }
  //#endregion Equals
}
