/**
 * utils/navigator.js — 统一路由跳转管理
 *
 * 【设计思想】
 * 将所有页面跳转方法集中管理，任何页面需要跳转时调用此文件的方法，
 * 而非直接写 uni.navigateTo。这样做的好处：
 * 1. 路由路径变更时只需改一处（如页面路径调整）
 * 2. 跳转参数统一管理，避免各页面传参不一致
 * 3. 未来加页面只需在此文件新增方法，不影响现有代码
 * 4. 编译时可检查路由是否存在，减少运行时错误
 *
 * 【使用方式】
 * import { navTo } from '@/utils/navigator'
 * navTo.productDetail(123)
 * navTo.orderDetail(456)
 *
 * 【跳转类型说明】
 * - navigateTo: 保留当前页面，跳转到新页面（最多 10 层）
 * - redirectTo: 关闭当前页面，跳转到新页面
 * - switchTab: 跳转到 TabBar 页面，关闭其他所有非 TabBar 页面
 * - reLaunch: 关闭所有页面，打开新页面
 * - navigateBack: 返回上一页
 */

/**
 * 构建 URL 查询字符串
 * 将对象转为 URL 参数格式：{ id: 1, name: 'a' } → '?id=1&name=a'
 * 忽略 undefined 和 null 值
 *
 * @param {Object} params - 查询参数对象
 * @returns {string} URL 查询字符串（含 ? 前缀）
 */
const buildQuery = (params = {}) => {
  const keys = Object.keys(params).filter(k => params[k] !== undefined && params[k] !== null)
  if (keys.length === 0) return ''
  return '?' + keys.map(k => `${k}=${encodeURIComponent(params[k])}`).join('&')
}

// ==================== 商品相关 ====================

/** 跳转商品详情页 */
const productDetail = (productId) => {
  uni.navigateTo({ url: `/pages/goods/detail?id=${productId}` })
}

/** 跳转搜索页 */
const search = (keyword = '') => {
  uni.navigateTo({ url: `/pages/search/search${buildQuery({ keyword })}` })
}

// ==================== 订单相关 ====================

/** 跳转订单详情页 */
const orderDetail = (orderId) => {
  uni.navigateTo({ url: `/pages/order/detail?id=${orderId}` })
}

/** 跳转订单列表页 */
const orderList = (status = '') => {
  uni.navigateTo({ url: `/pages/order/list${buildQuery({ status })}` })
}

/** 跳转下单确认页 */
const orderCreate = () => {
  uni.navigateTo({ url: '/pages/order/create' })
}

// ==================== 支付相关 ====================

/** 跳转支付结果页 */
const paymentResult = (orderId, amount, status = 'success') => {
  uni.redirectTo({ url: `/pages/payment/result${buildQuery({ orderId, amount, status })}` })
}

// ==================== 地址相关 ====================

/** 跳转地址列表页 */
const addressList = () => {
  uni.navigateTo({ url: '/pages/address/list' })
}

/** 跳转地址编辑页（新增模式） */
const addressAdd = () => {
  uni.navigateTo({ url: '/pages/address/edit' })
}

/** 跳转地址编辑页（编辑模式） */
const addressEdit = (addressId) => {
  uni.navigateTo({ url: `/pages/address/edit?id=${addressId}` })
}

// ==================== 用户相关 ====================

/** 跳转登录页 */
const login = () => {
  uni.navigateTo({ url: '/pages/login/login' })
}

/** 跳转收藏页 */
const favorites = () => {
  uni.navigateTo({ url: '/pages/favorites/favorites' })
}

/** 跳转通知页 */
const notify = () => {
  uni.navigateTo({ url: '/pages/notify/notify' })
}

/** 跳转设置页 */
const settings = () => {
  uni.navigateTo({ url: '/pages/settings/settings' })
}

// ==================== 商户端 ====================

/** 跳转商品管理列表 */
const merchantGoodsList = () => {
  uni.navigateTo({ url: '/pages/merchant/goods-manage/list' })
}

/** 跳转商品添加/编辑页 */
const merchantGoodsEdit = (productId = '') => {
  uni.navigateTo({ url: `/pages/merchant/goods-manage/edit${buildQuery({ id: productId })}` })
}

/** 跳转订单管理列表 */
const merchantOrderList = () => {
  uni.navigateTo({ url: '/pages/merchant/order-manage/list' })
}

// ==================== TabBar 页面 ====================

/** 跳转首页（TabBar 页面，使用 switchTab） */
const home = () => {
  uni.switchTab({ url: '/pages/index/index' })
}

/** 跳转分类页（TabBar 页面） */
const category = () => {
  uni.switchTab({ url: '/pages/category/category' })
}

/** 跳转购物车（TabBar 页面） */
const cart = () => {
  uni.switchTab({ url: '/pages/cart/cart' })
}

/** 跳转个人中心（TabBar 页面） */
const user = () => {
  uni.switchTab({ url: '/pages/user/user' })
}

// ==================== 通用 ====================

/** 返回上一页 */
const back = (delta = 1) => {
  uni.navigateBack({ delta })
}

/** 重新启动到登录页（用于 Token 过期等场景） */
const reLaunchToLogin = () => {
  uni.reLaunch({ url: '/pages/login/login' })
}

// ==================== 导出 ====================
export const navTo = {
  // 商品
  productDetail,
  search,
  // 订单
  orderDetail,
  orderList,
  orderCreate,
  // 支付
  paymentResult,
  // 地址
  addressList,
  addressAdd,
  addressEdit,
  // 用户
  login,
  favorites,
  notify,
  settings,
  // 商户
  merchantGoodsList,
  merchantGoodsEdit,
  merchantOrderList,
  // TabBar
  home,
  category,
  cart,
  user,
  // 通用
  back,
  reLaunchToLogin
}