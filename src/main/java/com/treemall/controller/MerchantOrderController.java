package com.treemall.controller;                                         // 控制器包

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;      // 分页结果对象
import com.treemall.common.Result;                                      // 统一响应格式
import com.treemall.dto.request.DeliverRequest;                         // 发货请求 DTO
import com.treemall.entity.Order;                                       // 订单实体
import com.treemall.service.OrderService;                               // 订单服务接口
import lombok.RequiredArgsConstructor;                                   // 构造器注入
import org.springframework.web.bind.annotation.*;                           // Spring MVC 注解

/**
 * 订单控制器（商户端）
 *
 * 所有接口都需要 Token（商户后台管理，必须登录）
 * 三个接口：订单列表、订单详情、发货
 *
 * 请求路径统一前缀：/api/v1/merchant/order
 *
 * 注意：商户端接口需要 JwtInterceptor 校验 Token（已通过 WebConfig 配置拦截 /api/v1/merchant/**）
 *       V1 阶段不做商户角色校验（MerchantInterceptor），V2 完善权限控制
 */
@RestController
@RequestMapping("/api/v1/merchant/order")
@RequiredArgsConstructor
public class MerchantOrderController {

    private final OrderService orderService;

    /**
     * 商户端：订单列表（分页）
     *
     * 请求方式：GET /api/v1/merchant/order/list?page=1&size=10&status=PENDING_DELIVERY
     * 用途：商户查看所有订单，可按状态筛选（如筛选"待发货"订单）
     * V1 单商户：显示所有订单
     * V2 多商户：增加 merchantId 过滤
     */
    @GetMapping("/list")
    public Result<Page<Order>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status                 // 状态筛选：如 PENDING_DELIVERY（待发货）
    ) {
        Page<Order> orderPage = orderService.getMerchantOrderList(page, size, status);
        return Result.success(orderPage);
    }

    /**
     * 商户端：订单详情（含订单项 + 物流）
     *
     * 请求方式：GET /api/v1/merchant/order/{orderId}
     * 用途：商户查看订单详情，包括商品列表、收货地址、物流信息
     */
    @GetMapping("/{orderId}")
    public Result<Order> detail(@PathVariable Long orderId) {
        Order order = orderService.getMerchantOrderDetail(orderId);
        return Result.success(order);
    }

    /**
     * 商户端：发货
     *
     * 请求方式：PUT /api/v1/merchant/order/{orderId}/deliver
     * 请求体：  {
     *            "companyName": "顺丰速运",
     *            "trackingNo": "SF1234567890"
     *          }
     *
     * 业务流程：
     *   1. 校验订单状态必须为 PENDING_DELIVERY（待发货）
     *   2. 更新订单状态为 DELIVERED，记录发货时间
     *   3. 插入物流记录（company_name + tracking_no + status=PENDING）
     */
    @PutMapping("/{orderId}/deliver")
    public Result<Void> deliver(@PathVariable Long orderId,
                                @RequestBody DeliverRequest request) {
        orderService.deliverOrder(orderId, request);
        return Result.success();
    }
}