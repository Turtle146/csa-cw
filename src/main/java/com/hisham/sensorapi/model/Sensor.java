package com.hisham.sensorapi.model;


public class Sensor {

    private String id;           // the id of the sensor for example "TEMP-001"
    private String type;         // the type of sensor for example "Temperature", "CO2", "Occupancy"
    private String status;       //  lets you know if sensor is "ACTIVE", "MAINTENANCE", or "OFFLINE"
    private double currentValue; // most recent measurement
    private String roomId;       // foreign key linking to the Room

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
