package com.eos.streamus.dto.validators;

import com.eos.streamus.dto.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.text.ParseException;

import static com.eos.streamus.utils.Constants.EMAIL_REGEX;

@Component
public class UserDTOValidator implements Validator {
  @Value("${minPasswordLength}")
  private int minPasswordLength;
  @Value("${minUsernameLength}")
  private int minUsernameLength;

  @Override
  public boolean supports(final Class<?> aClass) {
    return aClass.equals(UserDTO.class);
  }

  @Override
  public void validate(final Object o, final Errors errors) {
    UserDTO userDTO = (UserDTO) o;

    if (userDTO.getFirstName() == null || userDTO.getFirstName().isBlank()) {
      errors.reject("First name must be defined");
    }

    if (userDTO.getLastName() == null || userDTO.getLastName().isBlank()) {
      errors.reject("Last name must be defined");
    }

    if (userDTO.getDateOfBirth() == null) {
      errors.reject("Last name must be defined");
    }

    if (userDTO.getDateOfBirth() != null) {
      try {
        if (java.sql.Date.valueOf(userDTO.getDateOfBirth()).after(new java.sql.Date(System.currentTimeMillis()))) {
          errors.reject("Date of birth cannot be in the future");
        }
      } catch (IllegalArgumentException illegalArgumentException) {
        errors.reject("Invalid date format");
      }
    }

    if (userDTO.getEmail().isBlank()) {
      errors.reject("Invalid email");
    }
    if (!userDTO.getEmail().matches(EMAIL_REGEX)) {
      errors.reject("Invalid email");
    }

    if (userDTO.getUsername().isBlank()) {
      errors.reject("Username cannot be empty");
    }
    if (userDTO.getUsername().trim().length() < minUsernameLength) {
      errors.reject("Username not long enough");
    }

    if (userDTO.getPassword().isBlank()) {
      errors.reject("Password cannot be empty");
    }
    if (userDTO.getPassword().trim().length() < minPasswordLength) {
      errors.reject("Password not long enough");
    }

    if (userDTO.getUpdatedPassword() != null) {
      if (userDTO.getUpdatedPassword().isBlank()) {
        errors.reject("Password cannot be empty");
      }
      if (userDTO.getUpdatedPassword().trim().length() < minPasswordLength) {
        errors.reject("Password not long enough");
      }
    }
  }
}
