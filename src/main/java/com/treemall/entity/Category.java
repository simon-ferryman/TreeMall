package com.treemall.entity;                                            // 实体类包

import com.baomidou.mybatisplus.annotation.*;                           // MyBatis-Plus 注解
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;                                                     // 自动生成 getter/setter
import java.time.LocalDateTime;                                         // Java 8 时间类型

/**
 * 商品分类实体 — 映射数据库表 t_category
 *
 * 设计：V1 做二级分类，通过 parent_id 关联，天然支持无限级分类
 */
@Data
@TableName("t_category")                                               // 显式指定表名
public class Category {

    @TableId(type = IdType.ASSIGN_ID)                                   // 主键：雪花 ID
    @JsonSerialize(using = ToStringSerializer.class)                       // 序列化，避免雪花id在前端截断而丢失
    private Long id;                                                    // 分类 ID

    private String name;                                                  // 分类名称

    private Long parentId;                                               // 父分类 ID，0 表示顶级分类

    private Integer sortOrder;                                            // 排序值，越小越靠前

    private String icon;                                                  // 分类图标 URL

    @TableLogic                                                        // 逻辑删除
    private Integer deleted;                                            // 0=未删除, 1=已删除

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}