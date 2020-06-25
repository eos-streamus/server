package com.eos.streamus.controllers;

import com.eos.streamus.StreamusTestConfiguration;
import com.eos.streamus.models.Activity;
import com.eos.streamus.models.Artist;
import com.eos.streamus.models.Collection;
import com.eos.streamus.models.Person;
import com.eos.streamus.models.Resource;
import com.eos.streamus.models.Song;
import com.eos.streamus.utils.IDatabaseConnection;
import com.eos.streamus.utils.IResourcePathResolver;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@WebMvcTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(classes = StreamusTestConfiguration.class)
@AutoConfigureMockMvc
abstract class ControllerTests {
  static class TestJsonFactory extends JsonNodeFactory {
    private static final long serialVersionUID = 6068382117192685166L;

  }

  protected final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

  protected static final Path SAMPLE_AUDIO_PATH = Paths.get(
      String.format(
          "src%stest%sresources%ssample-audio.mp3",
          File.separator,
          File.separator,
          File.separator
      )
  );

  protected static final Path SAMPLE_VIDEO_PATH = Paths.get(
      String.format(
          "src%stest%sresources%ssample-video.mp4",
          File.separator,
          File.separator,
          File.separator
      )
  );

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected IResourcePathResolver resourcePathResolver;

  @Autowired
  protected IDatabaseConnection databaseConnection;

  protected final Date date(final String dateString) throws ParseException {
    return new Date(dateFormatter.parse(dateString).getTime());
  }

  @AfterAll
  void emptyDatabase() throws SQLException, IOException {
    try (Connection connection = databaseConnection.getConnection()) {
      // Delete all Resources
      List<String> pathStrings = new ArrayList<>();
      try (PreparedStatement preparedStatement = connection.prepareStatement(
          String.format("select distinct %s from %s", Resource.PATH_COLUMN, Resource.TABLE_NAME)
      )) {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          while (resultSet.next()) {
            pathStrings.add(resultSet.getString(1));
          }
        }
      }
      for (final String pathString : pathStrings) {
        Files.delete(Paths.get(pathString));
      }

      try (PreparedStatement preparedStatement = connection
          .prepareStatement("truncate table " + Resource.TABLE_NAME + " cascade")) {
        preparedStatement.execute();
      }

      // Delete all Persons
      try (PreparedStatement preparedStatement = connection
          .prepareStatement("truncate table " + Person.TABLE_NAME + " cascade")) {
        preparedStatement.execute();
      }

      // Delete all Artists
      try (PreparedStatement preparedStatement = connection
          .prepareStatement("truncate table " + Artist.TABLE_NAME + " cascade")) {
        preparedStatement.execute();
      }

      // Delete all Collections
      try (PreparedStatement preparedStatement = connection
          .prepareStatement("truncate table " + Collection.TABLE_NAME + " cascade")) {
        preparedStatement.execute();
      }

      // Delete all Activities
      try (PreparedStatement preparedStatement = connection
          .prepareStatement("truncate table " + Activity.TABLE_NAME + " cascade")) {
        preparedStatement.execute();
      }
    }
  }

}
