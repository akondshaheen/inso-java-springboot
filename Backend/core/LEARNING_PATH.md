# TaskFlow Learning Path — Read This First

This file is your **study order**. Every file listed below already contains
a full teaching-script comment at the top (and inside the important
methods) written in plain beginner English. Those comments are your main
textbook — this file only tells you **in which order to open them** so the
concepts build on each other instead of feeling random.

Follow the phases **in order, top to bottom**. Inside a phase, open the
files in the exact order listed. Do not jump ahead — every phase is built
so that it reuses and repeats concepts from the phase before it, the same
way a real trainer would teach you.

How to use each file: open it, read the class-level comment first (it
explains the big picture), then read every method with a comment above it.
If a comment references a Java or Spring concept you have not seen yet,
that is fine — it will be explained fully the first time it truly matters,
and repeated in later files so it sticks.

---

## Phase 0 — Get the app running once

Before reading any code, actually run the project once so you can see it
work. This makes every class you read next feel real instead of abstract.

1. `README.md` — read only the **"How to run"** section.
2. Run `./mvnw spring-boot:run` and confirm the app starts on port 8080.

You do not need to understand the code yet. You only need to see it boot.

---

## Phase 1 — The entry point and how Spring Boot starts

**Goal:** understand what actually happens the moment you run the app,
before looking at any business code.

1. `pom.xml` — read the dependency list and comments. This tells you
   exactly what libraries this app is built from (Spring Web, Spring Data
   JPA, Spring Security, Validation, Cache, Actuator) and why each is here.
2. `src/main/resources/application.yml` — configuration: database
   connection, JPA settings, JWT secret, Hikari pool, Actuator exposure.
3. `TaskFlowApplication.java` — the `@SpringBootApplication` class and its
   Javadoc explaining component scanning, auto-configuration, and the
   application context, step by step.

**Java/Spring concepts covered here:** JDK vs JRE vs JVM, Maven, Spring vs
Spring Boot, `@SpringBootApplication`, component scanning, the application
context, configuration files, profiles, environment variables.

---

## Phase 2 — The domain model (plain Java, no framework)

**Goal:** learn core Java (classes, encapsulation, enums, records,
equals/hashCode, Comparable) using objects that have nothing to do with
Spring yet. This is deliberately framework-free so you can focus purely on
Java fundamentals.

Read in this order:

1. `domain/Role.java` — a simple `enum`, and why enums beat plain Strings
   for a fixed set of values.
2. `domain/Priority.java` — another enum, this time also implementing
   ordering logic used later by `Comparable`/`Comparator`.
3. `domain/TaskStatus.java` — enum modelling a state machine (a task's
   lifecycle), including which transitions are valid.
4. `domain/User.java` — classes, constructors (including overloading),
   encapsulation, `equals()`/`hashCode()`/`toString()`, and why identity is
   based on `id`.
5. `domain/Tag.java` — a small immutable-style value class, good practice
   for `equals`/`hashCode` on a "value" concept.
6. `domain/Project.java` — a class that owns a `List` of related objects
   and exposes a small business rule (`isOwnedBy`) directly on the domain
   object instead of scattering that check elsewhere.
7. `domain/Task.java` — the richest domain class: implements
   `Comparable<Task>`, works with `Set`/`List`, and demonstrates
   `Optional` usage for a nullable-but-explicit field.

**Java concepts covered here:** classes/objects, constructors, method
overloading, encapsulation, access modifiers, `final`, `enum`, records,
`equals()`/`hashCode()`/`toString()`, `Comparable`, generics, `Optional`,
collections (`List`, `Set`, `Map`).

---

## Phase 3 — Persistence: entities and JPA/Hibernate

**Goal:** see how a plain domain object gets mapped onto a database table,
and learn JPA/Hibernate mechanics.

1. `entity/UserEntity.java` — `@Entity`, `@Id`, generated ids, column
   mapping.
2. `entity/ProjectEntity.java` — `@OneToMany`, fetch types, indexes
   (`@Table(indexes = ...)`) and why they matter for performance.
3. `entity/TaskEntity.java` — `@ManyToOne`, `@ManyToMany`, cascade basics,
   lazy loading, and more indexes.
4. `entity/TagEntity.java` — the other side of the `@ManyToMany`
   relationship.
5. `mapper/UserMapper.java`, `mapper/ProjectMapper.java`,
   `mapper/TaskMapper.java`, `mapper/TagMapper.java` — why we keep a
   separate domain model and entity model, and how we convert between
   them. Read these four together; they all follow the same pattern.

