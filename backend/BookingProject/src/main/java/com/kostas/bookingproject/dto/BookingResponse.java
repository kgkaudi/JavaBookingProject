package com.kostas.bookingproject.dto;

import java.time.LocalDate;

public class BookingResponse {
    private String id;
    private int roomNumber;
    private String userId;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalPrice;

    public BookingResponse(String id,
            int roomNumber,
            String userId,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            double totalPrice) {

        this.id = id;
        this.roomNumber = roomNumber;
        this.userId = userId;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
    }

    // Getters
    public String getId() {
        return id;
    }

    public int getRoomNumber() {
        return roomNumber;
    } // ✅ FIXED

    public String getUserId() {
        return userId;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    // Setter for roomNumber (optional)
    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }
}
