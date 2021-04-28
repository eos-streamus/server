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
    /** Name of the table in the database. */
    public static final String TABLE_NAME = "UserActivity";
    /** User id column name. */
    public static final String USER_ID_COLUMN = "idUser";
    /** Activity foreign key column name. */
    public static final String ACTIVITY_ID_COLUMN = "idActivity";
    /** Manages flag column name. */
    public static final String MANAGES_COLUMN = "manages";
    //#endregion Static attributes

    //#region Constructors
    public UserActivity(final User user, final Boolean isManager) {
      super(user, isManager);
    }
    //#endregion Constructors

    //#region Getters and Setters

    /** @return Containing {@link Activity}. */
    public Activity getActivity() {
      return Activity.this;
    }

    /** @return The {@link com.eos.streamus.models.User} of the UserActivity. */
    public User getUser() {
      return this.getKey();
    }

    /** @return If the {@link com.eos.streamus.models.User} is a manager of this {@link Activity}. */
    public Boolean isManager() {
      return this.getValue();
    }
    //#endregion Getters and Setters

    //#region Database operations

    /**
     * Checks if this UserActivity has been saved to the database.
     *
     * @param connection {@link Connection} to use.
     * @return If this UserActivity has been saved to database or not.
     */
    public boolean isNotPersisted(final Connection connection) throws SQLException {
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

    /** {@inheritDoc} */
    @Override
    public void delete(final Connection connection) throws SQLException {
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
     * Saves the {@link Activity} to the database.
     *
     * @param connection {@link Connection} to use to perform the operation.
     * @throws SQLException If an error occurred while performing the database operation.
     */
    @Override
    public void save(final Connection connection) throws SQLException {
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
          int columnNumber = 1;
          preparedStatement.setInt(columnNumber++, getUser().getId());
          preparedStatement.setInt(columnNumber++, Activity.this.id);
          preparedStatement.setBoolean(columnNumber, isManager());
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

    /** @return The hashcode of the {@link Activity}. */
    @Override
    public int hashCode() {
      return Objects.hash(Activity.this.id, getUser().getId());
    }

    /**
     * Returns if the given object is the same as this {@link Activity}.
     * Two Activities are equal if:
     * - Same class.
     * - Their {@link User}s are equal.
     * - Both {@link User} are managers or not of this Activity.
     * - Both Activities have the same id.
     *
     * @param obj Object to compare.
     * @return If the given Object is equal.
     */
    @Override
    public boolean equals(final Object obj) {
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

  public class ActivityMessage implements SavableDeletableEntity {
    //#region Static attributes
    /** Table name in database. */
    private static final String TABLE_NAME = "ActivityMessage";
    /** Primary key column name in database. */
    private static final String PRIMARY_KEY_NAME = "id";
    /** Activity foreign key column name. */
    private static final String ACTIVITY_ID_COLUMN = "idActivity";
    /** {@link com.eos.streamus.models.User} foreign key column name. */
    private static final String USER_ID_COLUMN = "idUser";
    /** "PostedAt" timestamp column name. */
    private static final String POSTED_AT_COLUMN = "postedAt";
    /** Message content column name. */
    private static final String CONTENT_COLUMN = "content";
    //#endregion Static attributes

    //#region Instance attributes
    /** Message primary key - id. */
    private Integer id;
    /** {@link com.eos.streamus.models.User} that posted the message. */
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

    /** @return Id of this message. */
    @Override
    public Integer getId() {
      return id;
    }

    /** @return {@link com.eos.streamus.models.User} that posted the message. */
    public User getUser() {
      return user;
    }

    /** @return Message content. */
    public String getContent() {
      return content;
    }

    /** @return {@link com.eos.streamus.models.Activity} this message was posted in. */
    public Activity getActivity() {
      return Activity.this;
    }

    /** @return This message posted at timestamp. */
    public Timestamp getPostedAt() {
      return postedAt;
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

    /** {@inheritDoc} */
    @Override
    public String creationFunctionName() {
      return null;
    }
    //#endregion Getters and Setters

    //#region Database operations

    /** {@inheritDoc} */
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
          int columnNumber = 1;
          preparedStatement.setInt(columnNumber++, Activity.this.id);
          preparedStatement.setInt(columnNumber++, user.getId());
          preparedStatement.setString(columnNumber, content);
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

    /** @return hashCode of this Activity, i.e. id. */
    @Override
    public int hashCode() {
      return id;
    }

    /**
     * Returns if this ActivityMessage is the same as another ActivityMessage.
     * Will be equal if:
     * - Not null
     * - Same class
     * - Same id
     * - Same {@link User} id
     * - Equal {@link Activity}.
     *
     * @param obj Object to compare.
     * @return If the two objects are equal.
     */
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
  /** Table name. */
  public static final String TABLE_NAME = "Activity";
  /** Primary key name. */
  protected static final String PRIMARY_KEY_NAME = "id";
  //#endregion Static Attributes

  //#region Instance Attributes
  /** Id of the activity. */
  private Integer id;
  /** List of {@link com.eos.streamus.models.User} of this activity. */
  private final List<UserActivity> users = new ArrayList<>();
  /** List of {@link com.eos.streamus.models.Activity.ActivityMessage} of this activity. */
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

  /** {@inheritDoc} */
  @Override
  public Integer getId() {
    return id;
  }

  /**
   * Set id of the instance.
   *
   * @param id Id to set.
   */
  protected void setId(final Integer id) {
    this.id = id;
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

  /**
   * Add {@link User} to the Activity.
   *
   * @param user      {@link User} to add.
   * @param isManager Whether the added {@link User} is a manager of this Activity.
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

  /** @return List of {@link com.eos.streamus.models.User} of this Activity. */
  public List<UserActivity> getUsers() {
    return new ArrayList<>(users);
  }

  /** @return List of {@link com.eos.streamus.models.Activity.ActivityMessage} of this Activity. */
  public List<ActivityMessage> getMessages() {
    return messages;
  }
  //#endregion Getters and Setters

  //#region Database operations

  /** {@inheritDoc} */
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
   * Populate list of {@link UserActivity} from database.
   *
   * @param connection {@link Connection} to use.
   * @throws SQLException      If the database operation failed to perform.
   * @throws NoResultException Should not happen but can be raised by {@link User}::findById.
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

  /**
   * Populate list of {@link ActivityMessage} from database.
   *
   * @param connection {@link Connection} to use.
   * @throws SQLException      If the database operation failed to perform.
   * @throws NoResultException Should not happen but can be raised by {@link User}::findById.
   */
  public void fetchActivityMessages(final Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(String
        .format("select * from %s where %s = ?;",
            ActivityMessage.TABLE_NAME,
            ActivityMessage.ACTIVITY_ID_COLUMN))) {
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

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return defaultToString();
  }
  //#endregion String representations

  //#region Equals

  /** @return Activity hashCode, which is its id. */
  @Override
  public int hashCode() {
    return id;
  }

  /**
   * Returns whether the given object is equal to this Activity.
   * Will be equal if:
   * - Not null
   * - Same class
   * - Same ids (either both null or both equal)
   * - Same number of {@link User}s.
   * - Same number of {@link ActivityMessage}.
   * - Same {@link User}s and {@link ActivityMessage}s.
   *
   * @param obj Object to compare.
   * @return If the given Object is equal to this Activity.
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