**Java/Spring concepts covered here:** JPA vs Hibernate, `@Entity`, `@Id`,
generated ids, `@OneToMany`/`@ManyToOne`/`@ManyToMany`, fetch types
(`LAZY`/`EAGER`), cascade, database indexes and Big-O basics, the
domain/entity separation pattern (and why it is not over-engineering).

---

## Phase 4 — Repository layer: from interface to database

**Goal:** understand exactly what happens when a repository method is
called, all the way down to real SQL.

1. `repository/UserRepository.java` — the plain interface the service
   layer depends on (a "port").
2. `repository/jpa/UserJpaRepository.java` — the Spring Data JPA interface,
   with a full Javadoc walkthrough of what Spring Data generates behind
   the scenes for a derived query method.
3. `repository/impl/UserRepositoryImpl.java` — the `@Repository` adapter
   connecting the two above.
4. Repeat the same three-file pattern for Projects:
   `repository/ProjectRepository.java` →
   `repository/jpa/ProjectJpaRepository.java` →
   `repository/impl/ProjectRepositoryImpl.java`.
5. Then for Tags:
   `repository/TagRepository.java` → `repository/jpa/TagJpaRepository.java`
   → `repository/impl/TagRepositoryImpl.java`.
6. Finally Tasks — read this one last and most carefully, because it
   contains the **N+1 query problem** explanation and fix:
   `repository/TaskRepository.java` →
   `repository/jpa/TaskJpaRepository.java` (the big teaching Javadoc is
   here) → `repository/impl/TaskRepositoryImpl.java`.

