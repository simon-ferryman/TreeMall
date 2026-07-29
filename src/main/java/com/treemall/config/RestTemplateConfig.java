package com.treemall.config;                                            // 配置类包

import org.springframework.context.annotation.Bean;                      // 声明 Bean
import org.springframework.context.annotation.Configuration;            // 标记为配置类
import org.springframework.web.client.RestTemplate;                      // Spring 的 HTTP 客户端

/**
 * RestTemplate 配置类
 * 用于发送 HTTP 请求（调用微信 API 等外部接口）
 */
@Configuration
public class RestTemplateConfig {

    @Bean                                                               // 将 RestTemplate 注册到 Spring 容器
    public RestTemplate restTemplate() {
        return new RestTemplate();                                      // 返回实例，后续用 @Autowired 或构造器注入
    }
}