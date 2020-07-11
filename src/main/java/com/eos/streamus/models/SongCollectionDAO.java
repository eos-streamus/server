package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.Connection;
import java.sql.SQLException;

public final class SongCollectionDAO {
  private SongCollectionDAO() {}

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
