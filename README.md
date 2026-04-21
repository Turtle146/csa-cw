# Smart Campus Sensor & Room Management API

This is my JAX-RS REST API built with Jersey and Maven for managing rooms and sensors across a Smart Campus.

---

## How to Build and Run

### Prerequisites
- Java 11+
- Maven 3.6+
- Apache Tomcat 10+

### Steps

1. Make a copy of the repository
2. Open a terminal in the project root its where `pom.xml` is
3. Build the WAR file:
   ```
   mvn clean package
   ```
4. Deploy `target/sensor-api.war` to Tomcat's `webapps/` folder
5. Start Tomcat:
   ```
   $TOMCAT_HOME/bin/startup.sh
   ```
6. The API is available at: `http://localhost:8080/sensor-api/api/v1`

---

## curl Commands

### 1. Discovery endpoint
```bash
   curl http://localhost:8080/sensor-api/api/v1
```

### 2. How to create a room
```bash
    curl -X POST http://localhost:8080/sensor-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}'
```

### 3. Create a sensor this will link to the room above
```bash
   curl -X POST http://localhost:8080/sensor-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"LIB-301"}'
```

### 4. How to filter sensors by type
```bash
   curl "http://localhost:8080/sensor-api/api/v1/sensors?type=Temperature"
```

### 5. How to POST a reading to a sensor
```bash
   curl -X POST http://localhost:8080/sensor-api/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":22.5}'
```

### 6. How to GET reading history for a sensor
```bash
    curl http://localhost:8080/sensor-api/api/v1/sensors/TEMP-001/readings
```

### 7. Try to delete a room that has sensors response will be 409
```bash
   curl -X DELETE http://localhost:8080/sensor-api/api/v1/rooms/LIB-301
```

### 8. Try to create a sensor with a non-existent roomId response will be 422
```bash
   curl -X POST http://localhost:8080/sensor-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-999","type":"CO2","roomId":"FAKE-999"}'
```

---

## Report

### Part 1.1  JAX-RS Resource Lifecycle

By default JAX-RS creates a new instance of each resource class per request which means instance variables are not shared between requests, this was significant because it meant each request was naturally isolated from others.
However the harder part was the shared DataStore singleton. The issue I was having was that two requests hitting at the same time both writing to a normal HashMap can corrupt the data because HashMap isn't built for that kind of thing. so I used ConcurrentHashMap instead which handles concurrent writes without locking the whole map at once, this meant the data would stay consistent.


### Part 1.2   HATEOAS

HATEOAS is a principle that means the API includes metadata like line and names so clients can navigate it without needing to know all the URLs beforehand. a benefit is that if URLs change later clients following links will still work. Static documentation goes out of date pretty quickly and you can't always guarantee it matches what the API is actually doing. The discovery endpoint at GET /api/v1 gives you a starting point that finds everything else. I found this useful when I was testing because I could just start there. Also clients are able to explore api from a single entry without the documentation 


### Part 2.1  ID-only vs Full Object in List Responses

I went with returning full objects in the list instead of just IDs. Returning only IDs meant the client had to make a separate request for each room which felt unnecessary especially for something small therefore I picked full object in the list to improve efficiency. But  If the objects were much bigger with loads of fields I would probably reconsider it but for rooms it made more sense to just send everything back at once.


### Part 2.2  Idempotency of DELETE

Idempotency is concept of repeating the same request more than once which still gives the same result of requesting the first time. For DELETE is idempotent here. Because if you send the same request twice or more the end state is the same either way the room doesn't exist.
If the room is there it gets deleted and gives 204 response and if its already deleted you get 404 response. The response code is different but the actual result of the resource is the same which is what idempotency means, its not about getting the same response just that the server ends up in the same place.


### Part 3.1  @Consumes and Content-Type Mismatches

@Consumes(MediaType.APPLICATION_JSON) tells JAX-RS to only accept requests with application/json as the content type. Send the wrong one and you get 415 response back before it even reaches the resource method.
I kept running into this during testing because i kept forgetting to set the header in Postman and had no idea why everything kept failing. Once I worked out what was happening it made sense. Its actually better that JAX-RS handles this automatically because otherwise you'd have to manually check it in every single method which would get repetitive.


### Part 3.2   @QueryParam vs Path Parameter for Filtering

Query params made more sense for this than path params. Something like /sensors/CO2 makes it look like CO2 is the ID of a specific sensor which but whats happening is i was filtering a collection or array not looking up one specific thing. ?type=CO2 made it clearer what i was doing and its optional as well so if you leave it out you just get all sensors back.
Honestly I wasn't sure about this at first but after looking into it more query params are clearly the better approach for filtering. You can also combine them if needed which would get complicated with path segments.


###Part 4.1   Sub-Resource Locator Pattern

A sub-resource locator is basically a method that doesn't handle the request itself it just returns another object that will. It only has @Path on it with no @GET or @POST so JAX-RS knows to pass it on.
I used this for /sensors/{id}/readings so that SensorResource focuses on sensors and SensorReadingResource handles the readings side of things. Without it everything would end up in one class and it would get difficult to manage. This was probably the bit I found most interesting 


### Part 4.2   Historical Data Management

The way historical data management work is the GET function returns the history and the POST function adds a new reading. A side effect of POST function here is when a reading gets posted it also updates the parent sensors like currentValue automatically. For example if you post a reading of 22.5 currentValue will also automatically become the same, otherwise the sensor would have an outdated value which wouldn't make sense.

### Part 5.2  Why 422 over 404 for Missing References
A 404 response means  the URL doesn't exist but POST /api/v1/sensors is still a valid URL. A 422 response means the request has been understood but there is an issue with the content. The issue is the roomId in the body referencing a room that doesn't exist. 422 made more sense because the server understood the request and JSON was valid. But the problem was specifically with the content inside it. I think it gives a clearer idea about what went wrong compared to just sending back a 404 response. I think that’s why 422 responses are better for missing references.


### Part 5.4  Cybersecurity Risks of Exposing Stack Traces

A stack trace is an indepth error message that describes exactly what is happening inside the code this means if there was a stack trace information such as class names, method names and line numbers would be shown. This is a cybersecurity risk because when returning raw stack traces that will tell the attacker how the application is structured. For example, com.hisham.sensorapi.resource.SensorRessource as you can see it shows the structure.Furthermore, it also exposes which libraries and version is being used for example, Jersey 3.1.3. This is important because every library and version’s vulnerabilities can be easily searched up.Another risk is the line numbers being visible will also help the attack understand what part to target. Therefore the cybersecurity risks for Exposing Stack traces are big. However, global exception mapper handles this by just returning a generic message to the client and keeping the real error details in the server logs only.


### Part 5.5 — JAX-RS Filters vs Manual Logging

Adding Logger.info() manually to every method means you have to remember to do it every time you add something new and if you forget that endpoint just won't be logged. Using a JAX-RS filter with @Provider means it runs on everything automatically without having to think about it, which is much easier to maintain.