**Java/Spring concepts covered here:** interfaces vs implementations,
dependency inversion, Spring Data JPA query derivation, JPQL, pagination,
sorting, the N+1 query problem and `@EntityGraph`, connection pooling
(tie this back to `application.yml`'s Hikari settings from Phase 1).

---

## Phase 5 — DTOs: what the API actually sends and receives

**Goal:** understand why we never expose domain or entity objects directly
over HTTP.

1. `dto/request/UserRegistrationRequest.java` — a request DTO with Bean
   Validation annotations (`@NotBlank`, `@Email`, `@Size`).
2. `dto/response/UserResponse.java` — a response DTO, and notice which
   field is deliberately left out compared to the entity.
3. `dto/request/ProjectCreateRequest.java`, `dto/request/ProjectUpdateRequest.java`,
   `dto/response/ProjectResponse.java` — same pattern for Projects.
4. `dto/request/TaskCreateRequest.java`, `dto/request/TaskUpdateRequest.java`,
   `dto/request/TaskStatusUpdateRequest.java`, `dto/response/TaskResponse.java`
   — same pattern for Tasks, including the PUT-vs-PATCH split you will see
   again in Phase 7.
5. `dto/request/LoginRequest.java`, `dto/response/LoginResponse.java` —
   small DTOs used by the security stage later.
6. `dto/response/ApiErrorResponse.java` — the shape every error response
   takes; keep this in mind for Phase 8.

**Java/Spring concepts covered here:** records, DTOs, request vs response
DTOs, Bean Validation annotations, serialization/deserialization (Jackson),
the reason DTOs exist instead of returning entities.

---

## Phase 6 — Service layer: business rules and transactions

**Goal:** see where business logic actually belongs, and how
`@Transactional` protects data consistency.

1. `service/UserService.java` — registration logic, password hashing call,
   duplicate-email checking, constructor injection.
2. `service/TagService.java` — the smallest service; also where caching
   (`@Cacheable`/`@CacheEvict`) is introduced with a full explanation.
3. `service/ProjectService.java` — ownership checks, and where
   authorization-style business rules live versus pure HTTP concerns.
4. `service/TaskService.java` — the richest service: Stream API usage
   (`filter`, `sorted`, `collect`), `Comparator`, and the main
   `@Transactional` examples.

**Java/Spring concepts covered here:** `@Service`, constructor injection
and why it beats field injection, Stream API in real use, `@Transactional`
and transaction propagation, caching, Big-O reasoning about collections
(`Set` vs `List`).

---

## Phase 7 — Controllers: where HTTP meets your Java code

**Goal:** understand the full HTTP request/response cycle and REST
conventions.

1. `controller/UserController.java` — GET/POST, `ResponseEntity`, status
   codes 200/201.
2. `controller/ProjectController.java` — path variables, query parameters,
   ownership-based access.
3. `controller/TaskController.java` — the most complete controller: GET
   with pagination/sorting/filtering, POST, **PUT vs PATCH** side by side,
   DELETE.
4. `controller/AuthController.java` — the login endpoint; read this right
   before Phase 9 (Security), since it is the bridge into that topic.

**Java/Spring concepts covered here:** `@RestController` vs `@Controller`,
`@RequestMapping`/`@GetMapping`/etc., path variables, query/request
parameters, request/response bodies, `ResponseEntity`, HTTP status code
meanings (200 vs 201, 400 vs 404, 409), REST principles, statelessness,
idempotency.

---

## Phase 8 — Validation and centralized exception handling

**Goal:** see the full journey of a bad request, and why we handle errors
in one place instead of scattering try/catch everywhere.

1. Look back briefly at any `@NotBlank`/`@Email`/`@Size` annotation in
   Phase 5's DTOs, and at any `@Valid` parameter in Phase 7's controllers —
   this is where those two phases connect.
2. `exception/ResourceNotFoundException.java` — a simple custom unchecked
   exception, and why it is unchecked on purpose.
3. `exception/DuplicateResourceException.java` — another custom exception,
   used for the 409 Conflict case.
4. `exception/AuthorizationException.java` — a custom exception mapped to
   403 Forbidden.
5. `exception/GlobalExceptionHandler.java` — read this one carefully; it
   is the single place every exception above (plus validation failures and
   malformed JSON) turns into a consistent `ApiErrorResponse` with the
   right HTTP status code, with logging for genuinely unexpected errors.

**Java/Spring concepts covered here:** checked vs unchecked exceptions,
try/catch/finally, custom exceptions, `@RestControllerAdvice`,
`@ExceptionHandler`, consistent API error responses, logging.

---

## Phase 9 — Security: authentication and authorization

**Goal:** understand the full JWT login-and-authorize flow, end to end.

1. `security/JwtService.java` — how a token is created and verified.
2. `security/JwtAuthenticationFilter.java` — how every incoming request is
   intercepted and checked for a valid token.
3. `security/SecurityUtils.java` — small helper for reading the current
   authenticated user.
4. `security/RestAuthenticationEntryPoint.java` — what happens on a 401.
5. `security/RestAccessDeniedHandler.java` — what happens on a 403.
6. `security/SecurityConfig.java` — read this **last** in this phase; it
   wires everything above together and declares which endpoints need which
   role. Reading it last will make every rule inside it make sense, because
   you will already know what each referenced class does.

**Java/Spring concepts covered here:** authentication vs authorization,
password hashing (BCrypt), JWT concept, Spring Security filter chain,
roles/permissions, secured endpoints, CORS, CSRF concept, statelessness
(tie back to Phase 7).

---

## Phase 10 — Concurrency

**Goal:** see real thread-safety concerns in a backend, not toy examples.

1. `concurrency/RequestMetrics.java` — race conditions, `ConcurrentHashMap`,
   `AtomicLong`, `volatile`, explained with a step-by-step walkthrough of
   what goes wrong without them.
2. `concurrency/RequestMetricsFilter.java` — how this counter gets fed by
   every incoming request.
3. `service/NotificationService.java` — `ExecutorService`, `Callable`,
   `Future`, and bean lifecycle (`@PreDestroy`) shutting the pool down
   cleanly.
4. `controller/NotificationController.java` and
   `controller/MetricsController.java` — how the two concurrency examples
   are exposed as (admin-only) endpoints.

**Java concepts covered here:** Thread vs Process, `Runnable` vs
`Callable`, `ExecutorService`, `Future`, `synchronized`, race conditions,
thread safety, `ConcurrentHashMap`, `volatile`, deadlock concept.

---

## Phase 11 — Performance

**Goal:** connect everything you already know (JPA, caching, Big-O) to
real performance decisions.

1. Revisit `repository/jpa/TaskJpaRepository.java` from Phase 4 — reread
   the N+1 problem Javadoc now that you also know about `@Transactional`
   and lazy loading from Phases 3 and 6.
2. Revisit `entity/TaskEntity.java` and `entity/ProjectEntity.java` from
   Phase 3 — reread the `@Table(indexes = ...)` comments now with full
   context.
3. Revisit `service/TagService.java` from Phase 6 — reread the
   `@Cacheable`/`@CacheEvict` comments.
4. `config/CacheConfig.java` — why `@EnableCaching` lives in its own
   class instead of on `TaskFlowApplication`, and the test-slice gotcha
   this avoids.
5. `src/main/resources/application.yml` — reread the Hikari connection
   pool and Actuator sections from Phase 1 with fresh eyes.

**Concepts covered here:** database indexes, N+1 queries, lazy loading,
caching, connection pooling, Big-O basics, avoiding unnecessary database
calls, Actuator.

---

## Phase 12 — Testing

**Goal:** learn what to unit test vs integration test, and how Spring
Boot's test slices work, by reading real tests against the real code you
already understand.

1. `src/test/java/.../service/UserServiceTest.java` — a pure unit test
   with Mockito mocks; notice it needs no Spring context at all.
2. `src/test/java/.../service/NotificationServiceTest.java` — another
   Mockito-based unit test, this time around the concurrency code from
   Phase 10.
3. `src/test/java/.../repository/jpa/TaskJpaRepositoryTest.java` — an
   integration test using `@DataJpaTest` against a real (in-memory)
   database, proving the `@EntityGraph` fix from Phase 4/11 actually
   works.
4. `src/test/java/.../controller/UserControllerTest.java` — a
   `@WebMvcTest` + `MockMvc` test, and the "Filter bean" gotcha explained
   in its comments (why `RequestMetrics` needs to be mocked here).

**Concepts covered here:** JUnit, Mockito, unit vs integration testing,
mock vs real dependency, `@DataJpaTest`, `@WebMvcTest`, `MockMvc`, why
excessive mocking is a smell.

---

## Phase 13 — Docker and deployment

**Goal:** see how this whole application gets packaged and run anywhere.

1. `Dockerfile` — multi-stage build explained line by line.
2. `docker-compose.yml` — how the app container and database container
   find each other, and how environment variables override
   `application.yml` from Phase 1.
3. `.dockerignore` — why it exists.

**Concepts covered here:** containers, images, multi-stage builds,
Docker Compose networking, environment-variable configuration overrides.

---

## Phase 14 — Consolidation

**Goal:** lock everything in and prepare for the actual interview.

1. `README.md` — read the whole **Interview Question Bank** section
   top to bottom now that you have read every file it references. Try to
   answer each question OUT LOUD from memory before reading the provided
   answer, then check yourself against it and its follow-up question.
2. Without opening any file, try to draw the architecture diagram from
   `README.md`'s **Architecture** section from memory, and describe what
   each layer does, and why business logic does not belong in the
   controller.
3. Pick any one endpoint (for example `POST /api/tasks`) and narrate, out
   loud, the entire journey from HTTP request to database row and back —
   this exercises everything from Phases 1, 3, 4, 5, 6, 7, 8, and 9 at
   once, which is exactly the kind of question a Junior/Mid-level
   interview tends to ask.

---

### Quick reference: full reading order, file names only

```
Phase 0  : README.md (How to run section)
Phase 1  : pom.xml -> application.yml -> TaskFlowApplication.java
Phase 2  : Role -> Priority -> TaskStatus -> User -> Tag -> Project -> Task
Phase 3  : UserEntity -> ProjectEntity -> TaskEntity -> TagEntity
           -> UserMapper -> ProjectMapper -> TaskMapper -> TagMapper
Phase 4  : UserRepository -> UserJpaRepository -> UserRepositoryImpl
           -> ProjectRepository -> ProjectJpaRepository -> ProjectRepositoryImpl
           -> TagRepository -> TagJpaRepository -> TagRepositoryImpl
           -> TaskRepository -> TaskJpaRepository -> TaskRepositoryImpl
Phase 5  : UserRegistrationRequest -> UserResponse
           -> ProjectCreateRequest -> ProjectUpdateRequest -> ProjectResponse
           -> TaskCreateRequest -> TaskUpdateRequest -> TaskStatusUpdateRequest -> TaskResponse
           -> LoginRequest -> LoginResponse -> ApiErrorResponse
Phase 6  : UserService -> TagService -> ProjectService -> TaskService
Phase 7  : UserController -> ProjectController -> TaskController -> AuthController
Phase 8  : ResourceNotFoundException -> DuplicateResourceException
           -> AuthorizationException -> GlobalExceptionHandler
Phase 9  : JwtService -> JwtAuthenticationFilter -> SecurityUtils
           -> RestAuthenticationEntryPoint -> RestAccessDeniedHandler -> SecurityConfig
Phase 10 : RequestMetrics -> RequestMetricsFilter -> NotificationService
           -> NotificationController -> MetricsController
Phase 11 : (reread TaskJpaRepository, ProjectEntity/TaskEntity, TagService)
           -> CacheConfig -> application.yml (reread)
Phase 12 : UserServiceTest -> NotificationServiceTest -> TaskJpaRepositoryTest -> UserControllerTest
Phase 13 : Dockerfile -> docker-compose.yml -> .dockerignore
Phase 14 : README.md (Interview Question Bank)
```
