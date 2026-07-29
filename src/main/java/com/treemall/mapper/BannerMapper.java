package com.treemall.mapper;                                            // Mapper 包

import com.baomidou.mybatisplus.core.mapper.BaseMapper;                 // 通用 Mapper：自带 CRUD + 条件查询
import com.treemall.entity.Banner;                                      // 对应的实体类
import org.apache.ibatis.annotations.Mapper;                            // MyBatis 标记注解

/**
 * 轮播图 Mapper 接口
 *
 * 协作关系：
 *   BannerServiceImpl → BannerMapper → BaseMapper 方法 → 操作 t_banner 表
 *
 * BaseMapper<Banner> 提供的常用方法：
 *   - selectList(wrapper) → 条件查询列表（如：查询所有启用且未删除的轮播图，按 sort_order 排序）
 *   - insert(banner)      → 新增轮播图（商户后台管理用）
 *   - updateById(banner)  → 更新轮播图（商户后台管理用）
 *   - deleteById(id)      → 逻辑删除（标记 deleted=1）
 */
@Mapper                                                                 // Spring 扫描并创建代理实现
public interface BannerMapper extends BaseMapper<Banner> {               // 继承 BaseMapper，泛型指定 Banner
    // V1 阶段 BaseMapper 够用：查询启用轮播图列表
    // 后续商户后台管理时，可能需要自定义分页查询等方法
}