package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;

public class Admin extends User {
  //#region Static Attributes
  /**
   * Table name in database.
   */
  private static final String TABLE_NAME = "Admin";
  /**
   * Primary key name in database.
   */
  private static final String PRIMARY_KEY_NAME = "idUser";
  /**
   * Creation function name in database.
   */
  private static final String CREATION_FUNCTION_NAME = "createAdmin";
  /**
   * View name in database.
   */
  private static final String VIEW_NAME = "vadmin";
  //#endregion Static Attributes

  //#region Constructors
  private Admin(final Integer id, final String firstName, final String lastName, final Date dateOfBirth, // NOSONAR
                final Timestamp createdAt, final Timestamp updatedAt, final String email, final String username) {
    super(id, firstName, lastName, dateOfBirth, createdAt, updatedAt, email, username);
  }

  public Admin(final String firstName, final String lastName, final Date dateOfBirth,
               final String email, final String username) {
    super(firstName, lastName, dateOfBirth, email, username);
  }
  //#endregion Constructors

  //#region Getters and Setters

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
    return CREATION_FUNCTION_NAME;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String primaryKeyName() {
    return PRIMARY_KEY_NAME;
  }
  //#endregion Getters and Setters

  //#region Database operations


  /**
   * {@inheritDoc}
   */
  @Override
  public void save(final Connection connection) throws SQLException {
    if (getId() == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format(
              "select * from %s(?::varchar(200), ?::varchar(200), ?, ?::varchar(255), ?::varchar(50));",
              CREATION_FUNCTION_NAME
          )
      )) {
        int columnNumber = 0;
        preparedStatement.setString(++columnNumber, getFirstName());
        preparedStatement.setString(++columnNumber, getLastName());
        preparedStatement.setDate(++columnNumber, getDateOfBirth());
        preparedStatement.setString(++columnNumber, getEmail());
        preparedStatement.setString(++columnNumber, getUsername());
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


  /**
   * Attempts to find an Admin by id.
   *
   * @param id Id of Admin to find.
   * @param connection {@link Connection} to use to perform the operation.
   * @throws SQLException If an error occurred while performing the database operation.
   * @throws NoResultException If no Admin by this id was found in database.
   * @return Found Admin.
   */
  public static Admin findById(final Integer id, final Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select * from %s where %s = ?;", VIEW_NAME, Person.ID_COLUMN
        )
    )) {
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
  //#endregion Database Operations
}
