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

@Path("/rooms") // all endpoints in this class live under /api/v1/rooms
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance(); // access to our in-memory data

    @GET // GET /rooms - returns every room we have stored
    public Response getAllRooms() {
        List<Room> roomList = new ArrayList<>(store.getRooms().values());
        return Response.ok(roomList).build();
    }

    @POST // POST /rooms - creates a new room
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Room id is required (e.g. LIB-301)."))
                    .build();
        }

        if (store.getRooms().containsKey(room.getId())) { // stop duplicate ids
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "A room with ID " + room.getId() + " already exists."))
                    .build();
        }

        store.getRooms().put(room.getId(), room);

        URI location = UriBuilder.fromUri("/api/v1/rooms/{id}").build(room.getId()); // location header pointing to the new room
        return Response.created(location).entity(room).build(); // 201 Created
    }

    @GET
    @Path("/{id}") // GET /rooms/{id} this will fetch one specific room
    public Response getRoomById(@PathParam("id") String id) {
        Room room = store.getRooms().get(id);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Room with ID " + id + " not found."))
                    .build();
        }

        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{id}") // DELETE /rooms/{id} this will remove the room but only if it has no sensors
    public Response deleteRoom(@PathParam("id") String id) {
        Room room = store.getRooms().get(id);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Room with ID " + id + " not found."))
                    .build();
        }

        // blocks the deletion if any sensors are still linked to this room
        boolean hasSensors = store.getSensors().values().stream()
                .anyMatch(sensor -> id.equals(sensor.getRoomId()));

        if (hasSensors) {
            throw new RoomNotEmptyException(id); // triggers 409 Conflict
        }

        store.getRooms().remove(id);
        return Response.noContent().build(); // 204 No Content - deleted successfully
    }
}