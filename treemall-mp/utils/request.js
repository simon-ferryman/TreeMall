/**
 * utils/request.js — 网络请求封装（请求拦截器）
 *
 * 【设计思想】
 * uni-app 中不能使用 axios（axios 依赖浏览器 XMLHttpRequest，小程序中不可用），
 * 因此基于 uni.request 封装一个统一的请求函数，实现类似 axios 的拦截器效果。
 *
 * 【核心功能】
 * 1. 自动拼接 Base URL（避免每个 API 模块重复写完整路径）
 * 2. 自动注入 Token（从 userStore 读取，拼接到 Authorization 请求头）
 * 3. 统一响应解包（后端返回 {code, message, data}，这里提取 data 或抛出异常）
 * 4. 统一错误处理（401 跳转登录、网络异常提示）
 * 5. 请求/响应日志（开发阶段调试用）
 *
 * 【数据流】
 * API 模块调用 request(options) → 注入 Token → 发送请求 → 后端响应
 * → 检查 code → 成功则返回 data → 失败则抛出异常 → 调用方 catch 处理
 */

// ==================== 配置区 ====================

// 后端 API 基础地址
// 开发阶段：后端运行在 localhost:8080，前端通过微信开发者工具预览，两者在同一台电脑上
// 如果需要真机调试，将 localhost 改为电脑的局域网 IP（如 192.168.1.100）
const BASE_URL = 'http://localhost:8080/api/v1'

// 请求超时时间（毫秒）
// 设置 15 秒超时，避免网络异常时用户长时间等待
const TIMEOUT = 15000

// ==================== 请求封装 ====================

/**
 * 统一请求函数
 *
 * @param {Object} options - 请求配置
 * @param {string} options.url - 请求路径（不含 Base URL，如 '/auth/login'）
 * @param {string} [options.method='GET'] - 请求方法（GET/POST/PUT/DELETE）
 * @param {Object} [options.data={}] - 请求参数（GET 请求会自动拼接到 URL）
 * @param {Object} [options.header={}] - 自定义请求头（会与默认头合并）
 * @param {boolean} [options.showLoading=true] - 是否显示加载提示
 * @param {boolean} [options.skipAuth=false] - 是否跳过 Token 注入（公开接口用）
 * @returns {Promise} 返回 Promise，resolve 时返回后端 data 字段，reject 时返回错误信息
 *
 * 【使用示例】
 * // 公开接口（无需登录）
 * request({ url: '/banner/list', skipAuth: true })
 *
 * // 认证接口（需要登录）
 * request({ url: '/cart/list', method: 'GET' })
 *
 * // POST 请求
 * request({ url: '/auth/login', method: 'POST', data: { code: 'xxx' } })
 */
