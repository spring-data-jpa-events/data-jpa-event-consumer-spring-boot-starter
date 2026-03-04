package com.github.spring.data.jpa.event.consumer.sample;

import java.util.UUID;
import lombok.Data;

/**
 * Represent the original entity that is producing the events from which the local "event-driven"
 * entity are synced from.
 */
@Data
public class RemoteUserEntity {
  private UUID id;
  private String name;
  private String email;
  private UUID organizationId;
}
