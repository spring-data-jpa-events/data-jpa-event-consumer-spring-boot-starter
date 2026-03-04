package com.github.spring.data.jpa.event.consumer.autoconfigure;

import static java.util.stream.Collectors.toMap;

import com.github.spring.data.jpa.event.consumer.EntityEventToJpaHandler;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;

@Slf4j
public class KafkaEntityEventConsumer {

  @SuppressWarnings("rawtypes")
  private final Map<String, EntityEventToJpaHandler> handlersByTopic;

  @SuppressWarnings("rawtypes")
  public KafkaEntityEventConsumer(Set<EntityEventToJpaHandler<?, ?>> handlers) {
    this.handlersByTopic =
        handlers.stream().collect(toMap(EntityEventToJpaHandler::getTopic, h -> h));
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @KafkaListener(id = "entityEventConsumer", topics = "#{@entityEventTopics}")
  public void onMessage(ConsumerRecord<String, String> record) {
    log.info("Received event on topic [{}]", record.topic());
    EntityEventToJpaHandler handler = handlersByTopic.get(record.topic());
    if (handler != null) {
      handler.handle(record.value());
    } else {
      log.warn("No handler found for topic [{}]", record.topic());
    }
  }
}
