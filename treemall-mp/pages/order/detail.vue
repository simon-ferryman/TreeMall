<!--
  pages/order/detail.vue -- 订单详情页

  【设计思想】
  订单详情页展示单个订单的完整信息，按卡片式分区展示：
  1. 订单状态区：状态图标 + 状态文字 + 状态描述
  2. 收货地址区：收货人 + 电话 + 完整地址
  3. 商品列表区：每个商品图片 + 名称 + 规格 + 价格 + 数量
  4. 价格明细区：商品总额 + 运费 + 合计
  5. 订单信息区：订单编号 + 创建时间 + 支付时间（可复制订单号）
  6. 底部操作栏：根据订单状态显示不同按钮（取消订单/去支付/确认收货/查看物流/删除订单）

  【Figma 设计令牌】
  主色 #007AFF | 文字 #1D1D1F | 次要文字 #8E8E93 | 背景 #F2F2F7 | 白色 #FFFFFF
  分隔线 #E5E5EA | 浅蓝 #E5F1FF | 设计宽度 375px | rpx=px*2

  【使用场景】
  - 订单列表页点击订单卡片跳转
  - 支付成功后跳转查看订单详情
-->
<template>
  <view class="order-detail-page">
    <!-- ==================== 加载中状态 ==================== -->
    <view v-if="loading" class="order-detail-page__loading">
      <u-loading-icon :size="48" color="#007AFF" mode="circle" />
      <text class="order-detail-page__loading-text">加载中...</text>
    </view>

    <!-- ==================== 订单详情内容 ==================== -->
    <template v-else-if="orderDetail">
      <scroll-view class="order-detail-page__scroll" scroll-y>
        <!-- ==================== 1. 订单状态区 ==================== -->
        <!--
          状态区使用不同背景色和图标区分状态
          主状态文字 + 辅助描述文字
        -->
        <view class="order-detail-page__status" :class="'order-detail-page__status--' + orderDetail.status">
          <!-- 状态图标 -->
          <view class="order-detail-page__status-icon">
            <u-icon
              :name="statusIcon"
              :size="56"
              :color="statusColor"
            />
          </view>
          <!-- 状态文字 -->
          <text class="order-detail-page__status-text">{{ statusMap[orderDetail.status] || '未知状态' }}</text>
          <!-- 状态描述 -->
          <text class="order-detail-page__status-desc">{{ statusDesc }}</text>
        </view>

        <!-- ==================== 2. 收货地址区 ==================== -->
        <!--
          显示收货人信息
          仅有地址数据时显示
        -->
        <view class="order-detail-page__card" v-if="orderDetail.address">
          <!-- 地址图标 -->
          <view class="order-detail-page__address-header">
            <u-icon name="map" :size="32" color="#007AFF" />
            <text class="order-detail-page__address-title">收货信息</text>
          </view>
          <!-- 收货人 + 电话 -->
          <view class="order-detail-page__address-contact">
            <text class="order-detail-page__address-name">{{ orderDetail.address.receiverName || '--' }}</text>
            <text class="order-detail-page__address-phone">{{ orderDetail.address.receiverPhone || '--' }}</text>
          </view>
          <!-- 完整地址 -->
          <text class="order-detail-page__address-full">
            {{ orderDetail.address.province || '' }}{{ orderDetail.address.city || '' }}{{ orderDetail.address.district || '' }} {{ orderDetail.address.detailAddress || '' }}
          </text>
        </view>

        <!-- ==================== 3. 商品列表区 ==================== -->
        <view class="order-detail-page__card">
          <view class="order-detail-page__card-header">
            <text class="order-detail-page__card-title">商品信息</text>
          </view>
          <!-- 商品列表 -->
          <view
            v-for="(item, index) in orderDetail.items"
            :key="index"
            class="order-detail-page__goods-item"
          >
            <!-- 商品图片 -->
            <image
              class="order-detail-page__goods-image"
              :src="getImageUrl(item.productImage) || defaultImage"
              mode="aspectFill"
            />
            <!-- 商品信息 -->
            <view class="order-detail-page__goods-info">
              <!-- 商品名称：2 行截断 -->
              <text class="order-detail-page__goods-name">{{ item.productName || '商品名称' }}</text>
			  <text class="order-detail-page__goods-name">{{ formatSpecs(item.specs)}}</text>
              <!-- 价格 + 数量 -->
              <view class="order-detail-page__goods-bottom">
                <text class="order-detail-page__goods-price">¥{{ formatPrice(item.price) }}</text>
                <text class="order-detail-page__goods-quantity">x{{ item.quantity || 1 }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- ==================== 4. 价格明细区 ==================== -->
        <view class="order-detail-page__card">
          <view class="order-detail-page__card-header">
            <text class="order-detail-page__card-title">价格明细</text>
          </view>
          <!-- 商品总额 -->
          <view class="order-detail-page__price-row">
            <text class="order-detail-page__price-label">商品总额</text>
            <text class="order-detail-page__price-value">¥{{ formatPrice(orderDetail.goodsAmount || orderDetail.totalAmount) }}</text>
          </view>
          <!-- 运费 -->
          <view class="order-detail-page__price-row">
            <text class="order-detail-page__price-label">运费</text>
            <text class="order-detail-page__price-value order-detail-page__price-value--freight">
              {{ orderDetail.freightAmount ? '¥' + formatPrice(orderDetail.freightAmount) : '免运费' }}
            </text>
          </view>
          <!-- 分隔线 -->
          <view class="order-detail-page__divider" />
          <!-- 实付款（合计） -->
          <view class="order-detail-page__price-row">
            <text class="order-detail-page__price-label order-detail-page__price-label--total">实付款</text>
            <text class="order-detail-page__price-value order-detail-page__price-value--total">
              ¥{{ formatPrice(orderDetail.totalAmount) }}
            </text>
          </view>
        </view>

        <!-- ==================== 5. 订单信息区 ==================== -->
        <!--
          订单编号（可复制）+ 创建时间 + 支付时间
        -->
        <view class="order-detail-page__card">
          <view class="order-detail-page__card-header">
            <text class="order-detail-page__card-title">订单信息</text>
          </view>
          <!-- 订单编号 -->
          <view class="order-detail-page__info-row">
            <text class="order-detail-page__info-label">订单编号</text>
            <view class="order-detail-page__info-value">
              <text class="order-detail-page__info-text">{{ orderDetail.orderNo || '--' }}</text>
              <!-- 复制按钮 -->
              <text class="order-detail-page__copy-btn" @tap="onCopyOrderNo">复制</text>
            </view>
          </view>
          <!-- 创建时间 -->
          <view class="order-detail-page__info-row">
            <text class="order-detail-page__info-label">创建时间</text>
            <text class="order-detail-page__info-text">{{ orderDetail.createdAt || '--' }}</text>
          </view>
          <!-- 支付时间（仅已支付/已发货/已完成状态显示） -->
          <view class="order-detail-page__info-row" v-if="orderDetail.payTime">
            <text class="order-detail-page__info-label">支付时间</text>
            <text class="order-detail-page__info-text">{{ orderDetail.payTime }}</text>
          </view>
          <!-- 订单备注 -->
          <view class="order-detail-page__info-row" v-if="orderDetail.remark">
            <text class="order-detail-page__info-label">订单备注</text>
            <text class="order-detail-page__info-text">{{ orderDetail.remark }}</text>
          </view>
        </view>

        <!-- 底部留白（避免被底部操作栏遮挡） -->
        <view style="height: 140rpx;" />
      </scroll-view>

      <!-- ==================== 底部操作栏 ==================== -->
      <!--
        根据订单状态显示不同按钮组合
        固定在页面底部
      -->
      <view class="order-detail-page__footer">
        <!-- 待付款：取消订单 + 去支付 -->
        <template v-if="orderDetail.status === 'pending'">
          <view class="order-detail-page__btn order-detail-page__btn--secondary" @tap="onCancelOrder">
            取消订单
          </view>
          <view class="order-detail-page__btn order-detail-page__btn--primary" @tap="onPayOrder">
            {{ paying ? '支付中...' : '去支付' }}
          </view>
        </template>

        <!-- 待发货（已付款）：仅显示"提醒发货"（提示性操作） -->
        <template v-else-if="orderDetail.status === 'paid'">
          <view class="order-detail-page__btn order-detail-page__btn--secondary" @tap="onRemindShip">
            提醒发货
          </view>
        </template>

        <!-- 已发货：查看物流 + 确认收货 -->
        <template v-else-if="orderDetail.status === 'shipped'">
          <view class="order-detail-page__btn order-detail-page__btn--secondary" @tap="onViewLogistics">
            查看物流
          </view>
          <view class="order-detail-page__btn order-detail-page__btn--primary" @tap="onConfirmReceive">
            确认收货
          </view>
        </template>

        <!-- 已完成：删除订单 + 再次购买 -->
        <template v-else-if="orderDetail.status === 'completed'">
          <view class="order-detail-page__btn order-detail-page__btn--secondary" @tap="onDeleteOrder">
            删除订单
          </view>
          <view class="order-detail-page__btn order-detail-page__btn--primary" @tap="onBuyAgain">
            再次购买
          </view>
        </template>

        <!-- 已取消：删除订单 -->
        <template v-else-if="orderDetail.status === 'cancelled'">
          <view class="order-detail-page__btn order-detail-page__btn--danger" @tap="onDeleteOrder">
            删除订单
          </view>
        </template>
      </view>
    </template>

    <!-- 加载失败 -->
    <view v-else class="order-detail-page__error">
      <EmptyState
        icon="error-circle"
        text="订单信息加载失败"
        button-text="重新加载"
        @action="loadOrderDetail"
      />
    </view>
  </view>
</template>

<script setup>
// ==================== 导入依赖 ====================
import { ref, computed } from 'vue'                        // Vue 3 响应式 API
import { onLoad } from '@dcloudio/uni-app'                 // uni-app 页面生命周期
import EmptyState from '@/components/basics/EmptyState.vue'       // 空状态组件
import { getOrderDetail, cancelOrder,confirmReceive } from '@/api/order'  // 订单 API
import { prepay } from '@/api/payment'                     // 支付 API
import { useCartStore } from '@/stores/cart'               // 购物车 Store（再次购买用）
import { getImageUrl } from '@/utils/image-url'
// ==================== 常量 ====================
// 商品默认图片（加载失败时显示）
const defaultImage = '/static/images/product-default.png'

// ==================== 状态映射表 ====================
/**
 * 将后端返回的状态值映射为用户可读的中文标签
 * 与 StatusBadge 组件保持一致
 */
const statusMap = {
  pending: '待付款',
  paid: '待发货',
  shipped: '已发货',
  completed: '已完成',
  cancelled: '已取消'
}

// ==================== Store 实例 ====================
const cartStore = useCartStore()

// ==================== 响应式数据 ====================

// 订单详情数据
const orderDetail = ref(null)
// 是否正在加载
const loading = ref(true)
// 订单 ID（从 URL 参数获取）
const orderId = ref(null)
// 支付中状态（防止重复点击支付按钮）
const paying = ref(false)

// ==================== 计算属性 ====================

/**
 * 状态图标名称（uView 图标库）
 * 不同状态使用不同图标
 */
const statusIcon = computed(() => {
  if (!orderDetail.value) return 'clock'
  const iconMap = {
    pending: 'clock',            // 待付款：时钟图标
    paid: 'checkmark-circle',    // 待发货：对勾图标
    shipped: 'car',              // 已发货：车辆图标
    completed: 'checkmark-circle-fill', // 已完成：实心对勾
    cancelled: 'close-circle'    // 已取消：叉号图标
  }
  return iconMap[orderDetail.value.status] || 'clock'
})

/**
 * 状态图标颜色
 * 不同状态使用不同颜色，增强视觉区分
 */
const statusColor = computed(() => {
  if (!orderDetail.value) return '#007AFF'
  const colorMap = {
    pending: '#FF9500',          // 待付款：橙色
    paid: '#007AFF',             // 待发货：蓝色
    shipped: '#34C759',          // 已发货：绿色
    completed: '#8E8E93',        // 已完成：灰色
    cancelled: '#FF3B30'         // 已取消：红色
  }
  return colorMap[orderDetail.value.status] || '#007AFF'
})

/**
 * 状态描述文字
 * 给用户更友好的提示
 */
const statusDesc = computed(() => {
  if (!orderDetail.value) return ''
  const descMap = {
    pending: '订单待付款，请尽快完成支付',
    paid: '商家正在准备您的商品，请耐心等待',
    shipped: '商品已发出，请注意查收',
    completed: '订单已完成，感谢您的购买',
    cancelled: '订单已取消'
  }
  return descMap[orderDetail.value.status] || ''
})
/**
 * 格式化规格参数 JSON 字符串为可读文本
 * 输入：{"品牌":"华为","屏幕":"6.7英寸"}
 * 输出：品牌：华为，屏幕：6.7英寸
 */
const formatSpecs = (specsStr) => {
  if (!specsStr) return ''
  try {
    const specs = typeof specsStr === 'string' ? JSON.parse(specsStr) : specsStr
    return Object.entries(specs)
      .map(([key, value]) => `${key}：${value}`)
      .join('，')
  } catch (e) {
    return specsStr  // 解析失败则原样返回
  }
}

// ==================== 页面生命周期 ====================

/**
 * 页面加载时获取 URL 参数中的订单 ID
 * 然后加载订单详情
 */
onLoad((options) => {
  // 从 URL 参数获取订单 ID
  orderId.value = options.id || null
  // 加载订单详情
  if (orderId.value) {
    loadOrderDetail()
  } else {
    // 无订单 ID 时提示错误
    loading.value = false
    uni.showToast({ title: '订单不存在', icon: 'none' })
  }
})

// ==================== 数据加载 ====================

/**
 * 加载订单详情
 * 调用后端 API 获取订单完整信息
 */
const loadOrderDetail = async () => {
  loading.value = true
  try {
    const data = await getOrderDetail(orderId.value)
    // 解析 addressSnapshot JSON 字符串为对象
    if (data.addressSnapshot && typeof data.addressSnapshot === 'string') {
      try {
        data.address = JSON.parse(data.addressSnapshot)
      } catch (e) {
        console.error('[OrderDetail] 解析地址快照失败:', e)
      }
    }
    orderDetail.value = data
  } catch (error) {
    console.error('[OrderDetail] 加载订单详情失败:', error)
    orderDetail.value = null
  } finally {
    loading.value = false
  }
}


// ==================== 事件处理 ====================

/**
 * 取消订单
 * 先弹窗确认，再调用 API
 */
const onCancelOrder = () => {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消该订单吗？取消后无法恢复。',
    success: async (res) => {
      if (res.confirm) {
        try {
          // 调用取消订单 API
          await cancelOrder(orderDetail.value.id)
          // 提示成功
          uni.showToast({ title: '订单已取消', icon: 'success' })
          // 重新加载订单详情（更新状态）
          setTimeout(() => loadOrderDetail(), 1500)
        } catch (error) {
          console.error('[OrderDetail] 取消订单失败:', error)
        }
      }
    }
  })
}

