/**
 * api/cart.js — 购物车模块 API 封装
 *
 * 【对应后端】CartController — /api/v1/cart
 */

import request from '@/utils/request'

/**
 * 获取购物车列表
 *
 * @returns {Promise} 返回购物车项数组 [{ id, productId, productName, price, quantity, mainImage, checked }]
 *
 * 【使用场景】购物车页面加载、加入购物车后刷新
 * 【注意】此接口需要登录（需要 Token），不传 skipAuth
 */
export const getCartList = () => {
  return request({
    url: '/cart/list',            // 购物车列表接口
    method: 'GET'                 // GET 请求
    // 不传 skipAuth，自动注入 Token
  })
}

/**
 * 加入购物车
 *
 * @param {Object} params - { productId: Number, quantity: Number }
 * @returns {Promise}
 *
 * 【使用场景】商品详情页点击"加入购物车"
 */
export const addToCart = (params) => {
  return request({
    url: `/cart/add/${params.productId}`,  // 路径参数：/cart/add/123
    method: 'POST',                        // POST 请求
    data: { quantity: params.quantity }    // 请求体：{ "quantity": 1 }
  })
}

/**
 * 更新购物车商品数量
 *
 * @param {number} cartId - 购物车项ID
 * @param {number} quantity - 新数量
 * @returns {Promise}
 *
 * 【使用场景】购物车页点击 +/- 按钮
 */
export const updateCartQuantity = (cartId, quantity) => {
  return request({
    url: '/cart/update',          // 更新数量接口
    method: 'PUT',                // PUT 请求（RESTful 风格：更新）
    data: { id : cartId, quantity }    // 请求体：{ "cartId": 1, "quantity": 3 }
  })
}

/**
 * 切换购物车项选中状态
 *
 * @param {number} cartId - 购物车项ID
 * @returns {Promise}
 *
 * 【使用场景】购物车页点击勾选框
 */
export const toggleCartCheck = (cartId) => {
  return request({
    url: `/cart/toggle/${cartId}`, // 路径参数：/cart/toggle/1
    method: 'POST'                  // POST 请求
  })
}

/**
 * 删除购物车项
 *
 * @param {number} cartId - 购物车项ID
 * @returns {Promise}
 *
 * 【使用场景】购物车页滑动删除或点击删除按钮
 */
export const removeCartItem = (cartId) => {
  return request({
    url: `/cart/${cartId}`,       // 路径参数：/cart/1（RESTful 风格：DELETE 删除）
    method: 'DELETE'              // DELETE 请求
  })
}

/**
 * 删除已选中的购物车项
 *
 * @returns {Promise}
 *
 * 【使用场景】下单成功后清空已选商品
 */
export const deleteCheckedItems = () => {
  return request({
    url: '/cart/delete-checked',  // 批量删除已选接口
    method: 'POST'              // POST 请求
  })
}
