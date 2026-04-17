package com.hisham.sensorapi.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a physical room in the Smart Campus.
 *
 * Rooms have a String-based ID (e.g. "LIB-301"), a human-readable name,
 * a maximum capacity for safety regulations, and a list of sensor IDs
 * representing the sensors deployed in that room.
 */
public class Room {

    private String id;           // e.g. "LIB-301"
    private String name;         // e.g. "Library Quiet Study"
    private int capacity;        // Maximum occupancy
    private List<String> sensorIds = new ArrayList<>(); // IDs of sensors in this room

    // Jackson needs a no-argument constructor to deserialise incoming JSON
    public Room() {}

    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public List<String> getSensorIds() { return sensorIds; }
    public void setSensorIds(List<String> sensorIds) { this.sensorIds = sensorIds; }
}
