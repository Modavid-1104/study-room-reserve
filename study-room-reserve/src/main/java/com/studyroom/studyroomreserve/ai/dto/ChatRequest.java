package com.studyroom.studyroomreserve.ai.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ChatRequest {
    private String model;
    private List<ChatMessage> messages;
    private List<Map<String, Object>> tools;   // 工具定义
}
