package com.eos.streamus.dto.validators;

import com.eos.streamus.dto.PasswordUpdateDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

public class PasswordUpdateDTOValidator implements Validator {
  /** Password. */
  private static final String PASSWORD = "password";
  /** Minimum password length. */
  @Value("${minPasswordLength}")
  private int minPasswordLength;

  /** {@inheritDoc} */
  @Override
  public boolean supports(final Class<?> aClass) {
    return aClass.equals(PasswordUpdateDTO.class);
  }

  /** {@inheritDoc} */
  @Override
  public void validate(@NonNull final Object o, @NonNull final Errors errorsObject) {
    final BindingResult errors = (BindingResult) errorsObject;
    final PasswordUpdateDTO passwordUpdateDTO = (PasswordUpdateDTO) o;
    if (passwordUpdateDTO.getPassword().isBlank()) {
      errors.addError(new ObjectError(PASSWORD, "Password cannot be empty"));
    } else if (passwordUpdateDTO.getPassword().trim().length() < minPasswordLength) {
      errors.addError(new ObjectError(PASSWORD, "Password not long enough"));
    }

    if (passwordUpdateDTO.getUpdatedPassword() != null) {
      if (passwordUpdateDTO.getUpdatedPassword().isBlank()) {
        errors.addError(new ObjectError("updatedPassword", "New password cannot be empty"));
      } else if (passwordUpdateDTO.getUpdatedPassword().trim().length() < minPasswordLength) {
        errors.addError(new ObjectError("updatedPassword", "New password not long enough"));
      }
    }

  }
}
