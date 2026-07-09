package com.kostas.bookingproject.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

import com.kostas.bookingproject.config.MockMvcConfig;
import com.kostas.bookingproject.repositories.RoomRepository;
import com.kostas.bookingproject.models.Room;

@SpringBootTest
@Import(MockMvcConfig.class)
class RoomControllerIT {

    @Autowired MockMvc mvc;
    @Autowired RoomRepository rooms;
    @Autowired ObjectMapper mapper;

    @BeforeEach
    void setup() {
        rooms.deleteAll();
    }

    // ------------------------------------------------------------
    // SUCCESS CASES
    // ------------------------------------------------------------

    @Test
    void list_rooms() throws Exception {
        rooms.save(new Room(null, 101, "single", 1, 50.0, true));

        mvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void create_room_success() throws Exception {
        Room r = new Room(null, 101, "single", 1, 50.0, true);

        mvc.perform(post("/api/rooms")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(r)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.roomNumber").value(101));
    }

    @Test
    void update_room_success() throws Exception {
        Room r = rooms.save(new Room(null, 101, "single", 1, 50.0, true));

        Room updated = new Room(null, 101, "double", 2, 120.0, true);

        mvc.perform(put("/api/rooms/" + r.getId())
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("double"))
                .andExpect(jsonPath("$.capacity").value(2))
                .andExpect(jsonPath("$.price").value(120.0));
    }

    @Test
    void delete_room_success() throws Exception {
        Room r = rooms.save(new Room(null, 101, "single", 1, 50.0, true));

        mvc.perform(delete("/api/rooms/" + r.getId()))
                .andExpect(status().isOk());

        assert rooms.findById(r.getId()).isEmpty();
    }

    @Test
    void get_room_by_id_success() throws Exception {
        Room r = rooms.save(new Room(null, 101, "single", 1, 50.0, true));

        mvc.perform(get("/api/rooms/" + r.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value(101));
    }

    @Test
    void get_rooms_by_availability() throws Exception {
        rooms.save(new Room(null, 101, "single", 1, 50.0, true));
        rooms.save(new Room(null, 102, "double", 2, 120.0, false));

        mvc.perform(get("/api/rooms/availability?available=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void get_rooms_by_type() throws Exception {
        rooms.save(new Room(null, 101, "single", 1, 50.0, true));
        rooms.save(new Room(null, 102, "double", 2, 120.0, true));

        mvc.perform(get("/api/rooms/type/single"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void get_rooms_by_price_range() throws Exception {
        rooms.save(new Room(null, 101, "single", 1, 50.0, true));
        rooms.save(new Room(null, 102, "double", 2, 120.0, true));

        mvc.perform(get("/api/rooms/price?min=40&max=60"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ------------------------------------------------------------
    // VALIDATION EDGE CASES
    // ------------------------------------------------------------

    @Test
    void cannot_create_room_with_missing_type() throws Exception {
        Room r = new Room(null, 101, null, 1, 50.0, true);

        mvc.perform(post("/api/rooms")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(r)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_create_room_with_negative_price() throws Exception {
        Room r = new Room(null, 101, "single", 1, -10.0, true);

        mvc.perform(post("/api/rooms")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(r)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_create_room_with_zero_capacity() throws Exception {
        Room r = new Room(null, 101, "single", 0, 50.0, true);

        mvc.perform(post("/api/rooms")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(r)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannot_create_room_with_malformed_json() throws Exception {
        mvc.perform(post("/api/rooms")
                        .contentType("application/json")
                        .content("{ number: 'bad', type: 123 }"))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------
    // BUSINESS LOGIC EDGE CASES
    // ------------------------------------------------------------

    @Test
    void cannot_create_duplicate_room_number() throws Exception {
        rooms.save(new Room(null, 101, "single", 1, 50.0, true));

        Room r = new Room(null, 101, "double", 2, 120.0, true);

        mvc.perform(post("/api/rooms")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(r)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_nonexistent_room_not_found() throws Exception {
        Room r = new Room(null, 101, "single", 1, 50.0, true);

        mvc.perform(put("/api/rooms/nonexistent-id")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(r)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_nonexistent_room_not_found() throws Exception {
        mvc.perform(delete("/api/rooms/nonexistent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void get_nonexistent_room_not_found() throws Exception {
        mvc.perform(get("/api/rooms/nonexistent-id"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------
    // PRICE RANGE EDGE CASES
    // ------------------------------------------------------------

    @Test
    void price_range_returns_empty_list_if_no_matches() throws Exception {
        rooms.save(new Room(null, 101, "single", 1, 200.0, true));

        mvc.perform(get("/api/rooms/price?min=10&max=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void price_range_negative_min_bad_request() throws Exception {
        mvc.perform(get("/api/rooms/price?min=-10&max=50"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void price_range_min_greater_than_max_bad_request() throws Exception {
        mvc.perform(get("/api/rooms/price?min=100&max=50"))
                .andExpect(status().isBadRequest());
    }
}
