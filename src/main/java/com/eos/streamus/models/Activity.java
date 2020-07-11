package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.exceptions.NotPersistedException;
import com.eos.streamus.utils.Pair;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Activity implements SavableDeletableEntity {
  public class UserActivity extends Pair<User, Boolean> implements SavableDeletable {
    //#region Static attributes
    /** Name of the table. */
    public static final String TABLE_NAME = "UserActivity";
    /** Name of User id column in table. */
    public static final String USER_ID_COLUMN = "idUser";
    /** Name of Activity id column. */
    public static final String ACTIVITY_ID_COLUMN = "idActivity";
    /** Name of manages column in table. */
    public static final String MANAGES_COLUMN = "manages";
    //#endregion Static attributes

    //#region Constructors
    public UserActivity(final User user, final Boolean isManager) {
      super(user, isManager);
    }
    //#endregion Constructors

    //#region Getters and Setters

    /** @return {@link Activity} associated to this UserActivity. */
    public final Activity getActivity() {
      return Activity.this;
    }

    /** @return {@link User} associated to this UserActivity. */
    public final User getUser() {
      return this.getKey();
    }

    /** @return Whether associated {@link User} manages this {@link Activity}. */
    public final Boolean isManager() {
      return this.getValue();
    }
    //#endregion Getters and Setters

    //#region Database operations
    private boolean isNotPersisted(final Connection connection) throws SQLException {
      if (getUser() == null || getUser().getId() == null) {
        throw new NotPersistedException("User is not persisted");
      }
      if (Activity.this.id == null) {
        throw new NotPersistedException("Activity is not persisted");
      }
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format("select 1 from %s where %s = ? and %s = ?", TABLE_NAME, USER_ID_COLUMN, ACTIVITY_ID_COLUMN))) {
        preparedStatement.setInt(1, getUser().getId());
        preparedStatement.setInt(2, Activity.this.id);
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          return !resultSet.next();
        }
      }
    }

    /**
     * Delete this UserActivity from database.
     *
     * @param connection {@link Connection} to use to delete.
     * @throws SQLException if an error occurs.
     */
    @Override
    public final void delete(final Connection connection) throws SQLException {
      if (isNotPersisted(connection)) {
        throw new NotPersistedException("UserActivity is not persisted");
      }
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format("delete from %s where %s = ? and %s = ?;", TABLE_NAME, USER_ID_COLUMN, ACTIVITY_ID_COLUMN))) {
        preparedStatement.setInt(1, getUser().getId());
        preparedStatement.setInt(2, Activity.this.id);
        preparedStatement.execute();
      }
    }

    /**
     * Save this UserActivity in database.
     *
     * @param connection {@link Connection} to use to save.
     * @throws SQLException if an error occurs.
     */
    @Override
    public final void save(final Connection connection) throws SQLException {
      if (Activity.this.id == null) {
        throw new NotPersistedException("Activity is not persisted");
      }
      if (getUser() == null || getUser().getId() == null) {
        throw new NotPersistedException("User not persisted");
      }
      if (isNotPersisted(connection)) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format(
            "insert into %s(%s, %s, %s) values (?, ?, ?);", TABLE_NAME, USER_ID_COLUMN, ACTIVITY_ID_COLUMN,
            MANAGES_COLUMN))) {
          preparedStatement.setInt(1, getUser().getId());
          preparedStatement.setInt(2, Activity.this.id);
          preparedStatement.setBoolean(3, isManager());
          try (ResultSet resultSet = preparedStatement.executeQuery()) {
            if (!resultSet.next()) {
              throw new SQLException("Could not save " + TABLE_NAME);
            }
          }
        }
      }
    }
    //#endregion Database operations

    //#region Equals
    @Override
    public final int hashCode() {
      return Objects.hash(Activity.this.id, getUser().getId());
    }

    @Override
    public final boolean equals(final Object obj) {
      if (obj == null) {
        return false;
      }
      if (obj.getClass() != this.getClass()) {
        return false;
      }
      UserActivity userActivity = (UserActivity) obj;
      return
          userActivity.getUser().equals(getUser()) &&
          userActivity.isManager().equals(isManager()) &&
          userActivity.getActivity().id.equals(Activity.this.id);
    }
    //#endregion Equals
  }

  public final class ActivityMessage implements SavableDeletableEntity {
    //#region Static attributes
    /** Table name in database. */
    private static final String TABLE_NAME = "ActivityMessage";
    /** Name of primary key column in database. */
    private static final String PRIMARY_KEY_NAME = "id";
    /** Name of Activity id column in database. */
    private static final String ACTIVITY_ID_COLUMN = "idActivity";
    /** Name of {@link com.eos.streamus.models.User} id column in database. */
    private static final String USER_ID_COLUMN = "idUser";
    /** Name of posted at timestamp column name. */
    private static final String POSTED_AT_COLUMN = "postedAt";
    /** Name of content column name in database. */
    private static final String CONTENT_COLUMN = "content";
    //#endregion Static attributes

    //#region Instance attributes
    /** id value. */
    private Integer id;
    /** {@link com.eos.streamus.models.User} that writes the message. */
    private final User user;
    /** Message content. */
    private final String content;
    /** Message posted at timestamp. */
    private Timestamp postedAt;
    //#endregion Instance attributes

    //#region Constructors
    public ActivityMessage(final User user, final String content) {
      this.user = user;
      this.content = content;
      Activity.this.messages.add(this);
    }

    public ActivityMessage(final Integer id, final User user, final String content, final Timestamp postedAt) {
      this(user, content);
      this.id = id;
      this.postedAt = postedAt;
    }
    //#endregion Constructors

    //#region Getters and Setters
    @Override
    public Integer getId() {
      return id;
    }

    public User getUser() {
      return user;
    }

    public String getContent() {
      return content;
    }

    public Activity getActivity() {
      return Activity.this;
    }

    public Timestamp getPostedAt() {
      return postedAt;
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
      return null;
    }
    //#endregion Getters and Setters

    //#region Database operations
    @Override
    public void save(final Connection connection) throws SQLException {
      if (this.id == null) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
            String.format(
                "insert into %s(%s, %s, %s) values (?, ?, ?) returning %s, %s;",
                TABLE_NAME,
                ACTIVITY_ID_COLUMN,
                USER_ID_COLUMN,
                CONTENT_COLUMN,
                PRIMARY_KEY_NAME,
                POSTED_AT_COLUMN
            )
        )) {
          preparedStatement.setInt(1, Activity.this.id);
          preparedStatement.setInt(2, user.getId());
          preparedStatement.setString(3, content);
          try (ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            this.id = resultSet.getInt(1);
            this.postedAt = resultSet.getTimestamp(2);
          }
        }
      }
    }
    //#endregion Database operations

    //#region Equals
    @Override
    public int hashCode() {
      return Objects.hash(Activity.this.hashCode(), user.hashCode());
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == null) {
        return false;
      }
      if (obj.getClass() != getClass()) {
        return false;
      }
      ActivityMessage activityMessage = (ActivityMessage) obj;
      return
          activityMessage.id.equals(id) &&
          activityMessage.user.getId().equals(user.getId()) &&
          activityMessage.getActivity().id.equals(Activity.this.getId());
    }
    //#endregion Equals
  }

  //#region Static Attributes
  /** Table name in database. */
  public static final String TABLE_NAME = "Activity";
  /** Primary key column name in database. */
  protected static final String PRIMARY_KEY_NAME = "id";
  //#endregion Static Attributes

  //#region Instance Attributes
  /** Id of instance. */
  private Integer id;
  /** List of all {@link com.eos.streamus.models.Activity.UserActivity} of this instance. */
  private final List<UserActivity> users = new ArrayList<>();
  /** List of all {@link com.eos.streamus.models.Activity.ActivityMessage} of this activity. */
  private final List<ActivityMessage> messages = new ArrayList<>();
  //#endregion Instance Attributes

  //#region Constructors
  protected Activity(final User creator) {
    if (creator == null) {
      throw new NullPointerException("Creator cannot be null");
    }
    if (creator.getId() == null) {
      throw new NotPersistedException("Creator is not persisted");
    }
    users.add(new UserActivity(creator, true));
  }

  protected Activity(final Integer id) {
    this.id = id;
  }

  protected Activity() {
  }
  //#endregion Constructors

  //#region Getters and Setters
  /** @return id of this Activity. */
  @Override
  public Integer getId() {
    return id;
  }

  /**
   * Set id of Activity.
   * @param id id of Activity.
   */
  protected void setId(final Integer id) {
    this.id = id;
  }

  /** @return Name of underlying table in database. */
  @Override
  public String tableName() {
    return TABLE_NAME;
  }

  /** @return Name of underlying table primary key column in database. */
  @Override
  public String primaryKeyName() {
    return PRIMARY_KEY_NAME;
  }

  /**
   * Add a user to this activity.
   * @param user new User of activity.
   * @param isManager Whether the user can manage the activity.
   */
  public void addUser(final User user, final boolean isManager) {
    if (user == null) {
      throw new NullPointerException();
    }
    if (this.id == null) {
      throw new NotPersistedException("Cannot add another user if Activity is not persisted");
    }
    this.users.add(new UserActivity(user, isManager));
  }

  /** @return list of {@link com.eos.streamus.models.User}s. */
  public List<UserActivity> getUsers() {
    return new ArrayList<>(users);
  }

  /** @return list of {@link com.eos.streamus.models.Activity.ActivityMessage}s of the Activity. */
  public List<ActivityMessage> getMessages() {
    return messages;
  }
  //#endregion Getters and Setters

  //#region Database operations

  /**
   * Save this instance to database.
   *
   * @param connection {@link Connection} to use to save.
   * @throws SQLException if an error occurs during the operation.
   */
  @Override
  public void save(final Connection connection) throws SQLException {
    if (id == null) {
      throw new NotPersistedException("Activity#save cannot be called from non persisted Activity");
    }
    for (UserActivity userActivity : users) {
      if (userActivity.isNotPersisted(connection)) {
        userActivity.save(connection);
      }
    }
  }

  /**
   * Fetch and populate list of {@link UserActivity} of this activity.
   *
   * @param connection {@link Connection} to use to fetch entries.
   * @throws SQLException If an error occurs.
   * @throws NoResultException If an associated {@link User} cannot be found.
   */
  protected void fetchUserActivities(final Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format("select * from %s where %s = ?;", UserActivity.TABLE_NAME, UserActivity.ACTIVITY_ID_COLUMN))) {
      preparedStatement.setInt(1, this.getId());
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          this.users.add(this.new UserActivity(User.findById(resultSet.getInt(UserActivity.USER_ID_COLUMN), connection),
                                               resultSet.getBoolean(UserActivity.MANAGES_COLUMN)));
        }
      }
    }
  }

  public final void fetchActivityMessages(final Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select * from %s where %s = ?;",
            ActivityMessage.TABLE_NAME,
            ActivityMessage.ACTIVITY_ID_COLUMN
        )
    )) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          new ActivityMessage(
              resultSet.getInt(ActivityMessage.PRIMARY_KEY_NAME),
              User.findById(resultSet.getInt(ActivityMessage.USER_ID_COLUMN), connection),
              resultSet.getString(ActivityMessage.CONTENT_COLUMN),
              resultSet.getTimestamp(ActivityMessage.POSTED_AT_COLUMN)
          );
        }
      }
    }
  }
  //#endregion Database operations

  //#region String representations

  /** @return String representation of this instance. */
  @Override
  public String toString() {
    return defaultToString();
  }
  //#endregion String representations

  //#region Equals
  /** @return hashcode of instance. */
  @Override
  public int hashCode() {
    return id;
  }

  /**
   * Returns whether this Activity is equal to another object.
   * @param obj Object to compare.
   * @return if they are equal.
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }
    Activity activity = (Activity) obj;
    if (id == null && activity.id != null || id != null && activity.id == null) {
      return false;
    }
    if (id != null && !activity.id.equals(id)) {
      return false;
    }
    if (users.size() != activity.users.size()) {
      return false;
    }
    if (messages.size() != activity.messages.size()) {
      return false;
    }
    return messages.containsAll(activity.messages) && users.containsAll(activity.users);
  }
  //#endregion Equals
}
