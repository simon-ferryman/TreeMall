package com.treemall.service.impl;                                      // 服务实现包

import cn.hutool.core.util.IdUtil;                                      // Hutool 工具：生成随机字符串
import cn.hutool.json.JSONObject;                                       // Hutool JSON 工具：解析微信返回
import cn.hutool.json.JSONUtil;                                         // Hutool JSON 工具
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // 条件构造器
import com.treemall.common.BusinessException;                            // 业务异常
import com.treemall.config.WxPayConfig;                                 // 微信支付配置
import com.treemall.entity.Order;                                       // 订单实体
import com.treemall.mapper.OrderMapper;                                  // 订单 Mapper
import com.treemall.service.PaymentService;                             // 支付服务接口
import lombok.RequiredArgsConstructor;                                   // 构造器注入
import lombok.extern.slf4j.Slf4j;                                       // 日志
import org.springframework.stereotype.Service;                              // Spring 服务标记
import org.springframework.transaction.annotation.Transactional;              // 事务管理
import org.springframework.web.client.RestTemplate;                      // HTTP 客户端：调用微信支付 API
import com.treemall.common.UserContext;                                  //用户上下文
import java.time.LocalDateTime;                                         // 当前时间
import java.util.HashMap;                                               // 返回参数 Map
import java.util.Map;

/**
 * 支付服务实现类
 *
 * 核心逻辑：
 *   1. 统一下单：调用微信支付 API → 获取 prepay_id → 组装小程序支付参数
 *   2. 支付回调：验证订单存在 → 验证状态为待支付 → 更新订单状态 → 记录支付时间
 *
 * V1 简化说明：
 *   微信支付 APIv3 需要 HTTP 签名（用商户私钥对请求签名），配置较复杂。
 *   V1 阶段采用"模拟支付"策略：
 *     - prepay 接口：不调用微信 API，直接返回模拟支付参数（前端可以跳过真实支付）
 *     - callback 接口：手动调用，模拟支付成功回调
 *   这样可以先完成订单流程的闭环测试，后续再接入真实微信支付。
 *
 *   真实微信支付接入需要：
 *     1. 商户证书（apiclient_cert.pem + apiclient_key.pem）
 *     2. 微信支付平台证书（wechatpay_xxx.pem）
 *     3. HTTP 签名工具类（WechatPayHttpClient）
 *   这些在 V2 迭代中完善。
 */
@Slf4j                                                                  // 自动生成 log 对象
@Service                                                               // 标记为 Spring 服务 Bean
@RequiredArgsConstructor                                                    // 构造器注入
public class PaymentServiceImpl implements PaymentService {

    private final OrderMapper orderMapper;                                  // 订单 Mapper
    private final WxPayConfig wxPayConfig;                                 // 微信支付配置
    private final RestTemplate restTemplate;                                // HTTP 客户端

