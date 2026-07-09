package com.kostas.bookingproject.controllers;

import com.kostas.bookingproject.models.Room;
import com.kostas.bookingproject.services.RoomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public Room createRoom(@RequestBody Room room) {
        return roomService.createRoom(room);
    }

    @PutMapping("/{roomId}")
    public Room updateRoom(@PathVariable String roomId, @RequestBody Room room) {
        return roomService.updateRoom(roomId, room);
    }

    @DeleteMapping("/{roomId}")
    public void deleteRoom(@PathVariable String roomId) {
        roomService.deleteRoom(roomId);
    }

    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/{roomId}")
    public Room getRoomById(@PathVariable String roomId) {
        return roomService.getRoomById(roomId);
    }

    @GetMapping("/availability")
    public List<Room> getRoomsByAvailability(@RequestParam boolean available) {
        return roomService.getRoomsByAvailability(available);
    }

    @GetMapping("/type/{type}")
    public List<Room> getRoomsByType(@PathVariable String type) {
        return roomService.getRoomsByType(type);
    }

    @GetMapping("/price")
    public List<Room> getRoomsByPriceRange(
            @RequestParam double min,
            @RequestParam double max) {
        return roomService.getRoomsByPriceRange(min, max);
    }
}
