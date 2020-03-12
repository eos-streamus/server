package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.Connection;
import java.sql.SQLException;

public class CollectionDAO {
  private CollectionDAO() {}
  public static Collection findById(Integer id, Connection connection) throws SQLException, NoResultException {
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
