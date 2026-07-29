package com.treemall.service.impl;                                      // 服务实现包
import java.util.stream.Collectors;
import java.util.Map;
import cn.hutool.core.util.IdUtil;                                      // Hutool 工具：生成雪花 ID 订单号
import cn.hutool.json.JSONUtil;                                         // Hutool JSON 工具：对象 → JSON 字符串
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // 条件构造器：lambda 风格写查询条件
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper; // 更新条件构造器：lambda 风格写更新条件
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;      // 分页对象
import com.treemall.common.BusinessException;                            // 自定义业务异常
import com.treemall.common.UserContext;                                 // 当前用户上下文（ThreadLocal）
import com.treemall.dto.request.DeliverRequest;                         // 发货请求 DTO
import com.treemall.dto.request.OrderCreateRequest;                     // 下单请求 DTO
import com.treemall.entity.*;                                            // 所有实体类
import com.treemall.mapper.*;                                            // 所有 Mapper 接口
import com.treemall.service.OrderService;                               // 订单服务接口
import lombok.RequiredArgsConstructor;                                   // 构造器注入
import lombok.extern.slf4j.Slf4j;                                       // 日志
import org.springframework.stereotype.Service;                              // Spring 服务标记
import org.springframework.transaction.annotation.Transactional;              // 事务管理
import cn.hutool.json.JSONObject;
import java.math.BigDecimal;                                             // 精确金额计算
import java.time.LocalDateTime;                                         // 当前时间
import java.util.ArrayList;                                              // 订单项列表
import java.util.List;

/**
 * 订单服务实现类
 *
 * 涉及 5 张表的联动操作：
 *   t_address（查地址）→ t_cart（查购物车）→ t_product（扣库存）→ t_order（插订单）→ t_order_item（插订单项）
 *
 * 并发安全说明（为什么不用 Redis 锁）：
 *   扣库存 SQL：UPDATE t_product SET stock = stock - ? WHERE id = ? AND stock >= ?
 *   MySQL InnoDB 在执行这条 UPDATE 时会对该行加排他锁（X-Lock），
 *   两个并发请求同时扣同一商品→第二个请求必须等第一个提交后才能执行。
 *   WHERE stock >= ? 保证库存不足时返回 0 行受影响，从而抛出异常。
 *   这是数据库级别的原子操作，V1 单商户场景下并发量极低，无需 Redis 分布式锁。
 *
 * 事务说明：
 *   @Transactional 保证所有操作在同一个事务中，任何一步失败都自动回滚。
 *   例如：扣库存成功但插入订单失败 → 库存自动恢复，不会出现数据不一致。
 */
@Slf4j                                                                  // 自动生成 log 对象
@Service                                                               // 标记为 Spring 服务 Bean
@RequiredArgsConstructor                                                    // 构造器注入所有 final 字段
@Transactional                                                          // 所有 public 方法开启事务，异常自动回滚
public class OrderServiceImpl implements OrderService {

    private final AddressMapper addressMapper;                             // 地址 Mapper
    private final CartMapper cartMapper;                                   // 购物车 Mapper
    private final ProductMapper productMapper;                               // 商品 Mapper
    private final OrderMapper orderMapper;                                  // 订单 Mapper
    private final OrderItemMapper orderItemMapper;                            // 订单项 Mapper
    private final LogisticsMapper logisticsMapper;                            // 物流 Mapper

    // ============================================
    // 用户端：创建订单（下单）
    // ============================================
    @Override
    public Order createOrder(OrderCreateRequest request) {
        // 第 0 步：获取当前用户
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }

        // ============================================
        // 第 1 步：校验收货地址
        // ============================================
        Address address = addressMapper.selectById(request.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(400, "收货地址不存在");
        }

        // ============================================
        // 第 2 步：查询购物车选中项
        // ============================================
        // 条件：ID 在 cartIds 列表中 + 属于当前用户 + 已选中（checked=1）
        LambdaQueryWrapper<Cart> cartWrapper = new LambdaQueryWrapper<>();
        cartWrapper.in(Cart::getId, request.getCartIds())                // WHERE id IN (1, 2, 3)
                .eq(Cart::getUserId, userId)                          // AND user_id = ?
                .eq(Cart::getChecked, 1);                             // AND checked = 1（只结算选中的）

