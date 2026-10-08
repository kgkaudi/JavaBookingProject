package com.kostas.bookingproject.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rooms")
public class Room {

    @Id
    private String id;

    @Positive(message = "Room number must be positive")
    @Indexed(unique = true)
    private int roomNumber;

    @NotBlank(message = "Type is required")
    private String type;

    @Positive(message = "Capacity must be at least 1")
    private int capacity;

    @Positive(message = "Price must be greater than 0")
    private double price;
    private boolean available;

    public Room(String id, int roomNumber, String type, double price, boolean available) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.type = type;
        this.price = price;
        this.available = available;

        this.capacity = 1;
    }
}
