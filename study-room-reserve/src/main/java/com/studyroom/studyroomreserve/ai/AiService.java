package com.studyroom.studyroomreserve.ai;

public interface AiService {
    String chat(Long userId, String conversationId, String userMessage,
                Double latitude, Double longitude);

}
