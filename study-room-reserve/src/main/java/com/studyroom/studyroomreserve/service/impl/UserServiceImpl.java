package com.studyroom.studyroomreserve.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.studyroomreserve.entity.User;
import com.studyroom.studyroomreserve.exception.BizException;
import com.studyroom.studyroomreserve.mapper.UserMapper;
import com.studyroom.studyroomreserve.service.UserService;
import com.studyroom.studyroomreserve.util.JwtUtil;
import com.studyroom.studyroomreserve.VO.LoginVo;
import com.studyroom.studyroomreserve.VO.RegisterVo;
import com.studyroom.studyroomreserve.VO.TokenVo;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void register(RegisterVo registerVo) {
        // 1. 判断账号是否已经存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, registerVo.getUsername());
        User existUser = this.getOne(wrapper);
        if (existUser != null) {
            throw new BizException(400, "账号已被注册");
        }
        // 2. 组装用户对象，密码加密
        User user = new User();
        user.setUsername(registerVo.getUsername());
        // BCrypt加密，不要存明文！
        user.setPassword(passwordEncoder.encode(registerVo.getPassword()));
        user.setNickname(registerVo.getNickname());
        user.setPhone(registerVo.getPhone());
        // 默认角色：学生0
        user.setRole(0);
        this.save(user);
    }

    @Override
    public TokenVo login(LoginVo loginVo) {
        // 1. 根据账号查用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginVo.getUsername());
        User user = this.getOne(wrapper);
        if (user == null) {
            throw new BizException(400, "账号不存在");
        }
        // 2. 校验密码
        boolean match = passwordEncoder.matches(loginVo.getPassword(), user.getPassword());
        if (!match) {
            throw new BizException(400, "密码错误");
        }
        // 3. 生成token
        String token = JwtUtil.generateToken(user.getId());
        return new TokenVo(token, user.getRole());

    }
}

