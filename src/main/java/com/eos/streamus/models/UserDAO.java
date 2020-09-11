package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.Connection;
import java.sql.SQLException;

public class UserDAO {
  private UserDAO() {}

  /**
   * Find a User or Admin by id.
   *
   * @param id id of User or Admin to find.
   * @param connection database connection to use.
   * @return found user.
   * @throws SQLException if an error occurs.
   * @throws NoResultException if no User could be found.
   */
  public static User findById(final Integer id, final Connection connection) throws SQLException, NoResultException {
    try {
      return Admin.findById(id, connection);
    } catch (NoResultException noResultException) {
      // Do nothing, could be User
    }
    return User.findById(id, connection);
  }

  /**
   * Find a User or Admin by id.
   *
   * @param email Email of user to find.
   * @param connection database connection to use.
   * @return found user.
   * @throws SQLException if an error occurs.
   */
  public static User findByEmail(final String email, final Connection connection) throws SQLException {
    User user = Admin.findByEmail(email, connection);
    if (user != null) {
      return user;
    }
    return User.findByEmail(email, connection);
  }
}
