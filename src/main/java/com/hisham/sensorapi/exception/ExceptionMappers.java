package com.hisham.sensorapi.exception;

import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Exception Mappers translate Java exceptions into proper HTTP responses.
 * Each one is annotated with @Provider so Jersey discovers and registers them automatically.
 *
 * Without these, Jersey would return a raw 500 with a stack trace for any unhandled
 * exception - which is both confusing for clients and a security risk.
 */

// ─────────────────────────────────────────────
// 409 Conflict - Room still has sensors
// ─────────────────────────────────────────────
@Provider
class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 409);
        error.put("error", "Conflict");
        error.put("message", ex.getMessage());
        error.put("hint", "Remove or reassign all sensors in room '" + ex.getRoomId() + "' before deleting it.");

        return Response.status(Response.Status.CONFLICT)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

// ─────────────────────────────────────────────
// 422 Unprocessable Entity - Referenced room doesn't exist
// ─────────────────────────────────────────────
@Provider
class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 422);
        error.put("error", "Unprocessable Entity");
        error.put("message", ex.getMessage());
        error.put("hint", "Make sure the " + ex.getResourceType() + " with ID "
                + ex.getResourceId() + " exists before referencing it.");

        return Response.status(422)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

// ─────────────────────────────────────────────
// 403 Forbidden - Sensor is in maintenance
// ─────────────────────────────────────────────
@Provider
class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 403);
        error.put("error", "Forbidden");
        error.put("message", ex.getMessage());
        error.put("hint", "Set the sensor status to ACTIVE before posting readings.");

        return Response.status(Response.Status.FORBIDDEN)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

// ─────────────────────────────────────────────
// 500 Internal Server Error - Catch-all for anything unexpected
// ─────────────────────────────────────────────
@Provider
class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger logger = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        // Log the full details server-side so developers can debug
        logger.severe("Unhandled exception: " + ex.getClass().getName() + " - " + ex.getMessage());

        // But return NOTHING sensitive to the client - no stack traces, no class names.
        // Exposing stack traces is a serious security risk: attackers can learn about
        // your internal package structure, libraries used, and potential vulnerabilities.
        Map<String, Object> error = new HashMap<>();
        error.put("status", 500);
        error.put("error", "Internal Server Error");
        error.put("message", "Something went wrong on our end. Please try again later.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
