package com.eos.streamus.dto.validators;

import com.eos.streamus.dto.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserDTOValidator implements Validator {
  /** Minimum password length. */
  @Value("${minPasswordLength}")
  private int minPasswordLength;
  /** Minimum username length. */
  @Value("${minUsernameLength}")
  private int minUsernameLength;

  /** {@inheritDoc} */
  @Override
  public boolean supports(final Class<?> aClass) {
    return aClass.equals(UserDTO.class);
  }

  /** {@inheritDoc} */
  @Override
  public void validate(@NonNull final Object o, @NonNull final Errors errors) {
    UserDTO userDTO = (UserDTO) o;

    if (userDTO.getFirstName() == null || userDTO.getFirstName().isBlank()) {
      errors.reject("First name must be defined");
    }

    if (userDTO.getLastName() == null || userDTO.getLastName().isBlank()) {
      errors.reject("Last name must be defined");
    }

    if (userDTO.getUsername().isBlank()) {
      errors.reject("Username cannot be empty");
    }
    if (userDTO.getUsername().trim().length() < minUsernameLength) {
      errors.reject("Username not long enough");
    }

    checkDateOfBirth(userDTO, errors);
    checkPassword(userDTO, errors);
  }

  private void checkDateOfBirth(final UserDTO userDTO, final Errors errors) {
    if (userDTO.getDateOfBirth() != null) {
      try {
        if (java.sql.Date.valueOf(userDTO.getDateOfBirth()).after(new java.sql.Date(System.currentTimeMillis()))) {
          errors.reject("Date of birth cannot be in the future");
        }
      } catch (IllegalArgumentException illegalArgumentException) {
        errors.reject("Invalid date format");
      }
    } else {
      errors.reject("Birth dates must be defined");
    }
  }

  private void checkPassword(final UserDTO userDTO, final Errors errors) {
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
