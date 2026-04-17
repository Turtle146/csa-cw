package com.hisham.sensorapi.model;

import java.util.UUID;

/**
 * Represents a single reading recorded by a sensor.
 *
 * Each reading has a UUID-based ID, an epoch timestamp (milliseconds since
 * 1970) recording exactly when the reading was captured, and the actual
 * measured value.
 *
 * When a new reading is POSTed, the parent sensor's currentValue is
 * automatically updated to match.
 */
public class SensorReading {

    private String id;        // UUID e.g. "a3f1c2d4-..."
    private long timestamp;   // Epoch time in ms
    private double value;     // The measured value

    public SensorReading() {}

    public SensorReading(double value) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.value = value;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
