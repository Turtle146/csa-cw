package com.hisham.sensorapi.exception;

/**
 * Thrown when a client tries to create a sensor with a roomId that doesn't exist.
 *
 * We map this to HTTP 422 (Unprocessable Entity) rather than 404 (Not Found).
 * The reason: 404 means "the URL you requested doesn't exist". But here, the
 * URL /sensors is perfectly valid - the problem is that the data inside the
 * request body references a room that doesn't exist. The request was understood,
 * but couldn't be processed due to a broken reference. That's exactly what 422 means.
 */
public class LinkedResourceNotFoundException extends RuntimeException {
    private final String resourceType;
    private final String resourceId;

    public LinkedResourceNotFoundException(String resourceType, String resourceId) {
        super(resourceType + " with ID '" + resourceId + "' does not exist.");
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
}
