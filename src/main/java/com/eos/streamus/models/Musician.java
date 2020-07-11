package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Musician extends Artist {
  //#region Static attributes
  /** Table name in database. */
  private static final String TABLE_NAME = "Musician";
  /** Artist id column name. */
  private static final String PRIMARY_KEY_NAME = "idArtist";
  /** Person id column name. */
  private static final String PERSON_ID_COLUMN = "idPerson";
  /** Creation function name in database. */
  private static final String CREATION_FUNCTION_NAME = "createMusician";
  /** View name. */
  private static final String VIEW_NAME = "vMusician";
  /** Id column name in view. */
  private static final String VIEW_ID_COLUMN = "id";
  /** Name column name in view. */
  private static final String VIEW_NAME_COLUMN = "name";
  //#endregion Static attributes

  //#region Instance attributes
  /** Person (if exists) of this Musician. */
  private final Person person;
  //#endregion Instance attributes

  //#region Constructors
  private Musician(final Integer id, final String name) {
    super(id, name);
    person = null;
  }

  private Musician(final Integer id, final String name, final Person person) {
    super(id, name);
    this.person = person;
  }

  private Musician(final Integer id, final Person person) {
    super(id, null);
    this.person = person;
  }

  public Musician(final String name) {
    super(name);
    if (name == null) {
      throw new IllegalArgumentException("Musician should either be a Person or have a name");
    }
    person = null;
  }

  public Musician(final Person person) {
    super(null);
    if (person == null) {
      throw new IllegalArgumentException("Musician should either be a Person or have a name");
    }
    this.person = person;
  }

  public Musician(final String name, final Person person) {
    super(name);
    this.person = person;
  }
  //#endregion Constructors

  //#region Getters and Setters
  @Override
  public final String creationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }

  public final Person getPerson() {
    return person;
  }

  @Override
  public final String tableName() {
    return TABLE_NAME;
  }

  @Override
  public final String primaryKeyName() {
    return PRIMARY_KEY_NAME;
  }
  //#endregion Getters and Setters

  //#region Database operations
  @Override
  public final void save(final Connection connection) throws SQLException {
    if (this.getId() == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format(
              "select * from %s(?::varchar(191)%s);",
              CREATION_FUNCTION_NAME,
              person != null ? ", ?" : ""
          )
      )) {
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
      if (person != null && person.getId() == null) {
        person.save(connection);
      }
      super.save(connection);
    }
  }

  public static Musician findById(final Integer id, final Connection connection)
      throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select * from %s where %s = ?;",
            VIEW_NAME,
            VIEW_ID_COLUMN
        )
    )) {
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

  //#region Equals
  @Override
  public final int hashCode() {
    return getId();
  }

  @Override
  public final boolean equals(final Object obj) {
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
