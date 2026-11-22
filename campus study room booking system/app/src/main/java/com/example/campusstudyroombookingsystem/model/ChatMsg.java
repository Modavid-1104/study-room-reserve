package com.example.campusstudyroombookingsystem.model;

public class ChatMsg {
    public static final int ROLE_USER = 0;  // 用户消息
    public static final int ROLE_AI = 1;    // AI消息

    private int role;
    private String content;

    public ChatMsg(int role, String content) {
        this.role = role;
        this.content = content;
    }

    public int getRole() { return role; }
    public String getContent() { return content; }
}
