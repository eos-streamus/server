package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.exceptions.NotPersistedException;
import com.eos.streamus.utils.Pair;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Series extends VideoCollection {
  public class Episode extends Video {
    //#region Static attributes
    /**
     * Table name in the database.
     */
    public static final String TABLE_NAME = "Episode";
    /**
     * Primary key name in the database.
     */
    public static final String PRIMARY_KEY_NAME = "idVideo";
    /**
     * Season number column name in the database.
     */
    public static final String SEASON_NUMBER_COLUMN = "seasonNumber";
    /**
     * Episode number column name in the database.
     */
    public static final String EPISODE_NUMBER_COLUMN = "episodeNumber";
    /**
     * Creation function name in the database.
     */
    public static final String CREATION_FUNCTION_NAME = "createEpisode";
    /**
     * View name in the database.
     */
    public static final String VIEW_NAME = "vEpisode";
    /**
     * Series id column name in the database.
     */
    public static final String SERIES_ID_COLUMN = "idSeries";
    //#endregion Static attributes

    //#region Instance attributes
    /**
     * Season number.
     */
    private final short seasonNumber;
    /**
     * Episode number.
     */
    private final short episodeNumber;
    //#endregion Instance attributes

    //#region Constructors
    Episode(final Integer id, final String path, final String name, final Timestamp createdAt,
            final Integer duration, final short seasonNumber,
            final short episodeNumber) {
      super(id, path, name, createdAt, duration);
      this.seasonNumber = seasonNumber;
      this.episodeNumber = episodeNumber;
      Series.this.episodes.add(this);
    }

    public Episode(final String path, final String name, final Integer duration,
                   final short seasonNumber, final short episodeNumber) {
      super(path, name, duration);
      this.seasonNumber = seasonNumber;
      this.episodeNumber = episodeNumber;
      Series.this.episodes.add(this);
    }

    public Episode(final String path, final String name, final Integer duration, final short seasonNumber) {
      this(path, name, duration, seasonNumber, (short) (Series.this.getNumberOfEpisodesInSeason(seasonNumber) + 1));
    }
    //#endregion Constructors

    //#region Getters and Setters

    /**
     * @return SeasonNumber.
     */
    public short getSeasonNumber() {
      return seasonNumber;
    }

    /**
     * @return EpisodeNumber.
     */
    public short getEpisodeNumber() {
      return episodeNumber;
    }

    /**
     * @return Series.
     */
    public Series getSeries() {
      return Series.this;
    }

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
     * {@inheritDoc}
     */
    @Override
    public void save(final Connection connection) throws SQLException {
      if (Series.this.getId() == null) {
        throw new NotPersistedException("Episode series not persisted");
      }
      if (this.getId() == null) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format(
            "select * from %s(?::varchar(1041), ?::varchar(200), ?, ?, ?::smallint, ?::smallint);",
            CREATION_FUNCTION_NAME))) {
          int columnNumber = 0;
          preparedStatement.setString(++columnNumber, getPath());
          preparedStatement.setString(++columnNumber, getName());
          preparedStatement.setInt(++columnNumber, getDuration());
          preparedStatement.setInt(++columnNumber, Series.this.getId());
          preparedStatement.setShort(++columnNumber, seasonNumber);
          preparedStatement.setShort(++columnNumber, episodeNumber);
          try (ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            setId(resultSet.getInt(Collection.PRIMARY_KEY_NAME));
            setCreatedAt(resultSet.getTimestamp(Collection.CREATED_AT_COLUMN));
          }
        }
      } else {
        super.save(connection);
      }
    }
    //#endregion Database operations

    //#region String representations

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      return String.format(
          "%s[%s=%d, %s=%d, %s=%d]",
          getClass().getName(),
          primaryKeyName(),
          getId(),
          SEASON_NUMBER_COLUMN,
          seasonNumber,
          EPISODE_NUMBER_COLUMN,
          episodeNumber
      );
    }
    //#endregion String representations

    //#region Equals

    /**
     * @return Hashcode, i.e. id.
     */
    @Override
    public int hashCode() {
      return getId();
    }

    /**
     * Returns whether the given Object is equal.
     * Equal if:
     * - Same {@link Series} id.
     * - Same episode and season numbers.
     *
     * @param o Object to compare.
     * @return True if all conditions are met.
     */
    @Override
    public boolean equals(final Object o) {
      if (!super.equals(o)) {
        return false;
      }
      Episode episode = (Episode) o;
      return
          episode.getSeries().getId().equals(Series.this.getId()) &&
              // Don't compare Series instances to avoid infinite recursion
              episode.seasonNumber == seasonNumber &&
              episode.episodeNumber == episodeNumber;
    }
    //#endregion Equals
  }

  //#region Static attributes
  /**
   * Table name in the database.
   */
  public static final String TABLE_NAME = "Series";
  /**
   * Primary key name in the database.
   */
  public static final String PRIMARY_KEY_NAME = "idVideoCollection";
  /**
   * View name in the database.
   */
  public static final String VIEW_NAME = "vSeries";
  /**
   * Creation function name in the database.
   */
  public static final String CREATION_FUNCTION_NAME = "createSeries";
  /**
   * Episode id view_column in the database.
   */
  public static final String EPISODE_ID_VIEW_COLUMN = "idEpisode";
  /**
   * Episode created at column in the database.
   */
  public static final String EPISODE_CREATED_AT_COLUMN = "episodeCreatedAt";
  /**
   * Episode name column in the database.
   */
  public static final String EPISODE_NAME_COLUMN = "episodeName";
  //#endregion

  //#region Instance attributes
  /**
   * List of {@link Episode}s.
   */
  private final List<Episode> episodes = new ArrayList<>();
  //#endregion

  //#region Constructors
  public Series(final String name) {
    super(name);
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
  public String creationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String primaryKeyName() {
    return PRIMARY_KEY_NAME;
  }

  /**
   * @return Series-specific content.
   */
  @Override
  protected List<Pair<Integer, Resource>> getSpecificContent() {
    List<Pair<Integer, Resource>> content = new ArrayList<>();
    for (Episode episode : episodes) {
      int offset = 0;
      for (short i = 0; i < episode.seasonNumber; i++) {
        offset += getNumberOfEpisodesInSeason(i);
      }
      content.add(new Pair<>(offset + episode.episodeNumber, episode));
    }
    return content;
  }

  /**
   * @return Number of seasons.
   */
  public short getNumberOfSeasons() {
    List<Short> distinctSeasonNumbers = new ArrayList<>();
    for (Episode episode : episodes) {
      if (!distinctSeasonNumbers.contains(episode.seasonNumber)) {
        distinctSeasonNumbers.add(episode.seasonNumber);
      }
    }
    return (short) distinctSeasonNumbers.size();
  }

  /**
   * Returns number of episodes for given season id.
   *
   * @param seasonNumber Season number.
   * @return Number of episodes.
   */
  public short getNumberOfEpisodesInSeason(final short seasonNumber) {
    return (short) episodes.stream().filter(e -> e.seasonNumber == seasonNumber).count();
  }

  /**
   * Get episodes in given season number.
   *
   * @param seasonNumber Season number.s
   * @return List of {@link Episode}s in season.
   */
  public List<Episode> getSeason(final short seasonNumber) {
    return
        episodes
            .stream()
            .filter(e -> e.seasonNumber == seasonNumber)
            .sorted(Comparator.comparingInt(e -> e.episodeNumber))
            .collect(Collectors.toList());
  }
  //#endregion Getters and Setters

  //#region Database operations

  /**
   * {@inheritDoc}
   */
  @Override
  public void save(final Connection connection) throws SQLException {
    if (this.getId() == null) {
      try (PreparedStatement preparedStatement = connection
          .prepareStatement(String.format("select * from %s(?::varchar(200))", CREATION_FUNCTION_NAME))) {
        preparedStatement.setString(1, getName());
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          resultSet.next();
          this.setId(resultSet.getInt(Collection.PRIMARY_KEY_NAME));
          this.setCreatedAt(resultSet.getTimestamp(Collection.CREATED_AT_COLUMN));
          this.setUpdatedAt(resultSet.getTimestamp(Collection.UPDATED_AT_COLUMN));
        }
      }
    } else {
      super.save(connection);
    }
    for (Episode episode : episodes) {
      episode.save(connection);
    }
    if (!episodes.isEmpty()) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format(
          "select %s from %s where %s = ?;", UPDATED_AT_COLUMN, VIEW_NAME, Collection.PRIMARY_KEY_NAME))) {
        preparedStatement.setInt(1, getId());
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          resultSet.next();
          setUpdatedAt(resultSet.getTimestamp(UPDATED_AT_COLUMN));
        }
      }
    }
  }

  /**
   * Find a Series by id.
   *
   * @param id         Id of Series.
   * @param connection {@link Connection} to use to perform the operation.
   * @return Found Series.
   * @throws SQLException      If an error occurred while performing the database operation.
   * @throws NoResultException if no Series by this id was found.
   */
  public static Series findById(final int id, final Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection
        .prepareStatement(String.format("select * from %s where %s = ?;", VIEW_NAME, Collection.PRIMARY_KEY_NAME))) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (!resultSet.next()) {
          throw new NoResultException();
        }
        Series series = new Series(resultSet.getString(Collection.NAME_COLUMN));
        series.setId(resultSet.getInt(Collection.PRIMARY_KEY_NAME));
        series.setCreatedAt(resultSet.getTimestamp(Collection.CREATED_AT_COLUMN));
        series.setUpdatedAt(resultSet.getTimestamp(Collection.UPDATED_AT_COLUMN));

        // Handle episodes
        int firstEpisodeNumber = resultSet.getInt(EPISODE_ID_VIEW_COLUMN);
        if (!resultSet.wasNull()) {
          series.new Episode(
              firstEpisodeNumber,
              resultSet.getString(Resource.PATH_COLUMN),
              resultSet.getString(EPISODE_NAME_COLUMN),
              resultSet.getTimestamp(EPISODE_CREATED_AT_COLUMN),
              resultSet.getInt(Resource.DURATION_COLUMN),
              resultSet.getShort(Episode.SEASON_NUMBER_COLUMN),
              resultSet.getShort(Episode.EPISODE_NUMBER_COLUMN)
          );
          while (resultSet.next()) {
            series.new Episode(
                resultSet.getInt(EPISODE_ID_VIEW_COLUMN),
                resultSet.getString(Resource.PATH_COLUMN),
                resultSet.getString(EPISODE_NAME_COLUMN),
                resultSet.getTimestamp(EPISODE_CREATED_AT_COLUMN),
                resultSet.getInt(Resource.DURATION_COLUMN),
                resultSet.getShort(Episode.SEASON_NUMBER_COLUMN),
                resultSet.getShort(Episode.EPISODE_NUMBER_COLUMN)
            );
          }
        }
        return series;
      }
    }
  }
  //#endregion Database operations

  //#region Equals

  /**
   * @return HashCode, i.e. id.
   */
  @Override
  public int hashCode() {
    return getId();
  }

  /**
   * Returns whether the given Object is equal to this SongCollection.
   * Will be equal if:
   * - All {@link Collection}'s equality conditions are met.
   * - Same number of seasons.
   * - Same episodes.
   *
   * @param o Object to compare
   * @return True if all conditions are met.
   */
  @Override
  public boolean equals(final Object o) {
    if (!super.equals(o)) {
      return false;
    }

    Series series = (Series) o;

    if (series.getNumberOfSeasons() != getNumberOfSeasons()) {
      return false;
    }

    if (series.episodes.size() != episodes.size()) {
      return false;
    }

    for (Episode episode : episodes) {
      if (!series.episodes.contains(episode)) {
        return false;
      }
    }
    return true;
  }
  //#endregion Equals
}
