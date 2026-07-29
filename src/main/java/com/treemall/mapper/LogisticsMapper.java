package com.treemall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.treemall.entity.Logistics;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物流 Mapper 接口
 *
 * 协作关系：
 *   OrderServiceImpl → LogisticsMapper → 插入物流记录 / 按订单ID查询物流
 */
@Mapper
public interface LogisticsMapper extends BaseMapper<Logistics> {
    // BaseMapper 够用
}