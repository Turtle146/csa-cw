package com.hisham.sensorapi.exception;

// Thrown when a reading is posted to a sensor in MAINTENANCE status
// Mapped to 403 Forbidden - the request is valid but refused based on sensor state
public class SensorUnavailableException extends RuntimeException {
    private final String sensorId;

    public SensorUnavailableException(String sensorId) {
        super("Sensor " + sensorId + " is currently under maintenance and cannot accept readings.");
        this.sensorId = sensorId;
    }

    public String getSensorId() { return sensorId; }
}