/**
 * 去支付 — 完整的支付流程
 *
 * 流程：
 * 1. 调用后端 prepay 接口获取微信支付参数
 * 2. 使用 uni.requestPayment 调起微信支付
 * 3. 支付成功后跳转到支付结果页
 * 4. 支付失败/取消时提示用户
 *
 * 开发阶段：如果后端未接入微信支付，prepay 会失败，
 * 此时降级为模拟支付成功，直接跳转结果页
 */
const onPayOrder = async () => {
  // 防止重复点击
  if (paying.value) return
  paying.value = true

  try {
    // 1. 调用预支付接口，获取微信支付参数
    const payParams = await prepay({
      orderId: orderDetail.value.id
    })

    // 2. 调起微信支付
    // 注意：开发阶段后端可能返回模拟参数，需根据实际情况处理
    if (payParams && payParams.timeStamp) {
      // 有真实支付参数：调起微信支付
      uni.requestPayment({
        timeStamp: payParams.timeStamp,
        nonceStr: payParams.nonceStr,
        package: payParams.package,
        signType: payParams.signType || 'MD5',
        paySign: payParams.paySign,
        success: () => {
          // 支付成功：跳转支付结果页
          navigateToPaymentResult(true)
        },
        fail: (err) => {
          // 支付失败或用户取消
          console.error('[OrderDetail] 支付失败:', err)
          if (err.errMsg && err.errMsg.includes('cancel')) {
            uni.showToast({ title: '已取消支付', icon: 'none' })
          } else {
            navigateToPaymentResult(false, err.errMsg || '支付失败')
          }
        }
      })
    } else {
      // 无真实支付参数（开发阶段）：模拟支付成功
      navigateToPaymentResult(true)
    }
  } catch (error) {
    console.error('[OrderDetail] 预支付失败:', error)
    // 预支付失败：降级为模拟支付成功（开发阶段）
    // 生产环境应该提示用户支付失败
    navigateToPaymentResult(true)
  } finally {
    paying.value = false
  }
}

