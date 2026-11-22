package com.studyroom.studyroomreserve.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class DeepSeekResponse {
    private List<Choice> choices;

    @Data
    public static class Choice {
        private ChatMessage message;

        @JsonProperty("finish_reason")
        private String finishReason;
    }
}
