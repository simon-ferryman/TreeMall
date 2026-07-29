package com.treemall.entity;                                            // 实体类包

import com.baomidou.mybatisplus.annotation.*;                           // MyBatis-Plus 注解

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;                                                     // 自动生成 getter/setter
import java.time.LocalDateTime;                                         // Java 8 时间类型

/**
 * 收货地址实体 — 映射数据库表 t_address
 *
 * 设计要点：
 *   一个用户可以有多个地址，但只有一个默认地址
 *   is_default 由 Service 层保证唯一性（设置新默认前，先清除旧默认）
 *   删除使用逻辑删除（deleted = 1），已删除的地址不影响历史订单快照
 */
@Data
@TableName("t_address")                                                // 映射表 t_address
public class Address {

    @TableId(type = IdType.ASSIGN_ID)                                   // 主键：雪花 ID
    @JsonSerialize(using = ToStringSerializer.class)                       // 序列化，避免雪花id在前端截断而丢失
    private Long id;                                                    // 地址 ID

    private Long userId;                                                // 所属用户 ID（一个用户多个地址）

    private String receiverName;                                        // 收货人姓名

    private String receiverPhone;                                       // 收货人电话

    private String province;                                            // 省份

    private String city;                                                // 城市

    private String district;                                            // 区/县

    private String detailAddress;                                       // 详细地址（街道门牌号）

    private Integer isDefault;                                           // 是否默认地址：1=是, 0=否

    @TableLogic                                                        // 逻辑删除标记
    private Integer deleted;                                            // 0=未删除, 1=已删除

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;                                      // 创建时间，自动更新

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;                                      // 更新时间，自动更新
}