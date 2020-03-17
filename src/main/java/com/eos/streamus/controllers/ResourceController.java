package com.eos.streamus.controllers;

import com.eos.streamus.models.Film;
import com.eos.streamus.utils.TestDatabaseConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@RestController
public class ResourceController {
  @Autowired
  protected TestDatabaseConnection databaseConnection = null;

  @GetMapping("/videos")
  public List<Film> getResources() throws SQLException {
    try (Connection connection = databaseConnection.getConnection()) {
      return Film.all(connection);
    }
  }
}
