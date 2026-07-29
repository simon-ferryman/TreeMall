/**
 * stores/user.js — 用户状态管理（Pinia Store）
 *
 * 【设计思想】
 * userStore 管理所有与用户相关的全局状态：登录状态、Token、用户信息、角色等。
 * 多个页面需要共享用户状态（如首页显示用户昵称、购物车需要 Token 请求数据），
 * 因此放在全局 Store 中管理，避免每个页面重复获取用户信息。
 *
 * 【Token 持久化策略】
 * Token 使用 uni.setStorageSync 持久化到本地存储中：
 * - 登录成功后：调用 setToken() 将 token 存入 Storage
 * - 应用启动时：在 state 初始化函数中从 Storage 恢复 token
 * - 退出登录时：调用 logout() 清除 Storage 中的 token
 *
 * 【使用场景】
 * - 登录页：调用 login() 登录成功后更新状态
 * - 首页/个人中心：读取 userInfo 显示用户信息
 * - 所有需要认证的页面：通过 token 判断是否已登录
 * - 商户端页面：通过 role 判断是否为商户角色
 */

// 从 pinia 导入 defineStore 函数，用于定义一个 Store
import { defineStore } from 'pinia'

/**
 * useUserStore — 用户状态 Store
 *
 * 【State 说明】
 * - token：JWT Token，用于后端接口认证，从 Storage 恢复
 * - userId：当前登录用户ID
 * - role：用户角色（'consumer' 消费者 / 'merchant' 商户 / 'admin' 管理员）
 * - userInfo：用户详细信息（昵称、头像、手机号等）
 * - isLogin：计算属性，判断是否已登录
 */
export const useUserStore = defineStore('user', {
  // ==================== State：状态数据 ====================
  // state 是一个函数，返回初始状态对象
  // 注意：state 函数在每次创建 Store 实例时都会执行
state: () => {
    const savedToken = uni.getStorageSync('token') || ''
    // 同时恢复持久化的用户信息
    const savedUserInfo = uni.getStorageSync('userInfo')
    let parsedUserInfo = null
    if (savedUserInfo) {
      try {
        parsedUserInfo = JSON.parse(savedUserInfo)
      } catch (e) {
        parsedUserInfo = null
      }
    }

    return {
      token: savedToken,
      userId: parsedUserInfo ? (parsedUserInfo.userId || null) : null,
      role: parsedUserInfo ? (parsedUserInfo.role || 'consumer') : 'consumer',
      userInfo: parsedUserInfo ? {
        nickname: parsedUserInfo.nickname || '',
        avatarUrl: parsedUserInfo.avatarUrl || '',
        phone: parsedUserInfo.phone || ''
      } : {
        nickname: '',
        avatarUrl: '',
        phone: ''
      }
    }
  },

  // ==================== Getters：计算属性 ====================
  // getters 类似 Vue 组件的 computed，基于 state 派生数据
  getters: {
    /**
     * isLogin — 是否已登录
     * 判断标准：token 不为空字符串即为已登录
     * 使用场景：页面守卫判断、条件渲染（登录前显示登录按钮，登录后显示用户信息）
     */
    isLogin: (state) => {
      return !!state.token  // 将 token 转为布尔值，空字符串为 false，有值为 true
    },

    /**
     * isMerchant — 是否为商户角色
     * 使用场景：控制商户端入口显示/隐藏
     */
    isMerchant: (state) => {
      return state.role === 'merchant'
    }
  },

  // ==================== Actions：操作方法 ====================
  // actions 是修改 state 的唯一途径（类似 Vuex 的 mutations + actions 合并）
  actions: {
    /**
     * setToken — 设置 Token 并持久化
     * 登录成功后调用，将后端返回的 token 存入 state 和 Storage
     *
     * @param {string} token - 后端返回的 JWT Token
     *
     * 【为什么同时存 state 和 Storage？】
     * - state：供当前会话中所有组件通过 Store 访问
     * - Storage：应用重启后仍可读取，避免每次打开小程序都要重新登录
     */
    setToken(token) {
      this.token = token                          // 更新 state 中的 token
      uni.setStorageSync('token', token)          // 持久化到本地存储
    },

    /**
     * setUserInfo — 设置用户信息
     * 登录成功后调用，将后端返回的用户信息存入 state
     *
     * @param {Object} userInfo - 后端返回的用户信息对象
     */
    setUserInfo(userInfo) {
      this.userId = userInfo.id || userInfo.userId
      this.role = userInfo.role || 'consumer'
      this.userInfo = {
        nickname: userInfo.nickname || '',
        avatarUrl: userInfo.avatarUrl || '',
        phone: userInfo.phone || ''
      }
      // 持久化用户信息到 Storage，避免应用重启后丢失
      uni.setStorageSync('userInfo', JSON.stringify({
        userId: this.userId,
        role: this.role,
        nickname: this.userInfo.nickname,
        avatarUrl: this.userInfo.avatarUrl,
        phone: this.userInfo.phone
      }))
    },

    /**
     * login — 登录操作
     * 整合 Token 存储和用户信息存储，供登录页调用
     *
     * @param {Object} loginData - 登录成功后后端返回的完整数据
     * @param {string} loginData.token - JWT Token
     * @param {Object} loginData.userInfo - 用户信息
     *
     * 【调用流程】
     * 登录页 → 调用 API 登录接口 → 获取 {token, userInfo} → 调用本方法 → 更新 state
     */
    login(loginData) {
      this.setToken(loginData.token)              // 存储 Token
      this.setUserInfo(loginData.userInfo)        // 存储用户信息
    },

    /**
     * logout — 退出登录
     * 清除所有用户状态和本地存储的 Token
     *
     * 【调用场景】
     * - 用户点击"退出登录"按钮
     * - Token 过期被 401 拦截器自动调用
     *
     * 【注意】退出后需要跳转到登录页，由调用方处理跳转逻辑
     */
    logout() {
      this.token = ''
      this.userId = null
      this.role = 'consumer'
      this.userInfo = {
        nickname: '',
        avatarUrl: '',
        phone: ''
      }
      uni.removeStorageSync('token')              // 清除持久化的 token
      uni.removeStorageSync('userInfo')            // 清除持久化的用户信息
    }
  }
})