const request = (options) => {
  // 解构参数，设置默认值
  // method 默认 GET，data 默认空对象，showLoading 默认 true，skipAuth 默认 false
  const {
    url,           // 请求路径
    method = 'GET', // 请求方法
    data = {},      // 请求参数
    header = {},    // 自定义请求头
    showLoading = true, // 是否显示加载动画
    skipAuth = false    // 是否跳过认证（公开接口为 true）
  } = options

  // ==================== 1. 显示加载提示 ====================
  // 使用 uni.showLoading 显示全局加载动画，防止用户重复点击
  // 注意：showLoading 为 false 时不显示（如下拉刷新时不需要显示加载动画）
  if (showLoading) {
    uni.showLoading({
      title: '加载中...', // 加载提示文字
      mask: false         // 遮罩层，防止用户在此期间操作其他按钮
    })
  }

  // ==================== 2. 构建请求头 ====================
  // 默认请求头：声明请求体为 JSON 格式
  const finalHeader = {
    'Content-Type': 'application/json', // 告诉后端请求体是 JSON
    ...header                           // 合并调用方传入的自定义请求头
  }

  // 非公开接口：自动注入 Token
  // 从 userStore 读取 token，拼接到 Authorization 请求头
  // 格式：Bearer <token>（JWT 标准格式）
  if (!skipAuth) {
    // 动态导入 userStore 以获取 token
    // 注意：不能在文件顶部 import，因为 Pinia 实例需要在 main.js 中初始化后才能使用
    const { useUserStore } = require('../stores/user')
    const userStore = useUserStore()
    const token = userStore.token

    // 只有在 token 存在时才注入，避免发送空的 Authorization 头
    if (token) {
      finalHeader['Authorization'] = `Bearer ${token}`
    }
  }

  // ==================== 3. 发起请求 ====================
  // uni.request 是 uni-app 封装的网络请求 API，底层对应微信小程序的 wx.request
  // 返回 Promise 以便调用方使用 async/await 语法
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,  // 拼接完整 URL：http://localhost:8080/api/v1 + /auth/login
      method: method,       // 请求方法
      data: data,           // 请求参数
      header: finalHeader,  // 请求头
      timeout: TIMEOUT,     // 超时时间

      // ==================== 4. 处理响应 ====================
      success: (res) => {
        // 隐藏加载提示
        if (showLoading) {
          uni.hideLoading()
        }

        // 获取 HTTP 状态码
        const statusCode = res.statusCode
        // 获取后端返回的响应体
        const responseData = res.data

        // ----- 开发日志：打印请求/响应信息，方便调试 -----
        // 在微信开发者工具的控制台中可以看到每次请求的详细信息
        console.log(`[Request] ${method} ${url}`)
        console.log('[Request] 请求参数:', data)
        console.log('[Request] 响应数据:', responseData)

        // ==================== 5. 业务状态码判断 ====================
        // 后端统一返回格式：{ code: 200, message: "success", data: {...} }
        // code === 200 表示业务处理成功

        if (statusCode === 200 && responseData.code === 200) {
          // 请求成功：返回 data 字段（业务数据）
          // 调用方直接拿到业务数据，无需关心 code 和 message
          resolve(responseData.data)
        } else if (statusCode === 401) {
          // HTTP 401 未授权：Token 无效或已过期
          // 处理流程：
          // 1. 清除本地存储的 Token
          // 2. 提示用户重新登录
          // 3. 跳转到登录页
          // 使用 uni.reLaunch 清空页面栈，确保用户无法通过返回键绕过登录
          uni.removeStorageSync('token')
          uni.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none',
            duration: 2000
          })
          // 延迟跳转，让用户看到提示信息
          setTimeout(() => {
            uni.reLaunch({
              url: '/pages/login/login'
            })
          }, 1500)
          reject(new Error('未授权，请重新登录'))
        } else if (statusCode === 403) {
          // HTTP 403 禁止访问：权限不足（如普通用户访问商户接口）
          uni.showToast({
            title: responseData.message || '权限不足',
            icon: 'none',
            duration: 2000
          })
          reject(new Error(responseData.message || '权限不足'))
        } else {
          // 其他业务错误（如参数校验失败、业务逻辑错误）
          // 后端返回的 message 中包含具体错误原因
          const errorMsg = responseData.message || '请求失败'
          uni.showToast({
            title: errorMsg,
            icon: 'none',
            duration: 2000
          })
          reject(new Error(errorMsg))
        }
      },

      // ==================== 6. 处理网络异常 ====================
      // fail 回调在请求完全无法发出时触发（如网络断开、DNS 解析失败、超时）
      fail: (err) => {
        // 隐藏加载提示
        if (showLoading) {
          uni.hideLoading()
        }

        // 开发日志：打印网络错误详情
        console.error('[Request] 网络异常:', err)

        // 显示网络异常提示
        uni.showToast({
          title: '网络异常，请检查网络连接',
          icon: 'none',
          duration: 2000
        })

        // 将错误传递给调用方
        reject(err)
      }
    })
  })
}

// ==================== 导出 ====================
// 默认导出 request 函数，其他模块通过 import request from '@/utils/request' 引入
export default request
