package com.eos.streamus.dto.validators;

import com.eos.streamus.dto.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import static com.eos.streamus.utils.Constants.EMAIL_REGEX;

@Component
public class UserDTOValidator extends PersonDTOValidator {
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
    super.validate(o, errors);
    UserDTO userDTO = (UserDTO) o;

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
