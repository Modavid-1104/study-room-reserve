package com.example.campusstudyroombookingsystem.model;

public class CreateReservationRequest {
    private Long seatId;
    private String startTime;
    private String endTime;

    public CreateReservationRequest(Long seatId, String startTime, String endTime) {
        this.seatId = seatId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
