package com.example.campusstudyroombookingsystem.model;

public class SendPrivateMsgRequest {
    private final Long conversationId;
    private final String content;

    public SendPrivateMsgRequest(Long conversationId, String content) {
        this.conversationId = conversationId;
        this.content = content;
    }

    public Long getConversationId() { return conversationId; }
    public String getContent() { return content; }
}
