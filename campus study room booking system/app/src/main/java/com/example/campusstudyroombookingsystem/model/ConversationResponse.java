package com.example.campusstudyroombookingsystem.model;

public class ConversationResponse {
    private Long id;
    private Long peerId;
    private String peerNickname;
    private String peerAvatar;
    private String lastMessage;
    private String lastTime;
    private Integer unread;

    public Long getId() { return id; }
    public Long getPeerId() { return peerId; }
    public String getPeerNickname() { return peerNickname; }
    public String getPeerAvatar() { return peerAvatar; }
    public String getLastMessage() { return lastMessage; }
    public String getLastTime() { return lastTime; }
    public Integer getUnread() { return unread; }
}
