package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.exceptions.NotPersistedException;
import com.eos.streamus.utils.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CollectionActivity extends Activity {
  //#region Static attributes
  /**
   * Table name in database.
   */
  public static final String TABLE_NAME = "CollectionActivity";
  /**
   * Primary_key name in database.
   */
  public static final String PRIMARY_KEY_NAME = "idActivity";
  /**
   * Collection id in database.
   */
  public static final String COLLECTION_ID = "IdCollection";
  /**
   * Creation function name in database.
   */
  public static final String CREATION_FUNCTION_NAME = "createCollectionActivity";
  /**
   * View name in database.
   */
  public static final String VIEW_NAME = "vFullCollectionActivity";
  /**
   * Id in view in database.
   */
  public static final String VIEW_ID = "idCollectionActivity";
  /**
   * Id of resource activity in view in database.
   */
  public static final String VIEW_RESOURCE_ACTIVITY_ID = "idResourceActivity";
  /**
   * Resource id in view in database.
   */
  public static final String VIEW_RESOURCE_ID = "idResource";
  /**
   * Number column name in view in database.
   */
  public static final String VIEW_NUMBER_ID = "num";
  //#endregion Static attributes

  //#region Instance attributes
  /**
   * ResourceActivities.
   */
  private final List<Pair<Integer, Pair<Resource, ResourceActivity>>> resourceActivities;
  /**
   * Collection of activity.
   */
  private final Collection collection;
  //#endregion Instance attributes

  //#region Constructors
  protected CollectionActivity(final User creator, final Collection collection) {
    super(creator);
    if (collection == null) {
      throw new NullPointerException("Collection cannot be null");
    }
    this.collection = collection;
    this.resourceActivities = new ArrayList<>();
    for (Pair<Integer, Resource> entry : collection.getContent()) {
      resourceActivities.add(new Pair<>(entry.getKey(), new Pair<>(entry.getValue(), null)));
    }
  }

  private CollectionActivity(final Integer id, final Collection collection) {
    super(id);
    if (collection == null) {
      throw new NullPointerException("Collection cannot be null");
    }
    this.collection = collection;
    this.resourceActivities = new ArrayList<>();
  }
  //#endregion Constructors

  //#region Getters and Setters

  /** {@inheritDoc} */
  @Override
  public String creationFunctionName() {
    return null;
  }

  /**
   * @return Content of Activity.
   */
  public List<Pair<Integer, Pair<Resource, ResourceActivity>>> getContent() {
    return resourceActivities;
  }
  //#endregion Getters and Setters

  //#region Database operations

  /**
   * Returns currently paused {@link ResourceActivity} or next one.
   *
   * @param connection {@link Connection} to use to perform the operation.
   * @return Current ResourceActivity of CollectionActivity.
   * @throws SQLException If an error occurred while performing the database operation.
   */
  public ResourceActivity continueOrNext(final Connection connection) throws SQLException {
    if (this.getId() == null) {
      throw new NotPersistedException("CollectionActivity must be saved before starting");
    }
    for (Pair<Integer, Pair<Resource, ResourceActivity>> entry : resourceActivities) {
      if (entry.getValue().getValue() == null) {
        ResourceActivity resourceActivity = new ResourceActivity(entry.getValue().getKey(), this);
        resourceActivity.save(connection);
        entry.getValue().setValue(resourceActivity);
        return resourceActivity;
      } else if (entry.getValue().getValue().getPausedAt() < entry.getValue().getValue().getResource().getDuration()) {
        return entry.getValue().getValue();
      }
    }
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public void save(final Connection connection) throws SQLException {
    if (getId() == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format(
              "select * from %s(?, ?);",
              CREATION_FUNCTION_NAME
          )
      )) {
        preparedStatement.setInt(1, collection.getId());
        preparedStatement.setInt(2, getUsers().get(0).getUser().getId());
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          resultSet.next();
          setId(resultSet.getInt(Activity.PRIMARY_KEY_NAME));
        }
      }
    } else {
      for (Pair<Integer, Pair<Resource, ResourceActivity>> entry : resourceActivities) {
        super.save(connection);
        entry.getValue().getValue().save(connection);
      }
    }
  }

  /**
   * Finds a CollectionActivity by id.
   *
   * @param id         Id of CollectionActivity to find.
   * @param connection {@link Connection} to use to perform the operation.
   * @return Found CollectionActivity.
   * @throws SQLException      If an error occurred while performing the database operation.
   * @throws NoResultException if not found.
   */
  public static CollectionActivity findById(final Integer id, final Connection connection)
      throws SQLException, NoResultException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(
        String.format(
            "select * from %s where %s = ?;",
            VIEW_NAME,
            VIEW_ID
        )
    )) {
      preparedStatement.setInt(1, id);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (!resultSet.next()) {
          throw new NoResultException();
        }
        CollectionActivity collectionActivity = new CollectionActivity(
            id,
            CollectionDAO.findById(resultSet.getInt(COLLECTION_ID), connection)
        );
        do {
          collectionActivity.resourceActivities.add(
              new Pair<>(
                  resultSet.getInt(VIEW_NUMBER_ID),
                  new Pair<>(
                      ResourceDAO.findById(resultSet.getInt(VIEW_RESOURCE_ID), connection),
                      ResourceActivity.findById(resultSet.getInt(VIEW_RESOURCE_ACTIVITY_ID), connection)
                  )
              )
          );
        } while (resultSet.next());
        collectionActivity.fetchUserActivities(connection);
        collectionActivity.fetchActivityMessages(connection);
        return collectionActivity;
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
   * - Equal {@link Collection}s.
   * - Same {@link ResourceActivity}s.
   *
   * @param obj Object to compare.
   * @return True if all conditions are met.
   */
  @Override
  public boolean equals(final Object obj) {
    if (!super.equals(obj)) {
      return false;
    }
    CollectionActivity collectionActivity = (CollectionActivity) obj;
    if (!collectionActivity.collection.equals(collection)) {
      return false;
    }
    if (resourceActivities.size() != collectionActivity.resourceActivities.size()) {
      return false;
    }
    for (Pair<Integer, Pair<Resource, ResourceActivity>> resourceActivityEntry : resourceActivities) {
      if (!collectionActivity.resourceActivities.contains(resourceActivityEntry)) {
        return false;
      }
    }
    return true;
  }
  //#endregion Equals
}
