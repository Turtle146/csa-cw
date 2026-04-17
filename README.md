# Smart Campus Sensor & Room Management API

A JAX-RS REST API built with Jersey and Maven for managing rooms and sensors across a Smart Campus.

---

## How to Build and Run

### Prerequisites
- Java 11+
- Maven 3.6+
- Apache Tomcat 10+

### Steps

1. Clone the repository
2. Open a terminal in the project root (where `pom.xml` is)
3. Build the WAR file:
   ```
   mvn clean package
   ```
4. Deploy `target/sensor-api.war` to Tomcat's `webapps/` folder
5. Start Tomcat:
   ```
   $TOMCAT_HOME/bin/startup.sh
   ```
6. The API is available at: `http://localhost:8080/api/v1`

---

## Sample curl Commands

### 1. Discovery endpoint
```bash
curl http://localhost:8080/api/v1
```

### 2. Create a room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}'
```

### 3. Create a sensor (linked to the room above)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"LIB-301"}'
```

### 4. Filter sensors by type
```bash
curl "http://localhost:8080/api/v1/sensors?type=Temperature"
```

### 5. Post a reading to a sensor
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":22.5}'
```

### 6. Get reading history for a sensor
```bash
curl http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

### 7. Try to delete a room that has sensors (expect 409)
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

