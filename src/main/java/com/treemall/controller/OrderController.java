package com.treemall.controller;                                         // 控制器包

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;      // 分页结果对象
import com.treemall.common.Result;                                      // 统一响应格式
import com.treemall.dto.request.OrderCreateRequest;                     // 下单请求 DTO
import com.treemall.entity.Order;                                       // 订单实体
import com.treemall.service.OrderService;                               // 订单服务接口
import lombok.RequiredArgsConstructor;                                   // 构造器注入
import org.springframework.web.bind.annotation.*;                           // Spring MVC 注解

/**
 * 订单控制器（用户端）
 *
 * 所有接口都需要 Token（订单是用户私人数据，必须登录）
 * 四个接口：创建订单、订单列表、订单详情、取消订单
 *
 * 请求路径统一前缀：/api/v1/order
 */
@RestController                                                         // = @Controller + @ResponseBody，所有方法返回 JSON
@RequestMapping("/api/v1/order")                                          // 统一路径前缀
@RequiredArgsConstructor                                                    // 构造器注入
public class OrderController {

    private final OrderService orderService;                                // 订单服务

    /**
     * 创建订单（下单）
     *
     * 请求方式：POST /api/v1/order
     * 请求头：  Authorization: Bearer <token>
     * 请求体：  {
     *            "addressId": 1,
     *            "cartIds": [1, 2, 3],
     *            "remark": "请尽快发货"
     *          }
     * 响应体：  {
     *            "code": 200,
     *            "message": "成功",
     *            "data": {
     *              "orderNo": "1825071512345678901",
     *              "totalAmount": 2999.00,
     *              "status": "PENDING_PAYMENT",
     *              "items": [ {...}, {...} ]
     *            }
     *          }
     */
    @PostMapping                                                        // 处理 POST 请求
    public Result<Order> create(@RequestBody OrderCreateRequest request) { // @RequestBody：JSON → Java 对象
        Order order = orderService.createOrder(request);
        return Result.success(order);
    }

    /**
     * 订单列表（分页）
     *
     * 请求方式：GET /api/v1/order/list?page=1&size=10&status=PENDING_PAYMENT
     * 参数：page（默认1）、size（默认10）、status（可选）
     */
    @GetMapping("/list")
    public Result<Page<Order>> list(
            @RequestParam(defaultValue = "1") Integer page,               // 页码，默认 1
            @RequestParam(defaultValue = "10") Integer size,              // 每页条数，默认 10
            @RequestParam(required = false) String status                 // 状态筛选，非必填
    ) {
        Page<Order> orderPage = orderService.getOrderList(page, size, status);
        return Result.success(orderPage);
    }

    /**
     * 订单详情（含订单项 + 物流）
     *
     * 请求方式：GET /api/v1/order/{orderId}
     * 示例：GET /api/v1/order/1
     */
    @GetMapping("/{orderId}")
    public Result<Order> detail(@PathVariable Long orderId) {
        Order order = orderService.getOrderDetail(orderId);
        return Result.success(order);
    }

    /**
     * 取消订单
     *
     * 请求方式：PUT /api/v1/order/cancel/{orderId}
     * 约束：只能取消自己的订单 + 只有 PENDING_PAYMENT 状态可取消
     * 取消后自动恢复库存
     */
    @PutMapping("/cancel/{orderId}")
    public Result<Void> cancel(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return Result.success();
    }

    /**
     * 确认收货
     *
     * 请求方式：PUT /api/v1/order/{orderId}/receive
     * 约束：只能确认自己的订单 + 只有 DELIVERED（已发货）状态可确认
     */
    @PutMapping("/{orderId}/receive")
    public Result<Void> receive(@PathVariable Long orderId) {
        orderService.confirmReceive(orderId);
        return Result.success();
    }

}