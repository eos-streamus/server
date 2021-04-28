package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Film extends Video {
  //#region Static attributes
  /** Table name in the database. */
  private static final String TABLE_NAME = "Film";
  /** View name in the database. */
  private static final String VIEW_NAME = "vFilm";
  /** Creation function name in the database. */
  private static final String CREATION_FUNCTION_NAME = "createFilm";
  /** Primary key name in the database. */
  private static final String PRIMARY_KEY_NAME = "idVideo";
  //#endregion Static attributes

  //#region Constructors
  public Film(final String path, final String name, final Integer duration) {
    super(path, name, duration);
  }
  //#endregion Constructors

  //#region Getters and Setters

  /** {@inheritDoc} */
  @Override
  public String creationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }

  /** {@inheritDoc} */
  @Override
  public String tableName() {
    return TABLE_NAME;
  }

  /** {@inheritDoc} */
  @Override
  public String primaryKeyName() {
    return PRIMARY_KEY_NAME;
  }
  //#endregion Getters and Setters

  //#region Database operations

  /**
   * Finds a Film by given id.
   *
   * @param id         Id of Film to find.
   * @param connection {@link Connection} to use to perform the operation.
   * @return Found Film.
   * @throws SQLException      If an error occurred while performing the database operation.
   * @throws NoResultException If no film by given id was found.
   */
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
        Film film = new Film(
            rs.getString(Resource.PATH_COLUMN),
            rs.getString(Resource.NAME_COLUMN),
            rs.getInt(Resource.DURATION_COLUMN)
        );
        film.setId(rs.getInt(Resource.ID_COLUMN));
        film.setCreatedAt(rs.getTimestamp(Resource.CREATED_AT_COLUMN));
        return film;
      }
    }
  }

  /**
   * Fetches all Films from database.
   *
   * @param connection {@link Connection} to use to perform the operation.
   * @return List of all Films.
   * @throws SQLException If an error occurred while performing the database operation.
   */
  public static List<Film> all(final Connection connection) throws SQLException {
    List<Film> allFilms = new ArrayList<>();
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format("select * from %s", VIEW_NAME)
    )) {
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          Film film = new Film(
              resultSet.getString(Resource.PATH_COLUMN),
              resultSet.getString(Resource.NAME_COLUMN),
              resultSet.getInt(Resource.DURATION_COLUMN)
          );
          film.setId(resultSet.getInt(Resource.ID_COLUMN));
          film.setCreatedAt(resultSet.getTimestamp(Resource.CREATED_AT_COLUMN));
          allFilms.add(film);
        }
      }
    }
    return allFilms;
  }
  //#endregion Database operations
}
