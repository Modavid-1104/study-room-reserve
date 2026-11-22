package com.studyroom.studyroomreserve.ai.dto;

import lombok.Data;

@Data
public class ToolCall {
    private String id;
    private String type;
    private Function function;

    @Data
    public static class Function {
        private String name;
        private String arguments;   // 参数是一段 JSON 字符串
    }
}
