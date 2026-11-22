package com.studyroom.studyroomreserve.controller;

import com.studyroom.studyroomreserve.service.MessageService;
import com.studyroom.studyroomreserve.util.Result;
import com.studyroom.studyroomreserve.VO.MessageSendVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/message")
@Tag(name = "管理员-消息管理")
public class AdminMessageController {

    @Resource
    private MessageService messageService;

    @PostMapping("/send")
    @Operation(summary = "单发/群发消息")
    public Result<?> send(@RequestBody MessageSendVo vo) {
        messageService.sendMessage(vo);
        return Result.success();
    }
}
