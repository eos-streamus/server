package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.Connection;
import java.sql.SQLException;

public final class ResourceDAO {
  private ResourceDAO() {
  }

  /**
   * Finds a {@link Resource} by id.
   *
   * @param id         Id of {@link Resource} to find.
   * @param connection {@link Connection} to use to perform the operation.
   * @return Found {@link Resource}
   * @throws NoResultException if no {@link Resource} by this id was found in database.
   * @throws SQLException      If an error occurred while performing the database operation.
   */
  public static Resource findById(final Integer id, final Connection connection)
      throws SQLException, NoResultException {
    try {
      return VideoDAO.findById(id, connection);
    } catch (NoResultException e) {
      // Ignore, could be Song
    }
    return Song.findById(id, connection);
  }

}
