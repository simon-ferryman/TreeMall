/**
 * stores/app.js — 应用全局状态管理（Pinia Store）
 *
 * 【设计思想】
 * appStore 管理应用级别的全局数据，这些数据在多个页面之间共享，
 * 放入 Store 可以避免每个页面重复请求，减少网络开销。
 *
 * 【核心数据】
 * - banners：首页轮播图数据（首页使用）
 * - categories：商品分类列表（首页 + 分类页共享）
 * - loading：全局加载状态
 */

import { defineStore } from 'pinia'
// 导入 API 模块（轮播图、分类）
import { getBannerList } from '@/api/banner'
import { getCategoryList } from '@/api/category'

export const useAppStore = defineStore('app', {
  // ==================== State ====================
  state: () => ({
    banners: [],           // 轮播图列表（首页使用）
    categories: [],        // 分类列表（首页分类宫格 + 分类页左侧导航）
    loading: false         // 全局加载状态
  }),

  // ==================== Getters ====================
  getters: {
    /**
     * topCategories — 获取顶级分类（parentId === 0 的分类）
     * 用于首页分类宫格和分类页左侧导航
     */
    topCategories: (state) => {
      return state.categories.filter(cat => cat.parentId === 0)
    }
  },

  // ==================== Actions ====================
  actions: {
    /**
     * fetchBanners — 获取轮播图数据
     * 在 App.vue 的 onLaunch 中调用，预加载数据
     * 首页直接使用缓存数据，无需重复请求
     */
    async fetchBanners() {
      try {
        // 调用轮播图 API（公开接口，无需登录）
        const data = await getBannerList()
        this.banners = data || [] // 空值保护：如果后端返回 null，使用空数组
      } catch (err) {
        console.error('[AppStore] 获取轮播图失败:', err)
        // 获取失败不影响应用启动，静默处理
        this.banners = []
      }
    },

    /**
     * fetchCategories — 获取分类列表
     * 在 App.vue 的 onLaunch 中调用，预加载数据
     */
    async fetchCategories() {
      try {
        // 调用分类列表 API（公开接口，无需登录）
        const data = await getCategoryList()
        this.categories = data || []
      } catch (err) {
        console.error('[AppStore] 获取分类失败:', err)
        this.categories = []
      }
    }
  }
})
