package com.treemall.service.impl;                                      // 服务实现包

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // MyBatis-Plus 条件构造器
import com.treemall.entity.Category;                                   // 分类实体
import com.treemall.mapper.CategoryMapper;                             // Mapper
import com.treemall.service.CategoryService;                           // 服务接口
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品分类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<Category> getAllCategories() {
        // 构造查询条件：deleted = 0，按 sort_order 排序
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getDeleted, 0)                             // 只查未删除的
                .orderByAsc(Category::getSortOrder);                     // 按排序值升序

        return categoryMapper.selectList(wrapper);                       // 查询列表
    }
}