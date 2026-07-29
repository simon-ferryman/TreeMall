package com.treemall.service.impl;                                      // 声明包路径：在 service/impl 包下（接口在上级，实现在下级）

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // MyBatis-Plus 条件构造器：lambda 风格写查询条件
import com.treemall.common.BusinessException;                            // 导入自定义业务异常：业务错误时抛出
import com.treemall.common.UserContext;                                 // 导入当前用户上下文：从这里拿 userId
import com.treemall.entity.Cart;                                         // 导入购物车实体
import com.treemall.entity.Product;                                       // 导入商品实体：添加购物车时需要校验商品是否存在
import com.treemall.mapper.CartMapper;                                   // 导入购物车 Mapper：操作数据库
import com.treemall.mapper.ProductMapper;                                 // 导入商品 Mapper：查询商品是否存在
import com.treemall.service.CartService;                                 // 导入服务接口

import lombok.RequiredArgsConstructor;                                   // Lombok 注解：生成包含所有 final 字段的构造器
import lombok.extern.slf4j.Slf4j;                                       // Lombok 注解：自动生成 log 对象
import org.springframework.stereotype.Service;                              // Spring 注解：标记这是一个服务类，Spring 会自动扫描注册
import org.springframework.transaction.annotation.Transactional;              // Spring 事务注解：方法出错自动回滚
import com.treemall.dto.response.CartVO;                                 // 购物车响应 DTO
import java.util.stream.Collectors;                                      // Stream 收集器
import java.util.Map;                                                    // Map 用于快速查找
import java.util.ArrayList;                                              // ArrayList
import java.util.List;

/**
 * 购物车服务实现类
 * 业务流程：
 * 1. 添加购物车 → 拿当前 userId → 查商品是否存在 → 查购物车是否已有这个商品 → 有则加数量，无则新增
 * 2. 更新数量 → 根据 cartId 更新
 * 3. 切换选中 → 修改 checked 字段
 * 4. 删除 → 逻辑删除购物车项
 * 5. 查询列表 → 查询当前 userId 的所有购物车项
 */
@Slf4j                                                                  // 自动生成 private static final Logger log = ...；方便打日志
@Service                                                               // 标记为 Spring 服务，放入容器
@RequiredArgsConstructor                                                    // Lombok 自动生成构造器，所有 final 字段都会被注入
@Transactional                                                          // 所有方法加事务，出错自动回滚
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;                                   // 购物车 Mapper：最终操作数据库，构造器注入
    private final ProductMapper productMapper;                               // 商品 Mapper：添加购物车前校验商品是否存在，构造器注入

    @Override
    public boolean addToCart(Long productId) {                              // 实现接口方法：参数是商品 ID
        // 从 ThreadLocal 拿当前用户 ID（JwtInterceptor 已经提前放好了）
        Long userId = UserContext.getUserId();
        if (userId == null) {                                            // 如果 userId 为 null，说明没有 Token 或者 Token 无效
            throw new BusinessException(401, "请先登录");                 // 抛出业务异常，全局异常处理会捕获
        }

        // 查询商品是否存在，不存在直接抛异常
        Product product = productMapper.selectById(productId);              // 根据 ID 查商品
        if (product == null || product.getStatus() == 0 || product.getDeleted() == 1) {
            throw new BusinessException(400, "商品不存在或已下架");         // 商品不存在直接告诉前端
        }

        // 查询购物车是否已经有这个商品（包括已逻辑删除的记录）
        Cart existCart = cartMapper.selectByUserIdAndProductIdIgnoreLogic(userId, productId);

        if (existCart != null) {
            // 用原生 SQL 绕过 @TableLogic 的 UPDATE 劫持
            // 同时完成：数量+1、恢复删除状态、恢复选中、刷新时间
            cartMapper.restoreAndIncrementQuantity(userId, productId);
        } else {
            // 真正的首次添加
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(1);
            cart.setChecked(1);
            cartMapper.insert(cart);
        }
        return true;                                                      // 成功返回 true
    }

    @Override
    public boolean updateQuantity(Long cartId, Integer quantity) {          // 更新数量方法：cartId + 新数量
        // 简单的校验：数量不能小于 1
        if (quantity < 1) {                                             // 数量小于 1 非法
            throw new BusinessException(400, "数量不能小于 1");            // 抛异常
        }

        // 查询原来的购物车项
        Cart cart = cartMapper.selectById(cartId);                         // 根据 ID 查询
        if (cart == null) {                                              // 不存在抛异常
            throw new BusinessException(400, "购物车项不存在");
        }

        // 检查是不是当前用户的（安全校验：防止用户改别人的购物车）
        if (!cart.getUserId().equals(UserContext.getUserId())) {           // 如果拿到的 userId 不等于当前登录用户
            throw new BusinessException(403, "无权修改他人的购物车");         // 无权修改，抛异常
        }

        // 更新数量
        cart.setQuantity(quantity);                                        // 修改内存中的数量
        cartMapper.updateById(cart);                                       // 更新到数据库

        return true;                                                      // 返回成功
    }

    @Override
    public boolean toggleChecked(Long cartId) {            // 切换选中：cartId + 新状态
        // 同样的安全校验：先查，再判断归属，再修改
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            throw new BusinessException(400, "购物车项不存在");
        }
        if (!cart.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(403, "无权修改他人的购物车");
        }

        cart.setChecked(cart.getChecked() == 1 ? 0 : 1);                                  // 修改 checked 状态：1=选中，0=不选中
        cartMapper.updateById(cart);                                       // 更新

        return true;
    }

    @Override
    public boolean deleteCart(Long cartId) {                                 // 删除购物车项
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            throw new BusinessException(400, "购物车项不存在");
        }
        if (!cart.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(403, "无权删除他人的购物车");
        }

        // 逻辑删除：MyBatis-Plus 的 removeById 会自动加 deleted=1，不会真删
        cartMapper.deleteById(cartId);
        return true;
    }

