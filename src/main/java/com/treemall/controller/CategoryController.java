package com.treemall.controller;                                         // 控制器包

import com.treemall.common.Result;                                      // 统一响应
import com.treemall.entity.Category;                                   // 分类实体
import com.treemall.service.CategoryService;                           // 分类服务
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品分类控制器
 *
 * 接口：获取全部分类列表（用于首页分类导航）
 */
@RestController
@RequestMapping("/api/v1/category")                                   // 统一路径前缀
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取所有分类列表（V1 二级分类，扁平结构）
     *
     * 请求方式：GET /api/v1/category/list
     * 不需要 Token（分类列表公开可见）
     *
     * @return 全部分类列表，按排序值升序
     */
    @GetMapping("/list")
    public Result<List<Category>> list() {
        List<Category> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }
}