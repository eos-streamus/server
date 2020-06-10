package com.eos.streamus.payloadmodels;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class MusicianValidator implements Validator {

  @Autowired
  private PersonValidator personValidator;

  @Override
  public boolean supports(final Class<?> aClass) {
    return aClass.equals(Musician.class);
  }

  @Override
  public void validate(final Object o, final Errors errors) {
    Musician musician = (Musician) o;
    if (musician.getPerson() != null) {
      personValidator.validate(musician.getPerson(), errors);
    }
    if (musician.getPerson() == null && musician.getName() == null) {
      errors.reject("Either <name> must be set, or <person> should be defined");
    }
    if (musician.getName() != null && musician.getName().isEmpty()) {
      errors.reject("<name> cannot be empty");
    }
  }

}
