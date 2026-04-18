package com.hisham.sensorapi.data;

import com.hisham.sensorapi.model.Room;
import com.hisham.sensorapi.model.Sensor;

import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    private static final DataStore INSTANCE = new DataStore(); // only one instance exists across the whole app

    private final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>(); // stores all rooms, keyed by their ID
    private final ConcurrentHashMap<String, Sensor> sensors = new ConcurrentHashMap<>(); // stores all sensors, keyed by their ID

    private DataStore() {} // private so nobody can create a second instance

    public static DataStore getInstance() { // how other classes get access to the store
        return INSTANCE;
    }

    public ConcurrentHashMap<String, Room> getRooms() {
        return rooms;
    }

    public ConcurrentHashMap<String, Sensor> getSensors() {
        return sensors;
    }
}