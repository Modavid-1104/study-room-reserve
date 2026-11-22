package com.studyroom.studyroomreserve.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录表单")
public class LoginVo {
    @Schema(description = "账号（学号）")
    private String username;
    @Schema(description = "密码")
    private String password;
}

