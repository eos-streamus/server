package com.eos.streamus.models;

import com.eos.streamus.exceptions.IncompleteDataException;
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

  //#region Static Attributes
  protected static final String TABLE_NAME = "Activity";
  protected static final String PRIMARY_KEY_NAME = "id";
  //#endregion Static Attributes

  //#region Instance Attributes
  private Integer id;
  private List<UserActivity> users = new ArrayList<>();
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

  protected Activity() {}
  //#endregion Constructors

  //#region Getters and Setters
  @Override
  public String getTableName() {
    return TABLE_NAME;
  }

  @Override
  public String getPrimaryKeyName() {
    return PRIMARY_KEY_NAME;
  }

  @Override
  public Integer getId() {
    return id;
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

  protected void setId(Integer id) {
    this.id = id;
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

  //#endregion Database operations

  //#region String representations
  @Override
  public String toString() {
    return String.format("{%s}", getFieldNamesAndValuesString());
  }

  @Override
  public String getFieldNamesAndValuesString() {
    return String.format("id: %d", id);
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
