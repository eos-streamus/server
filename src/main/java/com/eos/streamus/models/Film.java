package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Film extends Video {
  //#region Static attributes
  /** Table name in database. */
  private static final String TABLE_NAME = "Film";
  /** View name in database. */
  private static final String VIEW_NAME = "vfilm";
  /** Creation function name in database. */
  private static final String CREATION_FUNCTION_NAME = "createFilm";
  /** Primary key column name. */
  private static final String PRIMARY_KEY_NAME = "idVideo";
  //#endregion Static attributes

  //#region Constructors
  private Film(final Integer id, final String path, final String name,
               final Timestamp createdAt, final Integer duration) {
    super(id, path, name, createdAt, duration);
  }

  public Film(final String path, final String name, final Integer duration) {
    super(path, name, duration);
  }
  //#endregion Constructors

  //#region Getters and Setters
  @Override
  public final String creationFunctionName() {
    return CREATION_FUNCTION_NAME;
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
  public static Film findById(final int id, final Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement statement = connection.prepareStatement(
        String.format(
            "select * from %s where %s = ?",
            VIEW_NAME,
            Resource.ID_COLUMN
        )
    )) {
      statement.setInt(1, id);
      try (ResultSet rs = statement.executeQuery()) {
        if (!rs.next()) {
          throw new NoResultException();
        }
        return new Film(
            rs.getInt(Resource.ID_COLUMN),
            rs.getString(Resource.PATH_COLUMN),
            rs.getString(Resource.NAME_COLUMN),
            rs.getTimestamp(Resource.CREATED_AT_COLUMN),
            rs.getInt(Resource.DURATION_COLUMN)
        );
      }
    }
  }

  public static List<Film> all(final Connection connection) throws SQLException {
    List<Film> allFilms = new ArrayList<>();
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format("select * from %s", VIEW_NAME)
    )) {
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          allFilms.add(
              new Film(
                  resultSet.getInt(Resource.ID_COLUMN),
                  resultSet.getString(Resource.PATH_COLUMN),
                  resultSet.getString(Resource.NAME_COLUMN),
                  resultSet.getTimestamp(Resource.CREATED_AT_COLUMN),
                  resultSet.getInt(Resource.DURATION_COLUMN)
              )
          );
        }
      }
    }
    return allFilms;
  }
  //#endregion Database operations
}
