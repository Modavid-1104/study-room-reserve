package com.studyroom.studyroomreserve.interceptor;

import com.studyroom.studyroomreserve.exception.BizException;
import com.studyroom.studyroomreserve.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class JwtInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行登录、注册
        String uri = request.getRequestURI();
        if (uri.contains("/user/login") || uri.contains("/user/register")) {
            return true;
        }

        String token = request.getHeader("token");
        if (token == null || !JwtUtil.verifyToken(token)) {
            throw new BizException(401, "未登录或token失效");
        }

        // ★ 新增：解析出 userId，放进 request 作用域，后面 Controller 直接取
        Long userId = JwtUtil.getUserId(token);
        request.setAttribute("userId", userId);

        return true;
    }
}
