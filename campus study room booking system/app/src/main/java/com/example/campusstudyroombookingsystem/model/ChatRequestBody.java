package com.example.campusstudyroombookingsystem.model;

public class ChatRequestBody {
    private String conversationId;
    private String message;
    private Double latitude;
    private Double longitude;

    public ChatRequestBody(String conversationId, String message,
                           Double latitude, Double longitude) {
        this.conversationId = conversationId;
        this.message = message;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
