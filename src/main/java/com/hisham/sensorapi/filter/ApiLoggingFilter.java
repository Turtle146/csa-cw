package com.hisham.sensorapi.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * This filter runs on EVERY request and response that passes through the API.
 *
 * It implements two interfaces:
 *  - ContainerRequestFilter  → runs BEFORE the request reaches our resource method
 *  - ContainerResponseFilter → runs AFTER our resource method has produced a response
 *
 * This is much better than manually adding Logger.info() calls inside each resource class
 * because it's centralised - one place, logs everything, no duplication.
 *
 * We log:
 *  - The HTTP method (GET, POST, DELETE, etc.)
 *  - The URI that was requested
 *  - The final HTTP status code returned
 */
@Provider
public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger logger = Logger.getLogger(ApiLoggingFilter.class.getName());

    // A key used to store the start time in the request context, so we can calculate duration
    private static final String START_TIME_KEY = "requestStartTime";

    /**
     * Runs before the request is handled.
     * We log the method + URI and note the start time.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();

        // Save the start time so we can calculate how long the request took
        requestContext.setProperty(START_TIME_KEY, System.currentTimeMillis());

        logger.info(String.format("--> Incoming Request | Method: %s | URI: %s", method, uri));
    }

    /**
     * Runs after the response is ready, but before it's sent to the client.
     * We log the final HTTP status code.
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();
        int status = responseContext.getStatus();

        // Calculate how long the request took to process
        Long startTime = (Long) requestContext.getProperty(START_TIME_KEY);
        long duration = startTime != null ? System.currentTimeMillis() - startTime : -1;

        logger.info(String.format("<-- Outgoing Response | Method: %s | URI: %s | Status: %d | Duration: %dms",
                method, uri, status, duration));
    }
}
