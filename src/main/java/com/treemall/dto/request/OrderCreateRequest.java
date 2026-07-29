package com.treemall.dto.request;                                       // 请求 DTO 包：存放前端传来的请求体对象

import lombok.Data;                                                     // Lombok：自动生成 getter/setter/toString
import java.util.List;                                                  // 购物车 ID 列表

/**
 * 下单请求 DTO（Data Transfer Object — 数据传输对象）
 *
 * 使用场景：前端 POST /api/v1/order 时，请求体 JSON 映射到这个对象
 *
 * 请求体示例：
 * {
 *   "addressId": 1,
 *   "cartIds": [1, 2, 3],
 *   "remark": "请尽快发货"
 * }
 *
 * 设计原则：DTO 和 Entity 分离
 *   - Entity 映射数据库表结构，字段和列一一对应
 *   - DTO 映射前端请求/响应结构，可以灵活组合
 *   - 前端需求变化时只改 DTO，不影响数据库层
 *   - 放在 dto/request/ 下，表示这是"请求方向"的数据传输对象
 */
@Data                                                                   // 自动生成所有字段的 getter/setter
public class OrderCreateRequest {

    private Long addressId;                                               // 收货地址 ID（前端从地址列表中选择）

    private List<Long> cartIds;                                           // 要结算的购物车项 ID 列表（只结算选中的）

    private String remark;                                                // 用户备注（可选，如"请发顺丰"）
}