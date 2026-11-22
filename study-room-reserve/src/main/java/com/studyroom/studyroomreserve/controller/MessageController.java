package com.studyroom.studyroomreserve.controller;


import com.studyroom.studyroomreserve.common.Result;
import com.studyroom.studyroomreserve.entity.Message;
import com.studyroom.studyroomreserve.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
@Tag(name = "消息模块")
public class MessageController {
    @Resource
    private MessageService messageService;

    @GetMapping("/list")
    @Operation(summary = "我的消息列表")
    public Result<List<Message>> list(@RequestAttribute("userId") Long userId){
        return Result.success(messageService.myList(userId));
    }

    @PostMapping("/read/{id}")
    @Operation(summary = "标记已读")
    public Result<?> read(@RequestAttribute("userId") Long userId,
                          @PathVariable Long id){
        messageService.markRead(userId,id);
        return Result.success();
    }
}
