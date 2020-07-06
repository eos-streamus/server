package com.eos.streamus.dto.validators;

import com.eos.streamus.dto.MusicianDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class MusicianDTOValidator implements Validator {

  @Autowired
  private PersonDTOValidator personDTOValidator;

  @Override
  public boolean supports(final Class<?> aClass) {
    return aClass.equals(MusicianDTO.class);
  }

  @Override
  public void validate(final Object o, final Errors errors) {
    MusicianDTO musician = (MusicianDTO) o;
    if (musician.getPerson() != null) {
      personDTOValidator.validate(musician.getPerson(), errors);
    }
    if (musician.getPerson() == null && musician.getName() == null) {
      errors.reject("Either <name> must be set, or <person> should be defined");
    }
    if (musician.getName() != null && musician.getName().isEmpty()) {
      errors.reject("<name> cannot be empty");
    }
  }

}
