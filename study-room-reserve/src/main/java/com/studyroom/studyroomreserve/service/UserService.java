package com.studyroom.studyroomreserve.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.studyroom.studyroomreserve.entity.User;
import com.studyroom.studyroomreserve.VO.LoginVo;
import com.studyroom.studyroomreserve.VO.RegisterVo;
import com.studyroom.studyroomreserve.VO.TokenVo;

public interface UserService extends IService<User> {
    // 注册
    void register(RegisterVo registerVo);
    // 登录，返回token
    TokenVo login(LoginVo loginVo);
}
