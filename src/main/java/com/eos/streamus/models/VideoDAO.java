package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class VideoDAO {
  private VideoDAO() {
  }

  /**
   * Finds a {@link Video} by id.
   *
   * @param id         Id of {@link Video} to find.
   * @param connection {@link Connection} to use to perform the operation.
   * @return Found {@link Video}
   * @throws NoResultException if no {@link Video} by this id was found in database.
   * @throws SQLException      If an error occurred while performing the database operation.
   */
  public static synchronized Video findById(final Integer id, final Connection connection)
      throws NoResultException, SQLException {
    // Ignore NoResultException later
    try {
      return Film.findById(id, connection);
    } catch (NoResultException e) {
      // Ignore, could be episode
    }
    // Episode
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select * from %s where %s = ?;",
            Series.Episode.VIEW_NAME,
            Resource.ID_COLUMN
        )
    )) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (!resultSet.next()) {
          throw new NoResultException();
        }
        Series series = Series.findById(resultSet.getInt(Series.Episode.SERIES_ID_COLUMN), connection);
        return series.new Episode(
            id,
            resultSet.getString(Resource.PATH_COLUMN),
            resultSet.getString(Resource.NAME_COLUMN),
            resultSet.getTimestamp(Resource.CREATED_AT_COLUMN),
            resultSet.getInt(Resource.DURATION_COLUMN),
            resultSet.getShort(Series.Episode.SEASON_NUMBER_COLUMN),
            resultSet.getShort(Series.Episode.EPISODE_NUMBER_COLUMN)
        );
      }
    }
  }

}
