package com.eos.streamus.dto.validators;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.ArtistDAO;
import com.eos.streamus.dto.AlbumDTO;
import com.eos.streamus.utils.IDatabaseConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.sql.Connection;
import java.sql.SQLException;

@Component
public class AlbumDTOValidator extends SongCollectionDTOValidator {

  @Autowired
  private IDatabaseConnector databaseConnector;

  @Override
  public boolean supports(final Class<?> aClass) {
    return aClass.equals(AlbumDTO.class);
  }

  @Override
  protected void validateSubclassProperties(final Object o, final Errors errors) {
    AlbumDTO albumDTO = (AlbumDTO) o;
    try (Connection connection = databaseConnector.getConnection()) {
      for (Integer artistId : albumDTO.getArtistIds()) {
        ArtistDAO.findById(artistId, connection);
      }
    } catch (SQLException sqlException) {
      errors.reject("SQL Error");
    } catch (NoResultException noResultException) {
      errors.reject("Invalid artist or song ids");
    }

  }

}
