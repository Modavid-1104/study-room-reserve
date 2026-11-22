package com.studyroom.studyroomreserve.controller;

import com.studyroom.studyroomreserve.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Tag(name = "管理员接口")
public class AdminController {

    @GetMapping("/test")
    @Operation(summary = "管理员权限测试")
    public Result<String> test() {
        return Result.success("你是管理员，访问成功");
    }
}
