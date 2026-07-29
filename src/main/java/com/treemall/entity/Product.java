package com.treemall.entity;                                            // 实体类包

import com.baomidou.mybatisplus.annotation.*;                           // MyBatis-Plus 注解
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;                                                     // 自动生成 getter/setter
import java.math.BigDecimal;                                             // 价格用 BigDecimal（数据库 DECIMAL 对应）
import java.time.LocalDateTime;

/**
 * 商品实体 — 映射数据库表 t_product
 *
 * 核心：价格存 DECIMAL，规格参数用 JSON 格式存储
 */
@Data
@TableName("t_product")                                                // 映射表名
public class Product {

    @TableId(type = IdType.ASSIGN_ID)                                   // 雪花 ID 主键
    @JsonSerialize(using = ToStringSerializer.class)                       // 序列化，避免雪花id在前端截断而丢失
    private Long id;                                                    // 商品 ID

    private Long categoryId;                                            // 所属分类 ID

    private Long merchantId;                                            // 商户 ID（关联 t_user.id）

    private String name;                                                  // 商品名称

    private String description;                                           // 商品描述

    private BigDecimal price;                                             // 售价（元，数据库 DECIMAL）

    private BigDecimal originalPrice;                                    // 原价（用于划线价展示）

    private Integer stock;                                                // 库存数量

    private String mainImage;                                            // 主图 URL

    /**
     * 商品图片列表（JSON 数组）
     * 数据库类型：JSON，存储如 ["/images/p1.jpg","/images/p2.jpg"]
     * 使用场景：商品详情页的轮播图展示，前端拿到后用 swiper 组件渲染
     * 注意：MySQL 的 JSON 类型通过 JDBC 驱动自动映射为 Java String
     * 读取时是 JSON 字符串，前端自行 JSON.parse() 解析
     */
    private String images;                                                  // 商品图片列表（JSON 字符串）

    /**
     * 规格参数（JSON 对象）
     * 数据库类型：JSON，存储如 {"品牌":"华为","屏幕":"6.7英寸","内存":"8GB"}
     * 使用场景：商品详情页的参数展示区，前端解析后以表格形式渲染
     * V1 不做多规格 SKU，规格仅作为展示信息，不影响价格和库存
     */
    private String specs;                                                   // 规格参数（JSON 字符串）

    private Integer salesCount;                                          // 累计销量

    private Integer status;                                              // 状态：1=上架, 0=下架

    @TableLogic                                                        // 逻辑删除
    private Integer deleted;                                            // 0=未删除, 1=已删除

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
