package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;

public class Person implements SavableDeletableEntity {
  //#region Static attributes
  /** Table name in database. */
  public static final String TABLE_NAME = "Person";
  /** Primary key column (id) name. */
  protected static final String ID_COLUMN = "id";
  /** First name column name. */
  protected static final String FIRST_NAME_COLUMN = "firstName";
  /** Last name column name. */
  protected static final String LAST_NAME_COLUMN = "lastName";
  /** Date of birth column name. */
  protected static final String DATE_OF_BIRTH_COLUMN = "dateOfBirth";
  /** Created at timestamp column name. */
  protected static final String CREATED_AT_COLUMN = "createdAt";
  /** Updated at timestamp column name. */
  protected static final String UPDATED_AT_COLUMN = "updatedAt";
  //#endregion Static attributes

  //#region Instance attributes
  /** Id of the Person. */
  private Integer id;
  /** First name of the Person. */
  private String firstName;
  /** Last name of the Person. */
  private String lastName;
  /** Date of birth of the Person. */
  private Date dateOfBirth;
  /** Creation timestamp of the Person. */
  private Timestamp createdAt;
  /** Last update timestamp of the Person. */
  private Timestamp updatedAt;
  //#endregion Instance attributes

  //#region Constructors
  protected Person(final Integer id, final String firstName, final String lastName,
                   final Date dateOfBirth, final Timestamp createdAt, final Timestamp updatedAt) {
    this(firstName, lastName, dateOfBirth);
    this.id = id;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Person(final String firstName, final String lastName, final Date dateOfBirth) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.dateOfBirth = dateOfBirth;
  }
  //#endregion Constructors

  //#region getters and setters

  /** @return Table name. */
  @Override
  public String tableName() {
    return TABLE_NAME;
  }

  /** @return Primary key name. */
  @Override
  public String primaryKeyName() {
    return ID_COLUMN;
  }

  /** @return Creation function name in database. */
  @Override
  public String creationFunctionName() {
    return "createPerson";
  }

  @Override
  public final Integer getId() {
    return id;
  }

  public final void setId(final Integer id) {
    this.id = id;
  }

  public final String getFirstName() {
    return firstName;
  }

  public final void setFirstName(final String firstName) {
    this.firstName = firstName;
  }

  public final String getLastName() {
    return lastName;
  }

  public final void setLastName(final String lastName) {
    this.lastName = lastName;
  }

  public final Date getDateOfBirth() {
    return dateOfBirth;
  }

  public final void setDateOfBirth(final Date dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public final Timestamp getCreatedAt() {
    return createdAt;
  }

  protected final void setCreatedAt(final Timestamp createdAt) {
    this.createdAt = createdAt;
  }

  public final Timestamp getUpdatedAt() {
    return updatedAt;
  }

  protected final void setUpdatedAt(final Timestamp updatedAt) {
    this.updatedAt = updatedAt;
  }
  //#endregion getters and setters

  //#region Database operations

  /**
   * Save Person to database.
   *
   * @param connection {@link Connection} to use to save.
   * @throws SQLException If an error occurs.
   */
  @Override
  public void save(final Connection connection) throws SQLException {
    if (this.id == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format(
              "select * from %s(?::varchar(200), ?::varchar(200), ?)",
              creationFunctionName()
          )
      )) {
        preparedStatement.setString(1, firstName);
        preparedStatement.setString(2, lastName);
        preparedStatement.setDate(3, dateOfBirth);
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          if (!resultSet.next()) {
            throw new SQLException("Could not execute statement");
          }
          this.id = resultSet.getInt("id");
          this.createdAt = resultSet.getTimestamp(CREATED_AT_COLUMN);
          this.updatedAt = resultSet.getTimestamp(UPDATED_AT_COLUMN);
        }
      }
    } else {
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format(
              "update %s set %s = ?, %s = ?, %s = ? where id = ? returning updatedAt;",
              TABLE_NAME,
              FIRST_NAME_COLUMN,
              LAST_NAME_COLUMN,
              DATE_OF_BIRTH_COLUMN
          )
      )) {
        preparedStatement.setString(1, firstName);
        preparedStatement.setString(2, lastName);
        preparedStatement.setDate(3, dateOfBirth);
        preparedStatement.setInt(4, id);
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          resultSet.next();
          this.updatedAt = resultSet.getTimestamp(UPDATED_AT_COLUMN);
        }
      }
    }
  }

  public static Person findById(final Integer id, final Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select * from %s where id = ?;",
            TABLE_NAME
        )
    )) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (!resultSet.next()) {
          throw new NoResultException();
        }
        return new Person(
            resultSet.getInt(ID_COLUMN),
            resultSet.getString(FIRST_NAME_COLUMN),
            resultSet.getString(LAST_NAME_COLUMN),
            resultSet.getDate(DATE_OF_BIRTH_COLUMN),
            resultSet.getTimestamp(CREATED_AT_COLUMN),
            resultSet.getTimestamp(UPDATED_AT_COLUMN)
        );
      }
    }
  }
  //#endregion Database operations

  //#region String representations

  /** @return Get String representation of person. */
  @Override
  public String toString() {
    return defaultToString();
  }
  //#endregion String representations

  //#region Equals
  /** @return hashcode of this instance. */
  @Override
  public int hashCode() {
    return id;
  }

  /**
   * Checks if this instance is equal to given one.
   * @param o Object to test.
   * @return If the instances are equal.
   */
  @Override
  public boolean equals(final Object o) {
    if (o == null || o.getClass() != this.getClass()) {
      return false;
    }
    Person p = (Person) o;
    return
        p.id.equals(id) &&
        p.firstName.equals(firstName) &&
        p.lastName.equals(lastName) &&
        (p.dateOfBirth.compareTo(dateOfBirth) == 0) &&
        p.createdAt.equals(createdAt) &&
        p.updatedAt.equals(updatedAt);
  }
  //#endregion Equals
}
