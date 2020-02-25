package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Musician extends Artist {
  //#region Static attributes
  private static final String TABLE_NAME = "Musician";
  private static final String PRIMARY_KEY_NAME = "idArtist";
  private static final String PERSON_ID_COLUMN = "idPerson";
  private static final String CREATION_FUNCTION_NAME = "createMusician";
  private static final String VIEW_NAME = "vMusician";
  private static final String VIEW_ID_COLUMN = "id";
  private static final String VIEW_NAME_COLUMN = "name";
  //#endregion Static attributes

  //#region Instance attributes
  private final Person person;
  //#endregion Instance attributes

  //#region Constructors
  private Musician(Integer id, String name) {
    super(id, name);
    person = null;
  }

  private Musician(Integer id, String name, Person person) {
    super(id, name);
    this.person = person;
  }

  private Musician(Integer id, Person person) {
    super(id, null);
    this.person = person;
  }

  public Musician(String name) {
    super(name);
    if (name == null) {
      throw new IllegalArgumentException("Musician should either be a Person or have a name");
    }
    person = null;
  }

  public Musician(Person person) {
    super(null);
    if (person == null) {
      throw new IllegalArgumentException("Musician should either be a Person or have a name");
    }
    this.person = person;
  }

  public Musician(String name, Person person) {
    super(name);
    this.person = person;
  }
  //#endregion Constructors

  //#region Getters and Setters
  @Override
  public String getCreationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }

  public Person getPerson() {
    return person;
  }

  @Override
  public String getTableName() {
    return TABLE_NAME;
  }

  @Override
  public String getPrimaryKeyName() {
    return PRIMARY_KEY_NAME;
  }
  //#endregion Getters and Setters

  //#region Database operations
  @Override
  public void save(Connection connection) throws SQLException {
    if (this.getId() == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s(?::varchar(191)%s);", CREATION_FUNCTION_NAME, person != null ? ", ?" : ""))) {
        preparedStatement.setString(1, getName());
        if (person != null) {
          if (person.getId() == null) {
            person.save(connection);
          }
          preparedStatement.setInt(2, person.getId());
        }
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          resultSet.next();
          this.setId(resultSet.getInt(Artist.PRIMARY_KEY_NAME));
        }
      }
    } else {
      if (person != null) {
        if (person.getId() == null) {
          person.save(connection);
        }
      }
      super.save(connection);
    }
  }

  public static Musician findById(Integer id, Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s where %s = ?;", VIEW_NAME, VIEW_ID_COLUMN))) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (!resultSet.next()) {
          throw new NoResultException();
        }
        int retrievedId = resultSet.getInt(VIEW_ID_COLUMN);
        String name = resultSet.getString(VIEW_NAME_COLUMN);
        int personId = resultSet.getInt(PERSON_ID_COLUMN);
        if (resultSet.wasNull()) {
          return new Musician(retrievedId, name);
        } else if (name == null) {
          return new Musician(retrievedId, Person.findById(personId, connection));
        } else {
          return new Musician(retrievedId, name, Person.findById(personId, connection));
        }
      }
    }
  }
  //#endregion Database operations

  //#region String representations
  @Override
  public String getFieldNamesAndValuesString() {
    return String.format(
      "%s, %s: %d",
      super.getFieldNamesAndValuesString(),
      PERSON_ID_COLUMN,
      person == null ? null : person.getId()
    );
  }

  @Override
  public String toString() {
    return String.format("{%s}", getFieldNamesAndValuesString());
  }
  //#endregion String representations

  //#region Equals
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    Musician musician = (Musician) obj;
    if (person == null && musician.person != null || person != null && musician.person == null) {
      return false;
    }
    if (person == null) {
      return true;
    }
    return musician.person.equals(person);
  }
  //#endregion Equals
}
