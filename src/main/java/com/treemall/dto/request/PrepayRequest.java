package com.treemall.dto.request;                                       // 请求 DTO 包

import lombok.Data;                                                     // Lombok

/**
 * 支付预下单请求 DTO
 *
 * 使用场景：前端 POST /api/v1/pay/prepay 时，请求体 JSON 映射到此对象
 *
 * 请求体示例：
 * { "orderId": 1 }
 *
 * 为什么需要 DTO 而不是直接用 Map？
 *   - 类型安全：编译期就能发现字段名拼写错误
 *   - 可读性：一眼就知道需要传什么参数
 *   - 可扩展：后续如果要加新参数（如支付方式），直接在 DTO 加字段即可
 */
@Data
public class PrepayRequest {

    private Long orderId;                                                 // 订单 ID（要支付的订单）
}