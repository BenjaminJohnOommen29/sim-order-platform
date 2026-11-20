## Java Record
- Similar to a Kotlin data class with `val` properties.
- Generates constructor, accessors, equals(), hashCode(), and toString().
- Record fields are final; no generated copy() method.
- Example: `record CreateOrderRequest(String customerId) {}`

## @RequestBody
- Reads the HTTP request body and converts JSON into a Java object using an HTTP message converter.
- Example: `createOrder(@RequestBody CreateOrderRequest request)`
- Different from @RequestParam, which reads request parameters.


## @RestController and @PostMapping
- @RestController handles HTTP requests and returns response data.
- @PostMapping maps an HTTP POST request to a method.
- @RequestMapping('/api/orders') defines the base URL.


## Sprint 1 — Validation, Exception Handling & HTTP Responses

### Custom Validation Messages
- `@NotBlank` rejects null, empty, and whitespace-only strings.
- `@NotBlank(message = "Customer ID is required")` overrides the default message.
- `@Valid` triggers validation on the incoming `@RequestBody` DTO.

### Global Exception Handling
- `@RestControllerAdvice` centralizes exception handling across REST controllers.
- `@ExceptionHandler(MethodArgumentNotValidException.class)` handles request DTO validation failures.
- `MethodArgumentNotValidException` provides field errors through `getBindingResult().getFieldErrors()`.
- `error.getField()` returns the field name; `error.getDefaultMessage()` returns the validation message.
- A centralized handler avoids duplicating error handling in controllers.

### ResponseEntity<T>
- `ResponseEntity<T>` represents an HTTP response, including status, headers, and body.
- `ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors)` returns HTTP 400 with validation errors.
- `ResponseEntity.status(HttpStatus.CREATED).body(message)` returns HTTP 201.
- HTTP 201 means a resource was created; our current endpoint only simulates creation, without persistence.

### Interview Takeaways
- Difference between `@Valid` and `@NotBlank`: one triggers object validation, the other defines a constraint.
- Difference between `@RestControllerAdvice` and `@ExceptionHandler`: one defines centralized advice, the other maps an exception type to a handler method.
- Difference between 200, 201, 404 and 400: successful request, resource creation, endpoint not available and invalid request respectively.

## Sprint 1 — Enums, Domain Models & DTO Mapping

### 1. Java Enums

An `enum` represents a fixed set of named constants.

We created `OrderStatus`:

```java
public enum OrderStatus {
    CREATED,
    INVENTORY_RESERVED,
    CONFIRMED,
    REJECTED,
    DISPATCHED,
    ACTIVATED
}
```

**Why use an enum instead of a String?**

- Provides compile-time type safety.
- Prevents arbitrary or misspelled status values in Java code.
- Makes business states explicit.
- Helps when implementing order lifecycle transitions.

**Kotlin comparison:** `enum class OrderStatus { CREATED, ... }`

### 2. Enums and JSON Serialization

Our response DTO now uses:

```java
public record CreateOrderResponse(
    String orderId,
    String customerId,
    OrderStatus status
) {}
```

Inside Java, the status is `OrderStatus.CREATED`.

Spring's JSON converter normally serializes it as:

```json
"status": "CREATED"
```

**Key learning:** We improved Java type safety without changing the JSON response contract.

### 3. Domain Model vs DTO

We created `SimOrder` to represent an order within our application.

| Model | Responsibility |
|---|---|
| `CreateOrderRequest` | Incoming API data |
| `SimOrder` | Internal business data |
| `CreateOrderResponse` | Outgoing API data |

`SimOrder` includes `simType` and `planCode`, which are needed internally but aren't currently included in the response.

**Why separate them?** Internal business models can evolve independently of the public API contract.


### Interview Takeaways

