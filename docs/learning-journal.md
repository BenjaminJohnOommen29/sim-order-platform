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