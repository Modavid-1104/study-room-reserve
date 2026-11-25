package com.studyroom.studyroomreserve.controller;

import com.studyroom.studyroomreserve.common.Result;
import com.studyroom.studyroomreserve.VO.ConversationVo;
import com.studyroom.studyroomreserve.VO.SendPrivateMsgVo;
import com.studyroom.studyroomreserve.entity.PrivateMessage;
import com.studyroom.studyroomreserve.service.ChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/chat")
@Tag(name = "管理员-在线咨询")
public class AdminChatController {

    @Resource
    private ChatService chatService;

    @GetMapping("/list")
    @Operation(summary = "所有咨询会话")
    public Result<List<ConversationVo>> list(@RequestAttribute("userId") Long adminId) {
        return Result.success(chatService.adminConversations(adminId));
    }

    @GetMapping("/history/{conversationId}")
    @Operation(summary = "历史消息")
    public Result<List<PrivateMessage>> history(@RequestAttribute("userId") Long adminId,
                                                @PathVariable Long conversationId) {
        return Result.success(chatService.history(adminId, conversationId));
    }

    @PostMapping("/send")
    @Operation(summary = "回复消息")
    public Result<?> send(@RequestAttribute("userId") Long adminId,
                          @RequestBody SendPrivateMsgVo vo) {
        chatService.sendMessage(adminId, vo);
        return Result.success();
    }

    @PostMapping("/read/{conversationId}")
    @Operation(summary = "标记已读")
    public Result<?> read(@RequestAttribute("userId") Long adminId,
                          @PathVariable Long conversationId) {
        chatService.markRead(adminId, conversationId);
        return Result.success();
    }
}
