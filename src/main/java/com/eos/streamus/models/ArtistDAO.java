package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class ArtistDAO {
  private ArtistDAO() {
  }

  /**
   * Finds a {@link Artist} by id.
   *
   * @param id         Id of {@link Artist} to find.
   * @param connection {@link Connection} to use to perform the operation.
   * @return Found {@link Artist}
   * @throws NoResultException if no {@link Artist} by this id was found in database.
   * @throws SQLException      If an error occurred while performing the database operation.
   */
  public static Artist findById(final Integer id, final Connection connection) throws SQLException, NoResultException {
    try {
      return Musician.findById(id, connection);
    } catch (NoResultException e) {
      // Do nothing, could be Band
    }
    return Band.findById(id, connection);
  }

  /**
   * Fetches all {@link Artist} in database.
   *
   * @param connection {@link Connection} to use to perform the operation.
   * @return Found {@link SongCollection}
   * @throws SQLException If an error occurred while performing the database operation.
   */
  public static List<Artist> all(final Connection connection) throws SQLException {
    List<Artist> artists = new ArrayList<>();
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select distinct id from %s",
            Artist.TABLE_NAME
        )
    )) {
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          try {
            artists.add(findById(resultSet.getInt("id"), connection));
          } catch (NoResultException e) {
            Logger.getLogger(ArtistDAO.class.getName()).severe(e.getMessage());
          }
        }
      }
    }
    return artists;
  }

}
