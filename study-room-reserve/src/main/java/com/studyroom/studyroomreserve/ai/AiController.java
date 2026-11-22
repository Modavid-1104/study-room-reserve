package com.studyroom.studyroomreserve.ai;

import com.studyroom.studyroomreserve.ai.dto.ChatSessionRequest;
import com.studyroom.studyroomreserve.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private AiService aiService;

    @PostMapping("/chat")
    @Operation(summary = "AI对话")
    public Result<String> chat(@RequestAttribute("userId") Long userId,
                               @RequestBody ChatSessionRequest body) {
        return Result.success(aiService.chat(
                userId, body.getConversationId(), body.getMessage(),
                body.getLatitude(), body.getLongitude()));
    }
}
