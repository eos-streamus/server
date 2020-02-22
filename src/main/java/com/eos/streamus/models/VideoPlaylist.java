package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.exceptions.NotPersistedException;
import com.eos.streamus.utils.Pair;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VideoPlaylist extends VideoCollection {
  public class VideoPlaylistVideo extends Pair<Integer, Video> implements SavableDeletable {
    public static final String TABLE_NAME = "VideoPlaylistVideo";
    public static final String ID_VIDEO_COLUMN = "idVideo";
    public static final String ID_VIDEO_PLAYLIST_VIDEO_COLUMN = "idVideoPlaylist";
    public static final String NUMBER_COLUMN = "number";
    public static final String CREATION_FUNCTION_NAME = "addVideoToPlaylist";

    public VideoPlaylistVideo(Integer key, Video value) {
      super(key, value);
    }

    @Override
    public void delete(Connection connection) throws SQLException {
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("delete from %s where %s = ? and %s = ?;", TABLE_NAME, ID_VIDEO_COLUMN, ID_VIDEO_PLAYLIST_VIDEO_COLUMN))) {
        preparedStatement.setInt(1, getValue().getId());
        preparedStatement.setInt(2, VideoPlaylist.this.getId());
        preparedStatement.execute();
        this.setKey(null);
        this.setValue(null);
      }
    }

    @Override
    public void save(Connection connection) throws SQLException {
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s(?, ?);", CREATION_FUNCTION_NAME))) {
        preparedStatement.setInt(1, getValue().getId());
        preparedStatement.setInt(2, VideoPlaylist.this.getId());
        try (ResultSet rs = preparedStatement.executeQuery()) {
          rs.next();
          setKey(rs.getInt(NUMBER_COLUMN));
        }
      }
    }

    @Override
    public String toString() {
      return String.format("{%s: %d, %s: %d, %s: %d}",
        ID_VIDEO_PLAYLIST_VIDEO_COLUMN,
        VideoPlaylist.this.getId(),
        ID_VIDEO_COLUMN,
        this.getValue().getId(),
        NUMBER_COLUMN,
        getKey()
      );
    }

    @Override
    public int hashCode() {
      return getKey() * 31 + getValue().hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || o.getClass() != getClass()) {
        return false;
      }
      VideoPlaylistVideo videoPlaylistVideo = (VideoPlaylistVideo) o;
      return videoPlaylistVideo.getValue().equals(getValue()) && videoPlaylistVideo.getKey().equals(getKey());
    }
  }

  private static final String CREATION_FUNCTION_NAME = "createVideoPlaylist";
  private static final String TABLE_NAME = "VideoPlaylist";
  private static final String PRIMARY_KEY_NAME = "idVideoCollection";
  private static final String VIEW_NAME = "VVideoPlaylist";
  private static final String USER_ID_COLUMN = "idUser";

  private final User user;
  private final List<VideoPlaylistVideo> videos = new ArrayList<>();

  VideoPlaylist(Integer id, String name, Timestamp createdAt, Timestamp updatedAt, User user) {
    super(id, name, createdAt, updatedAt);
    this.user = user;
  }

  public VideoPlaylist(String name, User user) {
    super(name);
    this.user = user;
  }

  @Override
  public String getTableName() {
    return TABLE_NAME;
  }

  @Override
  public String getPrimaryKeyName() {
    return PRIMARY_KEY_NAME;
  }

  public void addVideo(Video video) {
    Integer newVideoNumber = 0;
    for (VideoPlaylistVideo videoPlaylistVideo : videos) {
      if (videoPlaylistVideo.getKey() > newVideoNumber) {
        newVideoNumber = videoPlaylistVideo.getKey();
      }
    }
    videos.add(new VideoPlaylistVideo(newVideoNumber + 1, video));
  }

  public void addVideo(VideoPlaylistVideo video) {
    videos.add(video);
  }

  public List<VideoPlaylistVideo> getVideos() {
    return videos;
  }

  @Override
  public void save(Connection connection) throws SQLException {
    if (this.getId() == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s(?::varchar(200), ?);", CREATION_FUNCTION_NAME))) {
        preparedStatement.setString(1, getName());
        preparedStatement.setInt(2, user.getId());
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          resultSet.next();
          this.setId(resultSet.getInt("id"));
          this.setCreatedAt(resultSet.getTimestamp(Collection.CREATED_AT_COLUMN));
          this.setUpdatedAt(resultSet.getTimestamp(Collection.UPDATED_AT_COLUMN));
        }
        if (!videos.isEmpty()) {
          this.save(connection);
        }
      }
    } else {
      super.save(connection);
      List<VideoPlaylistVideo> databaseVideos = getVideosFromDatabase(connection);
      for (VideoPlaylistVideo videoPlaylistVideo : videos) {
        if (!databaseVideos.contains(videoPlaylistVideo)) {
          if (videoPlaylistVideo.getValue().getId() == null) {
            throw new NotPersistedException(String.format("%s %s is not persisted", videoPlaylistVideo.getValue().getTableName(), videoPlaylistVideo.getValue()));
          }
          videoPlaylistVideo.save(connection);
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
  }

  private List<VideoPlaylistVideo> getVideosFromDatabase(Connection connection) throws SQLException {
    List<VideoPlaylistVideo> loadedVideos = new ArrayList<>();
    try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s where %s = ?;", VideoPlaylistVideo.TABLE_NAME, VideoPlaylistVideo.ID_VIDEO_PLAYLIST_VIDEO_COLUMN))) {
      preparedStatement.setInt(1, this.getId());
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          try {
            loadedVideos.add(new VideoPlaylistVideo(resultSet.getInt(VideoPlaylistVideo.NUMBER_COLUMN), VideoDAO.findById(resultSet.getInt(VideoPlaylistVideo.ID_VIDEO_COLUMN), connection)));
          } catch (NoResultException e) {
            throw new SQLException(
              String.format(
                "%s {%s: %d, %s: %d, %s: %d} references non existing Video",
                VideoPlaylistVideo.TABLE_NAME,
                VideoPlaylistVideo.ID_VIDEO_PLAYLIST_VIDEO_COLUMN,
                getId(),
                VideoPlaylistVideo.ID_VIDEO_COLUMN,
                resultSet.getInt(VideoPlaylistVideo.ID_VIDEO_COLUMN),
                VideoPlaylistVideo.NUMBER_COLUMN,
                resultSet.getInt(VideoPlaylistVideo.NUMBER_COLUMN)
              )
            );
          }
        }
      }
    }
    return loadedVideos;
  }

  public static VideoPlaylist findById(Integer id, Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s where %s = ?;", VIEW_NAME, Collection.PRIMARY_KEY_NAME))) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (!resultSet.next()) {
          throw new NoResultException();
        }
        // Collection attributes
        String name = resultSet.getString(Collection.NAME_COLUMN);
        Timestamp createdAt = resultSet.getTimestamp(Collection.CREATED_AT_COLUMN);
        Timestamp updatedAt = resultSet.getTimestamp(Collection.UPDATED_AT_COLUMN);

        // User
        Integer userId = resultSet.getInt(USER_ID_COLUMN);
        User user = User.findById(userId, connection);

        VideoPlaylist videoPlaylist = new VideoPlaylist(
          id,
          name,
          createdAt,
          updatedAt,
          user
        );

        // Videos
        int firstVideoNumber = resultSet.getInt(VideoPlaylistVideo.NUMBER_COLUMN);
        if (!resultSet.wasNull()) {
          videoPlaylist.addVideo(videoPlaylist.new VideoPlaylistVideo(firstVideoNumber, VideoDAO.findById(resultSet.getInt(VideoPlaylistVideo.ID_VIDEO_COLUMN), connection)));
          while (resultSet.next()) {
            videoPlaylist.addVideo(videoPlaylist.new VideoPlaylistVideo(resultSet.getInt(VideoPlaylistVideo.NUMBER_COLUMN), VideoDAO.findById(resultSet.getInt(VideoPlaylistVideo.ID_VIDEO_COLUMN), connection)));
          }
        }
        return videoPlaylist;
      }
    }
  }

  @Override
  public String getFieldNamesAndValuesString() {
    return super.getFieldNamesAndValuesString();
  }

  @Override
  public String toString() {
    return super.toString();
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) {
      return false;
    }
    VideoPlaylist videoPlaylist = (VideoPlaylist) o;
    if (videoPlaylist.videos.size() != videos.size()) {
      return false;
    }
    for (VideoPlaylistVideo video : videos) {
      if (!videoPlaylist.videos.contains(video)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String getCreationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }
}
