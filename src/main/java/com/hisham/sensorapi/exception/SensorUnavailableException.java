package com.hisham.sensorapi.exception;

/**
 * Thrown when someone tries to post a reading to a sensor that is currently
 * in MAINTENANCE mode.
 *
 * We map this to HTTP 403 Forbidden - the client is authenticated and the
 * request is understood, but the server is refusing to allow it based on
 * the current state of the resource.
 */
public class SensorUnavailableException extends RuntimeException {
    private final String sensorId;

    public SensorUnavailableException(String sensorId) {
        super("Sensor " + sensorId + " is currently under maintenance and cannot accept readings.");
        this.sensorId = sensorId;
    }

    public String getSensorId() { return sensorId; }
}
