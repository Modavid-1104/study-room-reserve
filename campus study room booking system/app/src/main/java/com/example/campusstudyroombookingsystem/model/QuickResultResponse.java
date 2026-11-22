package com.example.campusstudyroombookingsystem.model;

public class QuickResultResponse {
    private Long reservationId;
    private Long seatId;
    private String seatNo;
    private String roomName;

    public Long getReservationId() { return reservationId; }
    public Long getSeatId() { return seatId; }
    public String getSeatNo() { return seatNo; }
    public String getRoomName() { return roomName; }
}
