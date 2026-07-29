package com.treemall.dto.response;                                      // 响应 DTO 包（统一放在这里，不是 vo/）

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;                                                     // Lombok：自动生成 getter/setter
import java.math.BigDecimal;                                             // 金额类型
import java.time.LocalDateTime;                                         // 时间类型

/**
 * 购物车列表响应 DTO — 用于 GET /api/v1/cart/list 接口返回
 *
 * 为什么需要这个 DTO 而不是直接返回 Cart 实体？
 *   因为 Cart 实体只有 productId（商品编号），没有商品名称、价格、图片。
 *   前端拿到购物车列表后需要展示"商品名称 ¥价格 x数量"，这些信息必须从 Product 表关联查询。
 *   如果让前端根据 productId 再逐个请求商品详情接口，N 个商品就要发 N 次 HTTP 请求，性能很差。
 *
 * DTO 的作用：把 Cart 和 Product 的关键字段合并到一个对象中，一次返回给前端。
 *
 * 设计原则：
 *   按后端设计文档，所有响应 DTO 统一放在 dto/response/ 目录下，
 *   请求 DTO 放在 dto/request/ 目录下，保持架构一致性。
 *
 * 使用场景：用户在购物车页面看到的是"商品图片 + 商品名称 + 单价 + 数量 + 是否选中"
 */
@Data                                                                   // 自动生成 getter/setter/toString/equals/hashCode
public class CartVO {

    // ===== 来自 Cart 表的字段 =====
    @JsonSerialize(using = ToStringSerializer.class)
    private Long cartId;                                                 // 购物车项 ID（对应 t_cart.id，用于更新数量/删除操作）
    private Long productId;                                              // 商品 ID（方便前端点击跳转商品详情页）
    private Integer quantity;                                             // 购买数量
    private Integer checked;                                              // 是否选中：1=选中，0=未选中（用于购物车全选/结算）

    // ===== 来自 Product 表的字段（通过 productId 关联查询） =====
    private String productName;                                          // 商品名称（用于展示）
    private BigDecimal price;                                             // 商品单价（元，用于展示和计算小计）
    private String mainImage;                                            // 商品主图 URL（用于展示缩略图）
    private Integer stock;                                                // 当前库存（用于判断是否还能加购、是否显示"库存不足"）

    private LocalDateTime createdAt;                                      // 加入购物车时间（用于排序）
}