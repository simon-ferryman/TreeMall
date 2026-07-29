package com.treemall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.treemall.common.BusinessException;
import com.treemall.entity.Product;
import com.treemall.mapper.ProductMapper;
import com.treemall.service.ProductService;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.treemall.common.UserContext;                                 // 当前用户上下文
import com.treemall.dto.request.ProductSaveRequest;                    // 商品保存请求 DTO
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public Page<Product> getProductList(int page, int pageSize, String keyword, Long categoryId) {
        // 构造分页对象
        Page<Product> pageParam = new Page<>(page, pageSize);

        // 构造查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)                               // 只查上架商品
                .eq(Product::getDeleted, 0);                            // 只查未删除

        // 如果传了关键词，模糊搜索商品名称
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Product::getName, keyword);
        }

        // 如果传了分类 ID，过滤分类
        if (categoryId != null && categoryId > 0) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }

        // 按销量降序排序（热门商品在前）
        wrapper.orderByDesc(Product::getSalesCount);

        // 执行分页查询
        return productMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Product getProductDetail(Long productId) {
        Product product = productMapper.selectById(productId);          // 根据 ID 查询
        if (product == null || product.getDeleted() == 1 || product.getStatus() == 0) {
            throw new BusinessException("商品不存在或已下架");
        }
        return product;
    }

    @Override
    public List<Product> getProductsByIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Product::getId, productIds)                          // IN 查询：WHERE id IN (..., ...)
                .eq(Product::getStatus, 1)                               // 只查上架商品
                .eq(Product::getDeleted, 0);
        return productMapper.selectList(wrapper);
    }



    // ============================================
    // 商户端方法实现
    // ============================================
    /**
     * 商户端：分页查询自己的商品（不过滤 status，可看到下架商品）
     *
     * 实现逻辑：
     *   1. 按 merchantId 过滤（V1 暂不过滤，V2 加上）
     *   2. 支持关键词搜索 + 分类筛选 + 状态筛选
     *   3. 按创建时间倒序排列
     */
    @Override
    public Page<Product> getMerchantProductList(int page, int pageSize,
                                                String keyword, Long categoryId, Integer status) {
        Page<Product> pageParam = new Page<>(page, pageSize);                // 创建分页对象

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        // V2 多商户时加上：wrapper.eq(Product::getMerchantId, UserContext.getUserId());

        // 关键词搜索（模糊匹配商品名称）
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Product::getName, keyword);
        }
        // 分类筛选
        if (categoryId != null && categoryId > 0) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        // 状态筛选（null=全部, 1=上架, 0=下架）
        // 注意：这里不用 eq(Product::getStatus, 1)，因为商户需要看到所有状态
        if (status != null) {
            wrapper.eq(Product::getStatus, status);                          // 按传入的 status 筛选
        }
        // 按创建时间倒序（最新添加的在前）
        wrapper.orderByDesc(Product::getCreatedAt);

        return productMapper.selectPage(pageParam, wrapper);                 // 返回分页结果
    }

    /**
     * 保存商品（添加 + 编辑合并）
     *
     * 实现逻辑：
     *   1. 从 UserContext 获取当前商户 ID（merchantId）
     *   2. 判断请求中有没有 id：
     *      - 有 id → 编辑：先查原商品 → 校验归属 → 更新非 null 字段
     *      - 无 id → 添加：创建新商品 → 设置 merchantId → 插入
     *   3. 返回保存后的商品
     *
     * 为什么添加和编辑合并为一个方法？
     *   - 减少接口数量（前端只需调用一个接口）
     *   - 减少重复代码（校验逻辑大部分相同）
     *   - 符合 RESTful 设计惯例（POST 创建，PUT 更新，但小程序前端习惯用一个接口）
     */
    @Override
    @Transactional                                                      // 写操作加事务
    public Product saveProduct(ProductSaveRequest request) {
        // 获取当前商户 ID（必须是商户角色才能操作）
        Long merchantId = UserContext.getUserId();
        if (merchantId == null) {
            throw new BusinessException(401, "请先登录");
        }
        // V2 多商户时增加角色校验：if (!"merchant".equals(UserContext.getRole())) throw ...

        if (request.getId() != null) {
            // ===== 编辑模式：id 不为空，更新已有商品 =====
            // 查询原商品
            Product exist = productMapper.selectById(request.getId());
            if (exist == null) {
                throw new BusinessException(400, "商品不存在");
            }
            // 安全校验：只能编辑自己的商品
            if (!exist.getMerchantId().equals(merchantId)) {
                throw new BusinessException(403, "无权修改他人的商品");
            }

            // 只更新非 null 字段（前端传了哪些字段就更新哪些）
            // MyBatis-Plus 的 updateById 默认只更新非 null 字段
            if (request.getCategoryId() != null) {
                exist.setCategoryId(request.getCategoryId());
            }
            if (request.getName() != null) {
                exist.setName(request.getName());
            }
            if (request.getDescription() != null) {
                exist.setDescription(request.getDescription());
            }
            if (request.getPrice() != null) {
                exist.setPrice(request.getPrice());
            }
            if (request.getOriginalPrice() != null) {
                exist.setOriginalPrice(request.getOriginalPrice());
            }
            if (request.getStock() != null) {
                exist.setStock(request.getStock());
            }
            if (request.getMainImage() != null) {
                exist.setMainImage(request.getMainImage());
            }
            if (request.getImages() != null) {
                exist.setImages(request.getImages());
            }
            if (request.getSpecs() != null) {
                exist.setSpecs(request.getSpecs());
            }

            productMapper.updateById(exist);                            // 更新到数据库
            log.info("商品编辑成功: id={}, name={}", exist.getId(), exist.getName());
            return exist;                                                // 返回更新后的商品

        } else {
            // ===== 添加模式：id 为空，创建新商品 =====
            Product product = new Product();
            product.setMerchantId(merchantId);                           // 设置商户 ID（从 UserContext 获取）
            product.setCategoryId(request.getCategoryId());
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setOriginalPrice(request.getOriginalPrice());
            product.setStock(request.getStock() != null ? request.getStock() : 0); // 库存默认 0
            product.setMainImage(request.getMainImage());
            product.setImages(request.getImages());
            product.setSpecs(request.getSpecs());
            product.setStatus(1);                                        // 默认上架
            product.setSalesCount(0);                                    // 新商品销量为 0

            productMapper.insert(product);                               // 插入数据库
            // 插入后 product.getId() 自动回填（MyBatis-Plus 雪花 ID）
            log.info("商品添加成功: id={}, name={}", product.getId(), product.getName());
            return product;
        }
    }

    /**
     * 删除商品（逻辑删除）
     *
     * 实现逻辑：
     *   1. 查询商品 → 校验存在
     *   2. 校验归属（merchantId 匹配）
     *   3. 逻辑删除（MyBatis-Plus 自动转 UPDATE SET deleted=1）
     *
     * 注意：逻辑删除后，已下单的商品不受影响（订单项存的是快照）
     */
    @Override
    @Transactional                                                      // 写操作加事务
    public boolean deleteProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(400, "商品不存在");
        }

        // 安全校验：只能删除自己的商品
        Long merchantId = UserContext.getUserId();
        if (!product.getMerchantId().equals(merchantId)) {
            throw new BusinessException(403, "无权删除他人的商品");
        }

        // 逻辑删除：MyBatis-Plus 的 deleteById 自动执行 UPDATE SET deleted=1
        // 不会真的删除数据，已下单的订单项快照不受影响
        productMapper.deleteById(productId);
        log.info("商品删除成功: id={}, name={}", productId, product.getName());
        return true;
    }

    /**
     * 更新商品状态（上架/下架）
     *
     * 实现逻辑：
     *   1. 查询商品 → 校验存在
     *   2. 校验归属
     *   3. 更新 status 字段
     *
     * 使用场景：
     *   商户后台点击"上架"按钮 → status = 1 → 商品在用户端可见
     *   商户后台点击"下架"按钮 → status = 0 → 商品在用户端不可见（但已下单的不影响）
     */
    @Override
    @Transactional
    public boolean updateProductStatus(Long productId, Integer status) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(400, "商品不存在");
        }

        // 安全校验：只能操作自己的商品
        Long merchantId = UserContext.getUserId();
        if (!product.getMerchantId().equals(merchantId)) {
            throw new BusinessException(403, "无权操作他人的商品");
        }

        product.setStatus(status);                                        // 更新状态
        productMapper.updateById(product);                                // 更新到数据库
        log.info("商品状态更新: id={}, status={}", productId, status == 1 ? "上架" : "下架");
        return true;
    }
    /**
     * 商户端：获取商品详情（不过滤 status）
     * 商户编辑商品时需要看到所有状态的商品信息
     */
    @Override
    public Product getMerchantProductDetail(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted() == 1) {
            throw new BusinessException(400, "商品不存在");
        }
        return product;
    }
}