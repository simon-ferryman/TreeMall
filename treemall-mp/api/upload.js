/**
 * api/upload.js — 文件上传模块 API 封装
 *
 * 【对应后端】FileController — /api/v1/file
 *
 * 【设计说明】
 * 文件上传不能使用 request.js 封装（因为它基于 uni.request，不支持文件流），
 * 需要直接使用 uni.uploadFile API。
 * 但 Token 注入逻辑需要手动处理（因为跳过了 request.js 的拦截器）。
 */

// 导入 userStore 获取 Token
import { useUserStore } from '@/stores/user'

// 后端 API 基础地址（与 request.js 保持一致）
const BASE_URL = 'http://localhost:8080/api/v1'

/**
 * 上传图片文件
 *
 * @param {Object} options - { filePath: String, fileName: String }
 * @param {string} options.filePath - 本地文件路径（通过 uni.chooseImage 获取）
 * @param {string} [options.fileName='file'] - 上传字段名
 * @returns {Promise} 返回 { url: String } 上传后的文件访问 URL
 *
 * 【使用场景】
 * - 商户商品管理页上传商品图片
 * - 用户个人中心上传头像
 *
 * 【注意】uni.uploadFile 不支持 request.js 的统一拦截器，需要手动注入 Token
 */
export const uploadImage = ({ filePath, fileName = 'file' }) => {
  return new Promise((resolve, reject) => {
    // 从 userStore 获取 Token（手动注入，因为跳过了 request.js 的拦截器）
    const userStore = useUserStore()
    const token = userStore.token

    // uni.uploadFile 是 uni-app 提供的文件上传 API
    // 底层对应微信小程序的 wx.uploadFile
    uni.uploadFile({
      url: BASE_URL + '/merchant/file/upload',   // 后端实际路径包含 /merchant 前缀
      filePath: filePath,               // 本地文件路径
      name: fileName,                   // 后端接收的字段名
      header: {
        'Authorization': `Bearer ${token}`  // 手动注入 Token（因为 uni.uploadFile 不走 request.js）
      },
      success: (res) => {
        // uni.uploadFile 返回的 res.data 是字符串，需要手动解析 JSON
        const data = JSON.parse(res.data)
        if (data.code === 200) {
          resolve(data.data)            // 上传成功：返回 { url: "http://..." }
        } else {
          uni.showToast({
            title: data.message || '上传失败',
            icon: 'none'
          })
          reject(new Error(data.message))
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '上传失败，请检查网络',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}
