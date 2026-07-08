package com.kostas.bookingproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class BookingResponse {

    private String id;

    private String roomId;
    private String userId;
    private String status;

    private LocalDate startDate;
    private LocalDate endDate;

    private double totalPrice;

    public BookingResponse() {}
}
