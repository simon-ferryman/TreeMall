package com.treemall.mapper;                                            // Mapper 接口包

import com.baomidou.mybatisplus.core.mapper.BaseMapper;                 // MyBatis-Plus 通用 Mapper
import com.treemall.entity.Category;                                   // 对应的实体类
import org.apache.ibatis.annotations.Mapper;                            // 标记为 Mapper

/**
 * 商品分类 Mapper 接口
 *
 * 功能：查询分类列表（递归或非递归）
 * BaseMapper 提供 selectList/selectPage 等通用方法，够用
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {        // 继承 BaseMapper，直接获得 CRUD
    // 目前不需要自定义 SQL，BaseMapper 够用
}