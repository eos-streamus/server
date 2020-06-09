package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;

public class Person implements SavableDeletableEntity {
  //#region Static attributes
  protected static final String TABLE_NAME = "Person";
  protected static final String ID_COLUMN = "id";
  protected static final String FIRST_NAME_COLUMN = "firstName";
  protected static final String LAST_NAME_COLUMN = "lastName";
  protected static final String DATE_OF_BIRTH_COLUMN = "dateOfBirth";
  protected static final String CREATED_AT_COLUMN = "createdAt";
  protected static final String UPDATED_AT_COLUMN = "updatedAt";
  //#endregion Static attributes

  //#region Instance attributes
  private Integer id;
  private String firstName;
  private String lastName;
  private Date dateOfBirth;
  private Timestamp createdAt;
  private Timestamp updatedAt;
  //#endregion Instance attributes

  //#region Constructors
  protected Person(Integer id, String firstName, String lastName, Date dateOfBirth, Timestamp createdAt, Timestamp updatedAt) {
    this(firstName, lastName, dateOfBirth);
    this.id = id;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Person(String firstName, String lastName, Date dateOfBirth) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.dateOfBirth = dateOfBirth;
  }
  //#endregion Constructors

  //#region getters and setters
  @Override
  public String tableName() {
    return TABLE_NAME;
  }

  @Override
  public String primaryKeyName() {
    return ID_COLUMN;
  }

  @Override
  public String creationFunctionName() {
    return "createPerson";
  }

  @Override
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public Date getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(Date dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  protected void setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
  }

  public Timestamp getUpdatedAt() {
    return updatedAt;
  }

  protected void setUpdatedAt(Timestamp updatedAt) {
    this.updatedAt = updatedAt;
  }
  //#endregion getters and setters

  //#region Database operations
  @Override
  public void save(Connection connection) throws SQLException {
    if (this.id == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s(?::varchar(200), ?::varchar(200), ?)", creationFunctionName()))) {
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
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("update %s set firstname = ?, lastname = ?, dateOfBirth = ? where id = ? returning updatedAt;", TABLE_NAME))) {
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

  public static Person findById(Integer id, Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s where id = ?;", TABLE_NAME))) {
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
  @Override
  public String toString() {
    return defaultToString();
  }
  //#endregion String representations

  //#region Equals
  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
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
