package com.github.spring.data.jpa.event.consumer.sample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.spring.data.jpa.event.consumer.EntityEvent;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EventProducer {
  private KafkaTemplate<String, String> kafkaTemplate;
  private ObjectMapper objectMapper;

  public void sendEvent(String topicName, EntityEvent<?> event) {
    try {
      String stringEvent = objectMapper.writeValueAsString(event);
      kafkaTemplate.send(topicName, stringEvent);
    } catch (JsonProcessingException e) {
      // TODO Auto-generated catch block
    }
  }
}
