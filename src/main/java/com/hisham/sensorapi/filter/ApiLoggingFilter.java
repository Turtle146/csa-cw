package com.hisham.sensorapi.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.logging.Logger;

// Logs every request and response - cleaner than adding logger calls to each resource method
@Provider
public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger logger = Logger.getLogger(ApiLoggingFilter.class.getName());
    private static final String START_TIME_KEY = "requestStartTime";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        requestContext.setProperty(START_TIME_KEY, System.currentTimeMillis());
        logger.info(String.format("--> %s %s",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri()));
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        Long startTime = (Long) requestContext.getProperty(START_TIME_KEY);
        long duration = startTime != null ? System.currentTimeMillis() - startTime : -1;

        logger.info(String.format("<-- %s %s | %d | %dms",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri(),
                responseContext.getStatus(),
                duration));
    }
}