package com.eos.streamus.payloadmodels.validators;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.ArtistDAO;
import com.eos.streamus.payloadmodels.Album;
import com.eos.streamus.utils.IDatabaseConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.sql.Connection;
import java.sql.SQLException;

@Component
public class AlbumValidator extends SongCollectionValidator {

  @Autowired
  private IDatabaseConnector databaseConnector;

  @Override
  public boolean supports(final Class<?> aClass) {
    return aClass.equals(Album.class);
  }

  @Override
  protected void validateSubclassProperties(final Object o, final Errors errors) {
    Album album = (Album) o;
    try (Connection connection = databaseConnector.getConnection()) {
      for (Integer artistId : album.getArtistIds()) {
        ArtistDAO.findById(artistId, connection);
      }
    } catch (SQLException sqlException) {
      errors.reject("SQL Error");
    } catch (NoResultException noResultException) {
      errors.reject("Invalid artist or song ids");
    }

  }

}
