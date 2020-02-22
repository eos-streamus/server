package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Person implements Entity, SavableEntity {
  private static final String PERSON_ID = "id";
  private static final String FIRST_NAME = "firstName";
  private static final String LAST_NAME = "lastName";
  private static final String DATE_OF_BIRTH = "dateOfBirth";
  private static final String CREATED_AT = "createdAt";
  private static final String UPDATED_AT = "updatedAt";

  private Integer id;
  private String firstName;
  private String lastName;
  private Date dateOfBirth;
  private Timestamp createdAt;
  private Timestamp updatedAt;

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
          this.createdAt = resultSet.getTimestamp(CREATED_AT);
          this.updatedAt = resultSet.getTimestamp(UPDATED_AT);
        }
      }
    } else {
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("update %s set firstname = ?, lastname = ?, dateOfBirth = ? where id = ? returning updatedAt;", tableName()))) {
        preparedStatement.setString(1, firstName);
        preparedStatement.setString(2, lastName);
        preparedStatement.setDate(3, dateOfBirth);
        preparedStatement.setInt(4, id);
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          resultSet.next();
          this.updatedAt = resultSet.getTimestamp(UPDATED_AT);
        }
      }
    }
  }

  @Override
  public String tableName() {
    return "Person";
  }

  @Override
  public String primaryKeyName() {
    return PERSON_ID;
  }

  @Override
  public String creationFunctionName() {
    return "createPerson";
  }

  //#region getters and setters
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

  public Timestamp getUpdatedAt() {
    return updatedAt;
  }
  //#endregion getters and setters

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

  @Override
  public String toString() {
    DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
    DateFormat timestampFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");
    return String.format(
      "{%s: %d, %s: %s, %s: %s, %s: %s, %s: %s, %s: %s}",
      PERSON_ID,
      id,
      FIRST_NAME,
      firstName,
      LAST_NAME,
      lastName,
      DATE_OF_BIRTH,
      dateFormat.format(dateOfBirth.getTime()),
      CREATED_AT,
      timestampFormat.format(createdAt),
      UPDATED_AT,
      timestampFormat.format(updatedAt));
  }

  public static Person findById(Integer id, Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement("select * from person where id = ?;")) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (!resultSet.next()) {
          throw new NoResultException();
        }
        return new Person(
          resultSet.getInt(PERSON_ID),
          resultSet.getString(FIRST_NAME),
          resultSet.getString(LAST_NAME),
          resultSet.getDate(DATE_OF_BIRTH),
          resultSet.getTimestamp(CREATED_AT),
          resultSet.getTimestamp(UPDATED_AT)
        );
      }
    }
  }
}
