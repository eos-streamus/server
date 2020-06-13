package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.exceptions.NotPersistedException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Band extends Artist {
  public class Member implements SavableDeletable {
    //#region Static attributes
    public static final String TABLE_NAME = "BandMusician";
    public static final String BAND_ID_COLUMN = "idBand";
    public static final String MUSICIAN_ID_COLUMN = "idMusician";
    public static final String FROM_COLUMN = "memberFrom";
    public static final String TO_COLUMN = "memberTo";
    //#endregion Static attributes

    //#region Instance attributes
    private final Musician musician;
    private final Date from;
    private Date to;
    //#endregion Instance attributes

    //#region Constructors
    public Member(Musician musician, Date from) {
      if (musician == null || from == null) {
        throw new IllegalArgumentException("Member.musician and Member.from cannot be null");
      }
      if (musician.getId() == null) {
        throw new NotPersistedException("Band member musician not persisted");
      }
      this.musician = musician;
      this.from = from;
    }

    public Member(Musician musician, Date from, Date to) {
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

    public Date getTo() {
      return to;
    }

    public Band getBand() {
      return Band.this;
    }
    //#endregion Getters and Setters

    //#region Database operations
    public boolean isPersisted(Connection connection) throws SQLException {
      try (PreparedStatement existsStatement = connection.prepareStatement(String.format(
          "select 1 from %s where %s = ? and %s = ? and %s = ?", TABLE_NAME, BAND_ID_COLUMN, MUSICIAN_ID_COLUMN,
          FROM_COLUMN))) {
        existsStatement.setInt(1, Band.this.getId());
        existsStatement.setInt(2, musician.getId());
        existsStatement.setDate(3, from);
        try (ResultSet resultSet = existsStatement.executeQuery()) {
          return resultSet.next();
        }
      }
    }

    @Override
    public void delete(Connection connection) throws SQLException {
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format(
          "delete from %s where %s = ? and %s = ? and %s = ?;", TABLE_NAME, BAND_ID_COLUMN, MUSICIAN_ID_COLUMN,
          FROM_COLUMN))) {
        preparedStatement.setInt(1, Band.this.getId());
        preparedStatement.setInt(2, musician.getId());
        preparedStatement.setDate(3, from);
        preparedStatement.execute();
      }
    }

    @Override
    public void save(Connection connection) throws SQLException {
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
        existsStatement.setInt(1, Band.this.getId());
        existsStatement.setInt(2, musician.getId());
        existsStatement.setDate(3, from);
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
          preparedStatement.setInt(1, Band.this.getId());
          preparedStatement.setInt(2, musician.getId());
          preparedStatement.setDate(3, from);
          preparedStatement.setDate(4, to);
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
          preparedStatement.setDate(1, to);
          preparedStatement.setInt(2, Band.this.getId());
          preparedStatement.setInt(3, musician.getId());
          preparedStatement.setDate(4, from);
          preparedStatement.execute();
        }
      }
    }
    //#endregion Database operations

    //#region String representations
    @Override
    public String toString() {
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
    @Override
    public int hashCode() {
      int hashCode = musician.hashCode() * 31 + Band.this.hashCode() * 31;
      if (from != null) {
        hashCode += 31 * from.hashCode();
      }
      if (to != null) {
        hashCode += 31 * to.hashCode();
      }
      return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
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
  public static final String TABLE_NAME = "Band";
  public static final String PRIMARY_KEY_NAME = "idArtist";
  public static final String CREATION_FUNCTION_NAME = "createBand";
  public static final String VIEW_NAME = "vBand";
  public static final String VIEW_ID_COLUMN = "id";
  public static final String VIEW_NAME_COLUMN = "name";
  //#endregion Static attributes

  //#region Instance attributes
  List<Member> members = new ArrayList<>();
  //#endregion Instance attributes

  //#region Constructors
  private Band(Integer id, String name) {
    super(id, name);
  }

  public Band(String name) {
    super(name);
    if (name == null) {
      throw new IllegalArgumentException("Band name cannot be null");
    }
  }
  //#endregion Constructors

  //#region Getters and Setters
  @Override
  public String tableName() {
    return TABLE_NAME;
  }

  @Override
  public String primaryKeyName() {
    return PRIMARY_KEY_NAME;
  }

  @Override
  public String creationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }

  public List<Member> getMembers() {
    return members;
  }

  public void addMember(Musician musician, Date from, Date to) {
    members.add(this.new Member(musician, from, to));
  }

  public void addMember(Musician musician, Date from) {
    members.add(new Member(musician, from));
  }

  public void addMember(Member member) {
    if (!member.getBand().equals(this)) {
      throw new IllegalArgumentException(String.format("Member %s has a different band", member));
    }
    members.add(member);
  }
  //#endregion Getters and Setters

  //#region Database operations
  @Override
  public void save(Connection connection) throws SQLException {
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

  public static Band findById(Integer id, Connection connection) throws SQLException, NoResultException {
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

  private void fetchMembers(Connection connection) throws SQLException, NoResultException {
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
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
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
