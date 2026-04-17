package com.hisham.sensorapi.application;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * This is the main entry point for the entire API.
 *
 * @ApplicationPath tells JAX-RS (Jersey) that all our endpoints
 * should live under /api/v1. So if a resource is mapped to /rooms,
 * the full URL becomes /api/v1/rooms.
 *
 * By extending Application and using this annotation, we don't need
 * a web.xml file at all - Jersey picks this up automatically.
 */
@ApplicationPath("/api/v1")
public class SensorApplication extends Application {
    // Nothing needed here - Jersey scans the classpath and finds
    // all our @Path annotated classes automatically.
}
