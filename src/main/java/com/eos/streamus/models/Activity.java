package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.exceptions.NotPersistedException;
import com.eos.streamus.utils.Pair;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Activity implements SavableDeletableEntity {
  public class UserActivity extends Pair<User, Boolean> implements SavableDeletable {
    //#region Static attributes
    public static final String TABLE_NAME = "UserActivity";
    public static final String USER_ID_COLUMN = "idUser";
    public static final String ACTTIVITY_ID_COLUMN = "idActivity";
    public static final String MANAGES_COLUMN = "manages";
    //#endregion Static attributes

    //#region Constructors
    public UserActivity(User user, Boolean isManager) {
      super(user, isManager);
    }
    //#endregion Constructors

    //#region Getters and Setters
    public Activity getActivity() {
      return Activity.this;
    }

    public User getUser() {
      return this.getKey();
    }

    public Boolean isManager() {
      return this.getValue();
    }
    //#endregion Getters and Setters

    //#region Database operations
    public boolean isNotPersisted(Connection connection) throws SQLException {
      if (getUser() == null || getUser().getId() == null) {
        throw new NotPersistedException("User is not persisted");
      }
      if (Activity.this.id == null) {
        throw new NotPersistedException("Activity is not persisted");
      }
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select 1 from %s where %s = ? and %s = ?", TABLE_NAME, USER_ID_COLUMN, ACTTIVITY_ID_COLUMN))) {
        preparedStatement.setInt(1, getUser().getId());
        preparedStatement.setInt(2, Activity.this.id);
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          return !resultSet.next();
        }
      }
    }

    @Override
    public void delete(Connection connection) throws SQLException {
      if (isNotPersisted(connection)) {
        throw new NotPersistedException("UserActivity is not persisted");
      }
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("delete from %s where %s = ? and %s = ?;", TABLE_NAME, USER_ID_COLUMN, ACTTIVITY_ID_COLUMN))) {
        preparedStatement.setInt(1, getUser().getId());
        preparedStatement.setInt(2, Activity.this.id);
        preparedStatement.execute();
      }
    }

    @Override
    public void save(Connection connection) throws SQLException {
      if (Activity.this.id == null) {
        throw new NotPersistedException("Activity is not persisted");
      }
      if (getUser() == null || getUser().getId() == null) {
        throw new NotPersistedException("User not persisted");
      }
      if (isNotPersisted(connection)) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("insert into %s(%s, %s, %s) values (?, ?, ?);", TABLE_NAME, USER_ID_COLUMN, ACTTIVITY_ID_COLUMN, MANAGES_COLUMN))) {
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

    //#region String representations
    public String getFieldNamesAndValuesString() {
      return String.format(
        "%s: %s, %s: %d, %s: %s",
        USER_ID_COLUMN,
        (getUser() == null ? "null" : getUser().getId()),
        ACTTIVITY_ID_COLUMN,
        Activity.this.id,
        MANAGES_COLUMN,
        isManager()
      );
    }

    @Override
    public String toString() {
      return String.format("{%s}", getFieldNamesAndValuesString());
    }
    //#endregion String representations

    //#region Equals
    @Override
    public int hashCode() {
      return Activity.this.id * 31 + getUser().getId();
    }

    @Override
    public boolean equals(Object obj) {
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
          userActivity.getActivity().equals(Activity.this);
    }
    //#endregion Equals
  }

  public class ActivityMessage implements SavableDeletableEntity {
    private static final String TABLE_NAME = "ActivityMessage";
    private static final String PRIMARY_KEY_NAME = "id";
    private static final String ACTIVITY_ID_COLUMN = "idActivity";
    private static final String USER_ID_COLUMN = "idUser";
    private static final String POSTED_AT_COLUMN = "postedAt";
    private static final String CONTENT_COLUMN = "content";

    private Integer id;
    private User user;
    private String content;
    private Timestamp postedAt;

    public ActivityMessage(User user, String content) {
      this.user = user;
      this.content = content;
      Activity.this.messages.add(this);
    }

    public ActivityMessage(Integer id, User user, String content, Timestamp postedAt) {
      this(user, content);
      this.id = id;
      this.postedAt = postedAt;
    }

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
    public String getTableName() {
      return TABLE_NAME;
    }

    @Override
    public String getPrimaryKeyName() {
      return PRIMARY_KEY_NAME;
    }

    @Override
    public String getCreationFunctionName() {
      return null;
    }

    @Override
    public void save(Connection connection) throws SQLException {
      if (this.id != null) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format(
            "insert into %s(%s, %s) values (?, ?) returning %s, %s;",
            TABLE_NAME,
            ACTIVITY_ID_COLUMN,
            USER_ID_COLUMN,
            PRIMARY_KEY_NAME,
            POSTED_AT_COLUMN
          )
        )) {
          preparedStatement.setInt(1, Activity.this.id);
          preparedStatement.setInt(2, user.getId());
          try (ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSet.next();
            this.id = resultSet.getInt(1);
            this.postedAt = resultSet.getTimestamp(2);
          }
        }
      }
    }

    @Override
    public String getFieldNamesAndValuesString() {
      return null;
    }
  }

  //#region Static Attributes
  protected static final String TABLE_NAME = "Activity";
  protected static final String PRIMARY_KEY_NAME = "id";
  //#endregion Static Attributes

  //#region Instance Attributes
  private Integer id;
  private List<UserActivity> users = new ArrayList<>();
  private List<ActivityMessage> messages = new ArrayList<>();
  //#endregion Instance Attributes

  //#region Constructors
  protected Activity(User creator) {
    if (creator == null) {
      throw new NullPointerException("Creator cannot be null");
    }
    if (creator.getId() == null) {
      throw new NotPersistedException("Creator is not persisted");
    }
    users.add(new UserActivity(creator, true));
  }

  protected Activity(Integer id) {
    this.id = id;
  }

  protected Activity() {
  }
  //#endregion Constructors

  //#region Getters and Setters
  @Override
  public Integer getId() {
    return id;
  }

  protected void setId(Integer id) {
    this.id = id;
  }

  @Override
  public String getTableName() {
    return TABLE_NAME;
  }

  @Override
  public String getPrimaryKeyName() {
    return PRIMARY_KEY_NAME;
  }

  public void addUser(User user, boolean isManager) {
    if (user == null) {
      throw new NullPointerException();
    }
    if (this.id == null) {
      throw new NotPersistedException("Cannot add another user if Activity is not persisted");
    }
    this.users.add(new UserActivity(user, isManager));
  }

  public List<UserActivity> getUsers() {
    return new ArrayList<>(users);
  }

  public List<ActivityMessage> getMessages() {
    return messages;
  }
  //#endregion Getters and Setters

  //#region Database operations
  @Override
  public void save(Connection connection) throws SQLException {
    if (id == null) {
      throw new NotPersistedException("Activity#save cannot be called from non persisted Activity");
    }
    for (UserActivity userActivity : users) {
      if (userActivity.isNotPersisted(connection)) {
        userActivity.save(connection);
      }
    }
  }

  protected void fetchUserActivities(Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s where %s = ?;", UserActivity.TABLE_NAME, UserActivity.ACTTIVITY_ID_COLUMN))) {
      preparedStatement.setInt(1, this.getId());
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          this.users.add(this.new UserActivity(User.findById(resultSet.getInt(UserActivity.USER_ID_COLUMN), connection), resultSet.getBoolean(UserActivity.MANAGES_COLUMN)));
        }
      }
    }
  }

  public void fetchActivityMessages(Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s where %s = ?;", ActivityMessage.TABLE_NAME, ActivityMessage.ACTIVITY_ID_COLUMN))) {
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
  @Override
  public String toString() {
    return String.format("{%s}", getFieldNamesAndValuesString());
  }

  @Override
  public String getFieldNamesAndValuesString() {
    StringBuilder usersString = new StringBuilder();
    usersString.append("[");
    for (UserActivity userActivity : this.users) {
      usersString
        .append("{userId: ").append(userActivity.getUser().getId())
        .append(", manages: ").append(userActivity.isManager())
        .append("}");
    }
    usersString.append("]");
    return String.format("id: %d, users: %s", id, usersString);
  }
  //#endregion String representations

  //#region Equals
  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }
    Activity activity = (Activity) obj;
    return activity.id.equals(id);
  }
  //#endregion Equals
}
