package com.kostas.bookingproject.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rooms")
public class Room {

    @Id
    private String id;

    private int roomNumber;
    private String type;
    private int capacity;
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
