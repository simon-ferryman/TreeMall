package com.treemall.entity;                                            // 实体类包

import com.baomidou.mybatisplus.annotation.*;                           // MyBatis-Plus 注解
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;                                                     // 自动生成 getter/setter/toString
import java.time.LocalDateTime;                                         // Java 8 时间类型

/**
 * 用户实体类 — 映射数据库表 t_user
 *
 * MyBatis-Plus 映射规则：
 *   类名 User → 自动映射表 t_user（application.yml 配置了 table-prefix: t_）
 *   字段名 userId → 自动映射列 user_id（驼峰转下划线）
 *   逻辑删除字段 deleted → 自动拼接 WHERE deleted=0
 */
@Data                                                                   // Lombok：自动生成 getter/setter/toString/equals/hashCode
@TableName("t_user")                                                    // 显式指定表名（与 table-prefix 配合）
public class User {

    @TableId(type = IdType.ASSIGN_ID)                                    // 主键策略：雪花 ID（ASSIGN_ID = 自动生成 19 位数字）
    @JsonSerialize(using = ToStringSerializer.class)                     // 序列化，避免雪花id在前端截断而丢失
    private Long id;                                                    // 用户 ID

    private String openid;                                              // 微信 openid（小程序唯一标识）

    private String nickname;                                            // 用户昵称

    private String avatarUrl;                                           // 头像 URL（驼峰 → avatar_url）

    private String phone;                                               // 手机号

    private String role;                                                // 角色：consumer=消费者, merchant=商户, admin=管理员

    private Integer status;                                             // 状态：1=正常, 0=禁用

    @TableLogic                                                        // 逻辑删除标记：MyBatis-Plus 自动处理
    private Integer deleted;                                            // 0=未删除, 1=已删除

    @TableField(fill = FieldFill.INSERT)                               // 插入时自动填充（由 MybatisPlusConfig 配置）
    private LocalDateTime createdAt;                                    // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)                        // 插入和更新时自动填充
    private LocalDateTime updatedAt;                                    // 更新时间
}