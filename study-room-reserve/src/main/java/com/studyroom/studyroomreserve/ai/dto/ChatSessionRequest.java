package com.studyroom.studyroomreserve.ai.dto;

import lombok.Data;

@Data
public class ChatSessionRequest {
    private String conversationId;
    private String message;
    private Double latitude;    // 当前定位（签到工具需要）
    private Double longitude;
}
