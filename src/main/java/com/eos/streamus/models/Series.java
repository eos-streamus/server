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
    public static final String TABLE_NAME = "Episode";
    public static final String PRIMARY_KEY_NAME = "idVideo";
    public static final String SEASON_NUMBER_COLUMN = "seasonNumber";
    public static final String EPISODE_NUMBER_COLUMN = "episodeNumber";
    public static final String CREATION_FUNCTION_NAME = "createEpisode";
    public static final String VIEW_NAME = "vEpisode";
    public static final String SERIES_ID_COLUMN = "idSeries";
    //#endregion Static attributes

    //#region Instance attributes
    private final short seasonNumber;
    private final short episodeNumber;
    //#endregion Instance attributes

    //#region Constructors
    Episode(Integer id, String path, String name, Timestamp createdAt, Integer duration, final short seasonNumber,
            final short episodeNumber) {
      super(id, path, name, createdAt, duration);
      this.seasonNumber = seasonNumber;
      this.episodeNumber = episodeNumber;
      Series.this.episodes.add(this);
    }

    public Episode(String path, String name, Integer duration, final short seasonNumber, final short episodeNumber) {
      super(path, name, duration);
      this.seasonNumber = seasonNumber;
      this.episodeNumber = episodeNumber;
      Series.this.episodes.add(this);
    }

    public Episode(String path, String name, Integer duration, final short seasonNumber) {
      this(path, name, duration, seasonNumber, (short) (Series.this.getNumberOfEpisodesInSeason(seasonNumber) + 1));
    }
    //#endregion Constructors

    //#region Getters and Setters
    public short getSeasonNumber() {
      return seasonNumber;
    }

    public short getEpisodeNumber() {
      return episodeNumber;
    }

    public Series getSeries() {
      return Series.this;
    }

    @Override
    public String tableName() {
      return TABLE_NAME;
    }

    @Override
    public String primaryKeyName() {
      return PRIMARY_KEY_NAME;
    }

    @Override
    public String creationFunctionName() {
      return CREATION_FUNCTION_NAME;
    }
    //#endregion Getters and Setters

    //#region Database operations
    @Override
    public void save(Connection connection) throws SQLException {
      if (Series.this.getId() == null) {
        throw new NotPersistedException("Episode series not persisted");
      }
      if (this.getId() == null) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format(
            "select * from %s(?::varchar(1041), ?::varchar(200), ?, ?, ?::smallint, ?::smallint);",
            CREATION_FUNCTION_NAME))) {
          preparedStatement.setString(1, getPath());
          preparedStatement.setString(2, getName());
          preparedStatement.setInt(3, getDuration());
          preparedStatement.setInt(4, Series.this.getId());
          preparedStatement.setShort(5, seasonNumber);
          preparedStatement.setShort(6, episodeNumber);
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
    @Override
    public int hashCode() {
      return getId();
    }

    @Override
    public boolean equals(Object o) {
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
  public static final String TABLE_NAME = "Series";
  public static final String PRIMARY_KEY_NAME = "idVideoCollection";
  public static final String VIEW_NAME = "vSeries";
  public static final String CREATION_FUNCTION_NAME = "createSeries";
  public static final String EPISODE_ID_VIEW_COLUMN = "idEpisode";
  public static final String EPISODE_CREATED_AT_COLUMN = "episodeCreatedAt";
  public static final String EPISODE_NAME_COLUMN = "episodeName";
  //#endregion

  //#region Instance attributes
  final List<Episode> episodes = new ArrayList<>();
  //#endregion

  //#region Constructors
  private Series(Integer id, String name, Timestamp createdAt, Timestamp updatedAt) {
    super(id, name, createdAt, updatedAt);
  }

  public Series(String name) {
    super(name);
  }
  //#endregion Constructors

  //#region Getters and Setters
  @Override
  public String tableName() {
    return TABLE_NAME;
  }

  @Override
  public String creationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }

  @Override
  public String primaryKeyName() {
    return PRIMARY_KEY_NAME;
  }

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

  public short getNumberOfSeasons() {
    List<Short> distinctSeasonNumbers = new ArrayList<>();
    for (Episode episode : episodes) {
      if (!distinctSeasonNumbers.contains(episode.seasonNumber)) {
        distinctSeasonNumbers.add(episode.seasonNumber);
      }
    }
    return (short) distinctSeasonNumbers.size();
  }

  public short getNumberOfEpisodesInSeason(final short seasonNumber) {
    return (short) episodes.stream().filter(e -> e.seasonNumber == seasonNumber).count();
  }

  public List<Episode> getSeason(short seasonNumber) {
    return
        episodes
            .stream()
            .filter(e -> e.seasonNumber == seasonNumber)
            .sorted(Comparator.comparingInt(e -> e.episodeNumber))
            .collect(Collectors.toList());
  }
  //#endregion Getters and Setters

  //#region Database operations
  @Override
  public void save(Connection connection) throws SQLException {
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

  public static Series findById(int id, Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection
        .prepareStatement(String.format("select * from %s where %s = ?;", VIEW_NAME, Collection.PRIMARY_KEY_NAME))) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (!resultSet.next()) {
          throw new NoResultException();
        }
        Series series = new Series(
            resultSet.getInt(Collection.PRIMARY_KEY_NAME),
            resultSet.getString(Collection.NAME_COLUMN),
            resultSet.getTimestamp(Collection.CREATED_AT_COLUMN),
            resultSet.getTimestamp(Collection.UPDATED_AT_COLUMN)
        );

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
  @Override
  public int hashCode() {
    return getId();
  }

  @Override
  public boolean equals(Object o) {
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
