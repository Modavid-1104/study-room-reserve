package com.example.campusstudyroombookingsystem.model;

public class PrivateMessageResponse {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Integer isRead;
    private String createTime;

    public Long getId() { return id; }
    public Long getConversationId() { return conversationId; }
    public Long getSenderId() { return senderId; }
    public Long getReceiverId() { return receiverId; }
    public String getContent() { return content; }
    public Integer getIsRead() { return isRead; }
    public String getCreateTime() { return createTime; }
}
