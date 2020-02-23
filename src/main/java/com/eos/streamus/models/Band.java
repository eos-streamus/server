package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.exceptions.NotPersistedException;

import java.sql.*;

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

    //#region Database operations
    @Override
    public void delete(Connection connection) throws SQLException {
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("delete from %s where %s = ? and %s = ? and %s = ?;", TABLE_NAME, BAND_ID_COLUMN, MUSICIAN_ID_COLUMN, FROM_COLUMN))) {
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
      try (PreparedStatement existsStatement = connection.prepareStatement(String.format("select 1 from %s where %s = ? and %s = ? and %s = ?", TABLE_NAME, BAND_ID_COLUMN, MUSICIAN_ID_COLUMN, FROM_COLUMN))) {
        existsStatement.setInt(1, Band.this.getId());
        existsStatement.setInt(2, musician.getId());
        existsStatement.setDate(3, from);
        try (ResultSet resultSet = existsStatement.executeQuery()) {
          exists = resultSet.next();
        }
      }
      if (!exists) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("insert into %s(%s, %s, %s, %s) values (?, ?, ?, ?);", TABLE_NAME, BAND_ID_COLUMN, MUSICIAN_ID_COLUMN, FROM_COLUMN, TO_COLUMN))) {
          preparedStatement.setInt(1, Band.this.getId());
          preparedStatement.setInt(2, musician.getId());
          preparedStatement.setDate(3, from);
          preparedStatement.setDate(4, to);
          preparedStatement.execute();
        }
      } else {
        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("update %s set %s = ? where %s = ? and %s = ? and %s = ?", TABLE_NAME, TO_COLUMN, BAND_ID_COLUMN, MUSICIAN_ID_COLUMN, FROM_COLUMN))) {
          preparedStatement.setDate(1, to);
          preparedStatement.setInt(2, Band.this.getId());
          preparedStatement.setInt(3, musician.getId());
          preparedStatement.setDate(4, from);
          preparedStatement.execute();
        }
      }
    }
    //#endregion Database operations
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
  public String getTableName() {
    return TABLE_NAME;
  }

  @Override
  public String getPrimaryKeyName() {
    return PRIMARY_KEY_NAME;
  }

  @Override
  public String getCreationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }
  //#endregion Getters and Setters

  //#region Database operations
  @Override
  public void save(Connection connection) throws SQLException {
    if (this.getId() == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s(?);", CREATION_FUNCTION_NAME))) {
        preparedStatement.setString(1, getName());
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          resultSet.next();
          this.setId(resultSet.getInt(Artist.PRIMARY_KEY_NAME));
        }
      }
    } else {
      super.save(connection);
    }
  }

  public static Band findById(Integer id, Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s where %s = ?", VIEW_NAME, VIEW_ID_COLUMN))) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (!resultSet.next()) {
          throw new NoResultException();
        }
        return new Band(
          id,
          resultSet.getString(VIEW_NAME_COLUMN)
        );
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
    return super.equals(obj);
  }
  //#endregion Equals
}
