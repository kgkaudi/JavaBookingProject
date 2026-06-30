package com.kostas.bookingproject.repositories;

import com.kostas.bookingproject.models.Room;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends MongoRepository<Room, String> {

    // Find all rooms that are available or unavailable
    List<Room> findByAvailable(boolean available);

    // Find rooms by type (e.g. "single", "double", "suite")
    List<Room> findByType(String type);

    // Find rooms by price range
    List<Room> findByPriceBetween(double min, double max);

    // Required for migrations and booking logic
    Optional<Room> findByRoomNumber(int roomNumber);
}
