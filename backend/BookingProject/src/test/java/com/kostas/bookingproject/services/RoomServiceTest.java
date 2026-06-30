package com.kostas.bookingproject.services;

import com.kostas.bookingproject.models.Room;
import com.kostas.bookingproject.repositories.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomServiceTest {

    private RoomRepository roomRepository;
    private RoomService roomService;

    @BeforeEach
    void setup() {
        roomRepository = mock(RoomRepository.class);
        roomService = new RoomService(roomRepository);
    }

    // ---------------------------------------------------------
    // CREATE ROOM
    // ---------------------------------------------------------

    @Test
    void createRoom_success() {
        Room room = new Room();
        room.setRoomNumber(101);

        when(roomRepository.save(room)).thenReturn(room);

        Room result = roomService.createRoom(room);

        assertEquals(101, result.getRoomNumber());
    }

    @Test
    void createRoom_invalidRoomNumber_zero() {
        Room room = new Room();
        room.setRoomNumber(0);

        assertThrows(IllegalArgumentException.class,
                () -> roomService.createRoom(room));
    }

    @Test
    void createRoom_invalidRoomNumber_negative() {
        Room room = new Room();
        room.setRoomNumber(-5);

        assertThrows(IllegalArgumentException.class,
                () -> roomService.createRoom(room));
    }

    // ---------------------------------------------------------
    // UPDATE ROOM
    // ---------------------------------------------------------

    @Test
    void updateRoom_success() {
        Room existing = new Room();
        existing.setId("r1");

        Room updated = new Room();
        updated.setRoomNumber(202);
        updated.setType("double");
        updated.setPrice(150);
        updated.setAvailable(false);

        when(roomRepository.findById("r1")).thenReturn(Optional.of(existing));
        when(roomRepository.save(existing)).thenReturn(existing);

        Room result = roomService.updateRoom("r1", updated);

        assertEquals(202, result.getRoomNumber());
        assertEquals("double", result.getType());
        assertEquals(150, result.getPrice());
        assertFalse(result.isAvailable());
    }

    @Test
    void updateRoom_notFound() {
        when(roomRepository.findById("r1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> roomService.updateRoom("r1", new Room()));
    }

    // ---------------------------------------------------------
    // GET ALL ROOMS
    // ---------------------------------------------------------

    @Test
    void getAllRooms_success() {
        List<Room> rooms = List.of(new Room(), new Room());

        when(roomRepository.findAll()).thenReturn(rooms);

        List<Room> result = roomService.getAllRooms();

        assertEquals(2, result.size());
    }

    // ---------------------------------------------------------
    // GET ROOM BY ID
    // ---------------------------------------------------------

    @Test
    void getRoomById_success() {
        Room room = new Room();
        room.setId("r1");

        when(roomRepository.findById("r1")).thenReturn(Optional.of(room));

        Room result = roomService.getRoomById("r1");

        assertEquals("r1", result.getId());
    }

    @Test
    void getRoomById_notFound() {
        when(roomRepository.findById("r1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> roomService.getRoomById("r1"));
    }

    // ---------------------------------------------------------
    // SEARCH BY AVAILABILITY
    // ---------------------------------------------------------

    @Test
    void getRoomsByAvailability_success() {
        List<Room> rooms = List.of(new Room(), new Room());

        when(roomRepository.findByAvailable(true)).thenReturn(rooms);

        List<Room> result = roomService.getRoomsByAvailability(true);

        assertEquals(2, result.size());
    }

    @Test
    void getRoomsByAvailability_emptyList() {
        when(roomRepository.findByAvailable(false)).thenReturn(List.of());

        List<Room> result = roomService.getRoomsByAvailability(false);

        assertTrue(result.isEmpty());
    }

    // ---------------------------------------------------------
    // SEARCH BY TYPE
    // ---------------------------------------------------------

    @Test
    void getRoomsByType_success() {
        List<Room> rooms = List.of(new Room());

        when(roomRepository.findByType("single")).thenReturn(rooms);

        List<Room> result = roomService.getRoomsByType("single");

        assertEquals(1, result.size());
    }

    @Test
    void getRoomsByType_emptyList() {
        when(roomRepository.findByType("suite")).thenReturn(List.of());

        List<Room> result = roomService.getRoomsByType("suite");

        assertTrue(result.isEmpty());
    }

    // ---------------------------------------------------------
    // SEARCH BY PRICE RANGE
    // ---------------------------------------------------------

    @Test
    void getRoomsByPriceRange_success() {
        List<Room> rooms = List.of(new Room());

        when(roomRepository.findByPriceBetween(50, 100)).thenReturn(rooms);

        List<Room> result = roomService.getRoomsByPriceRange(50, 100);

        assertEquals(1, result.size());
    }

    @Test
    void getRoomsByPriceRange_invalid_negativeMin() {
        assertThrows(IllegalArgumentException.class,
                () -> roomService.getRoomsByPriceRange(-10, 100));
    }

    @Test
    void getRoomsByPriceRange_invalid_negativeMax() {
        assertThrows(IllegalArgumentException.class,
                () -> roomService.getRoomsByPriceRange(10, -100));
    }

    @Test
    void getRoomsByPriceRange_invalid_minGreaterThanMax() {
        assertThrows(IllegalArgumentException.class,
                () -> roomService.getRoomsByPriceRange(200, 100));
    }

    @Test
    void getRoomsByPriceRange_emptyList() {
        when(roomRepository.findByPriceBetween(10, 20)).thenReturn(List.of());

        List<Room> result = roomService.getRoomsByPriceRange(10, 20);

        assertTrue(result.isEmpty());
    }
}
