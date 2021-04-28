package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;
import java.util.Objects;

public class Person implements SavableDeletableEntity {
  //#region Static attributes
  /** Table name in database. */
  public static final String TABLE_NAME = "Person";
  /** Id column name. */
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
  /** Id of this instance. */
  private Integer id;
  /** First name of this Person. */
  private String firstName;
  /** Last name of this Person. */
  private String lastName;
  /** Date of birth of this Person. */
  private Date dateOfBirth;
  /** Created at timestamp of this Person. */
  private Timestamp createdAt;
  /** Updated at timestamp of this Person. */
  private Timestamp updatedAt;
  //#endregion Instance attributes

  //#region Constructors
  public Person(final PersonBuilder builder) {
    Objects.requireNonNull(builder);
    Objects.requireNonNull(builder.getFirstName());
    Objects.requireNonNull(builder.getLastName());
    Objects.requireNonNull(builder.getDateOfBirth());
    this.firstName = builder.getFirstName();
    this.lastName = builder.getLastName();
    this.dateOfBirth = builder.getDateOfBirth();
    if (builder.getId() != null) {
      this.id = builder.getId();
      if (builder.hasTimestamps()) {
        this.createdAt = builder.getCreatedAt();
        this.updatedAt = builder.getUpdatedAt();
      }
    }
  }
  //#endregion Constructors

  //#region getters and setters

  /** {@inheritDoc} */
  @Override
  public String tableName() {
    return TABLE_NAME;
  }

  /** {@inheritDoc} */
  @Override
  public String primaryKeyName() {
    return ID_COLUMN;
  }

  /** {@inheritDoc} */
  @Override
  public String creationFunctionName() {
    return "createPerson";
  }

  /** {@inheritDoc} */
  @Override
  public Integer getId() {
    return id;
  }

  /**
   * Set id of this Person.
   *
   * @param id Id to set.
   */
  public void setId(final Integer id) {
    this.id = id;
  }

  /** @return First name of this Person. */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Set first name of this person.
   *
   * @param firstName First name of the Person.
   */
  public void setFirstName(final String firstName) {
    this.firstName = firstName;
  }

  /** @return Last name of this Person. */
  public String getLastName() {
    return lastName;
  }

  /**
   * Set last name of this Person.
   *
   * @param lastName Last name to set.
   */
  public void setLastName(final String lastName) {
    this.lastName = lastName;
  }

  /** @return {@link Date} of birth of this Person. */
  public Date getDateOfBirth() {
    return dateOfBirth;
  }

  /**
   * Set date of birth of this Person.
   *
   * @param dateOfBirth {@link Date} of birth to set.
   */
  public void setDateOfBirth(final Date dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  /** @return This Person's created at timestamp. */
  public Timestamp getCreatedAt() {
    return createdAt;
  }

  /**
   * Set this Person's created at {@link Timestamp}.
   *
   * @param createdAt timestamp to set.
   */
  protected void setCreatedAt(final Timestamp createdAt) {
    this.createdAt = createdAt;
  }

  /** @return This Person's updated at timestamp. */
  public Timestamp getUpdatedAt() {
    return updatedAt;
  }

  /**
   * Set this Person's updated at {@link Timestamp}.
   *
   * @param updatedAt timestamp to set.
   */
  protected void setUpdatedAt(final Timestamp updatedAt) {
    this.updatedAt = updatedAt;
  }
  //#endregion getters and setters

  //#region Database operations

  /** {@inheritDoc} */
  @Override
  public void save(final Connection connection) throws SQLException {
    if (this.id == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format(
              "select * from %s(?::varchar(200), ?::varchar(200), ?)",
              creationFunctionName()
          )
      )) {
        int columnNumber = 1;
        preparedStatement.setString(columnNumber++, firstName);
        preparedStatement.setString(columnNumber++, lastName);
        preparedStatement.setDate(columnNumber, dateOfBirth);
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
        int columnNumber = 0;
        preparedStatement.setString(++columnNumber, firstName);
        preparedStatement.setString(++columnNumber, lastName);
        preparedStatement.setDate(++columnNumber, dateOfBirth);
        preparedStatement.setInt(++columnNumber, id);
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          resultSet.next();
          this.updatedAt = resultSet.getTimestamp(UPDATED_AT_COLUMN);
        }
      }
    }
  }

  /**
   * Find a Person by id in the database.
   *
   * @param id         Id of the Person to find.
   * @param connection Database connection to use to perform the operation.
   * @return Found Person.
   * @throws SQLException      If an error occurred while performing the operation.
   * @throws NoResultException If no Person by this id was found.
   */
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
        return new PersonBuilder(
            resultSet.getString(FIRST_NAME_COLUMN),
            resultSet.getString(LAST_NAME_COLUMN),
            resultSet.getDate(DATE_OF_BIRTH_COLUMN)
        ).withId(resultSet.getInt(ID_COLUMN))
            .withTimestamps(resultSet.getTimestamp(CREATED_AT_COLUMN), resultSet.getTimestamp(UPDATED_AT_COLUMN))
            .build();
      }
    }
  }
  //#endregion Database operations

  //#region String representations

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return defaultToString();
  }
  //#endregion String representations

  //#region Equals

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return id;
  }

  /**
   * Returns whether the given Object is equal to this Person.
   * Will be equal if:
   * - Not null
   * - Same class
   * - Same id
   * - Same first name
   * - Same last name
   * - Same date of birth
   * - Same created at
   * - Same updated at
   *
   * @param o Object to compare
   * @return True if all conditions above are verified.
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
