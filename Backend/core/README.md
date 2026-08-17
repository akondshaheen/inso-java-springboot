# TaskFlow — A Java / Spring Boot Learning Project

TaskFlow is a small, realistic Project/Task tracker built specifically to
prepare for a **Junior/Mid-level Software Engineer interview**. It is not a
production system — it is a teaching project. Every important class has
detailed comments in plain English explaining **what** the code does,
**why** it exists, **how** it works, and **how** it connects to the rest of
the application. Those comments are the primary study material; this
README ties everything together and adds a consolidated interview
question bank.

## Domain

TaskFlow manages **Users**, **Projects**, **Tasks**, and **Tags**:

- A `User` owns zero or more `Project`s and can be `assignee` of many `Task`s.
- A `Project` has many `Task`s (`@OneToMany`).
- A `Task` belongs to exactly one `Project` (`@ManyToOne`), optionally has one
  `assignee` User (`@ManyToOne`), and can have many `Tag`s (`@ManyToMany`).

## Architecture

```
HTTP request
    ↓
Controller       (translates HTTP <-> Java calls; @RestController)
    ↓
Service          (business rules, transactions; @Service)
    ↓
Repository       (interface the service depends on; port)
    ↓
RepositoryImpl   (@Repository; adapter between our domain model and JPA)
    ↓
Spring Data JPA repository (interface; Spring generates the implementation)
    ↓
Hibernate -> JDBC -> Database
```

Two parallel models are used on purpose (a common interview topic):

