/**
 * utils/image-url.js — 图片路径转换工具
 *
 * 【设计思想】
 * 后端数据库存储的是相对路径（如 /images/banner/banner1.jpg），
 * 但小程序 <image> 组件需要绝对 URL 才能请求到后端服务器。
 * 本工具统一处理图片路径转换，所有组件通过此工具获取图片地址。
 *
 * 【使用方式】
 * import { getImageUrl } from '@/utils/image-url'
 * <image :src="getImageUrl(item.imageUrl)" />
 */

// 后端服务器基础地址（与 request.js 中的 BASE_URL 保持一致）
const BASE_URL = 'http://localhost:8080'

/**
 * 将相对路径转换为完整的图片访问 URL
 *
 * @param {string} path - 图片相对路径（如 /images/banner/banner1.jpg）
 * @returns {string} 完整的图片 URL（如 http://localhost:8080/images/banner/banner1.jpg ）
 *
 * 【处理逻辑】
 * 1. 空值保护：path 为空时返回默认占位图（本地静态资源）
 * 2. 已是完整 URL（http/https 开头）：直接返回
 * 3. 相对路径：拼接 BASE_URL 后返回
 */
export const getImageUrl = (path) => {
  // 空值保护：没有图片路径时返回默认占位图
  if (!path) {
    return '/static/images/product-default.png'
  }
  // 已经是完整 URL（如微信头像），直接返回
  if (path.startsWith('https://') || path.startsWith('http://')) {
    return path
  }
  // 相对路径：拼接后端基础地址
  // 归一化处理：将 Windows 反斜杠 \ 替换为 URL 正斜杠 /
  const normalizedPath = path.replace(/\\/g, '/')
  // 确保路径以 / 开头
  const finalPath = normalizedPath.startsWith('/') ? normalizedPath : '/' + normalizedPath
  return BASE_URL + finalPath
}
