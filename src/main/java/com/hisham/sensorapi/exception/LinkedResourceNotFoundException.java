package com.hisham.sensorapi.exception;

// Thrown when a sensor is created with a roomId that doesn't exist in the system
// Mapped to 422 rather than 404 - the URL is valid, but the referenced resource isn't
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