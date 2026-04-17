package com.hisham.sensorapi.model;

/**
 * Represents a sensor deployed in a Smart Campus room.
 *
 * Sensors have a String-based ID (e.g. "TEMP-001"), a type (e.g. "Temperature",
 * "CO2", "Occupancy"), a status (ACTIVE, MAINTENANCE, or OFFLINE), a currentValue
 * holding the most recent measurement, and a roomId linking it to its room.
 *
 * If status is "MAINTENANCE", new readings cannot be posted (403 Forbidden).
 */
public class Sensor {

    private String id;           // e.g. "TEMP-001"
    private String type;         // e.g. "Temperature", "CO2", "Occupancy"
    private String status;       // "ACTIVE", "MAINTENANCE", or "OFFLINE"
    private double currentValue; // Most recent measurement
    private String roomId;       // Foreign key linking to the Room

    public Sensor() {}

    public Sensor(String id, String type, String status, double currentValue, String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.currentValue = currentValue;
        this.roomId = roomId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
}
