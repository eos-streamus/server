package com.eos.streamus.payloadmodels.validators;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Song;
import com.eos.streamus.payloadmodels.SongCollection;
import com.eos.streamus.payloadmodels.Track;
import com.eos.streamus.utils.IDatabaseConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;

@Component
public abstract class SongCollectionValidator implements Validator {

  @Autowired
  private IDatabaseConnector databaseConnector;

  @Override
  public final void validate(final Object o, final Errors errors) {
    SongCollection songCollection = (SongCollection) o;

    if (songCollection.getName().isBlank()) {
      errors.reject("Invalid playlist name");
    }

    try (Connection connection = databaseConnector.getConnection()) {
      if (songCollection.getTracks() != null) {
        songCollection.getTracks().sort(Comparator.comparingInt(Track::getTrackNumber));
        for (int i = 0; i < songCollection.getTracks().size(); i++) {
          if (songCollection.getTracks().get(i).getTrackNumber() < 1) {
            errors.reject("Invalid track number, must be >= 1");
          }
          Song.findById(songCollection.getTracks().get(i).getSongId(), connection);
          if (
              (i == 0 && songCollection.getTracks().get(i).getTrackNumber() != 1) ||
              (
                  i > 0 &&
                  songCollection.getTracks().get(i - 1).getTrackNumber() !=
                  songCollection.getTracks().get(i).getTrackNumber() - 1
              )
          ) {
            errors.reject("Invalid track numbers");
          }
        }
      }
    } catch (SQLException sqlException) {
      errors.reject("SQL Error");
    } catch (NoResultException noResultException) {
      errors.reject("Invalid artist or song ids");
    }
    validateSubclassProperties(o, errors);
  }

  protected abstract void validateSubclassProperties(final Object o, final Errors errors);

}
