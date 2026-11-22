package com.example.campusstudyroombookingsystem.model;

public class MessageSendRequest {
    private Long userId;     // 留空表示群发
    private String title;
    private String content;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
