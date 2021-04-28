package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.exceptions.NotPersistedException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Band extends Artist {
  public class Member implements SavableDeletable {
    //#region Static attributes
    /** Table name in the database. */
    public static final String TABLE_NAME = "BandMusician";
    /** Band id column name in the database. */
    public static final String BAND_ID_COLUMN = "idBand";
    /** Musician id column name in the database. */
    public static final String MUSICIAN_ID_COLUMN = "idMusician";
    /** From column name in the database. */
    public static final String FROM_COLUMN = "memberFrom";
    /** To column name in the database. */
    public static final String TO_COLUMN = "memberTo";
    //#endregion Static attributes

    //#region Instance attributes
    /** Member {@link Musician}. */
    private final Musician musician;
    /** Membership start date. */
    private final Date from;
    /** Membership end date. */
    private Date to;
    //#endregion Instance attributes

    //#region Constructors
    public Member(final Musician musician, final Date from) {
      if (musician == null || from == null) {
        throw new IllegalArgumentException("Member.musician and Member.from cannot be null");
      }
      if (musician.getId() == null) {
        throw new NotPersistedException("Band member musician not persisted");
      }
      this.musician = musician;
      this.from = from;
    }

    public Member(final Musician musician, final Date from, final Date to) {
      this(musician, from);
      this.to = to;
    }
    //#endregion Constructors

    //#region Getters and Setters
    public final Musician getMusician() {
      return musician;
    }

    public final Date getFrom() {
      return from;
    }

    /** @return Id. */
    public Date getTo() {
      return to;
    }

    /** @return This Member's {@link Band}. */
    public Band getBand() {
      return Band.this;
    }
    //#endregion Getters and Setters

    //#region Database operations

    /**
     * Checks if this Member instance has been saved to database.
     *
     * @param connection {@link Connection} to use to perform the operation.
     * @return True if it has been persisted.
     * @throws SQLException If an error occurred while performing the database operation.
     */
    public boolean isPersisted(final Connection connection) throws SQLException {
      try (PreparedStatement existsStatement = connection.prepareStatement(String.format(
          "select 1 from %s where %s = ? and %s = ? and %s = ?", TABLE_NAME, BAND_ID_COLUMN, MUSICIAN_ID_COLUMN,
          FROM_COLUMN))) {
        int columnNumber = 0;
        existsStatement.setInt(++columnNumber, Band.this.getId());
        existsStatement.setInt(++columnNumber, musician.getId());
        existsStatement.setDate(++columnNumber, from);
        try (ResultSet resultSet = existsStatement.executeQuery()) {
          return resultSet.next();
        }
      }
    }

    /** {@inheritDoc} */
    @Override
    public void delete(final Connection connection) throws SQLException {
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format(
          "delete from %s where %s = ? and %s = ? and %s = ?;", TABLE_NAME, BAND_ID_COLUMN, MUSICIAN_ID_COLUMN,
          FROM_COLUMN))) {
        int columnNumber = 0;
        preparedStatement.setInt(++columnNumber, Band.this.getId());
        preparedStatement.setInt(++columnNumber, musician.getId());
        preparedStatement.setDate(++columnNumber, from);
        preparedStatement.execute();
      }
    }

    /** {@inheritDoc} */
    @Override
    public void save(final Connection connection) throws SQLException {
      if (Band.this.getId() == null) {
        throw new NotPersistedException("Band not persisted");
      }
      boolean exists;
      try (PreparedStatement existsStatement = connection.prepareStatement(
          String.format(
              "select 1 from %s where %s = ? and %s = ? and %s = ?",
              TABLE_NAME,
              BAND_ID_COLUMN,
              MUSICIAN_ID_COLUMN,
              FROM_COLUMN
          )
      )) {
        int columnNumber = 0;
        existsStatement.setInt(++columnNumber, Band.this.getId());
        existsStatement.setInt(++columnNumber, musician.getId());
        existsStatement.setDate(++columnNumber, from);
        try (ResultSet resultSet = existsStatement.executeQuery()) {
          exists = resultSet.next();
        }
      }
      if (!exists) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
            String.format(
                "insert into %s(%s, %s, %s, %s) values (?, ?, ?, ?);",
                TABLE_NAME,
                BAND_ID_COLUMN,
                MUSICIAN_ID_COLUMN,
                FROM_COLUMN,
                TO_COLUMN
            )
        )) {
          int columnNumber = 0;
          preparedStatement.setInt(++columnNumber, Band.this.getId());
          preparedStatement.setInt(++columnNumber, musician.getId());
          preparedStatement.setDate(++columnNumber, from);
          preparedStatement.setDate(++columnNumber, to);
          preparedStatement.execute();
        }
      } else {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
            String.format(
                "update %s set %s = ? where %s = ? and %s = ? and %s = ?",
                TABLE_NAME,
                TO_COLUMN,
                BAND_ID_COLUMN,
                MUSICIAN_ID_COLUMN,
                FROM_COLUMN
            )
        )) {
          int columnNumber = 0;
          preparedStatement.setDate(++columnNumber, to);
          preparedStatement.setInt(++columnNumber, Band.this.getId());
          preparedStatement.setInt(++columnNumber, musician.getId());
          preparedStatement.setDate(++columnNumber, from);
          preparedStatement.execute();
        }
      }
    }
    //#endregion Database operations

    //#region String representations

    /** {@inheritDoc} */
    @Override
    public final String toString() {
      return String.format(
          "%s[%s= %d, %s= %d, %s= %s, %s= %s]",
          getClass().getName(),
          BAND_ID_COLUMN,
          Band.this.getId(),
          MUSICIAN_ID_COLUMN,
          musician.getId(),
          FROM_COLUMN,
          from,
          TO_COLUMN,
          to
      );
    }
    //#endregion String representations

    //#region Equals

    /**
     * @return Combined hashcode of musician hashcode, band hashcode, from hashcode, to hashcode.
     */
    @Override
    public int hashCode() {
      return Objects.hash(musician.hashCode(), Band.this.hashCode(), from.hashCode(), to.hashCode());
    }

    /**
     * Returns whether the given Object is equal.
     * Will be equal if:
     * - Not null
     * - Same class
     * - Same musician
     * - Same from (both null or equal)
     * - Same to (both null or equal)
     *
     * @param obj Object to compare.
     * @return True if all conditions are met.
     */
    @Override
    public boolean equals(final Object obj) {
      if (obj == null) {
        return false;
      }
      if (!obj.getClass().equals(getClass())) {
        return false;
      }
      Member member = (Member) obj;
      if (!musician.equals(member.musician)) {
        return false;
      }
      if (from == null && member.from != null || from != null && member.from == null) {
        return false;
      }
      if (to == null && member.to != null || to != null && member.to == null) {
        return false;
      }
      return (from == null || from.equals(member.from)) && (to == null || to.equals(member.to));
    }
    //#endregion Equals
  }

  //#region Static attributes
  /** Table name in the database. */
  public static final String TABLE_NAME = "Band";
  /** Primary key name in the database. */
  public static final String PRIMARY_KEY_NAME = "idArtist";
  /** Creation function name in the database. */
  public static final String CREATION_FUNCTION_NAME = "createBand";
  /** View name in the database. */
  public static final String VIEW_NAME = "vBand";
  /** View id column in the database. */
  public static final String VIEW_ID_COLUMN = "id";
  /** View name column in the database. */
  public static final String VIEW_NAME_COLUMN = "name";
  //#endregion Static attributes

  //#region Instance attributes
  /** List of Band members. */
  private final List<Member> members = new ArrayList<>();
  //#endregion Instance attributes

  //#region Constructors
  private Band(final Integer id, final String name) {
    super(id, name);
  }

  public Band(final String name) {
    super(name);
    if (name == null) {
      throw new IllegalArgumentException("Band name cannot be null");
    }
  }
  //#endregion Constructors

  //#region Getters and Setters

  /** {@inheritDoc} */
  @Override
  public final String tableName() {
    return TABLE_NAME;
  }

  /** {@inheritDoc} */
  @Override
  public final String primaryKeyName() {
    return PRIMARY_KEY_NAME;
  }

  /** {@inheritDoc} */
  @Override
  public final String creationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }

  /** @return List of {@link Member}s. */
  public List<Member> getMembers() {
    return members;
  }

  /**
   * Add a {@link Member} to the Band.
   *
   * @param musician Musician to add
   * @param from     Membership start
   * @param to       Membership end
   */
  public void addMember(final Musician musician, final Date from, final Date to) {
    members.add(this.new Member(musician, from, to));
  }

  /**
   * Add a {@link Member} to the Band with no end date.
   *
   * @param musician Musician to add
   * @param from     Membership start
   */
  public void addMember(final Musician musician, final Date from) {
    members.add(new Member(musician, from));
  }

  /**
   * Add a {@link Member} to the Band.
   *
   * @param member {@link Member} to add
   */
  public void addMember(final Member member) {
    if (!member.getBand().equals(this)) {
      throw new IllegalArgumentException(String.format("Member %s has a different band", member));
    }
    members.add(member);
  }
  //#endregion Getters and Setters

  //#region Database operations

  /** {@inheritDoc} */
  @Override
  public void save(final Connection connection) throws SQLException {
    if (this.getId() == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format(
              "select * from %s(?);",
              CREATION_FUNCTION_NAME
          )
      )) {
        preparedStatement.setString(1, getName());
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          resultSet.next();
          this.setId(resultSet.getInt(Artist.PRIMARY_KEY_NAME));
        }
      }
    } else {
      super.save(connection);
    }
    for (Member member : members) {
      try {
        member.save(connection);
      } catch (SQLException e) {
        if (!e.getSQLState().isEmpty() && e.getSQLState().equals("40002")) {
          throw new SQLException(
              "Save/Update of Band and members could not be committed. Invalid member dates",
              "40002"
          );
        } else {
          throw e;
        }
      }
    }
  }

  /**
   * Finds a Band by id.
   *
   * @param id         Id of Band to find.
   * @param connection {@link Connection} to use to perform the operation.
   * @return Found Band.
   * @throws SQLException      If an error occurred while performing the database operation.
   * @throws NoResultException If no Band by this id was found.
   */
  public static Band findById(final Integer id, final Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select * from %s where %s = ?",
            VIEW_NAME,
            VIEW_ID_COLUMN
        )
    )) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (!resultSet.next()) {
          throw new NoResultException();
        }
        Band band = new Band(id, resultSet.getString(VIEW_NAME_COLUMN));
        band.fetchMembers(connection);
        return band;
      }
    }
  }

  /**
   * Populates the Band's {@link Member}s from the database.
   *
   * @param connection {@link Connection} to use to perform the operation.
   * @throws SQLException If an error occurred while performing the database operation.
   */
  public void fetchMembers(final Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select * from %s where %s = ?",
            Member.TABLE_NAME,
            Member.BAND_ID_COLUMN
        )
    )) {
      preparedStatement.setInt(1, getId());
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          Musician musician = Musician.findById(resultSet.getInt(Member.MUSICIAN_ID_COLUMN), connection);
          Date from = resultSet.getDate(Member.FROM_COLUMN);
          Date to = resultSet.getDate(Member.TO_COLUMN);
          this.members.add(new Member(musician, from, to));
        }
      }
    }
  }
  //#endregion Database operations

  //#region Equals

  /** @return Band's hashcode, i.e. its id. */
  @Override
  public final int hashCode() {
    return getId();
  }

  /**
   * Returns whether the given Object is equal.
   * Will be equal if:
   * - {@link Artist}'s conditions are met.
   * - Same members.
   *
   * @param obj Object to compare.
   * @return True if all conditions are met.
   */
  @Override
  public boolean equals(final Object obj) {
    if (!super.equals(obj)) {
      return false;
    }
    Band band = (Band) obj;
    if (band.members.size() != this.members.size()) {
      return false;
    }
    for (Member member : members) {
      if (!band.members.contains(member)) {
        return false;
      }
    }
    return true;
  }
  //#endregion Equals
}
