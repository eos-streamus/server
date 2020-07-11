package com.eos.streamus.dto.validators;

import com.eos.streamus.dto.BandMemberDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public final class BandMemberDTOValidator implements Validator {
  /** {@link com.eos.streamus.dto.validators.MusicianDTOValidator} to use. */
  @Autowired
  private MusicianDTOValidator musicianDTOValidator;

  @Override
  public boolean supports(final Class<?> aClass) {
    return aClass.equals(BandMemberDTO.class);
  }

  @Override
  public void validate(final Object o, final Errors errors) {
    BandMemberDTO bandMemberDTO = (BandMemberDTO) o;
    if (bandMemberDTO.getMusician() != null) {
      musicianDTOValidator.validate(bandMemberDTO.getMusician(), errors);
    }
    if (bandMemberDTO.getMusician() == null && bandMemberDTO.getMusicianId() == null) {
      errors.reject("Invalid musician data");
    }
  }

}
