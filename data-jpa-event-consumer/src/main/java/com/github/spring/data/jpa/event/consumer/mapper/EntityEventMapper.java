package com.github.spring.data.jpa.event.consumer.mapper;

import com.github.spring.data.jpa.event.consumer.EntityEvent;

public interface EntityEventMapper<E> {

  EntityEvent<E> map(String json, Class<E> entityClass);
}
