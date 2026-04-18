package com.hisham.sensorapi.exception;

// Thrown when attempting to delete a room that still has sensors assigned to it
public class RoomNotEmptyException extends RuntimeException {
    private final String roomId;

    public RoomNotEmptyException(String roomId) {
        super("Room " + roomId + " still has sensors assigned to it.");
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }
}