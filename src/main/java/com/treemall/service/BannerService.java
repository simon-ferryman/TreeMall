package com.treemall.service;                                           // 服务接口包

import com.treemall.entity.Banner;                                      // 轮播图实体
import java.util.List;                                                  // 返回列表

/**
 * 轮播图服务接口
 *
 * 功能：查询启用的轮播图列表（按 sort_order 升序排列）
 *
 * 使用场景：
 *   小程序首页加载时，前端调用 GET /api/v1/banner/list
 *   后端返回所有 status=1 且未删除的轮播图，按 sort_order 排序
 *   前端拿到数据后用 swiper 组件渲染轮播图
 *
 * 权限：公开接口，无需登录
 */
public interface BannerService {

    /**
     * 查询所有启用的轮播图（按 sort_order 升序排列）
     *
     * 筛选条件：
     *   - status = 1（启用状态）
     *   — deleted = 0（未删除，@TableLogic 自动处理）
     *
     * 排序规则：sort_order 升序（值越小越靠前）
     *
     * @return 轮播图列表（可能为空列表）
     */
    List<Banner> getActiveBanners();
}