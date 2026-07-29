package com.treemall.config;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;      //静态资源映射注解
import com.treemall.interceptor.JwtInterceptor;
import com.treemall.interceptor.MerchantInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 两大职责：
 * 1. 注册拦截器（JWT 认证 + 商户权限）
 * 2. 配置 CORS 跨域（开发阶段允许前端跨域访问）
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private MerchantInterceptor merchantInterceptor;

    /**
     * 注册拦截器
     *
     * 拦截顺序：JwtInterceptor 先执行，MerchantInterceptor 后执行
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截器1：JWT 认证（拦截所有 /api/v1/**，排除登录和支付回调）
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns(
                        "/api/v1/auth/login",      // 登录接口不需要 Token
                        "/api/v1/auth/dev-login",   // 开发测试登录不需要 Token ← 新增这行,测试用
                        "/api/v1/pay/callback",      // 支付回调由微信服务器发起，无 Token
                        "/api/v1/category/**",      // 分类列表（公开浏览）
                        "/api/v1/product/**",       // 商品列表 + 详情（公开浏览）
                        "/api/v1/banner/**"         // 轮播图列表（公开浏览）
                );

        // 拦截器2：商户角色校验（仅拦截 /api/v1/merchant/**）
        registry.addInterceptor(merchantInterceptor)
                .addPathPatterns("/api/v1/merchant/**");
    }


    /**
     * CORS 跨域配置
     * 开发阶段允许所有来源访问，生产环境应限制为正式域名
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
    /**
     * 静态资源映射
     * 将 /images/** 路径映射到本地磁盘的 /data/images/ 目录
     * 使用场景：上传商品图片后，通过 http://localhost:8080/images/xxx.jpg 访问
     * 注意：生产环境应由 Nginx 直接提供静态资源，不走 Spring Boot
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/images/**")
//                .addResourceLocations("file:/data/images/");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:/D:/data/images/");
    }
}