package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class ActivityTests extends DatabaseTests {

  @Test
  void testResourceActivity() throws SQLException, NoResultException {
    try (Connection connection = databaseConnector.getConnection()) {
      Song song = new Song(randomString(), randomString(), 100);
      song.save(connection);

      User user = randomUser();
      user.save(connection);

      ResourceActivity resourceActivity = new ResourceActivity(song, user);
      resourceActivity.save(connection);
      assertEquals(resourceActivity, ResourceActivity.findById(resourceActivity.getId(), connection));

      // Update
      resourceActivity.start();
      resourceActivity.save(connection);
      assertEquals(resourceActivity, ResourceActivity.findById(resourceActivity.getId(), connection));
      resourceActivity.setPausedAt(1);
      resourceActivity.save(connection);
      assertEquals(resourceActivity, ResourceActivity.findById(resourceActivity.getId(), connection));

      // Delete
      resourceActivity.delete(connection);
      assertNull(ResourceActivity.findById(resourceActivity.getId(), connection));

      user.delete(connection);
      song.delete(connection);
    }
  }

  @Test
  void testCollectionActivity() throws SQLException, NoResultException {
    try (Connection connection = databaseConnector.getConnection()) {
      Series theExpanse = new Series("The Expanse");
      short season1 = 1;
      theExpanse.new EpisodeBuilder(randomString(), "Dulcinea", 2700, season1).build();
      theExpanse.new EpisodeBuilder(randomString(), "The Big Empty", 2700, season1).build();
      theExpanse.new EpisodeBuilder(randomString(), "Remember the Cant", 2700, season1).build();
      theExpanse.new EpisodeBuilder(randomString(), "CQB (Close Quarter Battle)", 2700, season1).build();
      theExpanse.new EpisodeBuilder(randomString(), "Back to the Butcher", 2700, season1).build();
      theExpanse.new EpisodeBuilder(randomString(), "Rock Bottom", 2700, season1).build();
      theExpanse.new EpisodeBuilder(randomString(), "Windmills", 2700, season1).build();
      theExpanse.new EpisodeBuilder(randomString(), "Salvage", 2700, season1).build();
      theExpanse.new EpisodeBuilder(randomString(), "Critical Mass", 2700, season1).build();
      theExpanse.new EpisodeBuilder(randomString(), "Leviathan Wakes", 2700, season1).build();

      short season2 = 2;
      theExpanse.new EpisodeBuilder(randomString(), "Safe", 2700, season2).build();
      theExpanse.new EpisodeBuilder(randomString(), "Doors & Corners", 2700, season2).build();
      theExpanse.new EpisodeBuilder(randomString(), "Static", 2700, season2).build();
      theExpanse.new EpisodeBuilder(randomString(), "Godspeed", 2700, season2).build();
      theExpanse.new EpisodeBuilder(randomString(), "Home", 2700, season2).build();
      theExpanse.new EpisodeBuilder(randomString(), "Paradigm Shift", 2700, season2).build();
      theExpanse.new EpisodeBuilder(randomString(), "The Seventh Man", 2700, season2).build();
      theExpanse.new EpisodeBuilder(randomString(), "Pyre", 2700, season2).build();
      theExpanse.new EpisodeBuilder(randomString(), "The Weeping Somnambulist", 2700, season2).build();
      theExpanse.new EpisodeBuilder(randomString(), "Cascade", 2700, season2).build();
      theExpanse.new EpisodeBuilder(randomString(), "Here There be Dragons", 2700, season2).build();
      theExpanse.new EpisodeBuilder(randomString(), "The Monster and the Rocket", 2700, season2).build();
      theExpanse.new EpisodeBuilder(randomString(), "Caliban's War", 2700, season2).build();

      theExpanse.save(connection);

      assertEquals(theExpanse, Series.findById(theExpanse.getId(), connection));

      User user = randomUser();
      user.save(connection);
      CollectionActivity collectionActivity = new CollectionActivity(user, theExpanse);
      collectionActivity.save(connection);
      assertEquals(collectionActivity, CollectionActivity.findById(collectionActivity.getId(), connection));
      int i = 0;
      ResourceActivity resourceActivity;
      do {
        assertEquals(i, collectionActivity.getContent().stream().reduce(0, (subtotal, entry) -> subtotal +
                                                                                                (entry.getValue()
                                                                                                      .getValue() ==
                                                                                                 null ? 0 : 1),
                                                                        Integer::sum));
        resourceActivity = collectionActivity.continueOrNext(connection);
        if (resourceActivity != null) {
          resourceActivity.setPausedAt(resourceActivity.getResource().getDuration());
          resourceActivity.save(connection);
          i++;
        }
      } while (resourceActivity != null);
      assertEquals(collectionActivity, CollectionActivity.findById(collectionActivity.getId(), connection));

      theExpanse.delete(connection);
      user.delete(connection);
    }
  }

  @Test
  void testActivityMessages() throws SQLException, NoResultException {
    try (Connection connection = databaseConnector.getConnection()) {
      Film film = new Film(randomString(), randomString(), (getRandom().nextInt() & Integer.MAX_VALUE));
      film.save(connection);

      User user = randomUser();
      user.save(connection);

      ResourceActivity activity = new ResourceActivity(film, user);
      activity.save(connection);

      Activity.ActivityMessage message = activity.new ActivityMessage(user, "Test message");
      message.save(connection);

      assertEquals(1, activity.getMessages().size());
      assertEquals(activity, ResourceActivity.findById(activity.getId(), connection));

      for (int i = 0; i < 10; i++) {
        message = activity.new ActivityMessage(user, randomString());
        message.save(connection);
      }
      ResourceActivity fetchedActivity = ResourceActivity.findById(activity.getId(), connection);
      if (fetchedActivity == null) {
        fail("Null fetchedActivity");
      } else {
        assertEquals(activity.getMessages().size(), fetchedActivity.getMessages().size());
        assertTrue(activity.getMessages().containsAll(fetchedActivity.getMessages()));
      }
      film.delete(connection);
      assertNull(ResourceActivity.findById(activity.getId(), connection));
      user.delete(connection);
    }
  }

}
