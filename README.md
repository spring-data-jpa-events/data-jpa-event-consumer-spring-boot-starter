# data-jpa-event-consumer-spring-boot-starter

This is a Spring Boot starter for automatically configuring an event consumer that keeps local JPA entities in sync with remote entity changes received via Kafka. The goal is to let developers quickly consume CRUD events and persist them locally without writing boilerplate Kafka listener or repository code.

This is an opinionated implementation not meant to solve all event-driven usecases.

## Dependency
__Warning:__ _Not available in maven central yet but can be fetched from github repo._

### maven
``` xml
<dependency>
  <groupId>com.github.spring-data-jpa-events</groupId>
  <artifactId>data-jpa-event-consumer-spring-boot-starter</artifactId>
  <version>0.0.1</version>
</dependency>
```
### gradle
``` yaml
implementation 'com.github.spring-data-jpa-events:data-jpa-event-consumer-spring-boot-starter:0.0.1'
```

## Example

See the sample-app module for a fully working example.

Once you added the starter as a dependency to your project, annotate a local JPA entity with `@EventDriven` and provide a `JpaRepository` for it. The lib will automatically consume CREATED/UPDATED/DELETED events from the configured Kafka topic and apply the corresponding save or delete operation.

``` java
@Entity
@EventDriven(consumeTopic = "user")
@Table(name = "user")
@Data
public class User {
  @Id
  private UUID id;
  private String email;
}
```

``` java
public interface UserRepository extends JpaRepository<User, UUID> {}
```

Here is an example of a consumed event from the `user` topic based on the previous example:
``` json
{
  "action" : "CREATED",
  "timestamp" : "2024-01-01T00:00:00Z",
  "entity" : {
    "id" : "e0d2c165-4c5c-45bb-ba9b-d12af9a69bb4",
    "email" : "jacob@example.com"
  }
}
```
