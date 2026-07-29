/**
 * api/category.js — 商品分类模块 API 封装
 *
 * 【对应后端】CategoryController — /api/v1/category
 */

import request from '@/utils/request'

/**
 * 获取分类列表（树形结构）
 *
 * @returns {Promise} 返回分类树数组 [{ id, name, icon, parentId, children: [...] }]
 *
 * 【使用场景】
 * - App.vue onLaunch 预加载
 * - 首页 CategoryGrid 组件展示顶级分类
 * - 分类页左侧导航 + 右侧子分类
 *
 * 【注意】后端返回的是完整树形结构（顶级分类包含 children 子分类），
 * 前端不需要再手动拼接，直接使用即可
 */
export const getCategoryList = () => {
  return request({
    url: '/category/list',        // 分类列表接口
    method: 'GET',                // GET 请求
    skipAuth: true                // 公开接口
  })
}
