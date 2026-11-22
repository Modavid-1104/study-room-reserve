package com.studyroom.studyroomreserve.controller;


import com.studyroom.studyroomreserve.service.UserService;
import com.studyroom.studyroomreserve.util.Result;
import com.studyroom.studyroomreserve.VO.LoginVo;
import com.studyroom.studyroomreserve.VO.RegisterVo;
import com.studyroom.studyroomreserve.VO.TokenVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/user")
@Tag(name = "用户登录注册接口")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<?> register(@RequestBody RegisterVo registerVo) {
        userService.register(registerVo);
        return Result.success();
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<TokenVo> login(@RequestBody LoginVo loginVo) {
        TokenVo tokenVo = userService.login(loginVo);
        return Result.success(tokenVo);
    }
}
