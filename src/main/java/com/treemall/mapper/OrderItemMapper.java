package com.treemall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.treemall.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项 Mapper 接口
 *
 * 协作关系：
 *   OrderServiceImpl → OrderItemMapper → 批量插入订单项 / 按订单ID查询订单项列表
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    // BaseMapper 够用
}