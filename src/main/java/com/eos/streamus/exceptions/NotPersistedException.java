package com.eos.streamus.exceptions;

public class NotPersistedException extends RuntimeException {
  private static final long serialVersionUID = 2578342895174022386L;

  public NotPersistedException(final String message) {
    super(message);
  }
}
