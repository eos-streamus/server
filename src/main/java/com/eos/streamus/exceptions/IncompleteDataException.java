package com.eos.streamus.exceptions;

public class IncompleteDataException extends RuntimeException {
  private static final long serialVersionUID = -1814621622047362369L;

  public IncompleteDataException(String message) {
    super(message);
  }
}
