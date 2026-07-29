package com.treemall.controller;                                         // 控制器包

import com.treemall.common.Result;                                      // 统一响应格式
import com.treemall.service.PaymentService;                             // 支付服务
import lombok.RequiredArgsConstructor;                                   // 构造器注入
import org.springframework.web.bind.annotation.*;                       // Spring MVC 注解
import com.treemall.dto.request.PrepayRequest;
import java.util.Map;                                                  // 支付参数 Map

/**
 * 支付控制器
 *
 * 两个接口：
 *   1. prepay — 生成支付参数（需要 Token，用户登录后才能支付）
 *   2. callback — 支付回调（不需要 Token，来自微信服务器的通知）
 *
 * 路径设计：
 *   /api/v1/pay/prepay    → 需要 Token（JwtInterceptor 拦截）
 *   /api/v1/pay/callback  → 不需要 Token（已在 WebConfig 中排除，因为来自微信服务器）
 *
 * 支付流程：
 *   用户下单 → 调用 prepay 获取支付参数 → 前端调起微信支付
 *   → 支付成功 → 微信回调 callback → 更新订单状态为 PENDING_DELIVERY
 */
@RestController                                                         // 所有方法返回 JSON
@RequestMapping("/api/v1/pay")                                            // 统一路径前缀
@RequiredArgsConstructor                                                    // 构造器注入
public class PaymentController {

    private final PaymentService paymentService;                            // 支付服务

    /**
     * 统一下单（生成支付参数）
     *
     * 请求方式：POST /api/v1/pay/prepay
     * 请求头：  Authorization: Bearer <token>
     * 请求体：  { "orderId": 1 }
     * 响应体：  {
     *            "code": 200,
     *            "data": {
     *              "appId": "wx123456",
     *              "timeStamp": "1721030400",
     *              "nonceStr": "abc123",
     *              "package": "prepay_id=prepay_xxx",
     *              "signType": "RSA",
     *              "paySign": "V1_MOCK_SIGN_1"
     *            }
     *          }
     *
     * 前端拿到这些参数后，调用 wx.requestPayment() 调起微信支付
     */
    /**
    @PostMapping("/prepay")
    public Result<Map<String, String>> prepay(@RequestBody Map<String, Long> body) {
        Long orderId = body.get("orderId");                               // 从请求体取订单 ID
        if (orderId == null) {
            return Result.error(400, "订单 ID 不能为空");
        }
        Map<String, String> payParams = paymentService.prepay(orderId);   // 生成支付参数
        return Result.success(payParams);
    }
    **/
    //改用PrepayRequest来获取前端订单的参数进行支付校验，替换掉Map<>
    @PostMapping("/prepay")
    public Result<Map<String, String>> prepay(@RequestBody PrepayRequest request) {
        // 参数校验：订单 ID 不能为空
        if (request.getOrderId() == null) {
            return Result.error(400, "订单 ID 不能为空");
        }
        Map<String, String> payParams = paymentService.prepay(request.getOrderId());
        return Result.success(payParams);
    }

    /**
     * 支付回调（微信服务器通知）
     *
     * 请求方式：POST /api/v1/pay/callback
     * 不需要 Token（已在 WebConfig 中排除拦截）
     * 请求体：微信支付结果通知的 JSON（加密）
     *
     * V1 简化：接收普通 JSON 格式
     * 请求体示例：{ "orderNo": "1825071512345678901", "transactionId": "4200001234567890" }
     *
     * 注意：这个接口在 WebConfig 中已排除 JWT 拦截（因为回调来自微信服务器，不会有 Token）
     */
    @PostMapping("/callback")
    public String callback(@RequestBody String body) {                  // 直接接收原始请求体字符串
        return paymentService.handleCallback(body);                     // 处理回调，返回应答给微信
    }
}