package com.treemall.entity;                                            // 实体类包

import com.baomidou.mybatisplus.annotation.*;                           // MyBatis-Plus 注解
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;                                                     // Lombok：自动生成 getter/setter

import java.time.LocalDateTime;                                         // Java 8 时间类型

/**
 * 轮播图实体 — 映射数据库表 t_banner
 *
 * 使用场景：小程序首页顶部的轮播图展示
 * 数据流向：商户后台管理 → t_banner 表 → 前端 GET /api/v1/banner/list → 小程序轮播组件渲染
 *
 * 设计要点：
 *   1. link_type 决定点击后的跳转行为：
 *      PRODUCT  → 跳转商品详情页（link_target = 商品ID）
 *      CATEGORY → 跳转分类列表页（link_target = 分类ID）
 *      NONE     → 不跳转，纯展示
 *   2. sort_order 控制轮播顺序，值越小越靠前
 *   3. status 控制是否展示：1=启用，0=禁用（商户可临时下线某张轮播图）
 *   4. 公开接口，无需登录即可访问（WebConfig 中已排除 /api/v1/banner/**）
 */
@Data                                                                   // 自动生成 getter/setter/toString
@TableName("t_banner")                                                  // 映射表名（MyBatis-Plus 自动加 t_ 前缀）
public class Banner {

    @TableId(type = IdType.ASSIGN_ID)                                   // 主键：雪花 ID，自动生成
    @JsonSerialize(using = ToStringSerializer.class)                       // 序列化，避免雪花id在前端截断而丢失
    private Long id;                                                    // 轮播图 ID

    private String title;                                                 // 轮播图标题（展示在图片下方或作为 alt 文本）

    private String imageUrl;                                              // 轮播图图片 URL（如 /images/banner/banner1.jpg）

    /**
     * 跳转类型
     * PRODUCT  — 点击跳转商品详情，link_target 存商品 ID
     * CATEGORY — 点击跳转分类页，link_target 存分类 ID
     * NONE     — 无跳转，纯展示
     */
    private String linkType;

    /**
     * 跳转目标
     * 当 link_type = PRODUCT 时，存商品 ID（如 "1"）
     * 当 link_type = CATEGORY 时，存分类 ID（如 "5"）
     * 当 link_type = NONE 时，为 null
     */
    private String linkTarget;

    private Integer sortOrder;                                            // 排序值：越小越靠前（如 1, 2, 3...）

    private Integer status;                                              // 状态：1=启用（展示）, 0=禁用（隐藏）

    @TableLogic                                                        // 逻辑删除：删除时标记 deleted=1，不真删
    private Integer deleted;                                            // 0=未删除, 1=已删除

    @TableField(fill = FieldFill.INSERT)                                 // INSERT 时自动填充（由 MetaObjectHandler 处理）
    private LocalDateTime createdAt;                                      // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)                            // INSERT 和 UPDATE 时自动填充
    private LocalDateTime updatedAt;                                      // 更新时间
}