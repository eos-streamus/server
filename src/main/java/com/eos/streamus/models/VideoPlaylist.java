package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.exceptions.NotPersistedException;
import com.eos.streamus.utils.Pair;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VideoPlaylist extends VideoCollection {
  public class VideoPlaylistVideo extends Pair<Integer, Video> implements SavableDeletable {
    //#region Static attributes
    /**
     * Table name in database.
     */
    public static final String TABLE_NAME = "VideoPlaylistVideo";
    /**
     * Id video column in database.
     */
    public static final String ID_VIDEO_COLUMN = "idVideo";
    /**
     * Id video playlist video column in database.
     */
    public static final String ID_VIDEO_PLAYLIST_VIDEO_COLUMN = "idVideoPlaylist";
    /**
     * Number column in database.
     */
    public static final String NUMBER_COLUMN = "number";
    /**
     * Creation function name in database.
     */
    public static final String CREATION_FUNCTION_NAME = "addVideoToPlaylist";
    //#endregion Static attributes

    //#region Constructors
    public VideoPlaylistVideo(final Integer key, final Video value) {
      super(key, value);
    }
    //#endregion Constructors

    //#region Database operations

    /** {@inheritDoc} */
    @Override
    public void delete(final Connection connection) throws SQLException {
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format(
              "delete from %s where %s = ? and %s = ?;",
              TABLE_NAME,
              ID_VIDEO_COLUMN,
              ID_VIDEO_PLAYLIST_VIDEO_COLUMN
          )
      )) {
        preparedStatement.setInt(1, getValue().getId());
        preparedStatement.setInt(2, VideoPlaylist.this.getId());
        preparedStatement.execute();
        this.setKey(null);
        this.setValue(null);
      }
    }

    /** {@inheritDoc} */
    @Override
    public void save(final Connection connection) throws SQLException {
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format(
              "select * from %s(?, ?);",
              CREATION_FUNCTION_NAME
          )
      )) {
        preparedStatement.setInt(1, getValue().getId());
        preparedStatement.setInt(2, VideoPlaylist.this.getId());
        try (ResultSet rs = preparedStatement.executeQuery()) {
          rs.next();
          setKey(rs.getInt(NUMBER_COLUMN));
        }
      }
    }
    //#endregion Database operations

    //#region String representations

    /** {@inheritDoc} */
    @Override
    public String toString() {
      return String.format("%s[%s= %d, %s= %d, %s= %d]",
          getClass().getName(),
          ID_VIDEO_PLAYLIST_VIDEO_COLUMN,
          VideoPlaylist.this.getId(),
          ID_VIDEO_COLUMN,
          this.getValue().getId(),
          NUMBER_COLUMN,
          getKey()
      );
    }
    //#endregion String representations

    //#region Equals

    /**
     * @return HashCode of this VideoPlaylistVideo, combined from video number and Video hashCode.
     */
    @Override
    public int hashCode() {
      return Objects.hash(getKey(), getValue().hashCode());
    }

    /**
     * Returns whether the given object is equal to this VideoPlaylistVideo.
     * Will be equal if:
     * - Not null
     * - Equal class
     * - Same number
     * - Same video
     *
     * @param o Object to compare.
     * @return True if given Object is equal to this VideoPlaylistVideo.
     */
    @Override
    public boolean equals(final Object o) {
      if (o == null || o.getClass() != getClass()) {
        return false;
      }
      VideoPlaylistVideo videoPlaylistVideo = (VideoPlaylistVideo) o;
      return videoPlaylistVideo.getValue().equals(getValue()) && videoPlaylistVideo.getKey().equals(getKey());
    }
    //#endregion Equals
  }

  //#region Static attributes
  /**
   * Table name in database.
   */
  private static final String TABLE_NAME = "VideoPlaylist";
  /**
   * Primary key name in database.
   */
  private static final String PRIMARY_KEY_NAME = "idVideoCollection";
  /**
   * User id column in database.
   */
  private static final String USER_ID_COLUMN = "idUser";
  /**
   * View name in database.
   */
  private static final String VIEW_NAME = "VVideoPlaylist";
  /**
   * Creation function name in database.
   */
  private static final String CREATION_FUNCTION_NAME = "createVideoPlaylist";
  //#endregion Static attributes

  //#region Instance attributes
  /**
   * Creator {@link User} of this VideoPlaylist.
   */
  private final User user;
  /**
   * List of VideoPlaylistVideos of this playlist.
   */
  private final List<VideoPlaylistVideo> videos = new ArrayList<>();
  //#endregion Instance attributes

  //#region Constructors
  public VideoPlaylist(final String name, final User user) {
    super(name);
    this.user = user;
  }
  //#endregion Constructors

  //#region Accessors

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
   * @return VideoPlaylist specific content of this Collection.
   */
  @Override
  protected List<Pair<Integer, Resource>> getSpecificContent() {
    List<Pair<Integer, Resource>> content = new ArrayList<>();
    for (VideoPlaylistVideo video : videos) {
      content.add(new Pair<>(video.getKey(), video.getValue()));
    }
    return content;
  }

  /**
   * @return {@link VideoPlaylistVideo} of this playlist.
   */
  public List<VideoPlaylistVideo> getVideos() {
    return videos;
  }

  /**
   * Add a Video to this playlist.
   *
   * @param video Video to add.
   */
  public void addVideo(final Video video) {
    Integer newVideoNumber = 0;
    for (VideoPlaylistVideo videoPlaylistVideo : videos) {
      if (videoPlaylistVideo.getKey() > newVideoNumber) {
        newVideoNumber = videoPlaylistVideo.getKey();
      }
    }
    videos.add(new VideoPlaylistVideo(newVideoNumber + 1, video));
  }

  /**
   * Add a {@link VideoPlaylistVideo} to this playlist.
   *
   * @param video {@link VideoPlaylistVideo} to add.
   */
  public void addVideo(final VideoPlaylistVideo video) {
    videos.add(video);
  }
  //#endregion Accessors

  //#region Database operations

  /**
   * {@inheritDoc}
   */
  @Override
  public void save(final Connection connection) throws SQLException {
    if (this.getId() == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format(
              "select * from %s(?::varchar(200), ?);",
              CREATION_FUNCTION_NAME
          )
      )) {
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
            throw new NotPersistedException(
                String.format(
                    "%s %s is not persisted",
                    videoPlaylistVideo.getValue().tableName(),
                    videoPlaylistVideo.getValue()
                )
            );
          }
          videoPlaylistVideo.save(connection);
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
  }

  private List<VideoPlaylistVideo> getVideosFromDatabase(final Connection connection) throws SQLException {
    List<VideoPlaylistVideo> loadedVideos = new ArrayList<>();
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select * from %s where %s = ?;",
            VideoPlaylistVideo.TABLE_NAME,
            VideoPlaylistVideo.ID_VIDEO_PLAYLIST_VIDEO_COLUMN
        )
    )) {
      preparedStatement.setInt(1, this.getId());
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          try {
            loadedVideos.add(
                new VideoPlaylistVideo(
                    resultSet.getInt(VideoPlaylistVideo.NUMBER_COLUMN),
                    VideoDAO.findById(resultSet.getInt(VideoPlaylistVideo.ID_VIDEO_COLUMN), connection)
                )
            );
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

  /**
   * Finds a VideoPlaylist by id in the database.
   *
   * @param id         Id of VideoPlaylist to find.
   * @param connection {@link Connection} to use to perform the operation.
   * @return Found VideoPlaylist
   * @throws SQLException      If an error occurred while performing the database operation.
   * @throws NoResultException If no VideoPlaylist by this id was found.
   */
  public static VideoPlaylist findById(final Integer id, final Connection connection)
      throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select * from %s where %s = ?;",
            VIEW_NAME,
            Collection.PRIMARY_KEY_NAME
        )
    )) {
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
            name,
            user
        );
        videoPlaylist.setId(id);
        videoPlaylist.setCreatedAt(createdAt);
        videoPlaylist.setUpdatedAt(updatedAt);

        // Videos
        int firstVideoNumber = resultSet.getInt(VideoPlaylistVideo.NUMBER_COLUMN);
        if (!resultSet.wasNull()) {
          videoPlaylist.addVideo(
              videoPlaylist.new VideoPlaylistVideo(
                  firstVideoNumber,
                  VideoDAO.findById(resultSet.getInt(VideoPlaylistVideo.ID_VIDEO_COLUMN), connection)
              )
          );
          while (resultSet.next()) {
            videoPlaylist.addVideo(
                videoPlaylist.new VideoPlaylistVideo(
                    resultSet.getInt(VideoPlaylistVideo.NUMBER_COLUMN),
                    VideoDAO.findById(resultSet.getInt(VideoPlaylistVideo.ID_VIDEO_COLUMN), connection)
                )
            );
          }
        }
        return videoPlaylist;
      }
    }
  }
  //#endregion Database operations

  //#region Equals
  /** @return This VideoPlaylists hashCode, i.e. its id. */
  @Override
  public int hashCode() {
    return getId();
  }

  /**
   * Returns whether the given Object is equal to this VideoPlaylist.
   * Will be equal if:
   * - Not null
   * - Same class
   * - VideoCollection::equals is verified
   * - Same videos
   * @param o Object to compare
   * @return True if all conditions above are verified.
   */
  @Override
  public boolean equals(final Object o) {
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
  //#endregion Equals
}
