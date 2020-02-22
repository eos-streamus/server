package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.exceptions.NotPersistedException;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class SongCollection extends Collection {
  private static final String TABLE_NAME = "SongCollection";
  private static final String PRIMARY_KEY_NAME = "idCollection";

  private List<Track> tracks = new ArrayList<>();

  protected SongCollection(Integer id, String name, Timestamp createdAt, Timestamp updatedAt, Track... tracks) {
    super(id, name, createdAt, updatedAt);
    initTracks(tracks);
  }

  protected SongCollection(String name, Track... tracks) {
    super(name);
    initTracks(tracks);
  }

  private void initTracks(Track... tracks) {
    this.tracks.addAll(Arrays.asList(tracks));
  }

  public final List<Track> getTracks() {
    List<Track> tracksCopy = new ArrayList<>();
    for (Track track : this.tracks) {
      tracksCopy.add(new Track(track.getKey(), track.getValue()));
    }
    return tracksCopy;
  }

  public void addSong(Song song) {
    Integer newTrackNumber = 0;
    for (Track track : tracks) {
      if (track.getKey() > newTrackNumber) {
        newTrackNumber = track.getKey();
      }
    }
    tracks.add(new Track(newTrackNumber + 1, song));
  }

  @Override
  public void save(Connection connection) throws SQLException {
    super.save(connection);
    List<Track> databaseTracks = getTracksFromDatabase(connection);
    for (Track track : this.getTracks()) {
      if (!databaseTracks.contains(track)) {
        if (track.getValue().getId() == null) {
          throw new NotPersistedException(String.format("Song %s is not persisted", track.getValue()));
        }
        try (PreparedStatement songPreparedStatement = connection.prepareStatement("select * from addSongToSongCollection(?, ?);")) {
          songPreparedStatement.setInt(1, track.getValue().getId());
          songPreparedStatement.setInt(2, this.getId());
          try (ResultSet rs = songPreparedStatement.executeQuery()) {
            rs.next();
            track.setKey(rs.getInt(1));
          }
        }
      }
    }
    try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select %s from %s where %s = ?", UPDATED_AT_COLUMN, Collection.TABLE_NAME, Collection.PRIMARY_KEY_NAME))) {
      preparedStatement.setInt(1, getId());
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        resultSet.next();
        setUpdatedAt(resultSet.getTimestamp(Collection.UPDATED_AT_COLUMN));
      }
    }
  }

  private List<Track> getTracksFromDatabase(Connection connection) throws SQLException {
    List<Track> loadedTracks = new ArrayList<>();
    try (PreparedStatement preparedStatement = connection.prepareStatement("select * from SongCollectionSong where idSongCollection = ?;")) {
      preparedStatement.setInt(1, this.getId());
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          try {
            loadedTracks.add(new Track(resultSet.getInt("trackNumber"), Song.findById(resultSet.getInt("idSong"), connection)));
          } catch (NoResultException e) {
            throw new SQLException(
              String.format(
                "SongCollectionSong {idSongCollection: %d, trackNumber: %d, idSong: %s} references non existing song",
                getId(),
                resultSet.getInt("trackNumber"),
                resultSet.getInt("idSong")
              )
            );
          }
        }
      }
    }
    return loadedTracks;
  }

  @Override
  public String toString() {
    return String.format("{%s}", getFieldNamesAndValuesString());
  }

  @Override
  public String getFieldNamesAndValuesString() {
    return String.format(
      "%s, numberOfTracks: %d",
      super.getFieldNamesAndValuesString(),
      this.tracks.size()
    );
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) {
      System.out.println("Collection equals o not ok");
      return false;
    }
    SongCollection songCollection = (SongCollection) o;
    if (((SongCollection) o).tracks.size() != this.tracks.size()) {
      System.out.println("Not same number of tracks");
      return false;
    }
    for (Track track : tracks) {
      if (!songCollection.tracks.contains(track)) {
        System.out.println("Track " + track + " not contained in other");
        return false;
      }
    }
    return true;
  }
}