### 8. Try to create a sensor with a non-existent roomId (expect 422)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-999","type":"CO2","roomId":"FAKE-999"}'
```

---

## Report: Answers to Coursework Questions

### Part 1.1 — JAX-RS Resource Lifecycle

By default, JAX-RS creates a **new instance** of each resource class for every incoming HTTP request (request-scoped). This means no instance variables are shared between requests, which is naturally thread-safe for instance-level data.

However, because this API uses a shared in-memory `DataStore` singleton, thread safety must be handled explicitly. If two requests arrive simultaneously and both try to write to a plain `HashMap`, the internal state of the map can become corrupted. To prevent this, the `DataStore` uses `ConcurrentHashMap`, which allows safe concurrent reads and writes by locking only the affected segment of the map rather than the entire structure. This provides both safety and performance under concurrent load.

---

### Part 1.2 — HATEOAS

HATEOAS (Hypermedia As The Engine Of Application State) means that API responses include links to related resources, allowing clients to discover and navigate the API dynamically rather than relying on hardcoded URLs.

The benefit over static documentation is significant: if an endpoint URL changes in a future version, clients that follow links rather than hardcoding paths adapt automatically. New developers can also explore the API from a single entry point (`GET /api/v1`) without needing to read a separate document. Static documentation goes out of date and creates a disconnect between what the API does and what the docs say — hypermedia eliminates this problem.

---

### Part 2.1 — ID-only vs Full Object in List Responses

When returning a list of rooms, there are two approaches: return only the IDs (requiring clients to make additional GET requests for details), or return the full room objects in the list response.

Returning only IDs minimises the initial payload size but forces clients to make N additional requests for N rooms — increasing total latency, especially on slow connections. Returning full objects increases the initial response size but eliminates round trips. For small objects like rooms, returning full objects is the better trade-off. For very large objects with many nested fields, returning IDs and a link to the detail endpoint would reduce unnecessary data transfer.

---

### Part 2.2 — Idempotency of DELETE

Yes, DELETE is idempotent in this implementation. Idempotency means that making the same request multiple times produces the same server state as making it once.

- **First DELETE /rooms/LIB-301**: Room exists with no sensors → deleted → server returns 204 No Content.
- **Second DELETE /rooms/LIB-301**: Room no longer exists → server returns 404 Not Found.

The server state is identical after both calls (the room does not exist). The HTTP status code differs, but the resource state does not — this is correct and consistent with the HTTP specification.

---

### Part 3.1 — @Consumes and Content-Type Mismatches

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX-RS that the POST method only accepts requests where the `Content-Type` header is `application/json`.

If a client sends data with a different Content-Type (e.g. `text/plain` or `application/xml`), JAX-RS intercepts the request **before it reaches the resource method** and automatically returns an **HTTP 415 Unsupported Media Type** response. No custom code is needed — the framework enforces this constraint entirely. This prevents malformed or unexpected data formats from reaching the application logic.

---

### Part 3.2 — @QueryParam vs Path Parameter for Filtering

| Query Parameter (`?type=CO2`) | Path Parameter (`/sensors/type/CO2`) |
|-------------------------------|--------------------------------------|
| Optional — endpoint works without it | Required — path is incomplete without it |
| Designed for filtering/searching collections | Designed for identifying a specific resource |
| Multiple filters can be combined: `?type=CO2&status=ACTIVE` | Cannot easily represent multiple filter conditions |
| Semantically correct for search | Implies CO2 is a resource, not a filter value |

Query parameters are the correct approach for filtering because we are narrowing a collection, not navigating to a uniquely identified sub-resource. A path parameter like `/sensors/CO2` would incorrectly imply that CO2 is the identifier of a specific sensor.

---

### Part 4.1 — Sub-Resource Locator Pattern

The sub-resource locator pattern allows a resource method to delegate request handling to a separate class rather than handling it directly. The method has no HTTP verb annotation (@GET, @POST) — only @Path. JAX-RS detects this and passes the request to the returned object.

Without this pattern, all reading-related logic (GET history, POST reading, update currentValue) would be crammed into `SensorResource`. As the API grows, a single class handling too many responsibilities becomes difficult to read, test, and maintain. Delegating to `SensorReadingResource` means each class has one clear job, following the Single Responsibility Principle. New reading-related features can be added without touching sensor logic at all.

---

### Part 4.2 — Historical Data Management

`SensorReadingResource` supports GET (fetch all readings) and POST (record a new reading). When a POST is successful, the API performs a side effect: it updates the parent sensor's `currentValue` to match the new reading's value and saves it back to the `DataStore`. This ensures that subsequent calls to `GET /sensors/{id}` always reflect the latest measurement without the client needing to update it separately.

---

### Part 5.2 — Why 422 over 404 for Missing References

HTTP 404 means the requested URL does not exist. But when a client POSTs a sensor with a non-existent `roomId`, the URL (`/api/v1/sensors`) is perfectly valid — the problem is inside the request body.

HTTP 422 Unprocessable Entity is defined for exactly this scenario: the server understands the request, the Content-Type is correct, the JSON is valid — but the content contains a semantic error (a reference to a resource that doesn't exist) that prevents processing. Using 422 is more semantically precise and gives client developers a clearer signal about what went wrong and where to look.

---

### Part 5.4 — Cybersecurity Risks of Exposing Stack Traces

Exposing raw Java stack traces to API consumers is a serious security vulnerability. A stack trace reveals:

- **Internal package structure** (e.g. `com.hisham.sensorapi.resource.SensorResource`) — attackers learn how the application is organised.
- **Library names and versions** (e.g. Jersey 3.1.3) — these can be cross-referenced against public CVE databases to find known exploits.
- **Exact line numbers** where errors occurred — helps attackers understand how to craft inputs that trigger exploitable behaviour.
- **Internal error messages** — may reveal database query fragments, file paths, or configuration details.

The global `ExceptionMapper<Throwable>` prevents all of this by logging full details server-side (for developers) while returning only a generic message to the client.

---

### Part 5.5 — JAX-RS Filters vs Manual Logging

Manually inserting `Logger.info()` calls inside every resource method is error-prone: if a new endpoint is added and the developer forgets to add logging, that endpoint goes unmonitored. If the log format needs to change, every method must be updated.

A JAX-RS filter annotated with `@Provider` is automatically applied to **every request and response** across the entire application, including endpoints added in the future. Logging is guaranteed to be consistent and complete with zero extra effort from resource class developers. It also cleanly separates concerns — resource classes focus on business logic, the filter handles observability — making the codebase easier to maintain.
