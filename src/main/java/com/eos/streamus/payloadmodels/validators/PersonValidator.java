package com.eos.streamus.payloadmodels.validators;

import com.eos.streamus.payloadmodels.Person;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PersonValidator implements Validator {
  @Override
  public boolean supports(final Class<?> aClass) {
    return aClass.equals(Person.class);
  }

  @Override
  public void validate(final Object o, final Errors errors) {
    Person person = (Person) o;
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
  }

}