        List<Cart> cartItems = cartMapper.selectList(cartWrapper);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new BusinessException(400, "没有选中的商品");
        }

        // ============================================
        // 第 3 步：遍历购物车 → 校验商品 + 扣库存 + 构建订单项
        // ============================================
        BigDecimal totalAmount = BigDecimal.ZERO;                         // 总金额，初始化为 0
        List<OrderItem> orderItems = new ArrayList<>();                   // 订单项列表，先收集后批量插入

        for (Cart cartItem : cartItems) {
            // 3a. 查询商品
            Product product = productMapper.selectById(cartItem.getProductId());
            if (product == null || product.getStatus() == 0) {
                throw new BusinessException(400,
                        "商品【" + (product != null ? product.getName() : cartItem.getProductId()) + "】已下架");
            }

            // 3b. 原子扣库存
            // 生成的 SQL：
            //   UPDATE t_product SET stock = stock - ? WHERE id = ? AND stock >= ?
            // InnoDB 行锁保证并发安全，WHERE stock >= ? 保证不超卖
            LambdaUpdateWrapper<Product> stockWrapper = new LambdaUpdateWrapper<>();
            stockWrapper.eq(Product::getId, product.getId())
                    .ge(Product::getStock, cartItem.getQuantity())    // 库存足够才执行
                    .setSql("stock = stock - " + cartItem.getQuantity()); // SET stock = stock - quantity

            int rows = productMapper.update(null, stockWrapper);
            if (rows == 0) {                                              // 0 行受影响 = 库存不足
                throw new BusinessException(400,
                        "商品【" + product.getName() + "】库存不足，当前库存：" + product.getStock());
            }

            // 3c. 构建订单项（快照：冻结下单时的商品信息）
            OrderItem item = new OrderItem();
            item.setProductId(product.getId());                           // 商品 ID（关联查询用）
            item.setProductName(product.getName());                       // 快照：商品名称
            item.setProductImage(product.getMainImage());                 // 快照：商品主图
            item.setPrice(product.getPrice());                            // 快照：下单时单价
            item.setQuantity(cartItem.getQuantity());                     // 购买数量
            item.setDescription(product.getDescription());               // 快照：商品描述
            item.setSpecs(product.getSpecs());                           // 快照：商品规格
            orderItems.add(item);

            // 3d. 累加总金额
            // BigDecimal 乘法：price × quantity，然后 add 累加
            BigDecimal itemTotal = product.getPrice()
                    .multiply(new BigDecimal(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        // ============================================
        // 第 4 步：创建订单
        // ============================================
        Order order = new Order();
        // IdUtil.getSnowflake(1, 1) → 创建雪花算法实例（workerId=1, datacenterId=1）
        // .nextIdStr() → 生成下一个 ID 并转为字符串，如 "1825071512345678901"
        order.setOrderNo(IdUtil.getSnowflake(1, 1).nextIdStr());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING_PAYMENT");                               // 初始状态：待支付
        // 将收货地址对象序列化为 JSON 字符串存入订单
        // JSONUtil.toJsonStr(address) 结果示例：
        // {"receiverName":"张三","receiverPhone":"13800138000","province":"广东省","city":"广州市","district":"天河区","detailAddress":"体育西路100号"}
        // order.setAddressSnapshot(JSONUtil.toJsonStr(address));       // 我们只要提取收货人的如下关键信息，其余的ID之类的均不要
        JSONObject addressJson = new JSONObject();
        addressJson.set("receiverName", address.getReceiverName());     // 收货人姓名
        addressJson.set("receiverPhone", address.getReceiverPhone());   // 收货人电话
        addressJson.set("province", address.getProvince());             // 省份
        addressJson.set("city", address.getCity());                     // 城市
        addressJson.set("district", address.getDistrict());             // 区/县
        addressJson.set("detailAddress", address.getDetailAddress());   // 详细地址
        order.setAddressSnapshot(addressJson.toString());

        order.setRemark(request.getRemark());

        orderMapper.insert(order);                                        // 插入 t_order（插入后 order.getId() 自动回填）

        // ============================================
        // 第 5 步：批量插入订单项
        // ============================================
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());                               // 关联订单 ID
            orderItemMapper.insert(item);                                  // 逐条插入（V1 简单实现，V2 可优化为批量 insert）
        }

        // ============================================
        // 第 6 步：清空已结算的购物车项
        // ============================================
        // cartMapper.delete(wrapper) 在 @TableLogic 下实际执行：
        //   UPDATE t_cart SET deleted = 1 WHERE id IN (...) AND user_id = ? AND checked = 1
        cartMapper.delete(cartWrapper);

        // ============================================
        // 第 7 步：返回订单（含订单项）
        // ============================================
        order.setItems(orderItems);                                       // 填充 items 字段（非数据库字段，仅用于返回）

        log.info("订单创建成功: orderNo={}, userId={}, 金额={}, 商品数={}",
                order.getOrderNo(), userId, totalAmount, orderItems.size());
        order.setStatus(convertStatusToFrontend(order.getStatus()));          //将数据库状态转换为前端格式

        return order;
    }

    // ============================================
    // 用户端：订单列表（分页）
    // ============================================
    @Override
    public Page<Order> getOrderList(Integer page, Integer size, String status) {
        Long userId = UserContext.getUserId();

        Page<Order> pageParam = new Page<>(page, size);                   // 创建分页对象

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);                             // 只查当前用户的订单
        // 按状态筛选：将前端小写状态值转换为数据库存储格式
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Order::getStatus, convertStatusToDb(status));
        }
        wrapper.orderByDesc(Order::getCreatedAt);                         // 最新订单排最前面

        // ★ 先执行分页查询，再处理结果（修复：之前错误地在 selectPage 之前调用了 getRecords）
        Page<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);

        // 填充订单项（批量查询，避免 N+1）
        List<Order> orders = orderPage.getRecords();
        if (!orders.isEmpty()) {
            List<Long> orderIds = orders.stream()
                    .map(Order::getId)
                    .collect(Collectors.toList());

            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.in(OrderItem::getOrderId, orderIds);
            List<OrderItem> allItems = orderItemMapper.selectList(itemWrapper);

            // 按 orderId 分组
            Map<Long, List<OrderItem>> itemsMap = allItems.stream()
                    .collect(Collectors.groupingBy(OrderItem::getOrderId));

            for (Order order : orders) {
                order.setItems(itemsMap.getOrDefault(order.getId(), List.of()));
                // 将数据库状态转换为前端格式（PENDING_PAYMENT → pending）
                order.setStatus(convertStatusToFrontend(order.getStatus()));
            }
        }

        return orderPage;                                                  // 返回分页结果（含 total、records 等）
    }


    // ============================================
    // 用户端：订单详情（含订单项 + 物流）
    // ============================================
    @Override
    public Order getOrderDetail(Long orderId) {
        Long userId = UserContext.getUserId();

        // 查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(400, "订单不存在");
        }

        // 查询订单项列表
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        order.setItems(items);                                            // 填充到非数据库字段

        // 查询物流信息（如果已发货）
        LambdaQueryWrapper<Logistics> logisticsWrapper = new LambdaQueryWrapper<>();
        logisticsWrapper.eq(Logistics::getOrderId, orderId);
        Logistics logistics = logisticsMapper.selectOne(logisticsWrapper);
        order.setLogistics(logistics);                                    // 填充到非数据库字段（可能为 null）
        order.setStatus(convertStatusToFrontend(order.getStatus())); //数据库状态转换为前端格式
        return order;
    }

    // ============================================
    // 用户端：取消订单
    // ============================================
    @Override
    public boolean cancelOrder(Long orderId) {
        Long userId = UserContext.getUserId();

        // 查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(400, "订单不存在");
        }

        // 状态校验：只有待支付状态可以取消
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BusinessException(400, "只有待支付状态的订单可以取消，当前状态：" + order.getStatus());
        }

        // 查询订单项（用于恢复库存）
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        // 恢复库存：把每个订单项的数量加回商品库存
        for (OrderItem item : items) {
            LambdaUpdateWrapper<Product> stockWrapper = new LambdaUpdateWrapper<>();
            stockWrapper.eq(Product::getId, item.getProductId())
                    .setSql("stock = stock + " + item.getQuantity()); // SET stock = stock + quantity
            productMapper.update(null, stockWrapper);
        }

        // 更新订单状态为已取消
        order.setStatus("CANCELLED");
        orderMapper.updateById(order);                                    // MyBatis-Plus 只更新非 null 字段

        log.info("订单取消成功: orderNo={}, 恢复库存 {} 件", order.getOrderNo(), items.size());

        return true;
    }
    // ============================================
    // 用户端：确认收货
    // ============================================
    @Override
    public boolean confirmReceive(Long orderId) {
        Long userId = UserContext.getUserId();

        // 查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(400, "订单不存在");
        }

        // 状态校验：只有已发货状态才能确认收货
        if (!"DELIVERED".equals(order.getStatus())) {
            throw new BusinessException(400, "只有已发货状态的订单才能确认收货，当前状态：" + order.getStatus());
        }

        // 更新订单状态为已完成
        order.setStatus("RECEIVED");
        order.setReceiveTime(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("用户确认收货成功: orderNo={}, userId={}", order.getOrderNo(), userId);

        return true;
    }

    // ============================================
    // 商户端：全部订单列表（分页）
    // ============================================
    @Override
    public Page<Order> getMerchantOrderList(Integer page, Integer size, String status) {
        Page<Order> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        // V1 单商户：不按 userId 过滤，显示所有订单
        if (status != null && !status.isEmpty()) {
            // ★ 将前端状态值（pending/paid/shipped/completed）转为数据库格式（PENDING_PAYMENT/PENDING_DELIVERY/...）
            wrapper.eq(Order::getStatus, convertStatusToDb(status));
        }
        wrapper.orderByDesc(Order::getCreatedAt);                         // 最新订单在前

        Page<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);

        // ★ 填充订单项 + 状态转换（与用户端 getOrderList 逻辑一致）
        List<Order> orders = orderPage.getRecords();
        if (!orders.isEmpty()) {
            List<Long> orderIds = orders.stream()
                    .map(Order::getId)
                    .collect(Collectors.toList());

            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.in(OrderItem::getOrderId, orderIds);
            List<OrderItem> allItems = orderItemMapper.selectList(itemWrapper);

            // 按 orderId 分组
            Map<Long, List<OrderItem>> itemsMap = allItems.stream()
                    .collect(Collectors.groupingBy(OrderItem::getOrderId));

            for (Order order : orders) {
                order.setItems(itemsMap.getOrDefault(order.getId(), List.of()));
                // ★ 将数据库状态转换为前端格式
                order.setStatus(convertStatusToFrontend(order.getStatus()));
            }
        }

        return orderPage;
    }

    // ============================================
    // 商户端：订单详情（含订单项 + 物流）
    // ============================================
    @Override
    public Order getMerchantOrderDetail(Long orderId) {
        // 查询订单（无 userId 限制，商户可以查看任何订单）
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(400, "订单不存在");
        }

        // 查询订单项
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        order.setItems(items);

        // 查询物流信息
        LambdaQueryWrapper<Logistics> logisticsWrapper = new LambdaQueryWrapper<>();
        logisticsWrapper.eq(Logistics::getOrderId, orderId);
        Logistics logistics = logisticsMapper.selectOne(logisticsWrapper);
        order.setLogistics(logistics);
        // 将数据库状态转换为前端格式（PENDING_DELIVERY → paid），与 getMerchantOrderList 保持一致
        order.setStatus(convertStatusToFrontend(order.getStatus()));

        return order;
    }

    // ============================================
    // 商户端：发货
    // ============================================
    @Override
    public boolean deliverOrder(Long orderId, DeliverRequest request) {
        // 查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(400, "订单不存在");
        }

        // 状态校验：只有待发货状态才能发货
        if (!"PENDING_DELIVERY".equals(order.getStatus())) {
            throw new BusinessException(400, "只有待发货状态的订单才能发货，当前状态：" + order.getStatus());
        }

        // 校验物流信息
        if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
            throw new BusinessException(400, "物流公司名称不能为空");
        }
        if (request.getTrackingNo() == null || request.getTrackingNo().isBlank()) {
            throw new BusinessException(400, "物流单号不能为空");
        }

        // 更新订单状态
        order.setStatus("DELIVERED");                                     // 状态 → 已发货
        order.setDeliverTime(LocalDateTime.now());                        // 记录发货时间
        orderMapper.updateById(order);

        // 插入物流记录
        Logistics logistics = new Logistics();
        logistics.setOrderId(orderId);                                    // 关联订单
        logistics.setCompanyName(request.getCompanyName());               // 物流公司
        logistics.setTrackingNo(request.getTrackingNo());                 // 物流单号
        logistics.setStatus("PENDING");                                   // 初始状态：待揽收
        logistics.setShipTime(LocalDateTime.now());                       // 发货时间
        logisticsMapper.insert(logistics);

        log.info("订单发货成功: orderNo={}, 物流公司={}, 物流单号={}",
                order.getOrderNo(), request.getCompanyName(), request.getTrackingNo());

        return true;
    }
    // ============================================
    // 状态转换工具方法
    // 数据库存储大写+下划线（PENDING_PAYMENT），前端使用小写短名（pending）
    // ============================================

    /**
     * 前端状态 → 数据库状态（用于筛选条件）
     */
    private String convertStatusToDb(String frontendStatus) {
        if (frontendStatus == null) return null;
        return switch (frontendStatus) {
            case "pending" -> "PENDING_PAYMENT";
            case "paid" -> "PENDING_DELIVERY";
            case "shipped" -> "DELIVERED";
            case "completed" -> "RECEIVED";
            case "cancelled" -> "CANCELLED";
            default -> frontendStatus;
        };
    }

    /**
     * 数据库状态 → 前端状态（用于返回数据）
     */
    private String convertStatusToFrontend(String dbStatus) {
        if (dbStatus == null) return null;
        return switch (dbStatus) {
            case "PENDING_PAYMENT" -> "pending";
            case "PENDING_DELIVERY" -> "paid";
            case "DELIVERED" -> "shipped";
            case "RECEIVED" -> "completed";
            case "CANCELLED" -> "cancelled";
            default -> dbStatus;
        };
    }

}