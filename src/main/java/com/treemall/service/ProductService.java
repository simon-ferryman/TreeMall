package com.treemall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.treemall.entity.Product;
import com.treemall.dto.request.ProductSaveRequest;
import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 分页查询商品列表（支持关键词搜索 + 分类过滤）
     *
     * @param page      页码（从 1 开始）
     * @param pageSize  每页条数
     * @param keyword   搜索关键词（null/空表示不限）
     * @param categoryId 分类 ID（0 表示不限）
     * @return 分页结果
     */
    Page<Product> getProductList(int page, int pageSize, String keyword, Long categoryId);

    /**
     * 获取商品详情（带分类名称）
     *
     * @param productId 商品 ID
     * @return 商品详情
     */
    Product getProductDetail(Long productId);

    /**
     * 根据 ID 列表批量查询商品（购物车用）
     *
     * @param productIds 商品 ID 列表
     * @return 商品列表
     */
    List<Product> getProductsByIds(List<Long> productIds);





    // ============================================
    // 商户端方法（需要登录 + 商户角色）
    // ============================================
    /**
     * 商户端：分页查询自己的商品（不过滤 status，含下架商品）
     *
     * 与 getProductList() 的区别：
     *   getProductList() → 只查 status=1 的，给用户端用
     *   getMerchantProductList() → 查所有 status，给商户端用
     *
     * 使用场景：商户后台管理商品列表，需要看到上架和下架的所有商品
     *
     * @param page       页码（从 1 开始）
     * @param pageSize   每页条数
     * @param keyword    搜索关键词（null/空表示不限）
     * @param categoryId 分类 ID（null 表示不限）
     * @param status     状态筛选（null=全部, 1=上架, 0=下架）
     * @return 分页结果
     */
    Page<Product> getMerchantProductList(int page, int pageSize,
                                         String keyword, Long categoryId, Integer status);

    /**
     * 保存商品（添加 + 编辑合并）
     *
     * 根据请求体中是否有 id 判断操作类型：
     *   id == null → 新增商品（INSERT）
     *   id != null → 编辑商品（UPDATE）
     *
     * 后端自动设置 merchantId（从 UserContext 获取当前商户 ID）
     *
     * @param request 商品保存请求（categoryId + name + price + stock + ...）
     * @return 保存后的商品对象（含 ID）
     */
    Product saveProduct(ProductSaveRequest request);

    /**
     * 删除商品（逻辑删除）
     *
     * 安全校验：只能删除自己的商品（merchantId 匹配）
     *
     * @param productId 商品 ID
     * @return 成功返回 true
     */
    boolean deleteProduct(Long productId);
    /**
     * 商户端：获取商品详情（不过滤 status）
     * 与 getProductDetail() 的区别：不检查 status 字段，商户可以查看下架商品
     */
    Product getMerchantProductDetail(Long productId);

    /**
     * 更新商品状态（上架/下架）
     *
     * 使用场景：商户在后台点击"上架"或"下架"按钮
     * 安全校验：只能操作自己的商品
     *
     * @param productId 商品 ID
     * @param status    状态：1=上架, 0=下架
     * @return 成功返回 true
     */
    boolean updateProductStatus(Long productId, Integer status);
}
