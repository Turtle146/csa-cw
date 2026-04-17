package com.hisham.sensorapi.exception;

/**
 * Thrown when someone tries to delete a room that still has sensors in it.
 * This gets caught by RoomNotEmptyExceptionMapper and returns a 409 Conflict.
 */
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
