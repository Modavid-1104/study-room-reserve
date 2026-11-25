package com.studyroom.studyroomreserve.config;

import com.studyroom.studyroomreserve.interceptor.AdminInterceptor;
import com.studyroom.studyroomreserve.interceptor.JwtInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private AdminInterceptor adminInterceptor;
    @Resource
    private UploadProperties uploadProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/doc.html", "/swagger-ui/**",
                        "/v3/api-docs/**", "/upload/**");   // 放行图片访问

        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + uploadProperties.getUploadPath() + "/");
    }
}
