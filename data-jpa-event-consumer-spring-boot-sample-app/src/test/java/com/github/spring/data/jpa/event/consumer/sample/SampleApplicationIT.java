package com.github.spring.data.jpa.event.consumer.sample;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.github.spring.data.jpa.event.consumer.EntityEvent;
import com.github.spring.data.jpa.event.consumer.EntityEvent.Action;
import com.github.spring.data.jpa.event.consumer.sample.user.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(topics = {"user"})
public class SampleApplicationIT {

  private static final String USER_TOPIC = "user";

  @Autowired private UserRepository userRepository;
  @Autowired private EventProducer eventProducer;

  private RemoteUserEntity user;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    user = new RemoteUserEntity();
    user.setId(UUID.randomUUID());
    user.setName("Jacob");
    user.setEmail("jacob@example.com");
    user.setOrganizationId(UUID.randomUUID());
  }

  @Test
  void consumeEntityCreatedEvent_thenEntityIsSaved() {
    eventProducer.sendEvent(USER_TOPIC, new EntityEvent<>(Action.CREATED, user, Instant.now()));

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              var saved = userRepository.findById(user.getId());
              assertThat(saved).isPresent();
              assertThat(saved.get().getEmail()).isEqualTo(user.getEmail());
            });
  }

  @Test
  void consumeEntityUpdatedEvent_thenEntityIsUpdated() {
    eventProducer.sendEvent(USER_TOPIC, new EntityEvent<>(Action.CREATED, user, Instant.now()));
    await()
        .atMost(Duration.ofSeconds(5))
        .until(() -> userRepository.findById(user.getId()).isPresent());

    user.setEmail("updated@example.com");
    eventProducer.sendEvent(USER_TOPIC, new EntityEvent<>(Action.UPDATED, user, Instant.now()));

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () ->
                assertThat(userRepository.findById(user.getId()).get().getEmail())
                    .isEqualTo("updated@example.com"));
  }

  @Test
  void consumeEntityDeletedEvent_thenEntityIsDeleted() {
    eventProducer.sendEvent(USER_TOPIC, new EntityEvent<>(Action.CREATED, user, Instant.now()));
    await()
        .atMost(Duration.ofSeconds(5))
        .until(() -> userRepository.findById(user.getId()).isPresent());

    eventProducer.sendEvent(USER_TOPIC, new EntityEvent<>(Action.DELETED, user, Instant.now()));

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() -> assertThat(userRepository.findById(user.getId())).isEmpty());
  }
}
