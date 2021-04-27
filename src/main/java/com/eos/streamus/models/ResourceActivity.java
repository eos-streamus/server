package com.eos.streamus.models;

import com.eos.streamus.exceptions.IncompleteDataException;
import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.exceptions.NotPersistedException;

import java.sql.*;
import java.util.Date;

public class ResourceActivity extends Activity {
  //#region Static Attributes
  /**
   * Table name in the database.
   */
  public static final String TABLE_NAME = "ResourceActivity";
  /**
   * Primary key name in the database.
   */
  public static final String PRIMARY_KEY_NAME = "idActivity";
  /**
   * Resource id column name in the database.
   */
  public static final String RESOURCE_ID_COLUMN = "idResource";
  /**
   * Creation function name in the database.
   */
  public static final String CREATION_FUNCTION_NAME = "createResourceActivity";
  /**
   * Started at column name in the database.
   */
  public static final String STARTED_AT_COLUMN = "startedAt";
  /**
   * Paused at column name in the database.
   */
  public static final String PAUSED_AT_COLUMN = "pausedAt";
  /**
   * Collection activity id column name in the database.
   */
  public static final String COLLECTION_ACTIVITY_ID_COLUMN = "idCollectionActivity";
  //#endregion Static Attributes

  //#region Instance Attributes
  /**
   * {@link Resource} of this {@link Activity}.
   */
  private final Resource resource;
  /**
   * Started at timestamp.
   */
  private Timestamp startedAt;
  /**
   * Paused at second.
   */
  private int pausedAt;
  /**
   * Potentially associated {@link CollectionActivity}.
   */
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

  /**
   * @return Associated {@link CollectionActivity}.
   */
  public CollectionActivity getCollectionActivity() {
    return collectionActivity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String creationFunctionName() {
    return CREATION_FUNCTION_NAME;
  }

  /**
   * @return resource.
   */
  public Resource getResource() {
    return resource;
  }

  /**
   * @return startedAt.
   */
  public Timestamp getStartedAt() {
    return startedAt;
  }

  /**
   * @return pausedAt.
   */
  public int getPausedAt() {
    return pausedAt;
  }

  /**
   * Pause the activity.
   *
   * @param pausedAt time.
   */
  public void setPausedAt(final int pausedAt) {
    this.pausedAt = pausedAt;
  }

  /**
   * Start the activity.
   */
  public void start() {
    if (this.getId() == null) {
      throw new NotPersistedException("ResourceActivity is started on save");
    }
    this.startedAt = new Timestamp(new Date().getTime());
  }

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
  public String primaryKeyName() {
    return PRIMARY_KEY_NAME;
  }
  //#endregion Getters and Setters

  //#region Database operations

  /**
   * {@inheritDoc}
   */
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

  /**
   * @param connection {@link Connection} to use to perform the operation.
   * @throws SQLException If an error occurred while performing the database operation.
   */
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
      int columnNumber = 0;
      preparedStatement.setInt(++columnNumber, resource.getId());
      if (!getUsers().isEmpty()) {
        preparedStatement.setInt(++columnNumber, getUsers().get(0).getUser().getId());
      }
      if (collectionActivity != null) {
        preparedStatement.setInt(++columnNumber, collectionActivity.getId());
      }
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        resultSet.next();
        this.setId(resultSet.getInt(Activity.PRIMARY_KEY_NAME));
      }
    }
  }

  /**
   * Update in the database.
   *
   * @param connection {@link Connection} to use to perform the operation.
   * @throws SQLException If an error occurred while performing the database operation.
   */
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
      int columnNumber = 0;
      preparedStatement.setTimestamp(++columnNumber, startedAt);
      preparedStatement.setInt(++columnNumber, pausedAt);
      if (collectionActivity != null) {
        preparedStatement.setInt(++columnNumber, collectionActivity.getId());
      }
      preparedStatement.setInt(++columnNumber, getId());
      preparedStatement.execute();
    }
  }

  /**
   * Finds a ResourceActivity by given id.
   *
   * @param id         Id of ResourceActivity to find.
   * @param connection {@link Connection} to use to perform the operation.
   * @return Found ResourceActivity.
   * @throws SQLException If an error occurred while performing the database operation.
   */
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
  //#endregion Database operations

  //#region Equals

  /**
   * @return Hashcode, i.e. id.
   */
  @Override
  public int hashCode() {
    return getId();
  }

  /**
   * Returns whether the given Object is equal.
   * Equal if:
   * - {@link Activity} equality conditions are met.
   * - Same startedAt (null or equal)
   * - Same paused at
   * - Equal resources
   *
   * @param obj Object to compare.
   * @return True if all conditions are met.
   */
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
