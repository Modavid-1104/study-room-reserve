package com.example.campusstudyroombookingsystem.model;

public class ReviewMediaResponse {
    private Long id;
    private Long reviewId;
    private Integer type;    // 1 图片 2 视频
    private String url;      // 相对路径，如 review/xxx.jpg

    public Long getId() { return id; }
    public Long getReviewId() { return reviewId; }
    public Integer getType() { return type; }
    public String getUrl() { return url; }
}
