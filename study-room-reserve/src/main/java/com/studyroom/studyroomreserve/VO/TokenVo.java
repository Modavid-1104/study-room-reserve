package com.studyroom.studyroomreserve.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "登录返回信息")
public class TokenVo {
    @Schema(description = "JWT令牌")
    private String token;

    @Schema(description = "角色：0学生 1管理员")
    private Integer role;
}
