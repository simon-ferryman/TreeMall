package com.treemall.entity;                                            // 实体类包
import com.fasterxml.jackson.annotation.JsonFormat;
import com.baomidou.mybatisplus.annotation.*;                           // MyBatis-Plus 注解
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;                                                     // Lombok
import java.math.BigDecimal;                                             // 金额：精确计算，避免浮点误差
import java.time.LocalDateTime;                                         // Java 8 时间类型
import java.util.List;                                                  // 订单项列表（非数据库字段）

/**
 * 订单实体 — 映射数据库表 t_order
 *
 * 设计要点：
 *   1. 订单号（orderNo）使用雪花 ID 生成，19 位数字，全局唯一
 *   2. 地址快照（addressSnapshot）JSON 存储，下单时复制，防止后续地址修改影响历史订单
 *   3. 金额（totalAmount）BigDecimal 类型，与数据库 DECIMAL(10,2) 对应
 *   4. 订单状态机：PENDING_PAYMENT → PENDING_DELIVERY → DELIVERED → RECEIVED
 *      取消路径：PENDING_PAYMENT → CANCELLED（仅待支付状态可取消）
 *   5. items 字段（@TableField(exist = false)）不映射数据库，仅用于返回订单详情时携带订单项
 *   6. logistics 字段（@TableField(exist = false)）不映射数据库，仅用于返回订单详情时携带物流信息
 */
@Data                                                                   // 自动生成 getter/setter/toString
@TableName("t_order")                                                   // 映射表名（order 是 MySQL 保留字，加 t_ 前缀后安全）
public class Order {

    @TableId(type = IdType.ASSIGN_ID)                                   // 主键：雪花 ID，自动生成
    @JsonSerialize(using = ToStringSerializer.class)                       // 序列化，避免雪花id在前端截断而丢失
    private Long id;                                                    // 订单 ID（内部主键，不对外暴露）

    /**
     * orderNo为订单编号（对外展示）
     * 雪花 ID 生成，如 "1825071512345678901"
     * 用户查看订单、联系客服时使用此编号
     */
    private String orderNo;

    private Long userId;                                                // 下单用户 ID

    /**
     * 订单总金额（元）
     * BigDecimal 用于精确货币计算，不会出现 0.1 + 0.2 = 0.30000000000000004 的问题，做到精准无误
     * 数据库存储 DECIMAL(10,2)，Java 运算同样用 BigDecimal 保证精度
     */
    private BigDecimal totalAmount;

    /**
     * 订单状态
     * PENDING_PAYMENT  = 待支付（初始状态）
     * PENDING_DELIVERY = 待发货（支付成功后）
     * DELIVERED        = 已发货（商户填写物流后）
     * RECEIVED         = 已收货（用户确认收货后，终态）
     * CANCELLED        = 已取消（仅 PENDING_PAYMENT 状态可取消）
     */
    private String status;

    /**
     * 收货地址快照（JSON 字符串）
     * 下单时把完整地址 JSON 序列化存入，后续用户修改地址不影响历史订单
     * 内容示例：{"receiverName":"张三","receiverPhone":"13800138000","province":"广东省","city":"广州市","district":"天河区","detailAddress":"体育西路100号"}
     */
    private String addressSnapshot;

    private String remark;                                                // 用户备注信息（选择性填写，如"请发顺丰"）

    private String transactionId;                                         // 微信支付流水号（支付成功后由微信回调填入，用于回调幂等判断）

    @JsonFormat(pattern = "yyyy年MM月dd日 HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime payTime;                                          // 支付时间（支付成功时记录）
    @JsonFormat(pattern = "yyyy年MM月dd日 HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime deliverTime;                                       // 发货时间（商户发货时记录）
    @JsonFormat(pattern = "yyyy年MM月dd日 HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime receiveTime;                                       // 收货时间（用户确认收货时记录）

    @TableLogic                                                        // 逻辑删除：DELETE 时自动转 UPDATE SET deleted=1
    private Integer deleted;
    @JsonFormat(pattern = "yyyy年MM月dd日 HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)                                 // INSERT 时自动填充（由 MetaObjectHandler 处理）
    private LocalDateTime createdAt;                                      // 创建时间 = 下单时间
    @TableField(fill = FieldFill.INSERT_UPDATE)                            // INSERT 和 UPDATE 时自动填充
    @JsonFormat(pattern = "yyyy年MM月dd日 HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;                                      // 更新时间

    // ============================================
    // 以下字段不映射数据库（@TableField(exist = false)）
    // 仅用于组装返回给前端的数据
    // ============================================

    /**
     * 订单项列表（非数据库字段）
     * 使用场景：查询订单详情时，同时查出该订单的所有订单项，填充到这里返回给前端
     * 订单列表中不填充此字段（减少数据传输量）
     */
    @TableField(exist = false)                                           // 告诉 MyBatis-Plus：这个字段不对应数据库任何列
    private List<OrderItem> items;

    /**
     * 物流信息（非数据库字段）
     * 使用场景：查询订单详情时，如果订单已发货，同时查出物流信息填充到这里
     * 订单列表中不填充此字段
     */
    @TableField(exist = false)
    private Logistics logistics;                                          // 关联的物流记录（一个订单一条物流）
}