    /**
     * 统一下单（V1 简化版：模拟支付）
     *
     * 完整流程应该：
     *   1. 查订单 → 校验状态为 PENDING_PAYMENT
     *   2. 调用微信 JSAPI 下单接口 → 获取 prepay_id
     *   3. 组装小程序支付参数（签名）
     *   4. 返回给前端 → 前端调起 wx.requestPayment()
     *
     * V1 简化：不调用微信 API，直接返回模拟参数
     * 前端收到这些参数后，可以跳过真实支付直接调用回调接口
     *
     * @param orderId 订单 ID
     * @return 支付参数 Map
     */
    @Override
    @Transactional                                                      // 事务：防止并发重复支付
    public Map<String, String> prepay(Long orderId) {
        // 第1步：查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(400, "订单不存在");
        }
        // 第1.5步：校验订单归属（安全校验：只能支付自己的订单）
        // 防止用户 A 通过篡改请求中的 orderId 来支付用户 B 的订单
        if (!order.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(403, "无权操作此订单");
        }
        // 第2步：校验订单状态（只有待支付状态才能发起支付）
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BusinessException(400, "订单状态不正确，当前状态：" + order.getStatus());
        }

        // 第3步：V1 简化 — 不调用微信 API，直接返回模拟支付参数
        // 真实微信支付需要：
        //   String url = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";
        //   HttpHeaders headers = buildAuthHeaders(...);  // 签名
        //   String body = buildRequestBody(order);        // 请求体
        //   String response = restTemplate.postForObject(url, body, String.class);
        //   String prepayId = JSONUtil.parseObj(response).getStr("prepay_id");
        String prepayId = "prepay_" + IdUtil.fastSimpleUUID();            // 生成模拟 prepay_id

        // 第4步：组装小程序调起支付所需的参数
        // 这些参数会被前端传给 wx.requestPayment()
        Map<String, String> result = new HashMap<>();
        result.put("appId", wxPayConfig.getAppId());                     // 小程序 AppID
        result.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000)); // 时间戳（秒）
        result.put("nonceStr", IdUtil.fastSimpleUUID());                 // 随机字符串
        result.put("package", "prepay_id=" + prepayId);                  // 订单详情扩展字符串
        result.put("signType", "RSA");                                   // 签名方式
        result.put("paySign", "V1_MOCK_SIGN_" + orderId);                // 签名（V1 模拟）

        log.info("生成预支付订单: orderNo={}, prepayId={}", order.getOrderNo(), prepayId);
        return result;
    }

    /**
     * 支付回调处理（V1 简化版：模拟回调）
     *
     * 微信真实回调流程：
     *   1. 微信服务器 POST 加密的支付结果到 /api/v1/pay/callback
     *   2. 后端解密 → 验证签名 → 获取订单号 → 更新订单状态
     *   3. 返回 {"code": "SUCCESS"} 给微信（否则微信会重复通知）
     *
     * V1 简化：接收普通 JSON，直接更新订单状态
     * 前端在"模拟支付"完成后，调用此接口模拟支付回调
     *
     * 幂等性设计：
     *   如果订单已经是 PENDING_DELIVERY 状态（已支付），
     *   不会重复更新，直接返回成功。防止微信重复通知导致重复处理。
     *
     * @param body 回调请求体 JSON
     * @return 应答字符串
     */
    @Override
    @Transactional                                                      // 事务：确保订单状态和支付时间同时更新
    public String handleCallback(String body) {
        // 第1步：解析回调参数
        JSONObject callback = JSONUtil.parseObj(body);                  // 字符串 → JSON 对象
        String orderNo = callback.getStr("orderNo");                    // 提取订单编号

        if (orderNo == null || orderNo.isEmpty()) {
            log.error("支付回调参数异常：缺少 orderNo, body={}", body);
            return "{\"code\":\"FAIL\",\"message\":\"缺少订单号\"}";    // 返回失败，微信会重试
        }

        // 第2步：根据订单号查询订单
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);                         // 按订单号查询
        Order order = orderMapper.selectOne(wrapper);

        if (order == null) {
            log.error("支付回调订单不存在: orderNo={}", orderNo);
            return "{\"code\":\"FAIL\",\"message\":\"订单不存在\"}";
        }

        // 第3步：幂等性检查 — 如果已支付，直接返回成功
        // 为什么需要幂等？微信可能因为网络原因重复发送支付通知，
        // 如果不做幂等，可能导致重复记录支付时间等问题
        if ("PENDING_DELIVERY".equals(order.getStatus())
                || "DELIVERED".equals(order.getStatus())
                || "RECEIVED".equals(order.getStatus())) {
            log.info("支付回调重复通知，订单已支付: orderNo={}", orderNo);
            return "{\"code\":\"SUCCESS\"}";                            // 直接返回成功，不重复处理
        }

        // 第4步：校验状态（只有待支付状态才能转为已支付）
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            log.error("支付回调状态异常: orderNo={}, status={}", orderNo, order.getStatus());
            return "{\"code\":\"FAIL\",\"message\":\"订单状态异常\"}";
        }

        // 第5步：更新订单状态为待发货
        order.setStatus("PENDING_DELIVERY");                              // 支付成功 → 待发货
        order.setPayTime(LocalDateTime.now());                            // 记录支付时间
        // 微信真实回调中会返回 transaction_id（微信支付流水号），这里用模拟值
        String transactionId = callback.getStr("transactionId",
                "MOCK_TXN_" + IdUtil.fastSimpleUUID());                  // 优先取真实值，否则用模拟值
        order.setTransactionId(transactionId);                            // 记录支付流水号
        orderMapper.updateById(order);                                    // 更新数据库

        log.info("支付回调处理成功: orderNo={}, transactionId={}", orderNo, transactionId);
        return "{\"code\":\"SUCCESS\"}";                                  // 返回成功，微信收到后停止通知
    }
}