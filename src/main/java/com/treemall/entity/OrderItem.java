package com.treemall.entity;                                            // 实体类包

import com.baomidou.mybatisplus.annotation.*;                           // MyBatis-Plus 注解
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;                                                     // Lombok
import java.math.BigDecimal;                                             // 金额
import java.time.LocalDateTime;

/**
 * 订单项实体 — 映射数据库表 t_order_item
 *
 * 设计要点：快照设计
 *   一个订单包含多个订单项，每个订单项对应一个购买的商品
 *
 * 为什么存快照而不是关联 t_product？
 *   1. 商品可能改名：如"iPhone 15" → "iPhone 15 Pro"
 *   2. 商品可能换图：主图更换后，历史订单的展示图片不应变化
 *   3. 商品可能调价：价格从 5999 涨到 6999，历史订单价格不受影响
 *   4. 商品可能下架/删除：但订单中的商品信息必须保留
 *
 * 所以：product_name、product_image、price 三个字段是下单时刻的"冻结副本"
 *       product_id 保留用于关联查询（如"再次购买"功能），但不作为展示依据
 */
@Data
@TableName("t_order_item")                                              // 映射表名
public class OrderItem {

    @TableId(type = IdType.ASSIGN_ID)                                   // 雪花 ID 主键
    @JsonSerialize(using = ToStringSerializer.class)                       // 序列化，避免雪花id在前端截断而丢失
    private Long id;                                                    // 订单项 ID

    private Long orderId;                                               // 所属订单 ID（关联 t_order.id）

    private Long productId;                                               // 商品 ID（保留关联，用于"再次购买"等查询，不取当前价格/名称）

    private String productName;                                           // 商品名称（快照：下单时的名称，后续改名不影响）

    private String productImage;                                          // 商品主图（快照：下单时的图片 URL）

    private BigDecimal price;                                             // 下单时单价（快照，元），后续调价不影响

    private Integer quantity;                                             // 购买数量
    private String description;                                           // 商品描述（快照：下单时的描述，后续修改不影响）

    private String specs;                                                 // 商品规格（快照：下单时的规格 JSON，后续修改不影响）

    @TableLogic                                                        // 逻辑删除
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)                                 // INSERT 自动填充
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)                            // INSERT/UPDATE 自动填充
    private LocalDateTime updatedAt;
}