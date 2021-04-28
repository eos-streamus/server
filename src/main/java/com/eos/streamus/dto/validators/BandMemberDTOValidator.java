package com.eos.streamus.dto.validators;

import com.eos.streamus.dto.BandMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class BandMemberDTOValidator implements Validator {
  /**
   * {@link MusicianDTOValidator} to use to validate band members.
   */
  @Autowired
  private MusicianDTOValidator musicianDTOValidator;

  /** {@inheritDoc} */
  @Override
  public boolean supports(final Class<?> aClass) {
    return aClass.equals(BandMember.class);
  }

  /** {@inheritDoc} */
  @Override
  public void validate(@NonNull final Object o, @NonNull final Errors errors) {
    BandMember bandMember = (BandMember) o;
    if (bandMember.getMusician() != null) {
      musicianDTOValidator.validate(bandMember.getMusician(), errors);
    }
    if (bandMember.getMusician() == null && bandMember.getMusicianId() == null) {
      errors.reject("Invalid musician data");
    }
  }

}
