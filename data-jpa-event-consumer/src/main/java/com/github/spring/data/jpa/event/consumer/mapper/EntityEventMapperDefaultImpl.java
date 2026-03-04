package com.github.spring.data.jpa.event.consumer.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.spring.data.jpa.event.consumer.EntityEvent;
import com.github.spring.data.jpa.event.consumer.EntityEventConsumerException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EntityEventMapperDefaultImpl<E> implements EntityEventMapper<E> {

  private final ObjectMapper objectMapper;

  @Override
  public EntityEvent<E> map(String json, Class<E> entityClass) {
    try {
      var type =
          objectMapper.getTypeFactory().constructParametricType(EntityEvent.class, entityClass);
      return objectMapper.readValue(json, type);
    } catch (Exception e) {
      throw new EntityEventConsumerException("Error deserializing entity event", e);
    }
  }
}
