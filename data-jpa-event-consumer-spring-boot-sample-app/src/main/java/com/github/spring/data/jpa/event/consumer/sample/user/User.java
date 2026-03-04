package com.github.spring.data.jpa.event.consumer.sample.user;

import com.github.spring.data.jpa.event.consumer.autoconfigure.EventDriven;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Data;

@Entity
@EventDriven(topic = "user")
@Table(name = "\"user\"")
@Data
public class User {

  @Id private UUID id;

  private String email;
}
