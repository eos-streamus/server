package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.Connection;
import java.sql.SQLException;

public final class CollectionDAO {
  private CollectionDAO() {
  }

  /**
   * Finds a {@link Collection} by id.
   *
   * @param id         Id of {@link Collection} to find.
   * @param connection {@link Connection} to use to perform the operation.
   * @return Found {@link Collection}
   * @throws NoResultException if no {@link Collection} by this id was found in database.
   * @throws SQLException      If an error occurred while performing the database operation.
   */
  public static Collection findById(final Integer id, final Connection connection)
      throws SQLException, NoResultException {
    Collection collection = null;
    try {
      collection = VideoPlaylist.findById(id, connection);
    } catch (NoResultException e) {
      // Do nothing, could be other collection types
    }
    if (collection == null) {
      try {
        collection = Series.findById(id, connection);
      } catch (NoResultException e) {
        // Do nothing, could be other collection types
      }
    }
    if (collection == null) {
      collection = SongPlaylist.findById(id, connection);
    }
    return collection;
  }

}
