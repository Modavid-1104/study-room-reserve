package com.studyroom.studyroomreserve.interceptor;

import com.studyroom.studyroomreserve.entity.User;
import com.studyroom.studyroomreserve.exception.BizException;
import com.studyroom.studyroomreserve.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component   // 交给 Spring 管理，才能注入 UserService
public class AdminInterceptor implements HandlerInterceptor {

    @Resource
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // JwtInterceptor 已先执行，userId 已存进 request
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BizException(401, "未登录");
        }

        User user = userService.getById(userId);
        // role 为 1 管理员、2 超管才放行；0 学生拒绝
        if (user == null || user.getRole() == null || user.getRole() < 1) {
            throw new BizException(403, "无管理员权限");
        }
        return true;
    }
}
