package com.github.spring.data.jpa.event.consumer.autoconfigure;

import static java.util.stream.Collectors.toSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.spring.data.jpa.event.consumer.EntityEventToJpaHandler;
import com.github.spring.data.jpa.event.consumer.mapper.EntityEventMapperDefaultImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.repository.support.Repositories;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@ConditionalOnClass({KafkaTemplate.class, ObjectMapper.class})
public class DataJpaEventConsumerAutoconfiguration {

  @ConditionalOnMissingBean
  @Bean
  public KafkaEntityEventConsumer kafkaEntityEventConsumer(
      Set<EntityEventToJpaHandler<?, ?>> entityHandlers) {
    return new KafkaEntityEventConsumer(entityHandlers);
  }

  @ConditionalOnMissingBean(name = "entityEventTopics")
  @Bean
  @SuppressWarnings("rawtypes")
  public String[] entityEventTopics(Set<EntityEventToJpaHandler<?, ?>> entityHandlers) {
    return entityHandlers.stream().map(EntityEventToJpaHandler::getTopic).toArray(String[]::new);
  }

  @ConditionalOnMissingBean
  @Bean
  @SuppressWarnings({"rawtypes", "unchecked"})
  public Set<EntityEventToJpaHandler<?, ?>> entityHandlers(
      EntityManager entityManager,
      ApplicationContext applicationContext,
      ObjectMapper objectMapper) {

    var repositories = new Repositories(applicationContext);

    return (Set<EntityEventToJpaHandler<?, ?>>)
        (Set<?>)
            entityManager.getMetamodel().getEntities().stream()
                .map(EntityType::getJavaType)
                .filter(entityClass -> entityClass.isAnnotationPresent(EventDriven.class))
                .map(
                    entityClass ->
                        createHandler(entityClass, repositories, entityManager, objectMapper))
                .collect(toSet());
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static <T, ID> EntityEventToJpaHandler<T, ID> createHandler(
      Class<T> entityClass,
      Repositories repositories,
      EntityManager entityManager,
      ObjectMapper objectMapper) {
    String topic = entityClass.getAnnotation(EventDriven.class).consumeTopic();
    JpaRepository<T, ID> repository =
        (JpaRepository<T, ID>)
            repositories
                .getRepositoryFor(entityClass)
                .orElseThrow(
                    () ->
                        new IllegalStateException(
                            "No JpaRepository found for @EventDriven entity: "
                                + entityClass.getName()));
    var mapper = new EntityEventMapperDefaultImpl<T>(objectMapper);
    JpaEntityInformation<T, ID> entityInfo =
        (JpaEntityInformation<T, ID>)
            JpaEntityInformationSupport.getEntityInformation(entityClass, entityManager);
    return new EntityEventToJpaHandler<>(entityClass, topic, repository, mapper, entityInfo::getId);
  }
}
