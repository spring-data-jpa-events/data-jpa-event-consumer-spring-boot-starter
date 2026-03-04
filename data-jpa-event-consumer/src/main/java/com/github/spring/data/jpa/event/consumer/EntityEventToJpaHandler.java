package com.github.spring.data.jpa.event.consumer;

import com.github.spring.data.jpa.event.consumer.mapper.EntityEventMapper;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;

@Slf4j
public class EntityEventToJpaHandler<T, ID> {

  private final Class<T> entityClass;
  private final String topic;
  private final JpaRepository<T, ID> repository;
  private final EntityEventMapper<T> entityEventMapper;
  private final Function<T, ID> idExtractor;

  public EntityEventToJpaHandler(
      Class<T> entityClass,
      String topic,
      JpaRepository<T, ID> repository,
      EntityEventMapper<T> entityEventMapper,
      Function<T, ID> idExtractor) {
    this.entityClass = entityClass;
    this.topic = topic;
    this.repository = repository;
    this.entityEventMapper = entityEventMapper;
    this.idExtractor = idExtractor;
  }

  public String getTopic() {
    return topic;
  }

  public Class<T> getEntityClass() {
    return entityClass;
  }

  public void handle(String message) {
    log.info("Handling event for entity [{}]", entityClass.getSimpleName());
    try {
      var event = entityEventMapper.map(message, entityClass);
      switch (event.getAction()) {
        case CREATED, UPDATED -> {
          log.info("Saving entity [{}]", entityClass.getSimpleName());
          repository.save(event.getEntity());
        }
        case DELETED -> {
          ID id = idExtractor.apply(event.getEntity());
          log.info("Deleting entity [{}] with id [{}]", entityClass.getSimpleName(), id);
          repository.deleteById(id);
        }
      }
    } catch (EntityEventConsumerException e) {
      throw e;
    } catch (Exception e) {
      throw new EntityEventConsumerException("Unexpected error handling entity event", e);
    }
  }
}
