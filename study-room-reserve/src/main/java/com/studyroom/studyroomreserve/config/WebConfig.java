package com.studyroom.studyroomreserve.config;

import com.studyroom.studyroomreserve.interceptor.AdminInterceptor;
import com.studyroom.studyroomreserve.interceptor.JwtInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 第一个：登录校验，拦截所有
        registry.addInterceptor(new JwtInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/doc.html", "/swagger-ui/**", "/v3/api-docs/**");

        // 第二个：管理员校验，只拦截 /admin/**
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**");
    }
}
