package com.treemall.dto.request;                                       // 请求 DTO 包

import lombok.Data;                                                     // Lombok

/**
 * 发货请求 DTO
 *
 * 使用场景：商户 PUT /api/v1/merchant/order/{orderId}/deliver 时，请求体 JSON 映射到这个对象
 *
 * 请求体示例：
 * {
 *   "companyName": "顺丰速运",
 *   "trackingNo": "SF1234567890"
 * }
 *
 * 业务流程：
 *   商户在后台看到待发货订单 → 线下发货 → 拿到快递单号 → 在后台填入物流信息 → 调用此接口
 *   调用后：t_order.status → DELIVERED，t_logistics 插入一条物流记录
 */
@Data
public class DeliverRequest {

    private String companyName;                                           // 物流公司名称，如 "顺丰速运"、"中通快递"

    private String trackingNo;                                            // 物流单号，如 "SF1234567890"
}