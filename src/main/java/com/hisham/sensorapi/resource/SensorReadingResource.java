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

/**
 * Sub-resource for managing readings under a specific sensor.
 * Handles: /api/v1/sensors/{sensorId}/readings
 *
 * This class is not annotated with @Path at the class level - it is
 * instantiated and returned by SensorResource's sub-resource locator method.
 * JAX-RS then calls the appropriate @GET or @POST method on this object.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final Sensor sensor;
    private final DataStore store = DataStore.getInstance();

    // Reading history stored per sensor ID - shared across all instances
    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<SensorReading>> readingsStore
            = new ConcurrentHashMap<>();

    public SensorReadingResource(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * GET /sensors/{id}/readings
     * Returns the full reading history for this sensor.
     */
    @GET
    public Response getReadings() {
        List<SensorReading> history = readingsStore.getOrDefault(sensor.getId(), new CopyOnWriteArrayList<>());
        return Response.ok(history).build();
    }

    /**
     * POST /sensors/{id}/readings
     * Records a new reading for this sensor.
     *
     * Blocked if sensor status is MAINTENANCE (throws 403).
     * On success, also updates the parent sensor's currentValue.
     */
    @POST
    public Response addReading(SensorReading incoming) {
        // Block readings for sensors under maintenance
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensor.getId());
        }

        if (incoming == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Reading body with a value field is required."))
                    .build();
        }

        // Create the persisted reading with auto-generated UUID and timestamp
        SensorReading newReading = new SensorReading(incoming.getValue());

        // Store it in the readings history
        readingsStore.computeIfAbsent(sensor.getId(), k -> new CopyOnWriteArrayList<>())
                     .add(newReading);

        // Update the parent sensor's currentValue - key side effect!
        sensor.setCurrentValue(incoming.getValue());
        store.getSensors().put(sensor.getId(), sensor);

        URI location = UriBuilder.fromUri("/api/v1/sensors/{sensorId}/readings/{readingId}")
                .build(sensor.getId(), newReading.getId());

        return Response.created(location)
                .entity(newReading)
                .build();
    }
}
