/**
 * api/product.js — 商品模块 API 封装
 *
 * 【对应后端】ProductController — /api/v1/product
 */

import request from '@/utils/request'

/**
 * 获取商品列表（分页 + 筛选）
 *
 * @param {Object} params - 查询参数
 * @param {number} [params.page=1] - 页码
 * @param {number} [params.pageSize=10] - 每页数量（注意：后端参数名是 pageSize，不是 size）
 * @param {string} [params.keyword] - 搜索关键词
 * @param {number} [params.categoryId] - 分类ID
 * @param {string} [params.sortField] - 排序字段（price/sales）
 * @param {string} [params.sortOrder] - 排序方向（asc/desc）
 * @returns {Promise} 返回分页数据 { records: [], total: number, page: number, pageSize: number }
 *
 * 【使用场景】首页推荐商品列表、分类页商品列表、搜索结果
 * 【注意】后端 ProductController 的分页参数名是 pageSize，与其他 Controller 的 size 不同
 */
export const getProductList = (params = {}) => {
  return request({
    url: '/product/list',         // 商品列表接口
    method: 'GET',                // GET 请求
    data: params,                 // 查询参数拼接到 URL 上：/product/list?page=1&pageSize=10&keyword=手机
    skipAuth: true                // 公开接口
  })
}

/**
 * 获取商品详情
 *
 * @param {number} productId - 商品ID
 * @returns {Promise} 返回商品完整信息（含 images 数组、specs 对象、description 富文本）
 *
 * 【使用场景】商品详情页
 */
export const getProductDetail = (productId) => {
  return request({
    url: `/product/${productId}`, // 路径参数拼接：/product/123（RESTful 风格）
    method: 'GET',                // GET 请求
    skipAuth: true                // 公开接口
  })
}
