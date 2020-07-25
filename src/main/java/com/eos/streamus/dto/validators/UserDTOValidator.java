package com.eos.streamus.dto.validators;

import com.eos.streamus.dto.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

@Component
public final class UserDTOValidator implements Validator {
  /** Minimum password length to accept. */
  @Value("${minPasswordLength}")
  private int minPasswordLength;
  /** Minimum username length to accept. */
  @Value("${minUsernameLength}")
  private int minUsernameLength;

  @Override
  public boolean supports(final Class<?> aClass) {
    return aClass.equals(UserDTO.class);
  }

  private void checkNullAndBlank(final UserDTO userDTO, final Errors errorsObject) {
    BindingResult errors = (BindingResult) errorsObject;

    if (userDTO.getFirstName() == null || userDTO.getFirstName().isBlank()) {
      errors.addError(new ObjectError("firstName", "First name must be defined"));
    }

    if (userDTO.getLastName() == null || userDTO.getLastName().isBlank()) {
      errors.addError(new ObjectError("lastName", "Last name must be defined"));
    }

    if (userDTO.getDateOfBirth() == null) {
      errors.addError(new ObjectError("lastName", "Last name must be defined"));
    }
  }

  @Override
  public void validate(final Object o, final Errors errorsObject) {
    BindingResult errors = (BindingResult) errorsObject;
    UserDTO userDTO = (UserDTO) o;

    checkNullAndBlank(userDTO, errors);

    if (userDTO.getDateOfBirth() != null) {
      try {
        if (java.sql.Date.valueOf(userDTO.getDateOfBirth()).after(new java.sql.Date(System.currentTimeMillis()))) {
          errors.addError(new ObjectError("dateOfBirth", "Date of birth cannot be in the future"));
        }
      } catch (IllegalArgumentException illegalArgumentException) {
        errors.addError(new ObjectError("dateOfBirth", "Invalid date format"));
      }
    }

    if (userDTO.getUsername().isBlank()) {
      errors.addError(new ObjectError("username", "Username cannot be empty"));
    } else if (userDTO.getUsername().trim().length() < minUsernameLength) {
      errors.addError(new ObjectError("username", "Username not long enough"));
    }

    if (userDTO.getPassword().isBlank()) {
      errors.addError(new ObjectError("password", "Password cannot be empty"));
    } else if (userDTO.getPassword().trim().length() < minPasswordLength) {
      errors.addError(new ObjectError("password", "Password not long enough"));
    }

    if (userDTO.getUpdatedPassword() != null) {
      if (userDTO.getUpdatedPassword().isBlank()) {
        errors.addError(new ObjectError("password", "Password cannot be empty"));
      } else if (userDTO.getUpdatedPassword().trim().length() < minPasswordLength) {
        errors.addError(new ObjectError("password", "Password not long enough"));
      }
    }
  }

}
