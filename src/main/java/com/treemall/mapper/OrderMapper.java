package com.treemall.mapper;                                            // Mapper 包

import com.baomidou.mybatisplus.core.mapper.BaseMapper;                 // 通用 Mapper：自带 CRUD + 分页
import com.treemall.entity.Order;                                       // 对应的实体类
import org.apache.ibatis.annotations.Mapper;                            // MyBatis 标记注解

/**
 * 订单 Mapper 接口
 *
 * 协作关系：
 *   OrderServiceImpl → OrderMapper → BaseMapper 方法 → 操作 t_order 表
 *
 * BaseMapper<Order> 提供的常用方法：
 *   - insert(order)          → INSERT INTO t_order ...
 *   - updateById(order)      → UPDATE t_order SET ... WHERE id=?
 *   - selectById(id)         → SELECT * FROM t_order WHERE id=?
 *   - selectPage(page, wrapper) → 分页查询（配合 PaginationInnerInterceptor）
 *   - selectList(wrapper)    → 条件查询列表
 *   - deleteById(id)         → 逻辑删除（自动转 UPDATE SET deleted=1）
 */
@Mapper                                                                 // Spring 扫描并创建代理实现
public interface OrderMapper extends BaseMapper<Order> {                 // 继承 BaseMapper，泛型指定 Order
    // V1 阶段 BaseMapper 够用，后续复杂查询（如 JOIN 用户表）再添加自定义方法
}