package com.treemall.service;                                           // 声明包路径：在 service 包下


import com.treemall.entity.Cart;                                         // 导入实体类 Cart
import com.treemall.dto.response.CartVO;
import java.util.List;

/**
 * 购物车服务接口
 *
 * 功能：增删改查 + 切换选中状态
 * IService<Cart> 继承后自带 save/update/remove/list 等方法
 */
public interface CartService {               // 继承 IService，获得通用 CRUD 方法

    /**
     * 添加商品到购物车
     *
     * 当前用户从 UserContext 拿 userId
     * 如果这个商品已经在购物车 → 数量+1；如果不在 → 新增一条记录
     *
     * @param productId 要添加的商品 ID
     * @return 成功返回 true
     */
    boolean addToCart(Long productId);

    /**
     * 更新商品数量
     *
     * @param cartId    购物车项 ID
     * @param quantity 新数量
     * @return 成功返回 true
     */
    boolean updateQuantity(Long cartId, Integer quantity);

    /**
     * 切换选中状态
     *
     * @param cartId    购物车项 ID
     * @param checked 新状态：true=选中，false=未选中
     * @return 成功返回 true
     */
    boolean toggleChecked(Long cartId);

    /**
     * 删除购物车项
     *
     * @param cartId 购物车项 ID
     * @return 成功返回 true
     */
    boolean deleteCart(Long cartId);

    /**
     * 查询当前用户的所有购物车项
     *
     * @return 当前登录用户的购物车列表
     */
    List<CartVO> getCurrentUserCartList();

    /**
     * 批量删除选中的购物车项（下单后清空选中的）
     *
     * @param cartIds 要删除的购物车项 ID 列表
     * @return 成功返回 true
     */
    boolean deleteCheckedItems(List<Long> cartIds);
}