- **Enum vs String:** Enums provide compile-time type safety for a finite set of values.
- **Domain model vs DTO:** Domain models represent business concepts; DTOs define data exchanged across boundaries.
- **Why map DTOs?** To avoid tightly coupling API contracts to internal models.
- **Refactoring:** We changed the internal implementation without changing the API's observable behavior.

## Sprint 1 — In-Memory Repository, Optional & GET APIs

### 1. Repository Layer

We introduced `InMemoryOrderRepository` with `@Repository`.

Responsibilities:
- `OrderController`: Handles HTTP requests and responses.
- `OrderService`: Handles business logic.
- `InMemoryOrderRepository`: Handles order storage and retrieval.

The service receives the repository through constructor injection.

**Interview takeaway:** Separating data access from business logic improves maintainability, testability, and makes it easier to replace storage implementations.

### 2. ConcurrentHashMap

We used:

```java
private final Map<String, SimOrder> orders =
        new ConcurrentHashMap<>();
```

Why?
- Spring Boot can process multiple HTTP requests concurrently.
- The repository is a shared singleton bean.
- `HashMap` is not safe for unsynchronized concurrent modifications.
- `ConcurrentHashMap` supports thread-safe individual operations such as `put()` and `get()`.

Important: Thread-safe individual operations do not automatically make multi-step business operations atomic.

**Kotlin comparison:** Kotlin/JVM can use the same `ConcurrentHashMap` class.

### 3. Java Optional

Our repository returns:

```java
public Optional<SimOrder> findById(String orderId) {
    return Optional.ofNullable(orders.get(orderId));
}
```

`Optional` explicitly represents a value that might be absent.

- Existing order: `Optional` containing a `SimOrder`.
- Missing order: `Optional.empty()`.
- `isPresent()` returns `false` when empty.

**Kotlin comparison:** `Optional<SimOrder>` is conceptually similar to returning `SimOrder?`.

### 4. GET Endpoint and @PathVariable

We added:

```java
@GetMapping("/{orderId}")
public ResponseEntity<SimOrder> getOrderById(
        @PathVariable String orderId) {

    return orderService.getOrderById(orderId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
}
```

`@GetMapping` maps an HTTP GET request to a controller method.

`@PathVariable` extracts a value from the URL.

Example: `/api/orders/abc-123` supplies `abc-123` as the `orderId`.

### 5. Optional.map() and Method References

`map(ResponseEntity::ok)` transforms an existing `SimOrder` into a `ResponseEntity<SimOrder>` with HTTP 200.

`ResponseEntity::ok` is a Java method reference, equivalent here to:

```java
order -> ResponseEntity.ok(order)
```

`orElseGet()` provides an alternative response if the Optional is empty.

### 6. HTTP Response Semantics

| Operation | HTTP status |
|---|---|
| Create an order | 201 Created |
| Retrieve an existing order | 200 OK |
| Retrieve a missing order | 404 Not Found |
| Submit an invalid order request | 400 Bad Request |

### 7. Current Limitations

- Orders are stored only in application memory.
- Restarting the application deletes all stored orders.
- Different application instances would have separate maps.
- The GET endpoint currently exposes `SimOrder` directly instead of a dedicated response DTO.

Later, we'll replace in-memory storage with PostgreSQL and Spring Data JPA.

### Interview Takeaways

- Why use a repository layer?
- `HashMap` vs `ConcurrentHashMap`.
- `Optional` vs returning `null`.
- `@PathVariable` vs `@RequestBody`.
- Method references and `Optional.map()`.
- HTTP 200 vs 201 vs 400 vs 404.
- Why in-memory storage is unsuitable for durable production data.
## Sprint 1 — Unit Testing with JUnit and Mockito

### Key concepts learned

**JUnit:** Provides `@Test` and assertions such as `assertEquals()` and `assertTrue()`.

**Mockito:** Creates mock dependencies, configures their behavior, and verifies interactions.

- `@ExtendWith(MockitoExtension.class)` integrates Mockito with JUnit.
- `@Mock` creates a mock repository.
- `@InjectMocks` constructs the service and injects its mocked dependencies.

