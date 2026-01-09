package com.gewujie.zibian.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    
    // Custom interceptor to handle OPTIONS requests
    private static class OptionsInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            if ("OPTIONS".equals(request.getMethod())) {
                response.setStatus(HttpServletResponse.SC_OK);
                return false;
            }
            return true;
        }
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Add OPTIONS interceptor first (higher priority)
        registry.addInterceptor(new OptionsInterceptor())
                .addPathPatterns("/**");
        
        // Register Sa-Token Interceptor
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter.match("/**")
                    .notMatch("/api/auth/**")
                    .notMatch("/api/learning/lesson/current/**")
                    .notMatch("/api/learning/lesson/*")
                    .notMatch("/api/learning/lessons/**")
                    .notMatch("/test/**")
                    .notMatch("/error")
                    .notMatch("/swagger-ui/**")
                    .notMatch("/swagger-ui.html")
                    .notMatch("/v3/api-docs/**")
                    .notMatch("/swagger-resources/**")
                    .notMatch("/webjars/**")
                    .check(r -> StpUtil.checkLogin());
        }));
    }
}
