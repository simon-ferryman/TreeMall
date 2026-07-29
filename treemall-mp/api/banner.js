/**
 * api/banner.js — 轮播图模块 API 封装
 *
 * 【对应后端】BannerController — /api/v1/banner
 */

import request from '@/utils/request'

/**
 * 获取轮播图列表
 *
 * @returns {Promise} 返回轮播图数组 [{ id, imageUrl, linkUrl, sortOrder }]
 *
 * 【使用场景】
 * - App.vue onLaunch 预加载
 * - 首页 BannerSwiper 组件展示
 * - 管理后台轮播图管理
 */
export const getBannerList = () => {
  return request({
    url: '/banner/list',          // 轮播图列表接口
    method: 'GET',                // GET 请求
    skipAuth: true                // 公开接口（未登录用户也能看到首页轮播图）
  })
}
