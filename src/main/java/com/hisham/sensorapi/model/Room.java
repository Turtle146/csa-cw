package com.hisham.sensorapi.model;

import java.util.ArrayList;
import java.util.List;



public class Room {

    private String id;           //  the id of the room "LIB-301"
    private String name;         // the rooms name "Library Quiet Study"
    private int capacity;        // the max number of people
    private List<String> sensorIds = new ArrayList<>(); // array of the sensors in the room

    // used so Jackson can turn incoming JSON into a Sensor object
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
