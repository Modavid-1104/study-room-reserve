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
@RequestMapping("/chat")
@Tag(name = "在线咨询")
public class ChatController {

    @Resource
    private ChatService chatService;

    @GetMapping("/conversation")
    @Operation(summary = "获取或创建我的咨询会话")
    public Result<ConversationVo> conversation(@RequestAttribute("userId") Long userId) {
        return Result.success(chatService.getOrCreateConversation(userId));
    }

    @GetMapping("/list")
    @Operation(summary = "我的会话列表")
    public Result<List<ConversationVo>> list(@RequestAttribute("userId") Long userId) {
        return Result.success(chatService.myConversations(userId));
    }

    @GetMapping("/history/{conversationId}")
    @Operation(summary = "历史消息")
    public Result<List<PrivateMessage>> history(@RequestAttribute("userId") Long userId,
                                                @PathVariable Long conversationId) {
        return Result.success(chatService.history(userId, conversationId));
    }

    @PostMapping("/send")
    @Operation(summary = "发送消息")
    public Result<?> send(@RequestAttribute("userId") Long userId,
                          @RequestBody SendPrivateMsgVo vo) {
        chatService.sendMessage(userId, vo);
        return Result.success();
    }

    @PostMapping("/read/{conversationId}")
    @Operation(summary = "标记已读")
    public Result<?> read(@RequestAttribute("userId") Long userId,
                          @PathVariable Long conversationId) {
        chatService.markRead(userId, conversationId);
        return Result.success();
    }
}
