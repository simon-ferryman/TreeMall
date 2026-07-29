/**
 * api/auth.js — 认证模块 API 封装
 *
 * 【设计思想】
 * 每个 API 模块对应一个后端 Controller，将 HTTP 请求封装为语义化的函数。
 * 页面层不直接调用 request，而是调用这些 API 函数，实现关注点分离。
 *
 * 【对应后端】AuthController — /api/v1/auth
 */

// 导入统一请求函数（所有 API 模块都通过它发请求）
import request from '@/utils/request'

/**
 * 微信登录
 * 使用微信小程序 wx.login() 获取的 code 换取后端 JWT Token
 *
 * @param {string} code - 微信登录凭证（wx.login() 返回的 code）
 * @returns {Promise} 返回 { token, userInfo } 对象
 *
 * 【使用场景】用户点击"微信一键登录"按钮
 * 【注意】开发阶段后端可能未实现微信登录，使用 devLogin 代替
 */
export const wxLogin = (code) => {
  return request({
    url: '/auth/login',           // 后端接口路径（会自动拼接 Base URL）
    method: 'POST',               // POST 请求
    data: { code },               // 请求体：{ "code": "xxxxx" }
    skipAuth: true                // 公开接口，无需 Token（登录前还没有 Token）
  })
}

/**
 * 开发环境登录（模拟登录）
 * 直接传入 userId 和 role 换取 Token，跳过微信登录流程
 *
 * @param {Object} params - { userId: Number, role: String }
 * @returns {Promise} 返回 { token, userInfo } 对象
 *
 * 【使用场景】开发阶段测试用，无需真实微信环境
 * 【注意】生产环境应删除此接口调用
 */
export const devLogin = (params) => {
  return request({
    url: '/auth/dev-login',       // 后端开发专用登录接口
    method: 'POST',               // POST 请求
    data: params,                 // 请求体：{ "userId": 1, "role": "consumer" }
    skipAuth: true                // 公开接口
  })
}

/**
 * 获取当前用户信息
 *
 * @returns {Promise} 返回用户信息对象 { id, nickname, avatarUrl, phone, role }
 *
 * 【使用场景】
 * - 登录成功后获取完整用户信息
 * - 个人中心页面刷新用户信息
 * - 需要判断用户角色时
 *
 * 【对应后端】AuthController — /api/v1/auth/userinfo
 */
export const getUserInfo = () => {
  return request({
    url: '/auth/userinfo',        // 获取用户信息接口
    method: 'GET'                 // GET 请求
    // 注意：不传 skipAuth，会自动注入 Token（由 request.js 处理）
    // 只有登录用户才能获取自己的信息
  })
}

/**
 * 更新用户信息
 *
 * @param {Object} data - { nickname, avatarUrl, phone }
 * @returns {Promise}
 *
 * 【使用场景】个人中心编辑资料
 */
export const updateUserInfo = (data) => {
  return request({
    url: '/auth/userinfo',        // 更新用户信息接口（与获取是同一个 URL，通过 method 区分）
    method: 'PUT',                // PUT 请求（RESTful 风格：PUT 表示更新）
    data                          // 请求体：{ "nickname": "...", "avatarUrl": "...", "phone": "..." }
  })
}
