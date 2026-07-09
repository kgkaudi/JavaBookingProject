package com.kostas.bookingproject.services;

import com.kostas.bookingproject.models.Room;
import com.kostas.bookingproject.repositories.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    // ---------------------------------------------------------
    // CREATE ROOM
    // ---------------------------------------------------------
    public Room createRoom(Room room) {
        if (room.getRoomNumber() <= 0) {
            throw new IllegalArgumentException("Room number must be positive");
        }
        return roomRepository.save(room);
    }

    // ---------------------------------------------------------
    // UPDATE ROOM
    // ---------------------------------------------------------
    public Room updateRoom(String roomId, Room updatedRoom) {

        Room existing = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        existing.setRoomNumber(updatedRoom.getRoomNumber());
        existing.setType(updatedRoom.getType());
        existing.setCapacity(updatedRoom.getCapacity()); // ⭐ FIXED
        existing.setPrice(updatedRoom.getPrice());
        existing.setAvailable(updatedRoom.isAvailable());

        return roomRepository.save(existing);
    }

    // ---------------------------------------------------------
    // DELETE ROOM
    // ---------------------------------------------------------
    public void deleteRoom(String roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new IllegalArgumentException("Room not found");
        }
        roomRepository.deleteById(roomId);
    }

    // ---------------------------------------------------------
    // GET ALL ROOMS
    // ---------------------------------------------------------
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    // ---------------------------------------------------------
    // GET ROOM BY ID
    // ---------------------------------------------------------
    public Room getRoomById(String roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
    }

    // ---------------------------------------------------------
    // SEARCH: AVAILABLE / UNAVAILABLE
    // ---------------------------------------------------------
    public List<Room> getRoomsByAvailability(boolean available) {
        return roomRepository.findByAvailable(available);
    }

    // ---------------------------------------------------------
    // SEARCH: TYPE
    // ---------------------------------------------------------
    public List<Room> getRoomsByType(String type) {
        return roomRepository.findByType(type);
    }

    // ---------------------------------------------------------
    // SEARCH: PRICE RANGE
    // ---------------------------------------------------------
    public List<Room> getRoomsByPriceRange(double min, double max) {
        if (min < 0 || max < 0 || max < min) {
            throw new IllegalArgumentException("Invalid price range");
        }
        return roomRepository.findByPriceBetween(min, max);
    }
}
