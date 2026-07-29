package com.treemall.controller;                                         // 控制器包

import com.treemall.common.Result;                                      // 统一响应格式
import com.treemall.entity.Banner;                                      // 轮播图实体
import com.treemall.service.BannerService;                              // 轮播图服务
import lombok.RequiredArgsConstructor;                                   // 构造器注入
import org.springframework.web.bind.annotation.*;                           // Spring MVC 注解

import java.util.List;

/**
 * 轮播图控制器
 *
 * 使用场景：小程序首页加载时，前端调用此接口获取轮播图数据
 * 权限：公开接口，无需 Token（WebConfig 中已排除 /api/v1/banner/**）
 *
 * 请求路径前缀：/api/v1/banner
 */
@RestController                                                         // = @Controller + @ResponseBody，所有方法返回 JSON
@RequestMapping("/api/v1/banner")                                         // 统一路径前缀
@RequiredArgsConstructor                                                    // 构造器注入
public class BannerController {

    private final BannerService bannerService;                             // 轮播图服务

    /**
     * 获取首页轮播图列表
     *
     * 请求方式：GET /api/v1/banner/list
     * 无需 Token（公开访问）
     *
     * 响应体示例：
     * {
     *   "code": 200,
     *   "message": "成功",
     *   "data": [
     *     {
     *       "id": 1,
     *       "title": "夏季空调大促",
     *       "imageUrl": "/images/banner/banner1.jpg",
     *       "linkType": "CATEGORY",
     *       "linkTarget": "13",
     *       "sortOrder": 1,
     *       "status": 1
     *     },
     *     ...
     *   ]
     * }
     *
     * 前端处理逻辑：
     *   1. 小程序 onLaunch 或首页 onLoad 时调用此接口
     *   2. 拿到 data 数组后，用 swiper 组件渲染轮播图
     *   3. 点击轮播图时，根据 linkType 判断跳转目标：
     *      PRODUCT  → navigateTo 商品详情页
     *      CATEGORY → navigateTo 分类列表页
     *      NONE     → 不做跳转
     */
    @GetMapping("/list")                                                 // 处理 GET 请求
    public Result<List<Banner>> list() {
        // 调用 Service 查询所有启用的轮播图
        List<Banner> banners = bannerService.getActiveBanners();
        // 包装为统一响应格式返回（列表为空也正常返回，前端做空状态处理）
        return Result.success(banners);
    }
}