- **domain/** — plain Java classes with no framework annotations. The
  service layer only ever works with these.
- **entity/** — JPA-annotated classes (`@Entity`, `@Column`, ...) shaped for
  Hibernate to persist. Only the `mapper/` and `repository/impl/` packages
  know about entities.

Request/response shaping is handled by **dto/request** and **dto/response**
records — the HTTP layer never receives or returns a domain or entity
object directly.

## Project structure

```
src/main/java/com/inso/learning/taskflow/
  controller/    REST endpoints (thin; no business logic)
  service/       business rules, @Transactional boundaries
  repository/    interfaces the service layer depends on
  repository/jpa/    Spring Data JPA interfaces (talk to entities)
  repository/impl/   @Repository adapters (interface <-> Spring Data JPA)
  domain/        plain business objects (User, Project, Task, Tag, enums)
  entity/        JPA-annotated persistence classes
  mapper/        entity <-> domain conversion
  dto/request/   what the client sends
  dto/response/  what the API returns
  exception/     custom exceptions + @RestControllerAdvice
  security/      JWT, Spring Security configuration
  concurrency/   thread-safe metrics, ExecutorService example
  config/        cross-cutting configuration (e.g. caching)
```

## How to run

**Locally with Maven (uses Postgres on localhost):**
```
./mvnw spring-boot:run
```

**With Docker Compose (app + Postgres, fully containerised):**
```
docker compose up -d --build
```
This builds the image (see `Dockerfile`, a multi-stage build), starts
Postgres, waits for it to become healthy, then starts the app on
`http://localhost:8080`. See `docker-compose.yml` for detailed comments on
how the two containers find each other and how environment variables
override `application.yml`.

**Running tests:**
```
./mvnw test
```

## Stage-by-stage map (what was built, and where)

1. **Setup** — `pom.xml`, `application.yml`.
2. **JPA / entities** — `entity/`, `domain/`, relationships explained in
   `TaskEntity`, `ProjectEntity`.
3. **Repository layer** — `repository/`, `repository/jpa/`, `repository/impl/`.
4. **Service layer + DTOs** — `service/`, `dto/`.
5. **Controllers** — `controller/`.
6. **Validation** — Bean Validation annotations across `dto/request/*`.
7. **Global exception handling** — `exception/GlobalExceptionHandler.java`.
8. **Testing** — `src/test/java/...` (unit tests with Mockito, `@DataJpaTest`,
   `@WebMvcTest`).
9. **Security** — `security/` (JWT, Spring Security, BCrypt, roles).
10. **Concurrency** — `concurrency/` (thread-safe counter), `service/NotificationService.java`
    (`ExecutorService`/`Callable`/`Future`).
11. **Performance** — database indexes (`@Table(indexes = ...)`), N+1 fix via
    `@EntityGraph` (`TaskJpaRepository`), caching (`TagService`,
    `config/CacheConfig.java`), connection pooling and Actuator
    (`application.yml`).
12. **Docker** — `Dockerfile`, `docker-compose.yml`.

---

# Interview Question Bank

Each question includes an answer you could realistically give out loud,
plus a deeper follow-up an interviewer might ask next. Where relevant, the
concept is tied to a specific file in this project.

## Java fundamentals

**Q: What is the difference between a class and an object?**
A class is a blueprint (like `Task`); an object is a specific instance of
that blueprint created with `new` (a particular task with its own title
and id). *Follow-up: what is the difference between a class variable and
an instance variable?*

**Q: What is encapsulation, and where do you use it in this project?**
Encapsulation means hiding an object's internal state behind private
fields and exposing controlled access through methods. Every domain class
(`User`, `Project`, `Task`) keeps its fields `private` and only exposes
getters/setters — see `Project.isOwnedBy(...)` for a case where a small
business rule lives next to the data it protects, instead of being
scattered through the service layer. *Follow-up: how does encapsulation
help with testing and maintenance?*

**Q: What is the difference between an interface and an abstract class?**
An interface declares a contract (methods any implementer must provide)
with no state of its own; a class can implement many interfaces.
`UserRepository`, `ProjectRepository`, `TaskRepository`, and `TagRepository`
are interfaces — the service layer depends only on them, never on the JPA
implementation. An abstract class can hold real fields and partially
implemented behaviour, but a class can only extend one. *Follow-up: when
would you choose an abstract class over an interface?*

**Q: What is polymorphism, and where does it show up here?**
Calling a method through a reference typed as the interface
(`ProjectRepository`) actually runs the real implementation
(`ProjectRepositoryImpl`) chosen at runtime. Method overriding
(`Task.compareTo`, `equals`, `hashCode`, `toString`) is another form: the
JVM decides at runtime which overridden version actually runs.
*Follow-up: what is the difference between compile-time and runtime
polymorphism (overloading vs overriding)?*

**Q: What is method overloading vs overriding?**
Overloading is having multiple methods with the SAME name but DIFFERENT
parameter lists in the same class — `User` has two constructors
(`User(name, email, passwordHash, role)` and
`User(id, name, email, passwordHash, role, createdAt)`), and the compiler
picks the right one based on arguments. Overriding is a SUBCLASS providing
its own implementation of a method already defined in a superclass or
interface, with the SAME signature — resolved at runtime.
*Follow-up: can you overload a method by return type alone? (No.)*

**Q: Why do we override `equals()` and `hashCode()` together?**
If two objects are `equals()`, they MUST produce the same `hashCode()`,
or they will behave incorrectly inside hash-based collections like
`HashMap`/`HashSet` (an object could be "lost" — you could insert it and
then fail to find it again). Every domain and entity class here overrides
both, based only on `id`, and documents WHY id-based equality is
appropriate for entities. *Follow-up: why do these classes compare only
`id`, and not every field?* (Because two objects representing the "same"
row should be equal even if some in-memory fields differ slightly, and
because comparing every field would break once a mutable field changes
after the object is already stored in a Set.)

**Q: What is the difference between `HashMap` and `HashSet`?**
`HashMap<K, V>` stores key-value pairs; `HashSet<E>` stores unique
elements only — internally, `HashSet` is actually backed by a `HashMap`
where every value is a shared dummy constant. *Follow-up: how does
`HashMap` work internally?* It computes `hashCode()` on the key, uses that
to pick a "bucket" (an array index), and stores entries in a small linked
list (or a balanced tree, once a bucket gets large) within that bucket,
falling back to `equals()` to distinguish different keys that hash to the
same bucket.

**Q: What is a functional interface, and where do you use lambdas here?**
A functional interface has exactly one abstract method, which is what
allows a lambda expression to be used in its place. `Comparator<Task>`
is used with lambdas/method references throughout `TaskService`
(`getAllTasksSortedByPriority`), and `Callable<NotificationResult>` in
`NotificationService` is another functional interface used with a lambda.
*Follow-up: what is the difference between `Runnable` and `Callable`?*
(`Callable` can return a value and throw a checked exception; `Runnable`
cannot.)

**Q: Walk me through how the Stream API is used in this project.**
`TaskService.getOverdueTasks()` filters tasks by a predicate, then sorts
using `Task`'s natural `Comparable` order, then collects to a `List` with
`toList()`. `TagService.findOrCreateAll(...)` maps a `Set<String>` of
names to `Tag` objects and collects them into a `Set<Tag>`.
*Follow-up: are Streams always faster than a for-loop?* No — for small
collections a simple loop can actually be faster because Streams have some
setup overhead; Streams mainly win on readability and, for very large
data, on the ability to parallelize easily.

**Q: What is `Optional`, and why not just return `null`?**
`Optional<T>` makes "this value might not exist" explicit in the method's
return type, forcing the caller to consciously handle the missing case
(`orElseThrow`, `orElseGet`, `map`) instead of risking a
`NullPointerException` somewhere far from where the null was produced.
`TaskRepository.getById(...)` returns `Optional<Task>`; `TaskService`
turns an empty Optional into a `ResourceNotFoundException`.
*Follow-up: should you use `Optional` as a field type or method
parameter?* Generally no — it is intended for return types.

**Q: What is the difference between a checked and unchecked exception?**
Checked exceptions (like `IOException`) must be declared or caught — the
compiler enforces handling. Unchecked exceptions (subclasses of
`RuntimeException`) do not require this. `ResourceNotFoundException` and
`DuplicateResourceException` are unchecked on purpose — see their Javadoc
for the reasoning: it keeps service method signatures clean, and these
represent programming/business-flow situations better handled centrally
(in `GlobalExceptionHandler`) than forced onto every single caller.
*Follow-up: when WOULD you choose a checked exception?* When callers can
realistically and meaningfully recover from the specific failure right at
the call site.

## Java memory and execution

**Q: What is the difference between the JDK, JRE, and JVM?**
The JDK is the full development kit (compiler + JRE + tools). The JRE is
everything needed to RUN compiled Java (JVM + core libraries), but cannot
compile source code. The JVM loads and executes bytecode, translating it
to real machine instructions as it runs. *Follow-up: what is Just-In-Time
compilation?*

**Q: What is the difference between the stack and the heap?**
Each thread has its own stack, holding local variables and method call
frames — when a method returns, its frame is popped automatically. The
heap is shared across all threads and holds every object created with
`new` — this is why objects mutated by one thread can be seen by another,
and exactly why `RequestMetrics` needs to be thread-safe.
*Follow-up: which one can cause a `StackOverflowError`, and why?*
(The stack — usually from deep or infinite recursion.)

**Q: Is Java pass-by-value or pass-by-reference?**
Java is always pass-by-value. For object references, the VALUE that gets
copied is the reference itself (essentially a memory address) — so a
method can mutate the object the reference points to, but reassigning the
parameter inside the method never affects the caller's variable.
*Follow-up: if a method does `list.add(x)` on a passed-in `List`
parameter, does the caller see the change? Why?*

**Q: What is the String pool, and why does it matter?**
Java caches String literals in a special pool so that `"abc"` written in
two different places can literally share the same object in memory,
saving memory since Strings are immutable and safe to share.
`==` compares references (so two literals ARE `==`), while `.equals(...)`
compares actual content — this is why Strings should always be compared
with `.equals(...)`. *Follow-up: does `new String("abc")` use the pool?*
(No — it forces a new object outside the pool.)

**Q: What makes an object immutable, and why does it matter?**
An immutable object's state cannot change after construction — every
field is `final` and set only in the constructor, and no method can
mutate it. `Priority`, `TaskStatus`, and `Role` (enums) are inherently
immutable; `Task.createdAt` is a `final` field for the same reason.
Immutable objects are automatically thread-safe (nothing to race on) and
safe to share freely. *Follow-up: how would you make a class with a
mutable field (like a `List`) truly immutable?* (Defensive copies on both
construction and any getter that returns the collection.)

## Spring Boot fundamentals

**Q: What is the difference between Spring and Spring Boot?**
Spring is the underlying framework providing dependency injection, AOP,
and many modules; configuring a plain Spring application by hand requires
a lot of manual XML/Java configuration. Spring Boot builds on top of
Spring and auto-configures sensible defaults based on what is on the
classpath (an embedded Tomcat server, a `DataSource`, Jackson JSON
support) so we can start writing business code almost immediately.
*Follow-up: how does Spring Boot decide WHAT to auto-configure?*
(`@ConditionalOn...` annotations checked against the classpath and
existing beans — see `@EnableAutoConfiguration`.)

**Q: What is Dependency Injection, and what is Inversion of Control?**
IoC is the general principle: instead of a class creating its own
dependencies with `new`, something else (the Spring container) creates
and hands them the objects they need. Dependency Injection is the actual
mechanism IoC uses in Spring — most commonly, passing dependencies through
a constructor. Every service and controller in this project uses
constructor injection (see `TaskService`, `ProjectController`).
*Follow-up: why is constructor injection preferred over field injection
(`@Autowired` on a field)?* It makes dependencies explicit and required,
allows fields to be `final` (guaranteeing they are never reassigned), and
makes the class trivially testable without needing Spring at all — just
call `new TaskService(mockRepo, ...)`.

**Q: What happens when Spring sees `@Service` on a class?**
At startup, component scanning finds the annotation and registers a bean
DEFINITION for that class. When the context refreshes, Spring reads the
class's constructor, resolves each parameter type against other known
beans (creating them first if needed), creates an instance by calling
that constructor, and stores the finished object in the
`ApplicationContext` so it can be injected anywhere else it is needed.
See `TaskFlowApplication`'s Javadoc for the full startup sequence.
*Follow-up: what if two beans of the same interface type both exist?*
(`NoUniqueBeanDefinitionException`, unless you use `@Qualifier` or mark
one `@Primary`.)

**Q: What is the difference between `@Controller` and `@RestController`?**
`@Controller` methods return a VIEW NAME by default (for a server-rendered
HTML page); `@RestController` is `@Controller` plus `@ResponseBody`,
telling Spring to serialize the return value directly as the HTTP
response body (JSON, in our case) instead of looking for a view.
Every controller in this project (`TaskController`, `UserController`, ...)
is a `@RestController`, because we are building a JSON API.
*Follow-up: what does `@ResponseBody` do at a lower level?* (Tells
Spring MVC to run the return value through an `HttpMessageConverter`
— Jackson, here — instead of the `ViewResolver` machinery.)

**Q: What is a Spring Bean, and what is the Application Context?**
A bean is any object whose lifecycle (creation, dependency injection,
destruction) is managed by Spring instead of by our own code calling
`new`. The `ApplicationContext` is the container holding all of them —
think of it as a big map from bean name/type to the fully-constructed
object, built once at startup. *Follow-up: what is bean scope, and what
is the default?* (Default is `singleton` — one shared instance per
context; `prototype` creates a new instance every time the bean is
requested.)

## REST API

**Q: What is REST, and what does "stateless" mean here?**
REST (Representational State Transfer) is an architectural style for
building APIs around resources (`/api/tasks`, `/api/projects`) manipulated
with standard HTTP methods. "Stateless" means the server keeps no memory
of a client between requests — every request must carry everything needed
to process it (here, a JWT in the `Authorization` header). This is exactly
why `SecurityConfig` sets the session policy to `STATELESS`: any server
instance can handle any request without needing shared session state.
*Follow-up: why does statelessness make horizontal scaling easier?*

**Q: What is the difference between PUT and PATCH?**
PUT replaces an entire resource with the request body — every field the
client omits should conceptually become `null`/default. PATCH applies a
PARTIAL update — only the fields actually supplied change.
`TaskController` demonstrates both: `PUT /api/tasks/{id}` (via
`TaskUpdateRequest`) replaces title/description/priority/due date, while
`PATCH /api/tasks/{id}/status` (via `TaskStatusUpdateRequest`) changes
only the status. *Follow-up: is PUT idempotent? Is POST?* (PUT and DELETE
are idempotent — repeating them has the same effect as doing them once;
POST generally is not, since posting a create request twice normally
creates two resources.)

**Q: What is the difference between 200 and 201?**
200 OK is a generic success; 201 Created specifically means a new
resource was created, and conventionally comes with a `Location` header
pointing at the new resource's URL. `TaskController.create(...)` and
`ProjectController.create(...)` return
`ResponseEntity.created(URI...)` for exactly this reason.
*Follow-up: what does 204 No Content mean, and where might you use it?*
(Success, with deliberately no response body — often used for DELETE.)

**Q: What is the difference between 400 and 404?**
400 Bad Request means the request itself was malformed or failed
validation (see `MethodArgumentNotValidException` handling) — the problem
is visible before we even look at existing data. 404 Not Found means the
request was well-formed, but the specific resource being asked about does
not exist (`ResourceNotFoundException`). *Follow-up: what about 409
Conflict?* Used when the request is valid and the resource may exist, but
the current SERVER STATE conflicts with it — for example, registering a
duplicate email (`DuplicateResourceException`).

**Q: What is the difference between 401 and 403?**
401 Unauthorized: the server does not know who you are at all (missing or
invalid credentials/token). 403 Forbidden: the server knows exactly who
you are, but you are not allowed to perform this specific action. See
`RestAuthenticationEntryPoint` (401) vs `RestAccessDeniedHandler`/
`AuthorizationException` (403) for the concrete implementation of this
distinction. *Follow-up: what HTTP status would you return for an
expired JWT?* (401 — the caller's claimed identity can no longer be
trusted.)

**Q: What is a DTO, and why not just return the entity/domain object
directly from a controller?**
A DTO (Data Transfer Object) is a class shaped specifically for what an
HTTP request or response needs — decoupled from our internal domain and
persistence models. Returning an entity directly can accidentally leak
internal fields (like a password hash), can trigger `LazyInitialization`
issues outside a transaction, and tightly couples our public API's shape
to internal implementation details that should be free to change. See
`UserResponse` for a concrete example of a field (`passwordHash`)
deliberately excluded from the API response. *Follow-up: could you use
the SAME class for both request and response DTOs?* You could, but
separating them (as this project does) lets each carry exactly the fields
that specific operation needs.

## Controller-Service-Repository architecture

**Q: Why not put business logic directly in the controller?**
A controller's only job should be translating an HTTP request into a
method call, and a method result back into an HTTP response. Putting
business logic there mixes two very different concerns together, making
the logic hard to unit test (you would need a full HTTP-testing setup
just to test a business rule), hard to reuse (what if the same rule is
needed from a background job, not just an HTTP call?), and hard to read.
*Follow-up: what specifically belongs in the service layer vs the
repository layer?* Service = business rules and orchestration (validating
that a project exists before creating a task for it, for example);
Repository = translating between our domain model and the database,
nothing more.

## Database / JPA

**Q: What is the difference between JPA and Hibernate?**
JPA (Jakarta Persistence API) is a SPECIFICATION — a set of interfaces and
annotations (`@Entity`, `EntityManager`) describing HOW object-relational
mapping should work, without saying how it is actually implemented.
Hibernate is the most popular IMPLEMENTATION of that specification — it
is the library that actually generates SQL and talks to the database.
Spring Data JPA sits on top of both, generating repository implementations
at runtime. *Follow-up: could you swap Hibernate for a different JPA
provider without changing your code?* In principle yes, since your code
depends on JPA's interfaces, not Hibernate's classes directly.

**Q: What happens when a Spring Data JPA repository method is called?**
Spring Data generates a dynamic proxy implementing the repository
interface at startup. Calling a derived query method (like
`findByEmail(...)`) makes the proxy parse the METHOD NAME into a query,
which Hibernate translates into real SQL, executed through a JDBC
connection borrowed from the connection pool; the result rows are mapped
back into entity objects and returned. See `UserJpaRepository`'s Javadoc
for the full walkthrough. *Follow-up: what happens for a method annotated
with `@Query`?* (Hibernate parses the supplied JPQL instead of deriving a
query from the method name.)

**Q: What is the N+1 query problem?**
Fetching a list of N parent entities with a lazy relationship, then
touching that relationship for every item in a loop, causes 1 query to
fetch the list PLUS N more queries — one per item — to lazily load each
one's relationship, instead of one efficient query. See
`TaskJpaRepository`'s Javadoc for a full example using `TaskEntity.project`,
and the `@EntityGraph`-based fix on the overridden `findAll()`.
*Follow-up: what other techniques fix N+1 besides `@EntityGraph`?*
(`JOIN FETCH` in JPQL, or batch fetching via
`hibernate.default_batch_fetch_size`.)

**Q: What is lazy vs eager loading?**
`FetchType.LAZY` means a relationship is only loaded from the database
the moment code actually accesses it (`task.getProject()`);
`FetchType.EAGER` loads it immediately, as part of the original query.
Every `@ManyToOne`/`@ManyToMany` in this project uses `LAZY` deliberately
— loading relationships nobody asked for wastes time and memory.
*Follow-up: what is `LazyInitializationException`, and when does it
happen?* (Trying to access a lazy relationship after the
`EntityManager`/Hibernate session that loaded the owning entity has
already closed — commonly outside a `@Transactional` boundary.)

**Q: What is `@Transactional`, and why does it matter?**
It tells Spring to wrap a method in a database transaction — either every
database change inside it succeeds together, or (if an exception is
thrown) every change is rolled back together, so the database is never
left in a half-updated state. *Follow-up: what is transaction
propagation?* It controls what happens when a `@Transactional` method
calls ANOTHER `@Transactional` method — for example, `REQUIRED` (the
default) joins the existing transaction if one is already running, while
`REQUIRES_NEW` always starts a brand new, independent one.

**Q: Why does this project use database indexes, and on which columns?**
An index lets the database jump close to matching rows instead of
scanning the whole table (closer to O(log n) instead of O(n)).
`TaskEntity` indexes `project_id`, `assignee_id`, and `status` because
those are exactly the columns our query methods filter on
(`getTasksForProject`, `getTasksForAssignee`, `getByStatus`).
*Follow-up: is there a downside to adding too many indexes?* Yes — every
index also has to be updated on every insert/update, and takes up storage,
so indexes should be added deliberately, not on every column "just in
case".

## Validation

**Q: How does `@Valid` actually work?**
When a controller method parameter is annotated `@Valid`, Spring runs
Bean Validation against every constraint annotation on that DTO
(`@NotBlank`, `@Size`, `@Email`, ...) BEFORE the method body executes. If
any rule fails, Spring throws `MethodArgumentNotValidException`, which
`GlobalExceptionHandler` catches and turns into a 400 response listing
every failing field — the controller method itself never even runs with
bad data. *Follow-up: how would you add a custom validation rule that
`@NotBlank`/`@Size` cannot express?* (Write a custom annotation plus a
`ConstraintValidator` implementation.)

## Exception handling

**Q: Why is `@RestControllerAdvice` preferable to try/catch in every
controller?**
Exception handling is a "cross-cutting concern" — it has nothing to do
with any one controller's specific responsibility. Writing the same
try/catch and error-formatting logic in every controller method would be
repetitive and easy to get subtly inconsistent between endpoints.
`@RestControllerAdvice` lets us write it exactly once and have Spring
apply it everywhere automatically. *Follow-up: why do we log the
exception in `handleUnexpected` but not return `ex.getMessage()` to the
caller?* Internal error details could leak sensitive information to
whoever sent the request; the details still need to reach OUR logs so the
team can actually diagnose the problem.

## Testing

**Q: What should be unit tested vs integration tested?**
Business logic implemented in plain Java (a validation rule, a duplicate
check) is a perfect fit for a fast, isolated UNIT test with mocked
dependencies (`UserServiceTest`, `NotificationServiceTest`, using
Mockito). Behaviour that depends on a real framework or database (actual
JPA query correctness, pagination, HTTP request parsing) can only be
honestly verified with an INTEGRATION test against real infrastructure
(`TaskJpaRepositoryTest` uses `@DataJpaTest` with a real H2 database;
`UserControllerTest` uses `@WebMvcTest` with `MockMvc`).
*Follow-up: why can excessive mocking be a problem?* If you mock every
single collaborator, your test ends up only checking "did my code call
these methods in this order?" instead of proving the code actually
produces the right RESULT — the test becomes brittle and coupled to
implementation details instead of behaviour.

**Q: What is the difference between a mock and a real dependency in a
test?**
A mock is a fake object we fully control, programmed to return exactly
the values we want, so we can test one class's logic in complete
isolation. A real dependency (a real `UserRepositoryImpl` backed by a
real/in-memory database) actually exercises genuine behaviour, at the
cost of being slower and needing more setup. *Follow-up: what is the
difference between a mock and a stub?* (A stub simply returns canned
values; a mock can additionally VERIFY that specific interactions
happened — see `verify(...)` calls in the test suite.)

## Security

**Q: What is the difference between authentication and authorization?**
Authentication answers "who are you?" — proving identity, usually via a
password (`AuthController`'s login) or, on every later request, a JWT
(`JwtAuthenticationFilter`). Authorization answers "given that I know who
you are, are you ALLOWED to do this?" — for example, only an ADMIN may
list every user (`SecurityConfig`'s `.hasRole("ADMIN")` rule). A request
can be authenticated yet still unauthorized for a specific action — that
exact mismatch is the difference between a 401 and a 403.
*Follow-up: can you be authorized without being authenticated?* No —
authorization always depends on already knowing who the caller is.

**Q: Why hash passwords, and why specifically with BCrypt rather than
something like SHA-256?**
Storing a plain password means anyone who reads the database (an
attacker, or an insider) instantly has every user's real password.
Hashing makes this one-way — you can check a password by re-hashing it and
comparing, without ever storing the original. BCrypt specifically is
deliberately SLOW (computationally expensive) and automatically mixes in a
random "salt" per password, which defeats brute-force attacks and
precomputed "rainbow table" lookups far better than a single fast
general-purpose hash like SHA-256. *Follow-up: why does a slow hash
function actually help security here?* It makes trying millions of
guesses per second (as an attacker would) prohibitively slow, while adding
a negligible delay for one legitimate login check.

**Q: How does the JWT-based authentication flow work end to end in this
project?**
The client posts credentials to `/api/auth/login`; `AuthController`
checks them and, on success, `JwtService` issues a signed token containing
the user's id, email, and role as claims. The client stores that token and
sends it as a `Bearer` token in the `Authorization` header on every
following request. `JwtAuthenticationFilter` (a `OncePerRequestFilter`)
intercepts each request, validates and decodes the token, and — if valid —
populates Spring Security's `SecurityContextHolder` with the caller's
identity before the request reaches our controller. *Follow-up: what
happens if the token is expired or tampered with?* `JwtService` throws
during verification, the filter does not set an authenticated context,
and the request reaches `SecurityConfig`'s `.anyRequest().authenticated()`
rule unauthenticated — resulting in a 401 via `RestAuthenticationEntryPoint`.

**Q: Why is CSRF protection disabled in `SecurityConfig`, and is that
always safe?**
CSRF protection exists to guard COOKIE-based session authentication, where
a browser automatically attaches a session cookie to every request,
including ones from a malicious page. Our API is stateless and requires
the client to deliberately attach a JWT to the `Authorization` header —
something a malicious page cannot make a victim's browser do
automatically the way it can with cookies — so this specific attack does
not apply here. This is NOT a decision to copy blindly into an app that
DOES use cookie-based sessions. *Follow-up: what is CORS, and is it the
same concern as CSRF?* No — CORS is about which origins a BROWSER will
allow JavaScript to make requests to; it is a separate browser security
mechanism, configured in `SecurityConfig.corsConfigurationSource()`.

## Concurrency

**Q: Why does `RequestMetrics` need to be thread-safe, and how does it
achieve that?**
Tomcat handles concurrent HTTP requests on a POOL of threads, sharing the
same heap — so if two requests arrive close together, `RequestMetrics`'s
`increment(...)` method may genuinely run on two threads at the same
instant. A naive `HashMap<String, Long>` with `count + 1` would lose
updates due to a RACE CONDITION (see the class's Javadoc for the detailed
walkthrough). `RequestMetrics` instead uses `ConcurrentHashMap` combined
with `AtomicLong`, giving thread-safe, non-blocking increments without any
manual `synchronized` block. *Follow-up: would marking the counter field
`volatile` alone have fixed this?* No — `volatile` only guarantees
VISIBILITY of the latest value across threads; it does not make a
multi-step "read, add one, write" sequence atomic.

**Q: Why does `NotificationService` use `ExecutorService` instead of
creating `Thread` objects directly?**
Creating a brand-new OS thread per task is expensive, and with enough
concurrent work can exhaust system resources. `ExecutorService` manages a
reusable POOL of worker threads, queuing extra work when all threads are
busy — the standard, production-grade way to run concurrent Java code.
*Follow-up: what does `Future.get()` do, and what happens if the
underlying task threw an exception?* It BLOCKS the calling thread until
the task finishes and returns its result; if the task threw, `get()`
re-throws it wrapped in an `ExecutionException`.

**Q: What is a deadlock?**
A deadlock happens when two (or more) threads each hold a resource the
other one needs, and each is waiting for the other to release it first —
neither can ever proceed. A classic example: Thread A locks Object 1,
then tries to lock Object 2; Thread B locks Object 2, then tries to lock
Object 1, at nearly the same time. *Follow-up: how do you generally avoid
deadlocks?* Always acquire multiple locks in a single, consistent order
across the whole codebase.

## Spring Boot deeper concepts

**Q: What is a Spring bean's default scope, and when would you use a
different one?**
`singleton` — one shared instance for the whole application context — is
the default, and is what every `@Service`/`@Repository`/`@Component` in
this project uses. `prototype` scope creates a brand-new instance every
time the bean is requested; useful for a bean that holds mutable,
request-specific or short-lived state that must never be shared.
*Follow-up: is a singleton bean thread-safe automatically?* No — if it
holds mutable state (like `RequestMetrics`), that state must be made
thread-safe explicitly.

**Q: What is a circular dependency, and how does Spring handle it?**
Bean A needing Bean B in its constructor, while Bean B needs Bean A in
ITS constructor, is a circular dependency — with pure constructor
injection, Spring cannot resolve it at all and startup fails with a
`BeanCurrentlyInCreationException`. This is usually a sign the
responsibilities between the two classes should be redesigned (or one
dependency turned into a setter/field injection, though that only masks
the underlying design smell). *Follow-up: could you fix a circular
dependency without changing the class design?* `@Lazy` on one of the
constructor parameters defers that bean's creation, but the cleanest fix
is almost always to remove the actual circular need.

## Microservices and distributed-systems basics

**Q: What is the difference between a monolith and microservices, and
which is TaskFlow?**
TaskFlow is a MONOLITH — one deployable application containing every
layer (controllers, services, repositories) for the whole domain.
Microservices split an application into multiple independently deployable
services, each owning its own data and communicating over the network
(REST, message queues). Monoliths are simpler to build, test, and deploy
for a small team or small domain; microservices add real operational
complexity but let different teams scale, deploy, and choose technology
independently for different parts of a large system.
*Follow-up: what problems does splitting into microservices INTRODUCE
that a monolith does not have?* Network latency and failures between
services, harder cross-service transactions, and much more complex
testing/deployment/monitoring.

**Q: What is eventual consistency, and why would a distributed system
accept it?**
In a distributed system, updates to different services/databases cannot
always be applied in one single atomic transaction (as they could within
one database in a monolith like TaskFlow). Eventual consistency accepts
that, for a short window, different parts of the system may show slightly
different (stale) data, with a guarantee that they will converge to the
same state eventually — usually via asynchronous messages or retries.
*Follow-up: how does this relate to idempotency?* An operation is
idempotent if performing it multiple times has the same effect as
performing it once — critical in distributed systems because a RETRY
after a timeout might actually have succeeded the first time; without
idempotency, a retry could duplicate the effect (like double-charging a
payment).

**Q: What is a circuit breaker, conceptually?**
When a downstream service is failing repeatedly, a circuit breaker "trips
open" and stops sending it further requests for a while (failing fast
instead), giving the struggling service room to recover, and avoiding
piling up slow, doomed requests on the calling side. After a cooldown, it
allows a few test requests through to see if the downstream service has
recovered. *Follow-up: how is this different from a simple retry?* A
retry tries the SAME call again hoping for a different outcome; a circuit
breaker instead stops trying for a while once failures are clearly
persistent, to protect both sides.

## Performance

**Q: How would you investigate and improve a slow API endpoint?**
First, MEASURE — check logs/Actuator metrics, and enable SQL logging
(`show-sql: true`, already on in this project) to see what queries are
actually running. Common culprits: the N+1 query problem (fixed here with
`@EntityGraph`), missing indexes on frequently filtered columns, fetching
far more data than needed (fixed here with pagination), or doing
expensive work that could be cached (`TagService.getAllTags()`).
*Follow-up: how would you verify your fix actually helped?* Compare
before/after query counts and timings, ideally with a repeatable
benchmark or load test, not just "it feels faster".

**Q: Why can caching be dangerous if used carelessly?**
A cache trades a small risk of showing SLIGHTLY OLD data for real
performance — that trade-off is only safe for data that changes rarely
or where staleness is acceptable. `TagService.getAllTags()` is cached for
exactly this reason (tags rarely change), and `findOrCreateByName(...)`
explicitly EVICTS the cache whenever a new tag might have been created —
a cache with no eviction strategy, or one caching data that is different
per-user without including the user in its key, will silently serve WRONG
results. *Follow-up: what cache eviction/expiry strategies do you know?*
Time-based expiry (TTL), explicit eviction on write (used here), and
size-based eviction (LRU) once a cache grows too large.

## Docker

**Q: What is Docker actually doing, and why does it help here?**
Docker packages an application together with everything it needs to run
(a specific JVM, our compiled classes, dependencies) into one portable,
self-contained image, avoiding "it works on my machine" problems caused
by different Java versions or missing configuration across machines. The
`Dockerfile` in this project uses a MULTI-STAGE build: one temporary stage
with the full JDK and Maven compiles the app, and only the final, much
smaller stage (a bare JRE plus our .jar) is actually shipped.
*Follow-up: why use Docker Compose here instead of running two `docker
run` commands by hand?* Compose describes both containers (app + Postgres)
and how they should find each other on a shared network in ONE file, and
starts/stops them together in the right order — far less error-prone than
wiring it up manually every time.

**Q: How does the app container find the database container in
`docker-compose.yml`?**
Compose creates a private network where each service can reach the others
using its SERVICE NAME as a hostname. `SPRING_DATASOURCE_URL` is set to
`jdbc:postgresql://db:5432/taskflow` — `db` is the Postgres service's
name in `docker-compose.yml`, not `localhost` (which, from inside the
`app` container, would mean the app container itself).
*Follow-up: how did the app pick up that environment variable instead of
the value in `application.yml`?* Spring Boot's relaxed binding
automatically lets any property be overridden by an environment variable
of the matching name (`SPRING_DATASOURCE_URL` maps to
`spring.datasource.url`) — no code change needed to support this.
