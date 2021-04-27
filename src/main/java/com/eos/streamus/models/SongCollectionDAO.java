package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.Connection;
import java.sql.SQLException;

public final class SongCollectionDAO {
  private SongCollectionDAO() {
  }

  /**
   * Finds a {@link SongCollection} by id.
   *
   * @param id         Id of {@link SongCollection} to find.
   * @param connection {@link Connection} to use to perform the operation.
   * @return Found {@link SongCollection}
   * @throws NoResultException if no {@link SongCollection} by this id was found in database.
   * @throws SQLException      If an error occurred while performing the database operation.
   */
  public static SongCollection findById(final Integer id, final Connection connection)
      throws SQLException, NoResultException {
    try {
      return SongPlaylist.findById(id, connection);
    } catch (NoResultException e) {
      // Ignore, could be Song
    }
    return Album.findById(id, connection);
  }
}
