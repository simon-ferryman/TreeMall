/**
 * api/order.js — 订单模块 API 封装
 *
 * 【对应后端】OrderController — /api/v1/order
 */

import request from '@/utils/request'

/**
 * 创建订单
 *
 * @param {Object} data - { addressId, cartItemIds, remark }
 * @returns {Promise} 返回 { orderId, orderNo, totalAmount } 对象
 *
 * 【使用场景】下单确认页点击"提交订单"
 */
export const createOrder = (data) => {
  return request({
    url: '/order',         // 创建订单接口
    method: 'POST',               // POST 请求
    data                          // 请求体：{ "addressId": 1, "cartItemIds": [1,2,3], "remark": "快点发货" }
  })
}

/**
 * 获取订单列表（分页）
 *
 * @param {Object} params - { page: Number, size: Number, status: String }
 * @returns {Promise} 返回分页数据 { records: [], total: number }
 *
 * 【使用场景】订单列表页，支持按状态筛选
 * 【注意】后端参数名是 size，不是 pageSize（与 ProductController 不同）
 */
export const getOrderList = (params) => {
  return request({
    url: '/order/list',           // 订单列表接口
    method: 'GET',                // GET 请求
    data: params,                  // 查询参数：/order/list?page=1&size=10&status=PENDING_PAYMENT
	showLoading: false            // 关闭全局加载遮罩，页面使用自己的 LoadingMore 组件管理加载状态
  })
}

/**
 * 获取订单详情
 *
 * @param {number} orderId - 订单ID
 * @returns {Promise} 返回订单完整信息（含地址、商品列表、物流信息）
 *
 * 【使用场景】订单详情页
 */
export const getOrderDetail = (orderId) => {
  return request({
    url: `/order/${orderId}`,     // 路径参数：/order/123
    method: 'GET'                 // GET 请求
  })
}

/**
 * 取消订单
 *
 * @param {number} orderId - 订单ID
 * @returns {Promise}
 *
 * 【使用场景】订单详情页/订单列表页点击"取消订单"
 * 【注意】只有待付款状态的订单可以取消
 */
export const cancelOrder = (orderId) => {
  return request({
    url: `/order/cancel/${orderId}`,  // 取消订单接口
    method: 'PUT'                     // PUT 请求
  })
}

/**
 * 确认收货
 *
 * @param {number} orderId - 订单ID
 * @returns {Promise}
 *
 * 【使用场景】订单详情页点击"确认收货"
 * 【注意】只有已发货状态的订单可以确认收货
 */
export const confirmReceive = (orderId) => {
  return request({
    url: `/order/${orderId}/receive`,  // 确认收货接口
    method: 'PUT'                       ,// PUT 请求
	showLoading: false
  })
}
