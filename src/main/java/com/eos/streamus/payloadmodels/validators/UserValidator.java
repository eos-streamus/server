package com.eos.streamus.payloadmodels.validators;

import com.eos.streamus.payloadmodels.UserData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import static com.eos.streamus.utils.Constants.EMAIL_REGEX;

@Component
public class UserValidator extends PersonValidator {
  @Value("${minPasswordLength}")
  private int minPasswordLength;
  @Value("${minUsernameLength}")
  private int minUsernameLength;

  @Override
  public boolean supports(final Class<?> aClass) {
    return aClass.equals(UserData.class);
  }

  @Override
  public void validate(final Object o, final Errors errors) {
    super.validate(o, errors);
    UserData userData = (UserData) o;

    if (userData.getEmail().isBlank()) {
      errors.reject("Invalid email");
    }
    if (!userData.getEmail().matches(EMAIL_REGEX)) {
      errors.reject("Invalid email");
    }

    if (userData.getUsername().isBlank()) {
      errors.reject("Username cannot be empty");
    }
    if (userData.getUsername().trim().length() < minUsernameLength) {
      errors.reject("Username not long enough");
    }

    if (userData.getPassword().isBlank()) {
      errors.reject("Password cannot be empty");
    }
    if (userData.getPassword().trim().length() < minPasswordLength) {
      errors.reject("Password not long enough");
    }

    if (userData.getUpdatedPassword() != null) {
      if (userData.getUpdatedPassword().isBlank()) {
        errors.reject("Password cannot be empty");
      }
      if (userData.getUpdatedPassword().trim().length() < minPasswordLength) {
        errors.reject("Password not long enough");
      }
    }
  }
}
