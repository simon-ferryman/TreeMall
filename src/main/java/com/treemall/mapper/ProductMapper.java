package com.treemall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.treemall.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品 Mapper 接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    // BaseMapper 够用，暂不需要自定义
}