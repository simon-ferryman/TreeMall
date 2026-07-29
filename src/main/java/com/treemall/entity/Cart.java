package com.treemall.entity;                                            // 声明包路径：在 entity 包下

import com.baomidou.mybatisplus.annotation.*;                           // 导入 MyBatis-Plus 的注解：Id 类型、表名、字段填充等

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;                                                     // Lombok 注解：自动生成 getter/setter/toString

import java.time.LocalDateTime;                                         // Java 8 时间类型，比 Date 更好用

/**
 * 购物车实体类 — 映射数据库表 t_cart
 *
 * 设计：每个用户对每个商品只有一条购物车记录，再次添加购物车时更新数量
 * 主键：雪花 ID 策略，自动生成唯一 ID
 */
@Data                                                                   // Lombok：自动生成所有 getter/setter/toString 等方法，不用手写
@TableName("t_cart")                                                    // 告诉 MyBatis-Plus 这个类映射哪张表
public class Cart {

    @TableId(type = IdType.ASSIGN_ID)                                   // 主键生成策略：ASSIGN_ID = MyBatis-Plus 自动生成雪花 ID
    @JsonSerialize(using = ToStringSerializer.class)                       // 序列化，避免雪花id在前端截断而丢失
    private Long id;                                                    // 购物车项主键 ID

    private Long userId;                                                // 属于哪个用户（每个购物车项绑定一个用户）
    private Long productId;                                             // 绑定哪个商品
    private Integer quantity;                                             // 商品数量（用户可能加多次，每次加 1，数量累加）
    private Integer checked;                                              // 是否选中：1=选中，0=未选中，提交订单默认只包含选中的


    @TableLogic                                                        // 逻辑删除标记：MyBatis-Plus 查询时自动加条件 deleted=0
    private Integer deleted;                                            // 0=未删除，1=已删除

    @TableField(fill = FieldFill.INSERT)                                 // 插入时自动填充创建时间
    private LocalDateTime createdAt;                                      // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)                            // 插入和更新时自动填充更新时间
    private LocalDateTime updatedAt;                                      // 更新时间
}