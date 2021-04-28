package com.eos.streamus.dto.validators;

import com.eos.streamus.dto.MusicianDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class MusicianDTOValidator implements Validator {

  /**
   * {@link PersonDTOValidator} to use to validate Musican {@link com.eos.streamus.models.Person} data.
   */
  @Autowired
  private PersonDTOValidator personDTOValidator;

  /** {@inheritDoc} */
  @Override
  public boolean supports(final Class<?> aClass) {
    return aClass.equals(MusicianDTO.class);
  }

  /** {@inheritDoc} */
  @Override
  public void validate(@NonNull final Object o, @NonNull final Errors errors) {
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
