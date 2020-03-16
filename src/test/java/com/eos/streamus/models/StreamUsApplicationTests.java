package com.eos.streamus.models;

import com.eos.streamus.utils.TestDatabaseConnection;
import com.eos.streamus.exceptions.NoResultException;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransactionRollbackException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static java.sql.Date.valueOf;

@SpringBootTest
class StreamUsApplicationTests {
  @Autowired
  protected TestDatabaseConnection databaseConnection = null;

  @Test
  void connectToDatabase() {
    assertDoesNotThrow(() -> {
      Connection connection = databaseConnection.getConnection();
      connection.close();
    });
  }

  @Test
  void testSongCRUD() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      // Create
      Song song = new Song(String.format("test%d.mp3", new Date().getTime()), "Test song", 100);
      song.save(connection);

      // Read
      Song retrievedSong = Song.findById(song.getId(), connection);
      assertEquals(song, retrievedSong);

      // Update
      song.setName("Changed song name");
      song.setPath(String.format("test%d.mp3", new Date().getTime()));
      song.setDuration(101);
      song.save(connection);
      song.save(connection);

      retrievedSong = Song.findById(song.getId(), connection);
      assertEquals(song, retrievedSong);

      // Delete
      song.delete(connection);
      assertThrows(NoResultException.class, () -> Song.findById(song.getId(), connection));
    }
  }

  @Test
  void testFilmCRUD() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      // Create
      Film film = new Film(String.format("test%d.mp4", new Date().getTime()), "Test film", 100);
      film.save(connection);

      // Read
      Film retrievedFilm = Film.findById(film.getId(), connection);
      assertEquals(film, retrievedFilm);

      // Update
      film.setName("Changed film name");
      film.setPath(String.format("test%d.mp4", new Date().getTime()));
      film.setDuration(101);
      film.save(connection);

      retrievedFilm = Film.findById(film.getId(), connection);
      assertEquals(film, retrievedFilm);

      // Delete
      film.delete(connection);
      assertThrows(NoResultException.class, () -> Film.findById(film.getId(), connection));
    }
  }

  @Test
  void testPersonCRUD() throws SQLException, NoResultException, ParseException {
    try (Connection connection = databaseConnection.getConnection()) {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      java.sql.Date sqlDate = new java.sql.Date(dateFormat.parse("1970-08-01").getTime());

      // Create
      Person person = new Person("John", "Doe", sqlDate);
      person.save(connection);

      // Read
      Person retrievedPerson = Person.findById(person.getId(), connection);
      assertEquals(person, retrievedPerson);

      // Update
      person.setFirstName("Jane");
      person.setLastName("Donut");
      person.setDateOfBirth(new java.sql.Date(dateFormat.parse("1970-08-02").getTime()));
      person.save(connection);

      retrievedPerson = Person.findById(person.getId(), connection);
      assertEquals(person, retrievedPerson);

      // Delete
      person.delete(connection);
      assertThrows(NoResultException.class, () -> Person.findById(person.getId(), connection));
    }
  }

  @Test
  void testUserCRUD() throws SQLException, NoResultException, ParseException {
    try (Connection connection = databaseConnection.getConnection()) {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      java.sql.Date sqlDate = new java.sql.Date(dateFormat.parse("1970-08-01").getTime());

      // Create
      User user = new User("John", "Doe", sqlDate, String.format("john.doe%d@email.com", new Random().nextInt()), "johndoe");
      user.save(connection);

      // Read
      User retrievedUser = User.findById(user.getId(), connection);
      assertEquals(user, retrievedUser);

      // Update
      user.setFirstName("Jane");
      user.setLastName("Donut");
      user.setDateOfBirth(new java.sql.Date(dateFormat.parse("1970-08-02").getTime()));
      user.setEmail(String.format("jane.donut%d@email.com", new Random().nextInt()));
      user.setUsername("janedonut");
      user.save(connection);

      retrievedUser = User.findById(user.getId(), connection);
      assertEquals(user, retrievedUser);

      // Delete
      user.delete(connection);
      assertThrows(NoResultException.class, () -> User.findById(user.getId(), connection));
    }
  }

  @Test
  void testEmptySongPlaylistCRUD() throws SQLException, NoResultException, ParseException {
    try (Connection connection = databaseConnection.getConnection()) {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      java.sql.Date sqlDate = new java.sql.Date(dateFormat.parse("1970-08-01").getTime());
      User user = new User("John", "Doe", sqlDate, String.format("john.doe%d@email.com", new Random().nextInt()), "johndoe");
      user.save(connection);

      // Create
      SongPlaylist songPlaylist = new SongPlaylist("Test playlist", user);
      songPlaylist.save(connection);

      // Read
      SongPlaylist retrievedSongPlaylist = SongPlaylist.findById(songPlaylist.getId(), connection);
      assertEquals(songPlaylist, retrievedSongPlaylist);

      // Update
      songPlaylist.setName("Test playlist updated");
      songPlaylist.save(connection);

      retrievedSongPlaylist = SongPlaylist.findById(songPlaylist.getId(), connection);
      assertEquals(songPlaylist, retrievedSongPlaylist);

      // Delete
      songPlaylist.delete(connection);
      assertThrows(NoResultException.class, () -> SongPlaylist.findById(songPlaylist.getId(), connection));

      user.delete(connection);
    }
  }

  @Test
  void testPopulatedSongPlaylistCRUD() throws SQLException, NoResultException, ParseException {
    try (Connection connection = databaseConnection.getConnection()) {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      java.sql.Date sqlDate = new java.sql.Date(dateFormat.parse("1970-08-01").getTime());
      User user = new User("John", "Doe", sqlDate, String.format("john.doe%d@email.com", new Random().nextInt()), "johndoe");
      user.save(connection);

      // Create
      SongPlaylist songPlaylist = new SongPlaylist("Test playlist", user);
      songPlaylist.save(connection);
      List<Song> testSongs = new ArrayList<>(); // Used to keep track of songs to delete
      for (int i = 0; i < 10; i++) {
        Song song = new Song(String.format("test%d.mp3", new Date().getTime()), "Test song", 100);
        song.save(connection);
        testSongs.add(song);
        songPlaylist.addSong(song);
      }
      songPlaylist.save(connection);

      // Read
      SongPlaylist retrievedSongPlaylist = SongPlaylist.findById(songPlaylist.getId(), connection);
      assertEquals(songPlaylist, retrievedSongPlaylist);

      // Update
      songPlaylist.setName("Test playlist updated");
      for (int i = 0; i < 10; i++) {
        Song song = new Song(String.format("test%d.mp3", new Date().getTime()), "Test song", 100);
        song.save(connection);
        testSongs.add(song);
        songPlaylist.addSong(song);
      }
      songPlaylist.save(connection);

      retrievedSongPlaylist = SongPlaylist.findById(songPlaylist.getId(), connection);
      assertEquals(songPlaylist, retrievedSongPlaylist);

      // Delete
      songPlaylist.delete(connection);
      assertThrows(NoResultException.class, () -> SongPlaylist.findById(songPlaylist.getId(), connection));

      // Clean up Songs
      testSongs.forEach(song -> {
        try {
          song.delete(connection);
        } catch (SQLException e) {
          fail(String.format("Song %s could not be deleted", song));
        }
      });
      user.delete(connection);
    }
  }

  @Test
  void testEmptyVideoPlaylistCRUD() throws SQLException, NoResultException, ParseException {
    try (Connection connection = databaseConnection.getConnection()) {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      java.sql.Date sqlDate = new java.sql.Date(dateFormat.parse("1970-08-01").getTime());
      User user = new User("John", "Doe", sqlDate, String.format("john.doe%d@email.com", new Random().nextInt()), "johndoe");
      user.save(connection);

      // Create
      VideoPlaylist videoPlaylist = new VideoPlaylist("Test playlist", user);
      videoPlaylist.save(connection);

      // Read
      VideoPlaylist retrievedVideoPlaylist = VideoPlaylist.findById(videoPlaylist.getId(), connection);
      assertEquals(videoPlaylist, retrievedVideoPlaylist);

      // Update
      videoPlaylist.setName("Test playlist updated");
      videoPlaylist.save(connection);

      retrievedVideoPlaylist = VideoPlaylist.findById(videoPlaylist.getId(), connection);
      assertEquals(videoPlaylist, retrievedVideoPlaylist);

      // Delete
      videoPlaylist.delete(connection);
      assertThrows(NoResultException.class, () -> VideoPlaylist.findById(videoPlaylist.getId(), connection));
      user.delete(connection);
    }
  }

  @Test
  void testPopulatedVideoPlaylistCRUD() throws SQLException, NoResultException, ParseException {
    try (Connection connection = databaseConnection.getConnection()) {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      java.sql.Date sqlDate = new java.sql.Date(dateFormat.parse("1980-01-01").getTime());
      User user = new User("John", "Doe", sqlDate, String.format("john.doe%d@email.com", new Random().nextInt()), "johndoe");
      user.save(connection);
      VideoPlaylist videoPlaylist = new VideoPlaylist("Test video playlist", user);
      videoPlaylist.save(connection);
      List<Video> testVideos = new ArrayList<>();
      for (int i = 0; i < 10; i++) {
        Video video = new Film(String.format("test%d.mp3", new Date().getTime()), "Test video", 100);
        video.save(connection);
        testVideos.add(video);
        videoPlaylist.addVideo(video);
      }
      videoPlaylist.save(connection);

      VideoPlaylist retrievedVideoPlaylist = VideoPlaylist.findById(videoPlaylist.getId(), connection);
      assertEquals(videoPlaylist, retrievedVideoPlaylist);

      // Update
      videoPlaylist.setName("Test playlist updated");
      for (int i = 0; i < 10; i++) {
        Video video = new Film(String.format("test%d.mp3", new Date().getTime()), "Test video", 100);
        video.save(connection);
        testVideos.add(video);
        videoPlaylist.addVideo(video);
      }
      videoPlaylist.save(connection);

      retrievedVideoPlaylist = VideoPlaylist.findById(videoPlaylist.getId(), connection);
      assertEquals(videoPlaylist, retrievedVideoPlaylist);

      // Delete
      videoPlaylist.delete(connection);
      assertThrows(NoResultException.class, () -> SongPlaylist.findById(videoPlaylist.getId(), connection));

      user.delete(connection);
      for (Video video : testVideos) {
        video.delete(connection);
      }
    }
  }

  @Test
  void testEmptySeriesCRUD() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      // Create
      Series series = new Series(String.format("Test series %d", new Date().getTime()));
      series.save(connection);
      assertNotNull(series.getId());

      // Read
      Series retrievedSeries = Series.findById(series.getId(), connection);
      assertEquals(series, retrievedSeries);

      // Update
      series.setName(String.format("Test series updated %d", new Date().getTime()));
      series.save(connection);
      retrievedSeries = Series.findById(series.getId(), connection);
      assertEquals(series, retrievedSeries);

      // Delete
      series.delete(connection);
      assertThrows(NoResultException.class, () -> Series.findById(series.getId(), connection));
    }
  }

  @Test
  void testEpisodeCRUD() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      // Create
      Series series = new Series(String.format("Test series %d", new Date().getTime()));
      series.save(connection);
      Series.Episode episode = series.new Episode(String.format("test_episode_%d_%d.mp4", new Date().getTime(), new Random().nextInt()), "Test episode", 100, (short) 1, (short) 1);
      episode.save(connection);
      assertNotNull(episode.getId());

      // Read
      Series.Episode retrievedEpisode = (Series.Episode) VideoDAO.findById(episode.getId(), connection);
      assertEquals(episode, retrievedEpisode);

      // Update
      episode.setName("Test episode updated");
      episode.save(connection);
      retrievedEpisode = (Series.Episode) VideoDAO.findById(episode.getId(), connection);
      assertEquals(episode, retrievedEpisode);

      // Delete
      episode.delete(connection);
      assertThrows(NoResultException.class, () -> VideoDAO.findById(episode.getId(), connection));
      series.delete(connection);
    }
  }

  @Test
  void testEpisodeDeleteCascade() throws SQLException {
    try (Connection connection = databaseConnection.getConnection()) {
      // Create
      Series series = new Series(String.format("Test series %d", new Date().getTime()));
      series.save(connection);
      Series.Episode episode = series.new Episode(String.format("test_episode_%d_%d.mp4", new Date().getTime(), new Random().nextInt()), "Test episode", 100, (short) 1, (short) 1);
      episode.save(connection);
      assertNotNull(episode.getId());

      series.delete(connection);
      assertThrows(NoResultException.class, () -> VideoDAO.findById(episode.getId(), connection));
    }
  }

  @Test
  void testEpisodeWithInvalidSeasonAndEpisodeValues() throws SQLException {
    try (Connection connection = databaseConnection.getConnection()) {
      // Create
      Series series = new Series(String.format("Test series %d", new Date().getTime()));
      series.save(connection);
      Series.Episode episode = series.new Episode(String.format("test_episode_%d_%d.mp4", new Date().getTime(), new Random().nextInt()), "Test episode", 100, (short) 2, (short) 2);
      try {
        episode.save(connection);
      } catch (PSQLException e) {
        if (!e.getMessage().contains("Invalid episode numbers")) {
          fail();
        }
      }
      series.delete(connection);
    }
  }

  @Test
  void testEpisodeWithAutomaticEpisodeNumber() throws SQLException {
    try (Connection connection = databaseConnection.getConnection()) {
      // Create
      Series series = new Series(String.format("Test series %d", new Date().getTime()));
      series.save(connection);
      Series.Episode episode1 = series.new Episode(String.format("test_episode_%d_%d.mp4", new Date().getTime(), new Random().nextInt()), "Test episode", 100, (short) 1);
      assertEquals(1, episode1.getEpisodeNumber());
      Series.Episode episode2 = series.new Episode(String.format("test_episode_%d_%d.mp4", new Date().getTime(), new Random().nextInt()), "Test episode", 100, (short) 1);
      assertEquals(2, episode2.getEpisodeNumber());

      episode1.save(connection);
      episode2.save(connection);

      series.delete(connection);
    }
  }

  @Test
  void testPopulatedSeries() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      Series series = new Series("Test series");
      series.save(connection);
      List<Series.Episode> episodes = new ArrayList<>();
      for (short i = 1; i <= 5; i++) {
        for (short j = 1; j <= 10; j++) {
          episodes.add(
            series.new Episode(
              String.format(
                "test_path_%d_%d", new Date().getTime(), new Random().nextInt()),
              String.format("Episode %d",
                j),
              100,
              i));
        }
      }
      series.save(connection);

      Series retrievedSeries = Series.findById(series.getId(), connection);
      assertEquals(series, retrievedSeries);

      retrievedSeries.delete(connection);
      assertThrows(NoResultException.class, () -> Series.findById(series.getId(), connection));
      for (Series.Episode episode : episodes) {
        assertThrows(NoResultException.class, () -> VideoDAO.findById(episode.getId(), connection));
      }
    }
  }

  @Test
  void testNamedMusicianWithNoPerson() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      Musician musician = new Musician("Test");
      musician.save(connection);

      Musician retrievedMusician = Musician.findById(musician.getId(), connection);
      assertEquals(musician, retrievedMusician);

      musician.setName("Test updated");
      musician.save(connection);

      retrievedMusician = Musician.findById(musician.getId(), connection);
      assertEquals(musician, retrievedMusician);

      musician.delete(connection);
      assertThrows(NoResultException.class, () -> Musician.findById(musician.getId(), connection));
    }
  }

  @Test
  void testNotNamedMusicianWithPerson() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      Person person = new Person("Test firstname", "Test lastname", java.sql.Date.valueOf("1980-01-01"));
      person.save(connection);
      Musician musician = new Musician(person);
      musician.save(connection);

      Musician retrievedMusician = Musician.findById(musician.getId(), connection);
      assertEquals(musician, retrievedMusician);

      person.delete(connection);
      assertNotEquals(musician, Musician.findById(musician.getId(), connection));

      musician.delete(connection);
      assertThrows(NoResultException.class, () -> Musician.findById(musician.getId(), connection));
    }
  }

  @Test
  void testNamedMusicianWithPerson() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      Person person = new Person("Test firstname", "Test lastname", java.sql.Date.valueOf("1980-01-01"));
      person.save(connection);
      Musician musician = new Musician("Test musician", person);
      musician.save(connection);

      Musician retrievedMusician = Musician.findById(musician.getId(), connection);
      assertEquals(musician, retrievedMusician);

      musician.setName("Test musician updated");
      musician.save(connection);
      retrievedMusician = Musician.findById(musician.getId(), connection);
      assertEquals(musician, retrievedMusician);

      person.delete(connection);
      assertNotEquals(musician, Musician.findById(musician.getId(), connection));

      musician.delete(connection);
      assertThrows(NoResultException.class, () -> Musician.findById(musician.getId(), connection));
    }
  }

  @Test
  void testNotNamedMusicianWithNoPerson() {
    assertThrows(IllegalArgumentException.class, () -> new Musician((String) null));
    assertThrows(IllegalArgumentException.class, () -> new Musician((Person) null));
  }

  @Test
  void testEmptyBandCRUD() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      Band band = new Band("Test band");
      band.save(connection);

      Band retrievedBand = Band.findById(band.getId(), connection);
      assertEquals(band, retrievedBand);

      band.setName("Test band updated");
      band.save(connection);
      retrievedBand = Band.findById(band.getId(), connection);
      assertEquals(band, retrievedBand);

      band.delete(connection);
      assertThrows(NoResultException.class, () -> Band.findById(band.getId(), connection));
    }
  }

  @Test
  void testBandWithMusicians() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      List<Musician> artists = new ArrayList<>();

      Band pinkFloyd = new Band("Pink Floyd");
      pinkFloyd.save(connection);

      Musician sydBarrett = new Musician(new Person("Syd", "Barrett", valueOf("1946-01-06")));
      sydBarrett.save(connection);
      artists.add(sydBarrett);

      Musician nickMason = new Musician(new Person("Nick", "Mason", valueOf("1944-01-27")));
      nickMason.save(connection);
      artists.add(nickMason);

      Musician bobClose = new Musician(new Person("Bob", "Close", valueOf("1945-01-01")));
      bobClose.save(connection);
      artists.add(bobClose);

      Musician rogerWaters = new Musician(new Person("Roger", "Waters", valueOf("1943-09-06")));
      rogerWaters.save(connection);
      artists.add(rogerWaters);

      Musician richardWright = new Musician(new Person("Richard", "Wright", valueOf("1943-07-28")));
      richardWright.save(connection);
      artists.add(richardWright);

      Musician davidGilmour = new Musician(new Person("David", "Gilmour", valueOf("1946-03-06")));
      davidGilmour.save(connection);
      artists.add(davidGilmour);

      pinkFloyd.addMember(sydBarrett, valueOf("1965-01-01"), valueOf("1968-12-31"));
      pinkFloyd.save(connection);
      assertEquals(pinkFloyd, Band.findById(pinkFloyd.getId(), connection));
      for (Musician artist : artists) {
        artist.getPerson().delete(connection);
        artist.delete(connection);
      }
      pinkFloyd.delete(connection);
    }
  }

  @Test
  void testBandWithOverlappingMusicians() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      Band testBand = new Band("Test");
      testBand.save(connection);

      Musician musician = new Musician(new Person("Test", "Person", valueOf("1990-01-01")));
      musician.save(connection);

      testBand.addMember(musician, valueOf("2000-01-01"), valueOf("2010-01-01"));
      testBand.addMember(musician, valueOf("2005-01-01"));
      testBand.setName("Test updated");
      assertThrows(SQLTransactionRollbackException.class, () -> testBand.save(connection));
      assertNotEquals(testBand.getName(), Band.findById(testBand.getId(), connection).getName());
      musician.getPerson().delete(connection);
      assertThrows(NoResultException.class, () -> Person.findById(musician.getPerson().getId(), connection));
      musician.delete(connection);
      assertThrows(NoResultException.class, () -> Musician.findById(musician.getId(), connection));
      testBand.delete(connection);
      assertThrows(NoResultException.class, () -> Band.findById(testBand.getId(), connection));
    }
  }

  @Test
  void testAdmin() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      Admin admin = new Admin("Test", "Admin", valueOf("1990-01-01"), String.format("test%d@admin.com", new Random().nextInt()), "test_admin");
      admin.save(connection);
      assertNotNull(admin.getId());
      assertNotNull(admin.getCreatedAt());
      assertNotNull(admin.getUpdatedAt());

      assertEquals(admin, Admin.findById(admin.getId(), connection));
      admin.delete(connection);
      assertThrows(NoResultException.class, () -> Admin.findById(admin.getId(), connection));
    }
  }

  @Test
  void testResourceActivity() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      Song song = new Song(randomString(), randomString(), 100);
      song.save(connection);

      User user = new User(randomString(), randomString(), randomDate(), randomString() + "@" + randomString(), randomString());
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
    try (Connection connection = databaseConnection.getConnection()) {
      Series theExpanse = new Series("The Expanse");
      short season1 = 1;
      theExpanse.new Episode(randomString(), "Dulcinea", 2700, season1);
      theExpanse.new Episode(randomString(), "The Big Empty", 2700, season1);
      theExpanse.new Episode(randomString(), "Remember the Cant", 2700, season1);
      theExpanse.new Episode(randomString(), "CQB (Close Quarter Battle)", 2700, season1);
      theExpanse.new Episode(randomString(), "Back to the Butcher", 2700, season1);
      theExpanse.new Episode(randomString(), "Rock Bottom", 2700, season1);
      theExpanse.new Episode(randomString(), "Windmills", 2700, season1);
      theExpanse.new Episode(randomString(), "Salvage", 2700, season1);
      theExpanse.new Episode(randomString(), "Critical Mass", 2700, season1);
      theExpanse.new Episode(randomString(), "Leviathan Wakes", 2700, season1);

      short season2 = 2;
      theExpanse.new Episode(randomString(), "Safe", 2700, season2);
      theExpanse.new Episode(randomString(), "Doors & Corners", 2700, season2);
      theExpanse.new Episode(randomString(), "Static", 2700, season2);
      theExpanse.new Episode(randomString(), "Godspeed", 2700, season2);
      theExpanse.new Episode(randomString(), "Home", 2700, season2);
      theExpanse.new Episode(randomString(), "Paradigm Shift", 2700, season2);
      theExpanse.new Episode(randomString(), "The Seventh Man", 2700, season2);
      theExpanse.new Episode(randomString(), "Pyre", 2700, season2);
      theExpanse.new Episode(randomString(), "The Weeping Somnambulist", 2700, season2);
      theExpanse.new Episode(randomString(), "Cascade", 2700, season2);
      theExpanse.new Episode(randomString(), "Here There be Dragons", 2700, season2);
      theExpanse.new Episode(randomString(), "The Monster and the Rocket", 2700, season2);
      theExpanse.new Episode(randomString(), "Caliban's War", 2700, season2);

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
        assertEquals(i, collectionActivity.getContent().stream().reduce(0, (subtotal, entry) -> subtotal + (entry.getValue().getValue() == null ? 0 : 1), Integer::sum));
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
    try (Connection connection = databaseConnection.getConnection()) {
      Film film = new Film(randomString(), randomString(), Math.abs(new Random().nextInt()));
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
      }
      assertEquals(activity.getMessages().size(), fetchedActivity.getMessages().size());
      assertTrue(activity.getMessages().containsAll(fetchedActivity.getMessages()));

      film.delete(connection);
      assertNull(ResourceActivity.findById(activity.getId(), connection));
      user.delete(connection);
    }
  }

  @Test
  void testAlbum() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      Album album = new Album(randomString(), randomDate());

      Song[] songs = new Song[10];
      for (int i = 0; i < 10; i++) {
        songs[i] = new Song(randomString(), randomString(), 100);
        songs[i].save(connection);
        album.addSong(songs[i]);
      }
      album.save(connection);
      Album fetchedAlbum = Album.findById(album.getId(), connection);
      assertEquals(album.getContent().size(), fetchedAlbum.getContent().size());
      assertEquals(album, fetchedAlbum);

      album.delete(connection);
      for (Song song : songs) {
        song.delete(connection);
      }
    }
  }

  @Test
  void testAlbumWithArtists() throws SQLException, NoResultException {
    try (Connection connection = databaseConnection.getConnection()) {
      Band band = new Band("test band");
      band.save(connection);

      Musician musician = new Musician(randomPerson());
      musician.save(connection);

      Album album = new Album(randomString(), randomDate());

      album.addArtist(band);
      album.addArtist(musician);

      Song[] songs = new Song[10];
      for (int i = 0; i < 10; i++) {
        songs[i] = new Song(randomString(), randomString(), 100);
        songs[i].save(connection);
        album.addSong(songs[i]);
      }
      album.save(connection);
      Album fetchedAlbum = Album.findById(album.getId(), connection);
      assertEquals(album.getContent().size(), fetchedAlbum.getContent().size());
      assertEquals(album, fetchedAlbum);

      album.delete(connection);
      for (Song song : songs) {
        song.delete(connection);
      }
      musician.getPerson().delete(connection);
      musician.delete(connection);
      band.delete(connection);
    }
  }

  private String randomString() {
    return "randomString" + new Random().nextDouble();
  }

  private java.sql.Date randomDate() {
    Random random = new Random();
    int year = random.nextInt() % 70 + 1940;
    int month = Math.abs(random.nextInt()) % 12 + 1;
    return valueOf(String.format("%d-%s-01", year, (month < 10 ? "0" + month : month)));
  }

  private Person randomPerson() {
    return new Person(randomString(), randomString(), randomDate());
  }

  private User randomUser() {
    return new User(randomString(), randomString(), randomDate(), randomString() + "@" + randomString(), randomString());
  }
}
