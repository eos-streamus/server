package com.eos.streamus.payloadmodels;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.ArtistDAO;
import com.eos.streamus.models.Song;
import com.eos.streamus.utils.DatabaseConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;

@Component
public class AlbumValidator implements Validator {

  @Autowired
  private DatabaseConnection databaseConnection;

  @Override
  public boolean supports(final Class<?> aClass) {
    return aClass.equals(Album.class);
  }

  @Override
  public void validate(final Object o, final Errors errors) {
    Album album = (Album) o;

    if (album.getName().isBlank()) {
      errors.reject("Invalid album name");
    }

    try (Connection connection = databaseConnection.getConnection()) {
      for (Integer artistId : album.getArtistIds()) {
        ArtistDAO.findById(artistId, connection);
      }
      album.getTracks().sort(Comparator.comparingInt(Track::getTrackNumber));
      for (int i = 0; i < album.getTracks().size(); i++) {
        if (album.getTracks().get(i).getTrackNumber() < 1) {
          errors.reject("Invalid track number, must be >= 1");
        }
        Song.findById(album.getTracks().get(i).getSongId(), connection);
        if ((i == 0 && album.getTracks().get(i).getTrackNumber() != 1) ||
            (i > 0 && album.getTracks().get(i - 1).getTrackNumber() != album.getTracks().get(i).getTrackNumber() - 1)) {
          errors.reject("Invalid track numbers");
        }
      }
    } catch (SQLException sqlException) {
      errors.reject("SQL Error");
    } catch (NoResultException noResultException) {
      errors.reject("Invalid artist or song ids");
    }
  }

}
