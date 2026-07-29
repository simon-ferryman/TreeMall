package com.treemall.service;                                           // 服务接口包

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;      // MyBatis-Plus 分页对象
import com.treemall.dto.request.DeliverRequest;                         // 发货请求 DTO
import com.treemall.dto.request.OrderCreateRequest;                     // 下单请求 DTO
import com.treemall.entity.Order;                                       // 订单实体

/**
 * 订单服务接口
 *
 * 包含用户端 + 商户端的所有订单业务方法：
 *
 * 用户端（3个方法）：
 *   1. createOrder     — 下单（购物车结算 → 扣库存 → 生成订单 → 清空购物车）
 *   2. getOrderList    — 当前用户订单列表（分页 + 状态筛选）
 *   3. getOrderDetail  — 订单详情（含订单项 + 物流）
 *   4. cancelOrder     — 取消订单（仅待支付状态可取消，恢复库存）
 *   5. comfirmReceive  — 确认收货（仅待收货状态可进行，）
 *
 * 商户端（3个方法）：
 *   5. getMerchantOrderList   — 全部订单列表（分页 + 状态筛选）
 *   6. getMerchantOrderDetail — 订单详情（含订单项 + 物流）
 *   7. deliverOrder           — 发货（更新订单状态 + 插入物流记录）
 */
public interface OrderService {

    // ============================================
    // 用户端方法
    // ============================================

    /**
     * 创建订单（下单）
     *
     * 事务包含 6 步操作，任何一步失败都回滚：
     *   1. 校验收货地址（存在 + 属于当前用户）
     *   2. 查询购物车选中项（checked=1 + 属于当前用户）
     *   3. 遍历购物车：校验商品上架 + 原子扣库存 + 构建订单项快照 + 累加金额
     *   4. 生成订单号 → 插入 t_order（含地址快照 JSON）
     *   5. 批量插入 t_order_item（商品快照）
     *   6. 逻辑删除已结算的购物车项
     *
     * @param request 下单请求（addressId + cartIds + remark）
     * @return 创建成功的订单（含订单项列表）
     */
    Order createOrder(OrderCreateRequest request);

    /**
     * 当前用户订单列表（分页）
     *
     * @param page   页码（从 1 开始）
     * @param size   每页条数
     * @param status 状态筛选（可选，如 "PENDING_PAYMENT"）
     * @return 分页结果
     */
    Page<Order> getOrderList(Integer page, Integer size, String status);

    /**
     * 订单详情（含订单项 + 物流）
     * 安全校验：只能查看自己的订单
     *
     * @param orderId 订单 ID
     * @return 订单详情（items 和 logistics 已填充）
     */
    Order getOrderDetail(Long orderId);

    /**
     * 取消订单
     * 约束：只能取消自己的订单 + 只能是 PENDING_PAYMENT 状态
     * 取消后自动恢复库存
     *
     * @param orderId 订单 ID
     * @return 成功返回 true
     */
    boolean cancelOrder(Long orderId);

    // ============================================
    // 商户端方法
    // ============================================

    /**
     * 商户端：全部订单列表（分页）
     * V1 单商户：显示所有订单，不按商户过滤
     * V2 多商户：增加 merchantId 过滤
     *
     * @param page   页码
     * @param size   每页条数
     * @param status 状态筛选（可选）
     * @return 分页结果
     */
    Page<Order> getMerchantOrderList(Integer page, Integer size, String status);

    /**
     * 商户端：订单详情（含订单项 + 物流）
     * 无 userId 限制（商户可以查看任何订单）
     *
     * @param orderId 订单 ID
     * @return 订单详情
     */
    Order getMerchantOrderDetail(Long orderId);

    /**
     * 商户端：发货
     *
     * 业务流程：
     *   1. 查询订单，校验状态为 PENDING_DELIVERY（待发货）
     *   2. 更新订单状态为 DELIVERED，记录发货时间
     *   3. 插入物流记录（company_name + tracking_no + status=PENDING）
     *
     * @param orderId 订单 ID
     * @param request 发货请求（companyName + trackingNo）
     * @return 成功返回 true
     */
    boolean deliverOrder(Long orderId, DeliverRequest request);
    /**
     * 确认收货
     * 约束：只能确认自己的订单 + 只能是 DELIVERED（已发货）状态
     * 确认后订单状态变为 RECEIVED（已完成），记录收货时间
     *
     * @param orderId 订单 ID
     * @return 成功返回 true
     */
    boolean confirmReceive(Long orderId);
}