**Arrange–Act–Assert:** Organizes tests into setup, execution, and verification.


### Troubleshooting lessons

- Maven initially ran with Java 8 instead of Java 21, preventing Java records from compiling.
- Switching `JAVA_HOME` and `PATH` to Java 21 resolved the compilation issue.
- IntelliJ IDEA 2023.3.4's method-level JUnit runner encountered a compatibility error with JUnit 6.
- Running tests through Maven Surefire worked successfully.

### Interview takeaways

- Unit tests should isolate business logic from infrastructure.
- Assertions verify returned results; Mockito verification checks interactions with dependencies.
- Stubbing allows deterministic testing of success and failure scenarios.


## Mock vs Real Repository — Testing Insights

### 1. Comparing test approaches

| | Mockito unit test | Service + repository test |
|---|---|---|
| `OrderService` | Real | Real |
| `InMemoryOrderRepository` | Mock | Real |
| `save()` | Records the call | Executes `Map.put()` |
| `findById()` | Returns stubbed/default value | Executes `Map.get()` |
| Data actually stored? | ❌ No | ✅ Yes |
| Spring Boot context? | ❌ No | ❌ No |

### 2. How the two approaches work

**With Mockito**

```text
OrderService.createOrder()
         |
         v
MockRepository.save(order)
         |
         v
Records interaction only
(No ConcurrentHashMap update)
```

**With a real repository**

```text
OrderService.createOrder()
         |
         v
RealRepository.save(order)
         |
         v
ConcurrentHashMap.put(id, order)
         |
         v
RealRepository.findById(id)
         |
         v
Order successfully retrieved
```

### 3. Key annotations and methods

| Feature | Purpose |
|---|---|
| `@Mock` | Creates a fake dependency |
| `@InjectMocks` | Injects mocks into a real object |
| `when().thenReturn()` | Defines mock behavior |
| `verify().save(expectedOrder)` | Checks the argument passed to `save()` |
| `ArgumentCaptor` | Captures arguments for inspection |
| `assertEquals()` | Checks actual vs expected values |

### 4. Why no stubbing for `createOrder()`?

`OrderService` is real, so its method executes normally:

`Request → SimOrder → save() → CreateOrderResponse`

The response is **constructed by the service**, not returned by the repository.

### 5. Test organization

```text
src/test/java/.../
  service/
    OrderServiceTest.java
       → Mockito unit tests

    OrderServiceRepositoryTest.java
       → Real service + real repository

  SimOrderPlatformApplicationTests.java
       → Spring context startup test
```

**Interview takeaway:** Mock tests verify isolated behavior and interactions. Tests using real collaborators verify that components work together. A verified mock `save()` call does not prove data was stored.


## Sprint 1 — Immutability and Domain State Transitions

### 1. State Transitions in Domain Models

A **state transition** represents a valid change from one business state to another.

Instead of allowing unrestricted status updates, domain methods enforce permitted transitions.

```java
public SimOrder markInventoryReserved() {
    if (status != OrderStatus.CREATED) {
        throw new InvalidOrderStateTransitionException(
            "Invalid order state: " + status
        );
    }

    return new SimOrder(
        orderId, customerId, simType, planCode,
        OrderStatus.INVENTORY_RESERVED
    );
}
```

- Business rules remain inside the domain model.
- Invalid transitions are rejected explicitly.
- Immutable records return a new instance rather than modifying existing state.
- A Java record has no automatically generated `copy()` method, unlike a Kotlin data class.

**Interview takeaway:** Encapsulating state transitions prevents invalid business states and avoids scattering validation logic across services and controllers.

### 2. Immutability vs Persistence

Creating an updated immutable object does not automatically update stored data.

```java
SimOrder updated = order.markInventoryReserved();
repository.save(updated);
```

- `updated` is a new object.
- The original record remains unchanged.
- The repository must store the new version.

