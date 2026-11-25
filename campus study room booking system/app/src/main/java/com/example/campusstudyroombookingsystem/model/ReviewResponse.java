package com.example.campusstudyroombookingsystem.model;

import java.util.List;

public class ReviewResponse {
    private Long id;
    private Long reservationId;
    private Long roomId;
    private Long userId;
    private Integer score;
    private String content;
    private String createTime;
    private List<ReviewMediaResponse> mediaList;

    public List<ReviewMediaResponse> getMediaList() { return mediaList; }


    public Long getId() { return id; }
    public Long getReservationId() { return reservationId; }
    public Long getRoomId() { return roomId; }
    public Long getUserId() { return userId; }
    public Integer getScore() { return score; }
    public String getContent() { return content; }
    public String getCreateTime() { return createTime; }
}