//    @Override
//    public List<Cart> getCurrentUserCartList() {                            // 查询当前用户所有购物车
//        Long userId = UserContext.getUserId();                               // 从上下文拿 userId
//        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(Cart::getUserId, userId)                                 // 只查当前用户
//                .eq(Cart::getDeleted, 0)                               // 只查未删除的
//               .orderByDesc(Cart::getCreatedAt);                            // 按创建时间倒排：最新添加的在前
//
//        return cartMapper.selectList(wrapper);                              // 查询列表返回
//    }

    @Override
    public List<CartVO> getCurrentUserCartList() {                           // 返回类型改为 List<CartVO>
        Long userId = UserContext.getUserId();                               // 从 ThreadLocal 拿当前用户 ID

        // 第1步：查询当前用户的所有购物车项
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId)                                  // 只查当前用户
                .eq(Cart::getDeleted, 0)                                     // 只查未删除（显式表达意图，增强可读性）
                .orderByDesc(Cart::getCreatedAt);                            // 最新添加的在前

        List<Cart> cartList = cartMapper.selectList(wrapper);                // 查询购物车列表
        if (cartList.isEmpty()) {                                            // 购物车为空
            return List.of();                                                 // 返回空列表（Java 9+ 不可变空列表）
        }

        // 第2步：提取所有 productId，批量查询商品信息
        // 为什么用"批量查询"而不是"循环查询"？
        //   循环查询：购物车 10 个商品 → 发 10 次 SQL → 10 次网络往返
        //   批量查询：购物车 10 个商品 → 发 1 次 SQL（WHERE id IN (...)）→ 1 次网络往返
        //   性能差异：10 倍以上
        List<Long> productIds = cartList.stream()                            // 把 Cart 列表转为流
                .map(Cart::getProductId)                                     // 提取每个 Cart 的 productId
                .distinct()                                                  // 去重（防止重复 productId）
                .collect(Collectors.toList());                               // 收集为 List

        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.in(Product::getId, productIds);                      // WHERE id IN (1, 2, 3, ...)
        List<Product> productList = productMapper.selectList(productWrapper); // 批量查询

        // 第3步：将 Product 列表转为 Map（ID → Product），方便后续 O(1) 快速查找
        // 为什么要转 Map？避免双重循环 O(n²)，用 Map 实现 O(n) 查找
        Map<Long, Product> productMap = productList.stream()
                .collect(Collectors.toMap(
                        Product::getId,          // Key：商品 ID
                        p -> p                   // Value：Product 对象
                ));

        // 第4步：将 Cart 和 Product 合并为 CartVO
        List<CartVO> cartVOList = new ArrayList<>();
        for (Cart cart : cartList) {
            Product product = productMap.get(cart.getProductId());           // 从 Map 中快速查找对应商品
            if (product == null) {                                           // 商品可能已被删除
                continue;                                                     // 跳过这个购物车项
            }

            CartVO vo = new CartVO();                                        // 创建 VO 对象
            vo.setCartId(cart.getId());                                      // 购物车项 ID
            vo.setProductId(cart.getProductId());                            // 商品 ID
            vo.setQuantity(cart.getQuantity());                              // 数量
            vo.setChecked(cart.getChecked());                                // 选中状态
            vo.setProductName(product.getName());                            // 商品名称（来自 Product 表）
            vo.setPrice(product.getPrice());                                 // 商品单价（来自 Product 表）
            vo.setMainImage(product.getMainImage());                         // 商品主图（来自 Product 表）
            vo.setStock(product.getStock());                                 // 商品库存（来自 Product 表）
            vo.setCreatedAt(cart.getCreatedAt());                            // 加入购物车时间

            cartVOList.add(vo);                                              // 加入结果列表
        }

        return cartVOList;                                                   // 返回合并后的列表
    }


    @Override
    public boolean deleteCheckedItems(List<Long> cartIds) {                   // 批量删除选中项
        if (cartIds == null || cartIds.isEmpty()) {                          // 空列表直接返回
            return true;
        }
        Long userId = UserContext.getUserId();                               // 当前用户 ID
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Cart::getId, cartIds)                                    // IN 查询：ID 在列表里
                .eq(Cart::getUserId, userId)                                 // 必须是当前用户
                .eq(Cart::getDeleted, 0);                                   // 未删除

        cartMapper.delete(wrapper);                                        // 批量删除（逻辑删除，自动加 deleted=1）
        return true;
    }
}