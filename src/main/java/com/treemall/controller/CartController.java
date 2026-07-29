package com.treemall.controller;                                         // 包路径：控制器包

import com.treemall.common.Result;                                      // 导入统一响应格式：包装返回值
import com.treemall.dto.response.CartVO;
import com.treemall.entity.Cart;                                         // 导入购物车实体
import com.treemall.service.CartService;                                 // 导入购物车服务：调用业务逻辑
import lombok.RequiredArgsConstructor;                                   // Lombok 构造器注入
import org.springframework.web.bind.annotation.*;                           // Spring MVC 注解：GetMapping/PostMapping 等
import com.treemall.dto.response.CartVO;
import java.util.List;

/**
 * 购物车控制器
 *
 * 所有接口都需要 Token：购物车绑定用户，必须登录才能访问
 * 接口：添加、更新数量、切换选中、删除、查询列表
 */
@RestController                                                         // = @Controller + @ResponseBody → 所有方法返回 JSON
@RequestMapping("/api/v1/cart")                                          // 统一路径前缀：所有接口都以这个开头
@RequiredArgsConstructor                                                    // 构造器注入所有 final 字段
public class CartController {

    private final CartService cartService;                                   // 服务引用：构造器注入

    /**
     * 添加商品到购物车
     *
     * 请求方式：POST /api/v1/cart/add
     * 请求体： { "productId": 1 }
     * 请求头需要 Token：Authorization: Bearer <token>
     *
     * @param productId 要添加的商品 ID（路径参数）
     * @return 成功返回 Result.success(true)
     */
    @PostMapping("/add/{productId}")                                       // POST 请求，productId 放在路径里
    public Result<Boolean> add(@PathVariable Long productId) {               // @PathVariable 从路径拿参数
        boolean result = cartService.addToCart(productId);                  // 调用服务完成添加
        return Result.success(result);                                      // 返回成功，data 放 boolean
    }

    /**
     * 更新商品数量
     *
     * 请求方式：PUT /api/v1/cart/update
     * 请求体： { "cartId": 1, "quantity": 2 }
     */
    @PutMapping("/update")                                              // PUT 请求：更新资源
    public Result<Void> update(@RequestBody Cart cart) {                     // @RequestBody 拿请求体中的 cart 对象
        cartService.updateQuantity(cart.getId(), cart.getQuantity());      // 调用服务
        return Result.success();                                            // 返回成功
    }

    /**
     * 切换选中状态
     *
     * 请求方式：POST /api/v1/cart/toggle/{cartId}
     * 请求体： { "checked": true }
     */
    @PostMapping("/toggle/{cartId}")                                       // POST 请求：切换操作
    public Result<Void> toggle(@PathVariable Long cartId) {
        cartService.toggleChecked(cartId);                          // 调用服务
        return Result.success();
    }

    /**
     * 删除购物车项
     *
     * 请求方式：DELETE /api/v1/cart/{cartId}
     */
    @DeleteMapping("/{cartId}")                                          // DELETE 请求：删除资源
    public Result<Void> delete(@PathVariable Long cartId) {
        cartService.deleteCart(cartId);                                     // 调用服务
        return Result.success();
    }

    /**
     * 获取当前用户购物车列表
     *
     * 请求方式：GET /api/v1/cart/list
     * 不需要请求参数，userId 从 UserContext 拿
     */
    @GetMapping("/list")                                                 // GET 请求：查列表
    public Result<List<CartVO>> list() {
        List<CartVO> carts = cartService.getCurrentUserCartList();            // 调用服务查询
        return Result.success(carts);                                       // 返回列表
    }

    /**
     * 批量删除选中的购物车项（下单成功后清空选中的）
     *
     * 请求方式：POST /api/v1/cart/delete-checked
     * 请求体： { "cartIds": [1, 2, 3] }
     */
    @PostMapping("/delete-checked")
    public Result<Void> deleteChecked(@RequestBody List<Long> cartIds) {    // @RequestBody 拿 cartId 列表
        cartService.deleteCheckedItems(cartIds);            // 调用服务删除
        return Result.success();
    }
}