package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.exceptions.NotPersistedException;
import com.eos.streamus.utils.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public abstract class SongCollection extends Collection {
  public class Track extends Pair<Integer, Song> implements SavableDeletable {
    //#region Static attributes
    /** Table name in database. */
    public static final String TABLE_NAME = "SongCollectionSong";
    /** Track number column in database. */
    public static final String TRACK_NUMBER_COLUMN = "trackNumber";
    /** Id_song column in database. */
    public static final String ID_SONG_COLUMN = "idSong";
    /** Id_song collection column in database. */
    public static final String ID_SONG_COLLECTION_COLUMN = "idSongCollection";
    /** Creation function name in database. */
    public static final String CREATION_FUNCTION_NAME = "addSongToSongCollection";
    //#endregion Static attributes

    //#region Constructors
    public Track(final Integer trackNumber, final Song song) {
      super(trackNumber, song);
    }
    //#endregion Constructors

    //#region Getters and Setters

    /**
     * @return This Track's number.
     */
    public Integer getTrackNumber() {
      return getKey();
    }

    /**
     * Set this Track's number.
     *
     * @param trackNumber Number to set.
     */
    public void setTrackNumber(final int trackNumber) {
      this.setKey(trackNumber);
    }

    /**
     * @return Track song.
     */
    public Song getSong() {
      return getValue();
    }
    //#endregion Getters and Setters

    //#region Database operations

    /** {@inheritDoc} */
    public void save(final Connection connection) throws SQLException {
      if (this.getKey() == null) {
        try (PreparedStatement songPreparedStatement = connection.prepareStatement(
            String.format("select * from %s(?, ?);", CREATION_FUNCTION_NAME)
        )) {
          songPreparedStatement.setInt(1, getValue().getId());
          songPreparedStatement.setInt(2, SongCollection.this.getId());
          try (ResultSet rs = songPreparedStatement.executeQuery()) {
            rs.next();
            setKey(rs.getInt(1));
          }
        }
      } else {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
            String.format(
                "insert into %s(%s, %s, %s) values (?, ?, ?);",
                TABLE_NAME,
                ID_SONG_COLLECTION_COLUMN,
                ID_SONG_COLUMN,
                TRACK_NUMBER_COLUMN
            )
        )) {
          int columnNumber = 0;
          preparedStatement.setInt(++columnNumber, SongCollection.this.getId());
          preparedStatement.setInt(++columnNumber, getSong().getId());
          preparedStatement.setInt(++columnNumber, getTrackNumber());
          preparedStatement.execute();
        }
      }
    }

    /**
     * Update track number in database.
     *
     * @param connection {@link Connection} to use to perform the operation.
     * @throws SQLException If an error occurred while performing the database operation.
     */
    private void updateTrackNumber(final Connection connection) throws SQLException {
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format(
              "update %s set %s = ? where %s = ? and %s = ?",
              TABLE_NAME,
              TRACK_NUMBER_COLUMN,
              ID_SONG_COLLECTION_COLUMN,
              ID_SONG_COLUMN
          )
      )) {
        int columnNumber = 0;
        preparedStatement.setInt(++columnNumber, getTrackNumber());
        preparedStatement.setInt(++columnNumber, SongCollection.this.getId());
        preparedStatement.setInt(++columnNumber, getSong().getId());
        preparedStatement.execute();
      }
    }

    /** {@inheritDoc} */
    @Override
    public void delete(final Connection connection) throws SQLException {
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format(
              "delete from %s where %s = ? and %s = ?",
              TABLE_NAME,
              ID_SONG_COLLECTION_COLUMN,
              ID_SONG_COLUMN
          )
      )) {
        preparedStatement.setInt(1, SongCollection.this.getId());
        preparedStatement.setInt(2, getValue().getId());
        preparedStatement.execute();
      }
    }
    //#endregion Database operations

    //#region String representations

    /** {@inheritDoc} */
    @Override
    public String toString() {
      return String.format(
          "%s[%s= %s, %s= %s, %s= %s]",
          getClass().getName(),
          ID_SONG_COLLECTION_COLUMN,
          SongCollection.this.getId(),
          ID_SONG_COLUMN,
          this.getValue(),
          TRACK_NUMBER_COLUMN,
          this.getKey()
      );
    }
    //#endregion String representations

    //#region Equals

    /**
     * @return Track's hashcode, combined from track number and song hashcode.
     */
    @Override
    public int hashCode() {
      return Objects.hash(getKey(), getValue().hashCode());
    }

    /**
     * Returns whether the given Object is equal to this Track.
     * Will be equal if:
     * - Not null
     * - Same class
     * - Same song
     *
     * @param o Object to compare.
     * @return True if conditions are met.
     */
    @Override
    public boolean equals(final Object o) {
      if (o == null || o.getClass() != getClass()) {
        return false;
      }
      Track track = (Track) o;
      return track.getValue().equals(getValue());
    }
    //#endregion Equals
  }

  //#region Instance attributes
  /** List of tracks of this SongCollection. */
  private final List<Track> tracks = new ArrayList<>();
  //#endregion Instance attributes

  //#region Constructors
  protected SongCollection(final Integer id, final String name, final Timestamp createdAt,
                           final Timestamp updatedAt) {
    super(id, name, createdAt, updatedAt);
  }

  protected SongCollection(final String name) {
    super(name);
  }
  //#endregion Constructors

  //#region Accessors
  public final List<Track> getTracks() {
    return tracks;
  }

  /** @return SongCollection-specific content. */
  @Override
  protected List<Pair<Integer, Resource>> getSpecificContent() {
    List<Pair<Integer, Resource>> content = new ArrayList<>();
    for (Track track : tracks) {
      content.add(new Pair<>(track.getKey(), track.getValue()));
    }
    return content;
  }

  /**
   * Adds a song as a new Track at the end of the playlist. This track is <b>not saved to the database</b>.
   *
   * @param song Song to add.
   * @return Newly created Track.
   */
  public Track addSong(final Song song) {
    Integer newTrackNumber = 0;
    for (Track track : tracks) {
      if (track.getKey() > newTrackNumber) {
        newTrackNumber = track.getKey();
      }
    }
    Track track = new Track(newTrackNumber + 1, song);
    tracks.add(track);
    return track;
  }

  /**
   * Add track to playlist if song not already present.
   *
   * @param track Track to add.
   */
  public void addTrack(final Track track) {
    if (!this.tracks.contains(track)) {
      this.tracks.add(track);
    }
  }

  /** {@inheritDoc} */
  @Override
  public String tableName() {
    return TABLE_NAME;
  }

  /** {@inheritDoc} */
  @Override
  public String primaryKeyName() {
    return PRIMARY_KEY_NAME;
  }
  //#endregion Accessors

  //#region Database operations

  /** {@inheritDoc} */
  @Override
  public void save(final Connection connection) throws SQLException {
    super.save(connection);
    List<Track> databaseTracks = getTracksFromDatabase(connection);
    sortTracks();
    for (Track track : this.getTracks()) {
      if (!databaseTracks.contains(track)) {
        if (track.getValue().getId() == null) {
          throw new NotPersistedException(
              String.format(
                  "%s %s is not persisted",
                  track.getValue().tableName(),
                  track.getValue()
              )
          );
        }
        track.save(connection);
      }
    }
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select %s from %s where %s = ?",
            UPDATED_AT_COLUMN,
            Collection.TABLE_NAME,
            Collection.PRIMARY_KEY_NAME
        )
    )) {
      preparedStatement.setInt(1, getId());
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        resultSet.next();
        setUpdatedAt(resultSet.getTimestamp(Collection.UPDATED_AT_COLUMN));
      }
    }
  }

  /**
   * Fetches Tracks from database.
   *
   * @param connection {@link Connection} to use to perform the operation.
   * @return List of {@link Track} of this SongCollection.
   * @throws SQLException If an error occurred while performing the database operation.
   */
  private List<Track> getTracksFromDatabase(final Connection connection) throws SQLException {
    List<Track> loadedTracks = new ArrayList<>();
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select * from %s where %s = ?;",
            Track.TABLE_NAME,
            Track.ID_SONG_COLLECTION_COLUMN
        )
    )) {
      preparedStatement.setInt(1, this.getId());
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          try {
            loadedTracks.add(
                new Track(
                    resultSet.getInt(Track.TRACK_NUMBER_COLUMN),
                    Song.findById(resultSet.getInt(Track.ID_SONG_COLUMN), connection)
                )
            );
          } catch (NoResultException e) {
            throw new SQLException(
                String.format(
                    "%s {%s: %d, %s: %d, %s: %d} references non existing song",
                    Track.TABLE_NAME,
                    Track.ID_SONG_COLLECTION_COLUMN,
                    getId(),
                    Track.ID_SONG_COLUMN,
                    resultSet.getInt(Track.ID_SONG_COLUMN),
                    Track.TRACK_NUMBER_COLUMN,
                    resultSet.getInt(Track.TRACK_NUMBER_COLUMN)
                )
            );
          }
        }
      }
    }
    return loadedTracks;
  }

  /**
   * Move an existing track in the playlist to a different track number.
   *
   * @param trackToUpdate  Track whose track number should be changed.
   * @param newTrackNumber New track number of track to update.
   * @param connection     SQLConnection to use for queries.
   * @throws SQLException If SQL statements fail or if integrity constraints are violated.
   */
  public void moveTrack(final Track trackToUpdate, final int newTrackNumber, final Connection connection)
      throws SQLException {
    final int oldTrackNumber = trackToUpdate.getTrackNumber();
    if (!tracks.contains(trackToUpdate)) {
      return;
    }
    final boolean upwards = newTrackNumber > oldTrackNumber;
    final int step = upwards ? 1 : -1;
    for (int i = oldTrackNumber;
         upwards && i < newTrackNumber || !upwards && i > newTrackNumber;
         i += step) {
      sortTracks();
      Track track1;
      Track track2;
      if (upwards) {
        track2 = tracks.get(i - 1);
        track1 = tracks.get(i - 1 + step);
      } else {
        track1 = tracks.get(i - 1);
        track2 = tracks.get(i - 1 + step);
      }
      swapTrackNumbers(track1, track2, connection);
    }
    save(connection);
  }

  /**
   * Remove track from playlist if present.
   *
   * @param track Track to remove from SongPlaylist.
   */
  public void removeTrack(final Track track) {
    tracks.remove(track);
  }

  /**
   * Swaps the two given {@link Track}'s numbers in this SongCollection.
   *
   * @param track1     First Track.
   * @param track2     Second Track.
   * @param connection {@link Connection} to use to perform the operation.
   * @throws SQLException If an error occurred while performing the database operation.
   */
  private void swapTrackNumbers(final Track track1, final Track track2, final Connection connection)
      throws SQLException {
    final int tmpTrackNumber = track1.getTrackNumber();
    track1.setTrackNumber(track2.getTrackNumber());
    track2.setTrackNumber(tmpTrackNumber);
    track1.updateTrackNumber(connection);
    track2.updateTrackNumber(connection);
  }
  //#endregion Database operations

  //#region Private methods
  private void sortTracks() {
    this.tracks.sort(Comparator.comparingInt(Track::getTrackNumber));
  }
  //#endregion

  //#region Equals

  /** @return HashCode of this instance, i.e. its id. */
  @Override
  public int hashCode() {
    return getId();
  }

  /**
   * Returns whether the given Object is equal to this SongCollection.
   * Will be equal if:
   * - All {@link Collection}'s equality conditions are met.
   * - Same {@link Track}s.
   *
   * @param o Object to compare
   * @return True if all conditions are met.
   */
  @Override
  public boolean equals(final Object o) {
    if (!super.equals(o)) {
      return false;
    }
    SongCollection songCollection = (SongCollection) o;
    if (((SongCollection) o).tracks.size() != this.tracks.size()) {
      return false;
    }
    for (Track track : tracks) {
      if (!songCollection.tracks.contains(track)) {
        return false;
      }
    }
    return true;
  }
  //#endregion Equals
}
