package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Date.valueOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class BandTests extends DatabaseTests {

  @Test
  void testEmptyBandCRUD() throws SQLException, NoResultException {
    try (Connection connection = databaseConnector.getConnection()) {
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
    try (Connection connection = databaseConnector.getConnection()) {
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
    try (Connection connection = databaseConnector.getConnection()) {
      Band testBand = new Band("Test");
      testBand.save(connection);

      Musician musician = new Musician(new Person("Test", "Person", valueOf("1990-01-01")));
      musician.save(connection);

      testBand.addMember(musician, valueOf("2000-01-01"), valueOf("2010-01-01"));
      testBand.addMember(musician, valueOf("2005-01-01"));
      testBand.setName("Test updated");
      connection.setAutoCommit(false);
      try {
        testBand.save(connection);
        connection.commit();
      } catch (SQLException e) {
        connection.rollback();
      }
      connection.setAutoCommit(true);
      assertNotEquals(testBand.getName(), Band.findById(testBand.getId(), connection).getName());
      musician.getPerson().delete(connection);
      assertThrows(NoResultException.class, () -> Person.findById(musician.getPerson().getId(), connection));
      musician.delete(connection);
      assertThrows(NoResultException.class, () -> Musician.findById(musician.getId(), connection));
      testBand.delete(connection);
      assertThrows(NoResultException.class, () -> Band.findById(testBand.getId(), connection));
    }
  }
}
