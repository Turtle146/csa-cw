package com.hisham.sensorapi.resource;

import com.hisham.sensorapi.data.DataStore;
import com.hisham.sensorapi.exception.RoomNotEmptyException;
import com.hisham.sensorapi.model.Room;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles all room-related endpoints:
 *
 *   GET    /api/v1/rooms          → list all rooms
 *   POST   /api/v1/rooms          → create a new room
 *   GET    /api/v1/rooms/{id}     → get a specific room by ID
 *   DELETE /api/v1/rooms/{id}     → delete a room (fails if it has sensors)
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    // GET /rooms - return all rooms as a list
    @GET
    public Response getAllRooms() {
        List<Room> roomList = new ArrayList<>(store.getRooms().values());
        return Response.ok(roomList).build();
    }

    // POST /rooms - create a new room
    @POST
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Room id is required (e.g. LIB-301)."))
                    .build();
        }

        if (store.getRooms().containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "A room with ID " + room.getId() + " already exists."))
                    .build();
        }

        store.getRooms().put(room.getId(), room);

        URI location = UriBuilder.fromUri("/api/v1/rooms/{id}").build(room.getId());

        return Response.created(location)
                .entity(room)
                .build();
    }

    // GET /rooms/{id} - get a specific room
    @GET
    @Path("/{id}")
    public Response getRoomById(@PathParam("id") String id) {
        Room room = store.getRooms().get(id);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Room with ID " + id + " not found."))
                    .build();
        }

        return Response.ok(room).build();
    }

    // DELETE /rooms/{id} - delete a room, only if it has no sensors
    @DELETE
    @Path("/{id}")
    public Response deleteRoom(@PathParam("id") String id) {
        Room room = store.getRooms().get(id);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Room with ID " + id + " not found."))
                    .build();
        }

        // Check if any sensors are still assigned to this room
        boolean hasSensors = store.getSensors().values().stream()
                .anyMatch(sensor -> id.equals(sensor.getRoomId()));

        if (hasSensors) {
            throw new RoomNotEmptyException(id);
        }

        store.getRooms().remove(id);

        return Response.noContent().build(); // 204 No Content
    }
}
