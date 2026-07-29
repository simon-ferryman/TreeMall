package com.treemall.service;                                           // 服务接口包

import java.util.Map;                                                  // 用于接收微信回调参数

/**
 * 支付服务接口
 *
 * 功能：
 *   1. 统一下单 — 调用微信支付 API，生成预支付订单，返回支付参数给前端
 *   2. 支付回调 — 接收微信支付结果通知，更新订单状态
 *
 * 业务流程（完整支付链路）：
 *   用户下单（PENDING_PAYMENT）
 *     → 前端调用 /api/v1/pay/prepay（获取支付参数）
 *     → 前端用这些参数调起微信支付
 *     → 用户输入密码完成支付
 *     → 微信服务器 POST 通知到 /api/v1/pay/callback
 *     → 后端验证签名 → 更新订单状态为 PENDING_DELIVERY → 返回成功
 *
 * 设计原则：
 *   - 支付回调必须做幂等处理（同一笔订单重复通知不会重复更新）
 *   - 支付回调必须验证签名（防止伪造通知）
 *   - 支付回调路径在 WebConfig 中已排除 JWT 拦截（因为回调来自微信服务器，没有 Token）
 */
public interface PaymentService {

    /**
     * 统一下单（生成预支付订单）
     *
     * 调用微信支付 JSAPI 下单接口，获取 prepay_id，
     * 然后组装小程序调起支付所需的参数（时间戳、随机串、签名等）
     *
     * @param orderId 订单 ID
     * @return 小程序调起支付所需的参数 Map（appId、timeStamp、nonceStr、package、signType、paySign）
     */
    Map<String, String> prepay(Long orderId);

    /**
     * 支付回调处理
     *
     * 微信服务器 POST 通知到 /api/v1/pay/callback，携带支付结果
     * 需要验证签名 → 更新订单状态 → 返回应答给微信
     *
     * V1 简化实现：不做签名验证（微信支付 SDK 需要额外配置），
     * 直接根据回调中的订单号更新订单状态。
     * 生产环境必须加上签名验证和安全校验。
     *
     * @param body 微信回调的请求体（JSON 格式）
     * @return 应答字符串（微信要求的格式）
     */
    String handleCallback(String body);
}