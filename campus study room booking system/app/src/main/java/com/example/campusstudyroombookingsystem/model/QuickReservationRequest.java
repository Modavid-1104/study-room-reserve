package com.example.campusstudyroombookingsystem.model;

public class QuickReservationRequest {
    private Long roomId;        // 可空，null 表示全局找
    private String startTime;
    private String endTime;

    public QuickReservationRequest(Long roomId, String startTime, String endTime) {
        this.roomId = roomId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