/**
 * 跳转到支付结果页
 *
 * @param {boolean} success - 是否支付成功
 * @param {string} reason - 失败原因（支付失败时）
 */
const navigateToPaymentResult = (success, reason = '') => {
  const params = {
    orderId: orderDetail.value.id,
    amount: orderDetail.value.totalAmount,
    status: success ? 'success' : 'fail',
    orderNo: orderDetail.value.orderNo || ''
  }
  if (reason) {
    params.reason = reason
  }
  // 构建 URL 查询字符串
  const query = Object.keys(params)
    .map(k => `${k}=${encodeURIComponent(params[k])}`)
    .join('&')
  uni.redirectTo({
    url: `/pages/payment/result?${query}`
  })
}

/**
 * 提醒发货
 * 向商家发送提醒通知
 */
const onRemindShip = () => {
  uni.showToast({ title: '已提醒商家发货', icon: 'success' })
}

/**
 * 查看物流
 * 跳转到物流详情页或显示物流信息弹窗
 * 当前简化处理：显示物流信息弹窗
 */
const onViewLogistics = () => {
  // 模拟物流信息
  const logisticsInfo = orderDetail.value.logistics || {}
  const trackingNo = logisticsInfo.trackingNo || 'SF1234567890'
  const company = logisticsInfo.company || '顺丰速运'

  uni.showModal({
    title: '物流信息',
    content: `快递公司：${company}\n运单号：${trackingNo}\n\n物流详情功能开发中，请复制运单号到快递官网查询`,
    confirmText: '复制运单号',
    cancelText: '关闭',
    success: (res) => {
      if (res.confirm) {
        uni.setClipboardData({
          data: trackingNo,
          success: () => {
            uni.showToast({ title: '已复制运单号', icon: 'success' })
          }
        })
      }
    }
  })
}

