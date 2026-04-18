package com.hisham.sensorapi.model;

import java.util.UUID;


public class SensorReading {

    private String id;        // every reading has an auto generated id like "c3b1a2d4-..."
    private long timestamp;   //  stores a timestamp in ms for every reading
    private double value;     //  the actual measured value

    // used so Jackson can turn incoming JSON into a SensorReading object
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
