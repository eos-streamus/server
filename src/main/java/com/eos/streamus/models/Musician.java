package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Musician extends Artist {
  //#region Static attributes
  /**
   * Table name in the database.
   */
  private static final String TABLE_NAME = "Musician";
  /**
   * Primary key name in the database.
   */
  private static final String PRIMARY_KEY_NAME = "idArtist";
  /**
   * Person id column name in the database.
   */
  private static final String PERSON_ID_COLUMN = "idPerson";
  /**
   * Creation function name in the database.
   */
  private static final String CREATION_FUNCTION_NAME = "createMusician";
  /**
   * View name in the database.
   */
  private static final String VIEW_NAME = "vMusician";
  /**
   * View id column name in the database.
   */
  private static final String VIEW_ID_COLUMN = "id";
  /**
   * View name column name in the database.
   */
  private static final String VIEW_NAME_COLUMN = "name";
  //#endregion Static attributes

  //#region Instance attributes
  /**
   * Musician {@link Person}.
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public String creationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }

  /**
   * @return Artist's {@link Person}.
   */
  public Person getPerson() {
    return person;
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

  /**
   * Finds a Musician by id.
   *
   * @param id         Id of the Musician to find.
   * @param connection {@link Connection} to use to perform the operation.
   * @return Found Musician.
   * @throws SQLException      If an error occurred while performing the database operation.
   * @throws NoResultException If no Musician by this id was found.
   */
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

  /**
   * @return HashCode of this Musician, i.e. its id.
   */
  @Override
  public int hashCode() {
    return getId();
  }

  /**
   * Returns whether the given Object is equal.
   * Will be equal if:
   * - {@link Artist} equality conditions are met.
   * - Same {@link Person} (both null or equal)
   *
   * @param obj Object to compare.
   * @return True if all conditions are met.
   */
  @Override
  public boolean equals(final Object obj) {
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
