package com.hisham.sensorapi.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The discovery endpoint - GET /api/v1
 *
 * This is the "front door" of the API. When a client hits this endpoint,
 * they get back a JSON document that describes everything the API can do:
 * version info, who built it, and links to all available resources.
 *
 * This is a core principle of HATEOAS (Hypermedia As The Engine Of Application State)
 * - the idea that the API should be self-documenting. Clients don't need to rely on
 * external docs; they can discover everything from this single entry point.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("name", "Room Sensor Management API");
        response.put("version", "1.0");
        response.put("description", "REST API for managing rooms and their IoT sensors.");
        response.put("author", "Hisham");

        // Resource map - tells clients where everything lives
        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms",    "/api/v1/rooms");
        resources.put("sensors",  "/api/v1/sensors");
        resources.put("readings", "/api/v1/sensors/{sensorId}/readings");
        response.put("resources", resources);

        // Links section (HATEOAS)
        Map<String, String> links = new LinkedHashMap<>();
        links.put("self",    "/api/v1");
        links.put("rooms",   "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        response.put("links", links);

        return Response.ok(response).build();
    }
}