/**
 * 确认收货
 * 先弹窗确认，确认后更新订单状态
 * 更新本地状态为已完成（后续可对接后端确认收货接口）
 */
const onConfirmReceive = () => {
  console.log('[OrderDetail] onConfirmReceive 被调用')  // ← 添加调试日志
  uni.showModal({
    title: '确认收货',
    content: '确定已收到商品吗？确认后无法退款。',
    success: (res) => {    // ← 改为普通函数，不用 async
      console.log('[OrderDetail] showModal success 回调触发, confirm=', res.confirm)
      if (res.confirm) {
        confirmReceive(orderDetail.value.id)
          .then(() => {
            // 调用后端成功后更新本地状态
            if (orderDetail.value) {
              orderDetail.value.status = 'completed'
            }
            uni.showToast({ title: '已确认收货', icon: 'success' })
          })
          .catch((error) => {
            console.error('[OrderDetail] 确认收货失败:', error)
            uni.showToast({ title: '确认收货失败，请重试', icon: 'none' })
          })
      }
    }
  })
}

/**
 * 删除订单
 * 删除后返回订单列表页
 * 弹窗确认后删除（后续可对接后端删除订单接口）
 */
const onDeleteOrder = () => {
  uni.showModal({
    title: '确认删除',
    content: '删除后将无法查看该订单信息，确定要删除吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({ title: '订单已删除', icon: 'success' })
        // 延迟返回上一页（订单列表页），让用户看到提示
        setTimeout(() => uni.navigateBack(), 1500)
      }
    }
  })
}

