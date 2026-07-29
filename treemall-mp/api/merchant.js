/**
 * api/merchant.js — 商户端 API 封装
 *
 * 【对应后端】
 *   MerchantOrderController   — /api/v1/merchant/order
 *   MerchantProductController — /api/v1/merchant/product
 */

import request from '@/utils/request'

// ==================== 订单管理 ====================

/**
 * 商户端：获取全部订单列表（分页）
 */
export const getMerchantOrderList = (params) => {
  return request({
    url: '/merchant/order/list',
    method: 'GET',
    data: params,
    showLoading: false
  })
}

/**
 * 商户端：订单详情
 */
export const getMerchantOrderDetail = (orderId) => {
  return request({
    url: `/merchant/order/${orderId}`,
    method: 'GET'
  })
}

/**
 * 商户端：发货
 */
export const deliverOrder = (orderId, data) => {
  return request({
    url: `/merchant/order/${orderId}/deliver`,
    method: 'PUT',
    data
  })
}

// ==================== 商品管理 ====================

/**
 * 商户端：商品列表（分页，含下架商品）
 */
export const getMerchantProductList = (params) => {
  return request({
    url: '/merchant/product/list',
    method: 'GET',
    data: params,
    showLoading: false
  })
}

/**
 * 商户端：新增商品
 */
export const addProduct = (data) => {
  return request({
    url: '/merchant/product',
    method: 'POST',
    data
  })
}

/**
 * 商户端：编辑商品
 */
export const updateProduct = (data) => {
  return request({
    url: '/merchant/product',
    method: 'PUT',
    data
  })
}

/**
 * 商户端：上架/下架商品
 */
export const updateProductStatus = (productId, status) => {
  return request({
    url: `/merchant/product/${productId}/status`,
    method: 'PUT',
    data: { status }
  })
}

/**
 * 商户端：获取商品详情（含下架商品）
 */
export const getMerchantProductDetail = (productId) => {
  return request({
    url: `/merchant/product/${productId}`,
    method: 'GET'
  })
}
