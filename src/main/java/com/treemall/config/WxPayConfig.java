package com.treemall.config;                                            // 配置类包

import lombok.Data;                                                     // Lombok：自动生成 getter/setter
import org.springframework.boot.context.properties.ConfigurationProperties; // 将 yml 配置绑定到对象
import org.springframework.context.annotation.Configuration;            // 标记为配置类

/**
 * 微信支付配置类
 *
 * 设计思路：
 *   使用 @ConfigurationProperties 注解，自动将 application.yml 中的 wx.* 配置
 *   绑定到这个类的字段上，避免在代码中到处写 @Value 注解。
 *
 * 配置文件中的对应项（application.yml）：
 *   wx:
 *     app-id: your_wechat_app_id       → 绑定到 appId
 *     app-secret: your_wechat_secret   → 绑定到 appSecret（注意：微信支付用的是商户密钥，不是这个）
 *     mch-id: your_merchant_id         → 绑定到 mchId
 *     api-key: your_api_key_v3         → 绑定到 apiKeyV3（微信支付 APIv3 密钥）
 *
 * 使用场景：
 *   支付服务（PaymentServiceImpl）通过注入 WxPayConfig 获取商户号、密钥等配置，
 *   用于调用微信支付统一下单接口和验证支付回调签名。
 *
 * 注意：V1 版本使用微信支付 APIv3，需要商户号（mchId）和 APIv3 密钥（apiKeyV3）。
 *       回调通知地址（notifyUrl）需要在微信商户平台配置，这里存储的是回调路径。
 */
@Data                                                                   // 自动生成 getter/setter
@Configuration                                                          // 标记为 Spring 配置类
@ConfigurationProperties(prefix = "wx")                                 // 绑定 application.yml 中以 "wx" 开头的配置
public class WxPayConfig {

    /**
     * 小程序 AppID（微信公众平台获取）
     * 用于微信登录和微信支付，是微信生态的核心标识
     */
    private String appId;                                               // 对应 yml: wx.app-id

    /**
     * 小程序 AppSecret（微信公众平台获取）
     * 用于微信登录流程（UserServiceImpl 调用微信 code2Session 接口）
     * 注意：appSecret 和 apiKey 是两个不同的密钥：
     *   appSecret → 微信登录（小程序 API）
     *   apiKey    → 微信支付（商户平台 APIv3 密钥）
     */
    private String appSecret;                                           // 对应 yml: wx.app-secret

    /**
     * 商户号（微信支付商户平台获取）
     * 用于微信支付 API 调用，标识收款商户
     */
    private String mchId;                                               // 对应 yml: wx.mch-id

    /**
     * APIv3 密钥（微信支付商户平台 → API安全 → 设置APIv3密钥）
     * 32 位随机字符串，用于支付回调签名验证和 HTTP 请求签名
     */
    private String apiKey;                                              // 对应 yml: wx.api-key

    /**
     * 支付回调通知地址
     * 微信支付成功后，微信服务器会向这个地址 POST 支付结果通知
     * 注意：这个地址必须是公网可访问的 HTTPS URL（生产环境需要配置 Nginx + SSL）
     * 开发环境可以使用内网穿透工具（如 ngrok）暴露本地服务
     */
    private String notifyUrl;                                           // 对应 yml: wx.notify-url
}