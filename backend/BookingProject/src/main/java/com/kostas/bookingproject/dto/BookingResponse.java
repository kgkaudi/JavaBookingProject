package com.kostas.bookingproject.dto;

import java.time.LocalDate;

public class BookingResponse {
    private String id;
    private String room;
    private String userId;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalPrice;

    public BookingResponse(String id, String room, String userId,
                           String status, LocalDate startDate,
                           LocalDate endDate, double totalPrice) {
        this.id = id;
        this.room = room;
        this.userId = userId;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getRoom() { return room; }
    public String getUserId() { return userId; }
    public String getStatus() { return status; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public double getTotalPrice() { return totalPrice; }

    public void setRoom(String room) { this.room = room; }
}
