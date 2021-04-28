package com.eos.streamus.dto.validators;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.User;
import com.eos.streamus.dto.SongPlaylistDTO;
import com.eos.streamus.utils.IDatabaseConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.sql.Connection;
import java.sql.SQLException;

@Component
public class SongPlaylistDTOValidator extends SongCollectionDTOValidator {

  /**
   * {@link IDatabaseConnector} to use.
   */
  @Autowired
  private IDatabaseConnector databaseConnector;

  /** {@inheritDoc} */
  @Override
  public boolean supports(final Class<?> aClass) {
    return aClass.equals(SongPlaylistDTO.class);
  }

  /** {@inheritDoc} */
  @Override
  protected void validateSubclassProperties(final Object o, final Errors errors) {
    SongPlaylistDTO songPlaylistDTO = (SongPlaylistDTO) o;
    if (songPlaylistDTO.getName().isBlank()) {
      errors.reject("Invalid playlist name");
    }

    try (Connection connection = databaseConnector.getConnection()) {
      User.findById(songPlaylistDTO.getUserId(), connection);
    } catch (SQLException sqlException) {
      errors.reject("SQL Error");
    } catch (NoResultException noResultException) {
      errors.reject("Invalid artist or song ids");
    }

  }

}
