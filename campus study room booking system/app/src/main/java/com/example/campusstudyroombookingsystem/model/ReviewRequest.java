package com.example.campusstudyroombookingsystem.model;

public class ReviewRequest {
    private Long reservationId;
    private Integer score;
    private String content;

    public ReviewRequest(Long reservationId, Integer score, String content) {
        this.reservationId = reservationId;
        this.score = score;
        this.content = content;
    }
}
