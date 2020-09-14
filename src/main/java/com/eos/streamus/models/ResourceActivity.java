package com.eos.streamus.models;

import com.eos.streamus.exceptions.IncompleteDataException;
import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.exceptions.NotPersistedException;

import java.sql.*;
import java.util.Date;

public final class ResourceActivity extends Activity {
  //#region Static Attributes
  /** Table name in database. */
  public static final String TABLE_NAME = "ResourceActivity";
  /** Activity id column name. */
  public static final String PRIMARY_KEY_NAME = "idActivity";
  /** Resource id column name. */
  public static final String RESOURCE_ID_COLUMN = "idResource";
  /** Creation function name in database. */
  public static final String CREATION_FUNCTION_NAME = "createResourceActivity";
  /** Started at timestamp column name. */
  public static final String STARTED_AT_COLUMN = "startedAt";
  /** Paused at timestamp column name. */
  public static final String PAUSED_AT_COLUMN = "pausedAt";
  /** CollectionActivity id column name. */
  public static final String COLLECTION_ACTIVITY_ID_COLUMN = "idCollectionActivity";
  //#endregion Static Attributes

  //#region Instance Attributes
  /** {@link com.eos.streamus.models.Resource} of this Activity. */
  private final Resource resource;
  /** Started at timestamp. */
  private Timestamp startedAt;
  /** Paused at marker. */
  private int pausedAt;
  /** Possible associated CollectionActivity. */
  private CollectionActivity collectionActivity;
  //#endregion Instance Attributes

  //#region Constructors
  public ResourceActivity(final Resource resource, final User creator) {
    super(creator);
    this.resource = resource;
  }

  private ResourceActivity(final Integer id, final Resource resource, final Timestamp startedAt) {
    super(id);
    this.resource = resource;
    this.startedAt = startedAt;
  }

  ResourceActivity(final Resource resource, final CollectionActivity collectionActivity) {
    this.resource = resource;
    this.collectionActivity = collectionActivity;
  }
  //#endregion Constructors

  //#region Getters and Setters
  public CollectionActivity getCollectionActivity() {
    return collectionActivity;
  }

  @Override
  public String creationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }

  public Resource getResource() {
    return resource;
  }

  public Timestamp getStartedAt() {
    return startedAt;
  }

  public int getPausedAt() {
    return pausedAt;
  }

  public void setPausedAt(final int pausedAt) {
    this.pausedAt = pausedAt;
  }

  public void start() {
    if (this.getId() == null) {
      throw new NotPersistedException("ResourceActivity is started on save");
    }
    this.startedAt = new Timestamp(new Date().getTime());
  }

  @Override
  public String tableName() {
    return TABLE_NAME;
  }

  @Override
  public String primaryKeyName() {
    return PRIMARY_KEY_NAME;
  }
  //#endregion Getters and Setters

  //#region Database operations
  @Override
  public void save(final Connection connection) throws SQLException {
    if (getUsers().isEmpty() && collectionActivity == null) {
      throw new IncompleteDataException("At least one UserActivity must be present");
    }
    if (this.getId() == null) {
      saveNew(connection);
    } else {
      update(connection);
    }
    super.save(connection);
  }

  private void saveNew(final Connection connection) throws SQLException {
    try (PreparedStatement preparedStatement =
             connection.prepareStatement(
                 String.format(
                     "select * from %s(?%s%s);",
                     CREATION_FUNCTION_NAME,
                     getUsers().isEmpty() ? ", null" : ", ?",
                     collectionActivity == null ? "" : ", ?"
                 )
             )
    ) {
      preparedStatement.setInt(1, resource.getId());
      if (!getUsers().isEmpty()) {
        preparedStatement.setInt(2, getUsers().get(0).getUser().getId());
      }
      if (collectionActivity != null) {
        preparedStatement.setInt(getUsers().isEmpty() ? 2 : 3, collectionActivity.getId());
      }
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        resultSet.next();
        this.setId(resultSet.getInt(Activity.PRIMARY_KEY_NAME));
      }
    }
  }

  private void update(final Connection connection) throws SQLException {
    try (
        PreparedStatement preparedStatement = connection.prepareStatement(
            String.format(
                "update %s set %s = ?, %s = ?%s where %s = ?;",
                TABLE_NAME,
                STARTED_AT_COLUMN,
                PAUSED_AT_COLUMN,
                collectionActivity == null ? "" : String.format(",%s = ?", COLLECTION_ACTIVITY_ID_COLUMN),
                PRIMARY_KEY_NAME
            )
        )
    ) {
      preparedStatement.setTimestamp(1, startedAt);
      preparedStatement.setInt(2, pausedAt);
      if (collectionActivity != null) {
        preparedStatement.setInt(3, collectionActivity.getId());
        preparedStatement.setInt(4, getId());
      } else {
        preparedStatement.setInt(3, getId());
      }
      preparedStatement.execute();
    }
  }

  public static ResourceActivity findById(final Integer id, final Connection connection)
      throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select * from %s where %s = ?",
            TABLE_NAME,
            PRIMARY_KEY_NAME
        )
    )) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (!resultSet.next()) {
          return null;
        }
        ResourceActivity resourceActivity = new ResourceActivity(
            resultSet.getInt(PRIMARY_KEY_NAME),
            ResourceDAO.findById(resultSet.getInt(RESOURCE_ID_COLUMN), connection),
            resultSet.getTimestamp(STARTED_AT_COLUMN)
        );
        resourceActivity.setPausedAt(resultSet.getInt(PAUSED_AT_COLUMN));
        resourceActivity.fetchUserActivities(connection);
        resourceActivity.fetchActivityMessages(connection);
        return resourceActivity;
      }
    }
  }

  public static ResourceActivity findByUserAndResourceIds(final Integer userId, final Integer resourceId, final Connection connection) throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select * from %s inner join %s on %s.%s = %s.%s where %s = ? and %s = ? order by %s desc",
            TABLE_NAME,
            UserActivity.TABLE_NAME,
            TABLE_NAME,
            PRIMARY_KEY_NAME,
            UserActivity.TABLE_NAME,
            UserActivity.ACTIVITY_ID_COLUMN,
            RESOURCE_ID_COLUMN,
            UserActivity.USER_ID_COLUMN,
            STARTED_AT_COLUMN
        )
    )) {
      preparedStatement.setInt(1, resourceId);
      preparedStatement.setInt(2, userId);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (!resultSet.next()) {
          return null;
        }
        ResourceActivity resourceActivity = new ResourceActivity(
            resultSet.getInt(PRIMARY_KEY_NAME),
            ResourceDAO.findById(resultSet.getInt(RESOURCE_ID_COLUMN), connection),
            resultSet.getTimestamp(STARTED_AT_COLUMN)
        );
        resourceActivity.setPausedAt(resultSet.getInt(PAUSED_AT_COLUMN));
        return resourceActivity;
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
  public boolean equals(final Object obj) {
    if (!super.equals(obj)) {
      return false;
    }
    ResourceActivity resourceActivity = (ResourceActivity) obj;
    if (
        startedAt == null && resourceActivity.startedAt != null ||
        startedAt != null && resourceActivity.startedAt == null
    ) {
      return false;
    }
    return resourceActivity.resource.equals(resource) &&
           (resourceActivity.startedAt == null || resourceActivity.startedAt.equals(startedAt)) &&
           resourceActivity.pausedAt == pausedAt;
  }
  //#endregion Equals
}
