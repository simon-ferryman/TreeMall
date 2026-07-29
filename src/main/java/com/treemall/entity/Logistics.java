package com.treemall.entity;                                            // 实体类包

import com.baomidou.mybatisplus.annotation.*;                           // MyBatis-Plus 注解
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;                                                     // Lombok
import java.time.LocalDateTime;

/**
 * 物流实体 — 映射数据库表 t_logistics
 *
 * 设计要点：
 *   一个订单对应一条物流记录（通过 order_id 唯一约束保证）
 *   物流状态机：PENDING → IN_TRANSIT → DELIVERED
 *
 * 使用场景：
 *   商户发货时 → 插入一条物流记录（order_id + company_name + tracking_no）
 *   用户查看物流时 → 根据 order_id 查询物流记录，展示物流公司和单号
 *   V1 不做物流轨迹追踪（需对接第三方物流 API），仅展示物流公司+单号
 */
@Data
@TableName("t_logistics")                                               // 映射表名
public class Logistics {

    @TableId(type = IdType.ASSIGN_ID)                                   // 雪花 ID 主键
    @JsonSerialize(using = ToStringSerializer.class)                       // 序列化，避免雪花id在前端截断而丢失
    private Long id;                                                    // 物流 ID

    private Long orderId;                                               // 关联订单 ID（唯一，一个订单一条物流）

    private String companyName;                                           // 物流公司名称，如 "顺丰速运"

    private String trackingNo;                                            // 物流单号，如 "SF1234567890"

    /**
     * 物流状态
     * PENDING    = 待揽收（商户刚填写物流信息，快递员还未取件）
     * IN_TRANSIT = 运输中（快递已揽收，正在运输）
     * DELIVERED  = 已签收（用户已收货）
     */
    private String status;                                                // 物流状态：PENDING / IN_TRANSIT / DELIVERED

    private LocalDateTime shipTime;                                         // 发货时间（商户填写物流的时间）

    private LocalDateTime arriveTime;                                       // 签收时间（用户确认收货的时间）

    @TableLogic                                                             // 逻辑删除
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)                                 // INSERT 自动填充
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)                            // INSERT/UPDATE 自动填充
    private LocalDateTime updatedAt;
}