/**
 * api/address.js — 收货地址模块 API 封装
 *
 * 【对应后端】AddressController — /api/v1/address
 */

import request from '@/utils/request'

/**
 * 获取地址列表
 *
 * @returns {Promise} 返回地址数组 [{ id, receiver, phone, province, city, district, detail, isDefault }]
 *
 * 【使用场景】地址列表页、下单确认页选择地址
 */
export const getAddressList = () => {
  return request({
    url: '/address/list',         // 地址列表接口
    method: 'GET'                 // GET 请求
  })
}

/**
 * 获取地址详情
 *
 * @param {number} addressId - 地址ID
 * @returns {Promise} 返回地址完整信息
 *
 * 【使用场景】地址编辑页（编辑模式）回填表单
 */
export const getAddressDetail = (addressId) => {
  return request({
    url: `/address/${addressId}`, // 路径参数：/address/1
    method: 'GET'                 // GET 请求
  })
}

/**
 * 创建地址
 *
 * @param {Object} data - { receiver, phone, province, city, district, detail, isDefault }
 * @returns {Promise}
 *
 * 【使用场景】地址编辑页（新增模式）保存
 */
export const createAddress = (data) => {
  return request({
    url: '/address',              // 创建地址接口
    method: 'POST',               // POST 请求（RESTful 风格：创建）
    data                          // 请求体：{ "receiver": "张三", "phone": "138xxxx", ... }
  })
}

/**
 * 更新地址
 *
 * @param {Object} data - { id, receiver, phone, province, city, district, detail, isDefault }
 * @returns {Promise}
 *
 * 【使用场景】地址编辑页（编辑模式）保存
 * 【注意】后端 PUT /address 接口需要完整数据，路由中没有 id 参数
 */
export const updateAddress = (data) => {
  return request({
    url: '/address',              // 更新地址接口（注意：URL 中没有 ID，ID 在请求体中）
    method: 'PUT',                // PUT 请求
    data                          // 请求体：{ "id": 1, "receiver": "张三", ... }
  })
}

/**
 * 删除地址
 *
 * @param {number} addressId - 地址ID
 * @returns {Promise}
 *
 * 【使用场景】地址列表页滑动删除
 */
export const deleteAddress = (addressId) => {
  return request({
    url: `/address/${addressId}`, // 路径参数：/address/1
    method: 'DELETE'              // DELETE 请求
  })
}

/**
 * 设置默认地址
 *
 * @param {number} addressId - 地址ID
 * @returns {Promise}
 *
 * 【使用场景】地址列表页点击"设为默认"
 */
export const setDefaultAddress = (addressId) => {
  return request({
    url: `/address/default/${addressId}`, // 设置默认地址接口
    method: 'PUT'                         // PUT 请求
  })
}
