package com.eos.streamus.models;

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
  public static final String TABLE_NAME = "CollectionActivity";
  public static final String PRIMARY_KEY_NAME = "idActivity";
  public static final String CREATION_FUNCTION_NAME = "createCollectionActivity";
  //#endregion Static attributes

  //#region Instance attributes
  private List<Pair<Integer, Pair<Resource, ResourceActivity>>> resourceActivities;
  private Collection collection;
  //#endregion Instance attributes

  protected CollectionActivity(User creator, Collection collection) {
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

  private CollectionActivity(Integer id, Collection collection) {
    super(id);
    if (collection == null) {
      throw new NullPointerException("Collection cannot be null");
    }
    this.collection = collection;
  }

  public ResourceActivity continueOrNext(Connection connection) throws SQLException {
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

  @Override
  public String getCreationFunctionName() {
    return null;
  }

  public List<Pair<Integer, Pair<Resource, ResourceActivity>>> getContent() {
    return resourceActivities;
  }

  @Override
  public void save(Connection connection) throws SQLException {
    if (getId() == null) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s(?, ?);", CREATION_FUNCTION_NAME))) {
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

  //#region Equals
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
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
