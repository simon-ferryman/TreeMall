/**
 * stores/cart.js — 购物车状态管理（Pinia Store）
 *
 * 【设计思想】
 * 购物车是电商小程序的核心状态，需要在多个页面间共享：
 * - 商品详情页：加入购物车
 * - 购物车页：展示、修改、删除
 * - 下单确认页：展示已选商品、计算总价
 *
 * 因此购物车状态放入全局 Store，保证数据一致性。
 *
 * 【数据结构】
 * items 数组中每个元素：
 * {
 *   id: Number,          // 购物车项ID（后端 cartId）
 *   productId: Number,   // 商品ID
 *   productName: String, // 商品名称
 *   price: Number,       // 单价（元）
 *   quantity: Number,    // 数量
 *   mainImage: String,   // 商品主图
 *   checked: Boolean,    // 是否选中（用于结算）
 * }
 */

import { defineStore } from 'pinia'
// 导入购物车 API 模块
import { getCartList, addToCart, updateCartQuantity, removeCartItem, toggleCartCheck } from '@/api/cart'

export const useCartStore = defineStore('cart', {
  // ==================== State ====================
  state: () => ({
    items: []  // 购物车商品列表
  }),

  // ==================== Getters ====================
  getters: {
    /**
     * count — 购物车商品总数量
     * 使用场景：TabBar 购物车角标数字
     * 计算所有商品的 quantity 之和
     */
    count: (state) => {
      // reduce 累加所有商品的 quantity
      return state.items.reduce((sum, item) => sum + item.quantity, 0)
    },

    /**
     * checkedItems — 已选中的商品列表
     * 使用场景：下单确认页展示即将购买的商品
     */
    checkedItems: (state) => {
      return state.items.filter(item => item.checked === true)
    },

    /**
     * checkedCount — 已选中商品数量
     */
    checkedCount() {
      return this.checkedItems.length
    },

    /**
     * checkedTotal — 已选中商品总价（元）
     * 使用场景：底部结算栏显示合计金额
     * 计算：每个已选商品 单价 × 数量 的总和
     *
     * 【注意】toFixed(2) 返回字符串，用 Number() 转回数字
     * 保留两位小数，避免浮点数精度问题（如 0.1 + 0.2 = 0.30000000000000004）
     */
    checkedTotal: (state) => {
      const total = state.items
        .filter(item => item.checked)           // 筛选已选中的
        .reduce((sum, item) => {
          return sum + (item.price * item.quantity) // 累加：单价 × 数量
        }, 0)
      return Number(total.toFixed(2))            // 保留两位小数
    },

    /**
     * isAllChecked — 是否全选
     * 使用场景：全选/取消全选按钮的状态
     * 所有商品都选中时为 true
     */
    isAllChecked: (state) => {
      // 购物车为空时不算全选
      if (state.items.length === 0) return false
      // every 检查是否所有商品都 checked
      return state.items.every(item => item.checked)
    }
  },

  // ==================== Actions ====================
  actions: {
    /**
     * fetchCart — 从后端获取购物车列表
     * 在购物车页面 onShow 时调用，确保数据是最新的
     */
    async fetchCart() {
      try {
        const data = await getCartList()
        // 后端返回的 CartVO 中包含 checked 字段（TINYINT，1=选中/0=未选中）
        // 转换为布尔值方便前端使用
        this.items = (data || []).map(item => ({
          ...item,
		  id: item.cartId,   // ← 新增：将 CartVO 的 cartId 映射为前端通用的 id
          checked: item.checked === 1 || item.checked === true  // 统一转为布尔值
        }))
      } catch (err) {
        console.error('[CartStore] 获取购物车失败:', err)
        this.items = []
      }
    },

    /**
     * addItem — 加入购物车
     * 在商品详情页点击"加入购物车"时调用
     *
     * @param {Object} params - { productId, quantity }
     * 加入成功后刷新购物车列表
     */
    async addItem(params) {
      await addToCart(params)           // 调用后端 API
      await this.fetchCart()            // 重新获取购物车列表（同步最新数据）
    },

    /**
     * updateQuantity — 更新商品数量
     * 在购物车页点击 +/- 按钮时调用
     *
     * @param {Number} cartId - 购物车项ID
     * @param {Number} quantity - 新数量
     *
     * 【乐观更新策略】
     * 先更新本地状态（用户立即看到变化），再请求后端。
     * 如果后端请求失败，回滚到旧值。
     * 这种策略让用户感觉操作非常流畅，无需等待网络响应。
     */
    async updateQuantity(cartId, quantity) {
      const item = this.items.find(i => i.id === cartId)
      if (item) {
        const oldQuantity = item.quantity       // 保存旧值，用于失败回滚
        item.quantity = quantity                // 乐观更新：先改本地
        try {
          await updateCartQuantity(cartId, quantity) // 调用后端 API
        } catch (err) {
          item.quantity = oldQuantity           // 失败回滚：恢复旧值
          throw err
        }
      }
    },

    /**
     * removeItem — 删除购物车项
     * 在购物车页滑动删除或点击删除按钮时调用
     *
     * @param {Number} cartId - 购物车项ID
     */
    async removeItem(cartId) {
      await removeCartItem(cartId)       // 调用后端 API
      // 从本地列表中移除
      this.items = this.items.filter(item => item.id !== cartId)
    },

    /**
     * toggleCheck — 切换选中状态
     * 在购物车页点击勾选框时调用
     *
     * @param {Number} cartId - 购物车项ID
     */
    async toggleCheck(cartId) {
      const item = this.items.find(i => i.id === cartId)
      if (item) {
        item.checked = !item.checked          // 乐观更新：先切换
        try {
          await toggleCartCheck(cartId)        // 调用后端 API
        } catch (err) {
          item.checked = !item.checked         // 失败回滚
          throw err
        }
      }
    },

    /**
     * toggleAllCheck — 全选/取消全选
     * 在购物车页点击全选按钮时调用
     */
    toggleAllCheck() {
      const newChecked = !this.isAllChecked    // 目标状态：当前全选→取消全选，未全选→全选
      // 更新所有商品的选中状态
      this.items.forEach(item => {
        item.checked = newChecked
      })
      // 注意：全选目前只更新本地状态，暂不调用后端批量接口
      // 如需持久化，可在后续版本中调用后端批量更新接口
    },

    /**
     * clearChecked — 清空已选中的商品
     * 下单成功后调用，清空已选中的商品
     * 只保留未选中的商品（用户可能还想以后再买）
     */
    clearChecked() {
      this.items = this.items.filter(item => !item.checked)
    }
  }
})
