package com.eos.streamus.dto.validators;

import com.eos.streamus.dto.UserDTO;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
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
  public void validate(@NonNull final Object o, @NonNull final Errors errorsObject) {
    BindingResult errors = (BindingResult) errorsObject;
    UserDTO userDTO = (UserDTO) o;
    if (!EmailValidator.getInstance().isValid(userDTO.getEmail())) {
      System.out.println(userDTO.getEmail() + " doesnt match regex");
      errors.addError(new ObjectError("email", "Invalid email address"));
    }
    if (userDTO.getUsername().isBlank()) {
      errors.addError(new ObjectError("username", "Username cannot be empty"));
    } else if (userDTO.getUsername().trim().length() < minUsernameLength) {
      errors.addError(new ObjectError("username", "Username not long enough"));
    }

    checkDateOfBirth(userDTO, errors);
    checkPassword(userDTO, errors);

    if (userDTO.getFirstName() == null || userDTO.getFirstName().isBlank()) {
      errors.addError(new ObjectError("firstName", "First name must be defined"));
    }

    if (userDTO.getLastName() == null || userDTO.getLastName().isBlank()) {
      errors.addError(new ObjectError("lastName", "Last name must be defined"));
    }
  }

  private void checkDateOfBirth(final UserDTO userDTO, final BindingResult errors) {
    if (userDTO.getDateOfBirth() != null) {
      try {
        if (java.sql.Date.valueOf(userDTO.getDateOfBirth()).after(new java.sql.Date(System.currentTimeMillis()))) {
          errors.addError(new ObjectError("dateOfBirth", "Date of birth cannot be in the future"));
        }
      } catch (IllegalArgumentException illegalArgumentException) {
        errors.addError(new ObjectError("dateOfBirth", "Invalid date format"));
      }
    } else {
      errors.addError(new ObjectError("dateOfBirth", "Please specify a date of birth"));
    }
  }

  private void checkPassword(final UserDTO userDTO, final BindingResult errors) {
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
