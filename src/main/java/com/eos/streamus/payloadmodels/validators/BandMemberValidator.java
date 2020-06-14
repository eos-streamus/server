package com.eos.streamus.payloadmodels.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class BandMemberValidator implements Validator {
  @Autowired
  private MusicianValidator musicianValidator;

  @Override
  public boolean supports(final Class<?> aClass) {
    return aClass.equals(BandMember.class);
  }

  @Override
  public void validate(final Object o, final Errors errors) {
    BandMember bandMember = (BandMember) o;
    if (bandMember.getMusician() != null) {
      musicianValidator.validate(bandMember.getMusician(), errors);
    }
    if (bandMember.getMusician() == null && bandMember.getMusicianId() == null) {
      errors.reject("Invalid musician data");
    }
  }

}
