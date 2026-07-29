package com.treemall.service;                                           // 服务接口包

import com.treemall.entity.Category;                                   // 分类实体

import java.util.List;

/**
 * 商品分类服务接口
 */
public interface CategoryService {

    /**
     * 获取所有分类列表（树形结构）
     * V1 只支持二级分类，返回扁平列表，前端自己转树形
     *
     * @return 所有分类，按 sort_order 排序
     */
    List<Category> getAllCategories();
}