**Key learning:** Object immutability and persistence are separate concerns.

---

## Sprint 1 — Custom Exceptions and HTTP Error Handling

### 1. Custom Runtime Exceptions

Custom exceptions represent specific application or business failures.

```java
public class InvalidOrderStateTransitionException
        extends RuntimeException {

    public InvalidOrderStateTransitionException(String message) {
        super(message);
    }
}
```

- `RuntimeException` is unchecked.
- Java does not require unchecked exceptions to be declared with `throws`.
- Custom exception types allow precise error handling.
- Avoid using generic exceptions when callers need to distinguish business failures.

**Kotlin comparison:** Kotlin exceptions are also unchecked.

### 2. HTTP 409 Conflict

HTTP 409 indicates that a request conflicts with the current state of a resource.

Example: attempting a state transition that is not permitted from the current status.

| Status | Meaning |
|---|---|
| 400 | Invalid request |
| 404 | Resource not found |
| 409 | Conflict with current resource state |
| 500 | Unexpected server failure |

**Interview takeaway:** A business-state conflict should not normally be reported as an unexpected server error.

### 3. Handling Multiple Exceptions

`@ExceptionHandler` can handle more than one exception type.

```java
@ExceptionHandler({
    InvalidOrderStateTransitionException.class,
    InsufficientInventoryException.class
})
public ResponseEntity<?> handleConflict(RuntimeException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("message", ex.getMessage()));
}
```

- One handler can support multiple exception classes.
- Group exceptions when they share HTTP response semantics.
- Avoid mapping every `RuntimeException` to a business error, as this can hide programming defects.

**Interview takeaway:** Exception handlers should be organized around meaningful error semantics, not necessarily one method per exception class.

---

## Sprint 1 — Mockito and Spring Test Annotations

### @Mock vs @InjectMocks vs @MockitoBean

| Annotation | Purpose | Spring context? |
|---|---|---|
| `@Mock` | Creates a Mockito mock | No |
| `@InjectMocks` | Injects available mocks into the object under test | No |
| `@MockitoBean` | Overrides a bean with a Mockito mock in the Spring test context | Yes |

**Plain Mockito unit test:**

```text
@InjectMocks OrderService
          |
          v
    @Mock Repository
```

**Spring MVC test:**

```text
@WebMvcTest
     |
     v
Real Controller
     |
     v
@MockitoBean Service
```

- `@InjectMocks` does not create a Spring bean.
- `@MockitoBean` is useful when testing Spring-managed components.
- Mocking a dependency verifies isolated behaviour, not the real dependency's implementation.

**Interview takeaway:** Choose mocking annotations based on whether the test uses Mockito alone or a Spring application context.

---

## Sprint 1 — JUnit Exception Assertions

### assertThrows()

`assertThrows()` verifies that executing a block produces an expected exception type.

```java
assertThrows(
    InvalidOrderStateTransitionException.class,
    () -> service.reserveInventory(orderId)
);
```

- The second argument is a lambda executed by JUnit.
- The test fails if no exception is thrown.
- It also fails if an incompatible exception type is thrown.
- `assertThrows()` accepts the specified exception type or a subclass.

**Interview takeaway:** Test failure scenarios explicitly, not just successful results.

---

## Sprint 1 — Concurrency and Atomicity

### Thread Safety vs Atomic Business Operations

`ConcurrentHashMap` makes individual operations such as `get()` and `put()` thread-safe.

However:

```text
Read → Validate → Update → Save
```

is not automatically atomic.

Two threads can read the same initial state and independently attempt updates.

**Key distinction:**

- **Thread-safe operation:** An individual operation can be performed safely across threads.
- **Atomic operation:** The operation completes as one indivisible unit from the perspective of other threads.

Database transactions alone do not automatically prevent every race condition; appropriate locking, constraints, or concurrency-control strategies may also be required.

**Interview takeaway:** Thread-safe collections do not automatically guarantee business-level consistency.