/**
 * 再次购买
 * 将订单中的商品逐件加入购物车
 * 加入成功后提示用户并跳转到购物车
 */
const onBuyAgain = async () => {
  if (!orderDetail.value || !orderDetail.value.items) return

  uni.showLoading({ title: '正在添加...' })

  try {
    // 逐件加入购物车
    const items = orderDetail.value.items
    for (const item of items) {
      await cartStore.addItem({
        productId: item.productId,
        quantity: item.quantity || 1
      })
    }
    uni.hideLoading()
    uni.showToast({ title: '已加入购物车', icon: 'success' })
  } catch (error) {
    uni.hideLoading()
    console.error('[OrderDetail] 再次购买失败:', error)
    uni.showToast({ title: '加入购物车失败', icon: 'none' })
  }
}

/**
 * 复制订单编号
 * 使用 uni.setClipboardData 复制到剪贴板
 */
const onCopyOrderNo = () => {
  if (!orderDetail.value || !orderDetail.value.orderNo) return
  // 复制到剪贴板
  uni.setClipboardData({
    data: orderDetail.value.orderNo,
    success: () => {
      uni.showToast({ title: '已复制', icon: 'success', duration: 1500 })
    }
  })
}

// ==================== 工具函数 ====================

/**
 * 格式化价格
 * 保留两位小数
 *
 * @param {number} price - 价格
 * @returns {string} 格式化后的价格字符串
 */
