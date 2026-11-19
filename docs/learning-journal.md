## Java Record
- Similar to a Kotlin data class with `val` properties.
- Generates constructor, accessors, equals(), hashCode(), and toString().
- Record fields are final; no generated copy() method.
- Example: `record CreateOrderRequest(String customerId) {}`

## @RequestBody
- Reads the HTTP request body and converts JSON into a Java object using an HTTP message converter.
- Example: `createOrder(@RequestBody CreateOrderRequest request)`
- Different from @RequestParam, which reads request parameters.

## @NotBlank and @Valid
- @NotBlank rejects null, empty, and whitespace-only strings.
- @Valid triggers Bean Validation for the incoming DTO.
- Invalid requests typically return HTTP 400.

## @RestController and @PostMapping
- @RestController handles HTTP requests and returns response data.
- @PostMapping maps an HTTP POST request to a method.
- @RequestMapping('/api/orders') defines the base URL.