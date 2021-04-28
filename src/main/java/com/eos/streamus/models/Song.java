package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;

public class Song extends Resource implements SavableDeletableEntity {
  //#region Static attributes
  /**
   * Table name in database.
   */
  private static final String TABLE_NAME = "Song";
  /**
   * Primary key name in database.
   */
  private static final String PRIMARY_KEY_NAME = "idResource";
  /**
   * Creation function name in database.
   */
  private static final String CREATION_FUNCTION_NAME = "createSong";
  /**
   * View name in database.
   */
  private static final String VIEW_NAME = "vSong";
  //#endregion Static attributes

  //#region Constructors
  public Song(final String path, final String name, final int duration) {
    super(path, name, duration);
  }
  //#endregion Constructors

  //#region Getters and Setters

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

  /**
   * {@inheritDoc}
   */
  @Override
  public String creationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }
  //#endregion Getters and Setters

  //#region Database operations

  /**
   * Finds a Song by a given id in the database.
   *
   * @param id         Id of the Song to find.
   * @param connection {@link Connection} to use to perform the operation.
   * @return Found Song.
   * @throws SQLException      If an error occurred while performing the database operation.
   * @throws NoResultException if no Song by this id was found.
   */
  public static Song findById(final int id, final Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement statement = connection.prepareStatement(
        String.format("select * from %s where id = ?", VIEW_NAME)
    )) {
      statement.setInt(1, id);
      try (ResultSet rs = statement.executeQuery()) {
        if (!rs.next()) {
          throw new NoResultException();
        }
        Song song = new Song(
            rs.getString("path"),
            rs.getString("name"),
            rs.getInt("duration")
        );
        song.setId(rs.getInt("id"));
        song.setCreatedAt(rs.getTimestamp("createdAt"));
        return song;
      }
    }
  }
  //#endregion Database operations
}
