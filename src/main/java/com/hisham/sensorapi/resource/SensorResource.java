package com.hisham.sensorapi.resource;

import com.hisham.sensorapi.data.DataStore;
import com.hisham.sensorapi.exception.LinkedResourceNotFoundException;
import com.hisham.sensorapi.model.Room;
import com.hisham.sensorapi.model.Sensor;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles all sensor-related endpoints:
 *
 *   GET    /api/v1/sensors              → list all sensors (optional ?type= filter)
 *   POST   /api/v1/sensors              → register a new sensor
 *   GET    /api/v1/sensors/{id}         → get a specific sensor
 *   PATH   /api/v1/sensors/{id}/readings → sub-resource locator
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    /**
     * GET /sensors or GET /sensors?type=CO2
     *
     * Query parameter filtering is the right approach here because we are
     * filtering a collection, not navigating to a specific named resource.
     */
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> result = store.getSensors().values().stream()
                .filter(s -> type == null || s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());

        return Response.ok(result).build();
    }

    // POST /sensors - register a new sensor, validating that the room exists first
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Sensor id is required (e.g. TEMP-001)."))
                    .build();
        }

        // Validate that the referenced roomId actually exists
        if (!store.getRooms().containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("Room", sensor.getRoomId());
        }

        // Default status to ACTIVE if not provided
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }

        store.getSensors().put(sensor.getId(), sensor);

        // Also add this sensor's ID to the room's sensorIds list
        Room room = store.getRooms().get(sensor.getRoomId());
        if (!room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }

        URI location = UriBuilder.fromUri("/api/v1/sensors/{id}").build(sensor.getId());

        return Response.created(location)
                .entity(sensor)
                .build();
    }

    // GET /sensors/{id}
    @GET
    @Path("/{id}")
    public Response getSensorById(@PathParam("id") String id) {
        Sensor sensor = store.getSensors().get(id);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Sensor with ID " + id + " not found."))
                    .build();
        }

        return Response.ok(sensor).build();
    }

    /**
     * Sub-resource locator for /sensors/{id}/readings
     *
     * No HTTP method annotation here - JAX-RS sees this and knows to delegate
     * the request to whatever object this method returns (SensorReadingResource).
     */
    @Path("/{id}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("id") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);

        if (sensor == null) {
            throw new NotFoundException("Sensor with ID " + sensorId + " not found.");
        }

        return new SensorReadingResource(sensor);
    }
}
