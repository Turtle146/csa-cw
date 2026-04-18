package com.hisham.sensorapi.resource;

import com.hisham.sensorapi.data.DataStore;
import com.hisham.sensorapi.exception.SensorUnavailableException;
import com.hisham.sensorapi.model.Sensor;
import com.hisham.sensorapi.model.SensorReading;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// handles all reading endpoints under /api/v1/sensors/{sensorId}/readings
// this class is not registered directly - it gets returned by SensorResource's sub-resource locator
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final Sensor sensor; // the sensor this reading belongs to
    private final DataStore store = DataStore.getInstance();

    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<SensorReading>> readingsStore
            = new ConcurrentHashMap<>(); // stores reading history per sensor ID

    public SensorReadingResource(Sensor sensor) {
        this.sensor = sensor;
    }

    @GET // GET /sensors/{id}/readings  this returns all readings for this sensor
    public Response getReadings() {
        List<SensorReading> history = readingsStore.getOrDefault(sensor.getId(), new CopyOnWriteArrayList<>());
        return Response.ok(history).build();
    }

    @POST // POST /sensors/{id}/readings this records a new reading
    public Response addReading(SensorReading incoming) {
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensor.getId()); // triggers 403 Forbidden
        }

        if (incoming == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Reading body with a value field is required."))
                    .build();
        }

        SensorReading newReading = new SensorReading(incoming.getValue()); // auto generates the id and timestamp

        readingsStore.computeIfAbsent(sensor.getId(), k -> new CopyOnWriteArrayList<>())
                .add(newReading); // adds to this sensors reading history

        // update the parent sensors current value to reflect the latest reading
        sensor.setCurrentValue(incoming.getValue());
        store.getSensors().put(sensor.getId(), sensor);

        URI location = UriBuilder.fromUri("/api/v1/sensors/{sensorId}/readings/{readingId}")
                .build(sensor.getId(), newReading.getId());

        return Response.created(location).entity(newReading).build(); // 201 Created
    }
}