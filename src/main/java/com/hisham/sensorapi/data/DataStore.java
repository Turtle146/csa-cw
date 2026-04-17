package com.hisham.sensorapi.data;

import com.hisham.sensorapi.model.Room;
import com.hisham.sensorapi.model.Sensor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory data store for the entire application.
 *
 * Uses ConcurrentHashMap for thread safety - multiple requests can arrive
 * simultaneously and a regular HashMap would corrupt data under concurrent
 * writes. ConcurrentHashMap handles this safely without locking the whole map.
 *
 * IDs are now Strings (e.g. "LIB-301", "TEMP-001") supplied by the client,
 * so we no longer need AtomicInteger counters.
 *
 * This is a singleton - one shared instance across the whole application.
 */
public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Sensor> sensors = new ConcurrentHashMap<>();

    private DataStore() {}

    public static DataStore getInstance() {
        return INSTANCE;
    }

    public ConcurrentHashMap<String, Room> getRooms() {
        return rooms;
    }

    public ConcurrentHashMap<String, Sensor> getSensors() {
        return sensors;
    }
}
