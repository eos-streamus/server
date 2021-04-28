package com.eos.streamus.dto.validators;

import com.eos.streamus.dto.PersonDTO;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Date;

@Component
public class PersonDTOValidator implements Validator {

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supports(final Class<?> aClass) {
    return aClass.equals(PersonDTO.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void validate(@NonNull final Object o, @NonNull final Errors errors) {
    PersonDTO person = (PersonDTO) o;
    if ((person.getFirstName() == null) ^ (person.getLastName() == null)) {
      errors.reject("<firstName> and <lastName> must either both be defined, or not");
    }
    if (person.getId() == null && person.getFirstName() == null) {
      errors.reject("Either <id> or (<firstName> and <lastName>) should be defined");
    }
    if (person.getDateOfBirth() != null && person.getFirstName() == null) {
      errors.reject("<dateOfBirth> cannot be defined if <firstName> and <lastName> are not");
    }
    if (person.getFirstName() != null && person.getFirstName().isEmpty()) {
      errors.reject("<firstName> cannot be empty");
    }
    if (person.getLastName() != null && person.getLastName().isEmpty()) {
      errors.reject("<lastName> cannot be empty");
    }
    if (person.getDateOfBirth() != null && java.sql.Date.valueOf(person.getDateOfBirth()).after(new Date())) {
      errors.reject("<dateOfBirth> cannot be in the future");
    }
  }

}
