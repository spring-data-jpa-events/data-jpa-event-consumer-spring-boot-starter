package com.github.spring.data.jpa.event.consumer;

public class EntityEventConsumerException extends RuntimeException {

  public EntityEventConsumerException() {}

  public EntityEventConsumerException(String message) {
    super(message);
  }

  public EntityEventConsumerException(String message, Throwable cause) {
    super(message, cause);
  }

  public EntityEventConsumerException(Throwable cause) {
    super(cause);
  }

  protected EntityEventConsumerException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
