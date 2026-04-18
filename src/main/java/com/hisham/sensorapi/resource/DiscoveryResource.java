package com.hisham.sensorapi.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/")  // maps to GET /api/v1
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("name", "Room Sensor Management API");
        response.put("version", "1.0");
        response.put("description", "REST API for managing rooms and their IoT sensors.");
        response.put("author", "Hisham");

        // Resource map this tells clients where everything lives
        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms",    "/api/v1/rooms");
        resources.put("sensors",  "/api/v1/sensors");
        resources.put("readings", "/api/v1/sensors/{sensorId}/readings");
        response.put("resources", resources);

        // HATEOAS Links section
        Map<String, String> links = new LinkedHashMap<>();
        links.put("self",    "/api/v1");
        links.put("rooms",   "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        response.put("links", links);

        return Response.ok(response).build();
    }
}
