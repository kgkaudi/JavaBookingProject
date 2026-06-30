package com.kostas.bookingproject.repositories;

import com.kostas.bookingproject.models.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findByUserId(String userId);

    List<Booking> findByRoomId(String roomId);

    boolean existsByUserId(String userId);
}
