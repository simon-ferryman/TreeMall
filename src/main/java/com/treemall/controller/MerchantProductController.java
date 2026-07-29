package com.treemall.controller;                                         // 控制器包

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;      // 分页结果对象
import com.treemall.common.Result;                                      // 统一响应格式
import com.treemall.dto.request.ProductSaveRequest;                     // 商品保存请求 DTO
import com.treemall.entity.Product;                                     // 商品实体
import com.treemall.service.ProductService;                             // 商品服务接口
import lombok.RequiredArgsConstructor;                                   // 构造器注入
import org.springframework.web.bind.annotation.*;                           // Spring MVC 注解

/**
 * 商品管理控制器（商户端）
 *
 * 五个接口：商品列表、添加、编辑、删除、上架/下架
 * 所有接口都需要 Token + 商户角色（MerchantInterceptor 拦截 /api/v1/merchant/**）
 *
 * 请求路径前缀：/api/v1/merchant/product
 *
 * 使用场景：
 *   商户登录后台管理系统 → 管理自己的商品（增删改查 + 上下架）
 *   V1 单商户：所有商户端接口共享同一个 merchantId
 *   V2 多商户：增加 merchantId 过滤，商户只能管理自己的商品
 */
@RestController
@RequestMapping("/api/v1/merchant/product")
@RequiredArgsConstructor
public class MerchantProductController {

    private final ProductService productService;

    /**
     * 商户端：商品列表（分页）
     *
     * 请求方式：GET /api/v1/merchant/product/list?page=1&size=10&keyword=手机&status=1
     *
     * 用途：商户在后台查看自己的商品列表，支持关键词搜索和状态筛选
     * V1 显示所有商品（因为只有一个商户）
     * V2 增加 merchantId 过滤
     */
    @GetMapping("/list")
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") Integer page,               // 页码，默认 1
            @RequestParam(defaultValue = "10") Integer size,              // 每页条数，默认 10
            @RequestParam(required = false) String keyword,               // 搜索关键词（可选）
            @RequestParam(required = false) Long categoryId,              // 分类筛选（可选）
            @RequestParam(required = false) Integer status                // 状态筛选（可选，1=上架, 0=下架）
    ) {
        // 复用产品端的 getProductList 方法（搜索逻辑相同，但不过滤 status）
        // 商户端需要看到所有状态的商品（包括下架的），所以这里不传 status 给 getProductList
        // 如果传了 status，则按状态筛选
        /***
         * 以上代码逻辑应是有问题，采用以下新方法来实现
         * */
        //重新设计了针对商户端的专门方法来查询商品，商户可根据商品关键词，状态等等来查询显示商品信息。
        Page<Product> result = productService.getMerchantProductList(page, size, keyword, categoryId, status);
        return Result.success(result);
    }


    /**
     * 添加商品
     *
     * 请求方式：POST /api/v1/merchant/product
     * 请求体：  ProductSaveRequest（id 为空表示添加）
     *
     * 注意：添加前需要先调用图片上传接口（FileController）获取图片 URL，
     *       然后将 URL 填入 mainImage 和 images 字段
     */
    @PostMapping
    public Result<Product> add(@RequestBody ProductSaveRequest request) {
        Product product = productService.saveProduct(request);
        return Result.success(product);
    }

    /**
     * 编辑商品
     *
     * 请求方式：PUT /api/v1/merchant/product
     * 请求体：  ProductSaveRequest（id 不为空表示编辑，只传要修改的字段）
     */
    @PutMapping
    public Result<Product> update(@RequestBody ProductSaveRequest request) {
        if (request.getId() == null) {
            return Result.error(400, "编辑商品时 ID 不能为空");
        }
        Product product = productService.saveProduct(request);
        return Result.success(product);
    }

    /**
     * 删除商品（逻辑删除）
     *
     * 请求方式：DELETE /api/v1/merchant/product/{productId}
     * 逻辑删除：标记 deleted=1，不真删数据
     * 已下单的商品不受影响（订单项存的是快照）
     */
    @DeleteMapping("/{productId}")
    public Result<Void> delete(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return Result.success();
    }
    /**
     * 商户端：获取商品详情（含下架商品）
     *
     * 请求方式：GET /api/v1/merchant/product/{productId}
     * 与用户端 getProductDetail 的区别：不过滤 status，商户需要看到所有状态的商品
     */
    @GetMapping("/{productId}")
    public Result<Product> detail(@PathVariable Long productId) {
        // 直接查数据库，不做 status 过滤
        Product product = productService.getMerchantProductDetail(productId);
        return Result.success(product);
    }

    /**
     * 上架/下架商品
     *
     * 请求方式：PUT /api/v1/merchant/product/{productId}/status
     * 请求体：  { "status": 1 }   // 1=上架, 0=下架
     *
     * 使用场景：商户在后台点击"上架"或"下架"按钮
     * 上架后商品在用户端可见，下架后不可见
     */
    @PutMapping("/{productId}/status")
    public Result<Void> updateStatus(@PathVariable Long productId,
                                     @RequestBody java.util.Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return Result.error(400, "状态值必须为 0（下架）或 1（上架）");
        }
        productService.updateProductStatus(productId, status);
        return Result.success();
    }
}