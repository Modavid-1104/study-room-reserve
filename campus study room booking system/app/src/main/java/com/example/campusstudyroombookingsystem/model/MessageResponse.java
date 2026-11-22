package com.example.campusstudyroombookingsystem.model;

public class MessageResponse {
    private Long id;
    private String title;
    private String content;
    private Integer isRead;
    private String createTime;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Integer getIsRead() { return isRead; }
    public String getCreateTime() { return createTime; }
}
