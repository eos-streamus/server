package com.eos.streamus.payloadmodels.validators;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.User;
import com.eos.streamus.payloadmodels.SongPlaylist;
import com.eos.streamus.utils.IDatabaseConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.sql.Connection;
import java.sql.SQLException;

@Component
public class SongPlaylistValidator extends SongCollectionValidator {

  @Autowired
  private IDatabaseConnector databaseConnector;

  @Override
  public boolean supports(final Class<?> aClass) {
    return aClass.equals(SongPlaylist.class);
  }

  @Override
  protected void validateSubclassProperties(final Object o, final Errors errors) {
    SongPlaylist songPlaylist = (SongPlaylist) o;
    if (songPlaylist.getName().isBlank()) {
      errors.reject("Invalid playlist name");
    }

    try (Connection connection = databaseConnector.getConnection()) {
      User.findById(songPlaylist.getUserId(), connection);
    } catch (SQLException sqlException) {
      errors.reject("SQL Error");
    } catch (NoResultException noResultException) {
      errors.reject("Invalid artist or song ids");
    }

  }

}
