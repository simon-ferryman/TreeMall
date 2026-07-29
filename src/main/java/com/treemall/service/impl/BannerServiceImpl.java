package com.treemall.service.impl;                                      // 服务实现包

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // 条件构造器：lambda 风格写查询条件
import com.treemall.entity.Banner;                                      // 轮播图实体
import com.treemall.mapper.BannerMapper;                                // 轮播图 Mapper
import com.treemall.service.BannerService;                              // 轮播图服务接口
import lombok.RequiredArgsConstructor;                                   // 构造器注入
import lombok.extern.slf4j.Slf4j;                                       // 日志
import org.springframework.stereotype.Service;                              // Spring 服务标记

import java.util.List;

/**
 * 轮播图服务实现
 *
 * 这是项目中最简单的 Service 实现之一：
 *   - 无事务（只读操作，不需要 @Transactional）
 *   - 无权限校验（公开接口，任何人都能访问）
 *   - 无复杂业务逻辑（就是条件查询 + 排序）
 *
 * 设计原则：
 *   简单的查询不需要 @Transactional，因为只读操作不会破坏数据一致性
 *   只有写操作（INSERT/UPDATE/DELETE）才需要事务保护
 */
@Slf4j                                                                  // 自动生成 log 对象
@Service                                                               // 标记为 Spring 服务 Bean
@RequiredArgsConstructor                                                    // 构造器注入
public class BannerServiceImpl implements BannerService {

    private final BannerMapper bannerMapper;                               // 轮播图 Mapper，构造器注入

    /**
     * 查询所有启用的轮播图
     *
     * 实现逻辑：
     *   1. 构建查询条件：status = 1（启用）
     *   2. 排序：sort_order 升序
     *   3. 执行查询：bannerMapper.selectList(wrapper)
     *   4. 返回结果
     *
     * 注意：deleted = 0 的条件由 @TableLogic 自动添加，不需要手动写
     *       MyBatis-Plus 会在所有 SELECT 语句中自动拼接 AND deleted = 0
     */
    @Override
    public List<Banner> getActiveBanners() {
        // 构建查询条件
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        // 条件：只查启用状态的轮播图（status = 1）
        wrapper.eq(Banner::getStatus, 1)
                // 排序：按 sort_order 升序（值越小越靠前，如 1, 2, 3...）
                .orderByAsc(Banner::getSortOrder);

        // 执行查询
        // 生成的 SQL：
        //   SELECT * FROM t_banner WHERE status = 1 AND deleted = 0 ORDER BY sort_order ASC
        // 其中 deleted = 0 由 @TableLogic 自动添加
        List<Banner> banners = bannerMapper.selectList(wrapper);

        log.debug("查询到 {} 条启用的轮播图", banners.size());            // 调试日志：方便排查轮播图是否正常加载

        return banners;
    }
}