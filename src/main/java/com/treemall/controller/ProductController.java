package com.treemall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.treemall.common.Result;
import com.treemall.entity.Product;
import com.treemall.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器
 *
 * 接口：商品列表、商品详情
 */
@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 分页查询商品列表
     *
     * 请求方式：GET /api/v1/product/list?page=1&pageSize=10&keyword=手机&categoryId=1
     * 参数：page(可选, 默认1), pageSize(可选, 默认10), keyword(可选), categoryId(可选)
     *
     * 不需要 Token（商品列表公开可见）
     */
    @GetMapping("/list")
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        Page<Product> result = productService.getProductList(page, pageSize, keyword, categoryId);
        return Result.success(result);
    }

    /**
     * 获取商品详情
     *
     * 请求方式：GET /api/v1/product/{productId}
     * 不需要 Token
     */
    @GetMapping("/{productId}")
    public Result<Product> detail(@PathVariable Long productId) {
        Product product = productService.getProductDetail(productId);
        return Result.success(product);
    }
}