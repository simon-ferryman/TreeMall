/**
 * api/payment.js — 支付模块 API 封装
 *
 * 【对应后端】PaymentController — /api/v1/pay
 * 【注意】后端类路径是 /pay 不是 /payment
 */

import request from '@/utils/request'

/**
 * 发起支付（获取预支付信息）
 *
 * @param {Object} data - { orderId: Number }
 * @returns {Promise} 返回支付参数（微信支付所需的 timeStamp、nonceStr、package、signType、paySign）
 *
 * 【使用场景】下单确认页点击"提交订单"后，跳转到支付流程
 */
export const prepay = (data) => {
  return request({
    url: '/pay/prepay',           // 预支付接口（注意：是 /pay 不是 /payment）
    method: 'POST',               // POST 请求
    data                          // 请求体：{ "orderId": 123 }
  })
}

/**
 * 支付回调通知
 *
 * @param {Object} data - 微信支付回调数据
 * @returns {Promise}
 *
 * 【使用场景】微信支付完成后，小程序收到回调，通知后端验证
 */
export const paymentCallback = (data) => {
  return request({
    url: '/pay/callback',         // 支付回调接口
    method: 'POST',               // POST 请求
    data                          // 请求体：微信支付回调参数
  })
}
