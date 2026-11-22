package com.example.campusstudyroombookingsystem.model;

public class ReviewResponse {
    private Long id;
    private Long reservationId;
    private Long roomId;
    private Long userId;
    private Integer score;
    private String content;
    private String createTime;

    public Long getId() { return id; }
    public Long getReservationId() { return reservationId; }
    public Long getRoomId() { return roomId; }
    public Long getUserId() { return userId; }
    public Integer getScore() { return score; }
    public String getContent() { return content; }
    public String getCreateTime() { return createTime; }
}
