/**
 * pages/order/constants.js — 订单模块常量配置
 *
 * 【设计思想】
 * 将订单状态、支付状态等常量集中管理，页面和组件通过引用常量而非硬编码字符串。
 * 这样做的好处：
 * 1. 状态值变更时只需改一处（如状态码调整）
 * 2. 类型安全：IDE 可以自动补全和校验
 * 3. 新增状态时只需在此文件添加，不影响现有页面逻辑
 * 4. 统一了状态对应的 UI 展示（颜色、图标、文字）
 *
 * 【使用方式】
 * import { ORDER_STATUS, ORDER_STATUS_MAP } from '@/pages/order/constants'
 * const statusInfo = ORDER_STATUS_MAP[ORDER_STATUS.PENDING_PAYMENT]
 */

// ==================== 订单状态枚举 ====================
/** 后端返回的订单状态值 */
export const ORDER_STATUS = {
  PENDING_PAYMENT: 'PENDING_PAYMENT',   // 待付款
  PENDING_DELIVERY: 'PENDING_DELIVERY', // 待发货（已付款）
  DELIVERED: 'DELIVERED',               // 待收货（已发货）
  RECEIVED: 'RECEIVED',                 // 已完成（已收货）
  CANCELLED: 'CANCELLED'                // 已取消
}

// ==================== 订单状态 UI 映射 ====================
/** 每个状态对应的 UI 展示信息 */
export const ORDER_STATUS_MAP = {
  [ORDER_STATUS.PENDING_PAYMENT]: {
    label: '待付款',
    color: '#FF9500',        // iOS Orange
    bgColor: '#FFF2E5',
    icon: 'clock',
    description: '请尽快完成支付，超时订单将自动取消'
  },
  [ORDER_STATUS.PENDING_DELIVERY]: {
    label: '待发货',
    color: '#007AFF',        // Apple Blue
    bgColor: '#E5F1FF',
    icon: 'car',
    description: '商家正在备货中，请耐心等待'
  },
  [ORDER_STATUS.DELIVERED]: {
    label: '待收货',
    color: '#34C759',        // iOS Green
    bgColor: '#E8F8EE',
    icon: 'checkbox-mark',
    description: '商品已发货，请注意查收'
  },
  [ORDER_STATUS.RECEIVED]: {
    label: '已完成',
    color: '#8E8E93',        // iOS Gray
    bgColor: '#F2F2F7',
    icon: 'checkmark-circle',
    description: '感谢您的购买，欢迎再次光临'
  },
  [ORDER_STATUS.CANCELLED]: {
    label: '已取消',
    color: '#FF3B30',        // iOS Red
    bgColor: '#FFEBEA',
    icon: 'close-circle',
    description: '该订单已取消'
  }
}

// ==================== 订单状态 Tab 列表 ====================
/** 订单列表页的状态筛选 Tab */
export const ORDER_TABS = [
  { label: '全部', value: '' },
  { label: '待付款', value: ORDER_STATUS.PENDING_PAYMENT },
  { label: '待发货', value: ORDER_STATUS.PENDING_DELIVERY },
  { label: '待收货', value: ORDER_STATUS.DELIVERED },
  { label: '已完成', value: ORDER_STATUS.RECEIVED }
]

// ==================== 支付状态枚举 ====================
export const PAYMENT_STATUS = {
  SUCCESS: 'success',
  FAIL: 'fail',
  PENDING: 'pending'
}

// ==================== 支付状态 UI 映射 ====================
export const PAYMENT_STATUS_MAP = {
  [PAYMENT_STATUS.SUCCESS]: {
    label: '支付成功',
    icon: 'checkmark-circle',
    color: '#34C759',
    description: '感谢您的购买，我们将尽快为您发货'
  },
  [PAYMENT_STATUS.FAIL]: {
    label: '支付失败',
    icon: 'close-circle',
    color: '#FF3B30',
    description: '支付未完成，请重试或选择其他支付方式'
  },
  [PAYMENT_STATUS.PENDING]: {
    label: '支付处理中',
    icon: 'clock',
    color: '#FF9500',
    description: '支付正在处理中，请稍候'
  }
}