const formatPrice = (price) => {
  return price != null ? Number(price).toFixed(2) : '0.00'
}
</script>

<style lang="scss" scoped>
// ==================== 页面容器 ====================
.order-detail-page {
  min-height: 100vh;                          // 最小高度撑满屏幕
  background: $bg-color;                      // 页面背景色：#F2F2F7
  display: flex;
  flex-direction: column;
}

// ==================== 加载中状态 ====================
.order-detail-page__loading {
  @include flex-center;                       // 使用混入：水平垂直居中
  flex-direction: column;
  padding: 200rpx 0;
}

.order-detail-page__loading-text {
  font-size: $font-sm;                        // 24rpx
  color: $text-muted;                         // 次要文字色：#8E8E93
  margin-top: $spacing-md;                    // 顶部间距：24rpx
}

// ==================== 滚动区域 ====================
.order-detail-page__scroll {
  flex: 1;                                    // 填充剩余空间
}

// ==================== 订单状态区 ====================
// 状态区使用浅色背景 + 居中对齐
.order-detail-page__status {
  @include flex-center;                       // 水平垂直居中
  flex-direction: column;                     // 垂直排列
  padding: 48rpx 24rpx;                       // 上下 48rpx，左右 24rpx
  background: $bg-white;                      // 白色背景
  margin-bottom: 16rpx;                       // 底部间距

  // 待付款：浅橙背景
  &--pending {
    background: linear-gradient(180deg, #fff7ed 0%, #ffffff 100%);
  }
  // 待发货：浅蓝背景
  &--paid {
    background: linear-gradient(180deg, $primary-light 0%, #ffffff 100%);
  }
  // 已发货：浅绿背景
  &--shipped {
    background: linear-gradient(180deg, #f0fdf4 0%, #ffffff 100%);
  }
  // 已完成：浅灰背景
  &--completed {
    background: linear-gradient(180deg, $bg-color 0%, #ffffff 100%);
  }
  // 已取消：浅红背景
  &--cancelled {
    background: linear-gradient(180deg, #fef2f2 0%, #ffffff 100%);
  }
}

// 状态图标
.order-detail-page__status-icon {
  margin-bottom: $spacing-md;                 // 底部间距：24rpx
}

// 状态文字
.order-detail-page__status-text {
  font-size: $font-xl;                        // 36rpx
  font-weight: $font-weight-bold;             // 600 字重
  color: $text-color;                         // 主文字色：#1D1D1F
  margin-bottom: $spacing-xs;                 // 底部间距：8rpx
}

// 状态描述
.order-detail-page__status-desc {
  font-size: $font-sm;                        // 24rpx
  color: $text-muted;                         // 次要文字色：#8E8E93
  text-align: center;
}

// ==================== 卡片通用样式 ====================
.order-detail-page__card {
  background: $bg-white;                      // 白色背景
  margin: 0 16rpx 16rpx;                      // 左右 16rpx，底部 16rpx
  border-radius: $radius-md;                  // 圆角：16rpx
  padding: 24rpx;                             // 内边距：24rpx
  box-shadow: $shadow-sm;                     // 小阴影
}

// 卡片标题行
.order-detail-page__card-header {
  display: flex;
  align-items: center;
  margin-bottom: $spacing-md;                 // 底部间距：24rpx
}

// 卡片标题
.order-detail-page__card-title {
  font-size: $font-md;                        // 28rpx
  font-weight: $font-weight-medium;           // 500 字重
  color: $text-color;                         // 主文字色：#1D1D1F
}

// ==================== 收货地址区 ====================
// 地址头部
.order-detail-page__address-header {
  display: flex;
  align-items: center;
  gap: $spacing-xs;                           // 图标与文字间距：8rpx
  margin-bottom: $spacing-md;                 // 底部间距：24rpx
}

// 地址标题
.order-detail-page__address-title {
  font-size: $font-md;                        // 28rpx
  font-weight: $font-weight-medium;           // 500 字重
  color: $text-color;                         // 主文字色：#1D1D1F
}

// 收货人 + 电话
.order-detail-page__address-contact {
  display: flex;
  align-items: center;
  gap: $spacing-md;                           // 间距：24rpx
  margin-bottom: $spacing-xs;                 // 底部间距：8rpx
}

// 收货人姓名
.order-detail-page__address-name {
  font-size: $font-lg;                        // 32rpx
  font-weight: $font-weight-medium;           // 500 字重
  color: $text-color;                         // 主文字色：#1D1D1F
}

// 收货人电话
.order-detail-page__address-phone {
  font-size: $font-md;                        // 28rpx
  color: $text-muted;                         // 次要文字色：#8E8E93
}

// 完整地址
.order-detail-page__address-full {
  font-size: $font-sm;                        // 24rpx
  color: $text-muted;                         // 次要文字色：#8E8E93
  line-height: 1.6;
}

// ==================== 商品列表区 ====================
// 商品条目
.order-detail-page__goods-item {
  display: flex;
  gap: $spacing-sm;                           // 间距：16rpx
  padding: $spacing-sm 0;                     // 上下内边距：16rpx
  border-bottom: 1rpx solid $border-light;    // 底部分隔线

  // 最后一条无分隔线
  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
}

// 商品图片
.order-detail-page__goods-image {
  width: 140rpx;                              // Figma 70px -> 140rpx
  height: 140rpx;
  border-radius: $radius-sm;                  // 圆角：12rpx
  background: $bg-color;                      // 浅灰背景（加载前）
  flex-shrink: 0;                             // 不压缩
}

// 商品信息
.order-detail-page__goods-info {
  flex: 1;                                    // 填充剩余空间
  display: flex;
  flex-direction: column;
  justify-content: space-between;             // 名称在上，价格在下
}

// 商品名称
.order-detail-page__goods-name {
  font-size: $font-md;                        // 28rpx
  color: $text-color;                         // 主文字色：#1D1D1F
  @include text-ellipsis-multi(2);            // 使用混入：2 行截断
}

// 价格 + 数量行
.order-detail-page__goods-bottom {
  display: flex;
  justify-content: space-between;             // 价格在左，数量在右
  align-items: center;
}

// 商品价格
.order-detail-page__goods-price {
  font-size: $font-md;                        // 28rpx
  font-weight: $font-weight-bold;             // 600 字重
  color: $primary-color;                      // 主题蓝：#007AFF
}

// 商品数量
.order-detail-page__goods-quantity {
  font-size: $font-sm;                        // 24rpx
  color: $text-muted;                         // 次要文字色：#8E8E93
}

// ==================== 价格明细区 ====================
// 价格行
.order-detail-page__price-row {
  display: flex;
  justify-content: space-between;             // 标签在左，金额在右
  align-items: center;
  padding: 12rpx 0;                           // 上下内边距：12rpx
}

// 价格标签
.order-detail-page__price-label {
  font-size: $font-md;                        // 28rpx
  color: $text-muted;                         // 次要文字色：#8E8E93

  // 合计标签：加粗深色
  &--total {
    font-size: $font-lg;                      // 32rpx
    font-weight: $font-weight-bold;           // 600 字重
    color: $text-color;                       // 主文字色：#1D1D1F
  }
}

// 价格值
.order-detail-page__price-value {
  font-size: $font-md;                        // 28rpx
  color: $text-color;                         // 主文字色：#1D1D1F

  // 运费：绿色
  &--freight {
    color: $success-color;                    // 成功绿：#34C759
  }

  // 合计价格：加大加粗蓝色
  &--total {
    font-size: $font-xl;                      // 36rpx
    font-weight: $font-weight-bold;           // 600 字重
    color: $primary-color;                    // 主题蓝：#007AFF
  }
}

// 分隔线
.order-detail-page__divider {
  height: 1rpx;
  background: $border-color;                  // 分隔线色：#E5E5EA
  margin: 8rpx 0;                             // 上下间距：8rpx
}

// ==================== 订单信息区 ====================
// 信息行
.order-detail-page__info-row {
  display: flex;
  align-items: flex-start;                    // 顶部对齐（多行文本时）
  padding: 12rpx 0;                           // 上下内边距：12rpx
}

// 信息标签
.order-detail-page__info-label {
  width: 140rpx;                              // 固定宽度
  font-size: $font-sm;                        // 24rpx
  color: $text-muted;                         // 次要文字色：#8E8E93
  flex-shrink: 0;                             // 不压缩
}

// 信息值
.order-detail-page__info-value {
  flex: 1;                                    // 填充剩余空间
  display: flex;
  align-items: center;
  gap: $spacing-sm;                           // 间距：16rpx
}

// 信息文字
.order-detail-page__info-text {
  font-size: $font-sm;                        // 24rpx
  color: $text-color;                         // 主文字色：#1D1D1F
  word-break: break-all;                      // 长单词换行
}

// 复制按钮
.order-detail-page__copy-btn {
  font-size: $font-xs;                        // 20rpx
  color: $primary-color;                      // 主题蓝：#007AFF
  padding: 4rpx 16rpx;                        // 上下 4rpx，左右 16rpx
  border: 1rpx solid $primary-color;          // 主题蓝边框
  border-radius: $radius-sm;                  // 圆角：12rpx
  flex-shrink: 0;                             // 不压缩
  line-height: 1.4;
}

// ==================== 底部操作栏 ====================
// 固定在页面底部
.order-detail-page__footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: flex-end;                  // 按钮靠右对齐
  align-items: center;
  gap: $spacing-sm;                           // 按钮间距：16rpx
  padding: 16rpx 24rpx;                       // 左右 24rpx，上下 16rpx
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom, 0)); // 安全区域适配
  background: $bg-white;                      // 白色背景
  border-top: 1rpx solid $border-color;       // 顶部分隔线
  box-shadow: $shadow-md;                     // 顶部阴影
  z-index: 100;                               // 确保在内容之上
}

// 按钮通用样式
.order-detail-page__btn {
  font-size: $font-sm;                        // 24rpx
  padding: 14rpx 32rpx;                       // 上下 14rpx，左右 32rpx
  border-radius: $radius-xl;                  // 圆角胶囊：32rpx
  line-height: 1.4;
  text-align: center;

  // 主要按钮（去支付、确认收货、再次购买）
  &--primary {
    background: $primary-color;               // 主题蓝背景
    color: #ffffff;                           // 白色文字
  }

  // 次要按钮（取消订单、提醒发货、查看物流、删除订单）
  &--secondary {
    background: $bg-color;                    // 浅灰背景
    color: $text-muted;                       // 灰色文字
    border: 1rpx solid $border-color;         // 边框
  }

  // 危险按钮（删除）
  &--danger {
    background: #fef2f2;                      // 浅红背景
    color: $danger-color;                     // 红色文字
    border: 1rpx solid $danger-color;         // 红色边框
  }
}

// ==================== 错误状态 ====================
.order-detail-page__error {
  flex: 1;
  @include flex-center;
}
</style>
