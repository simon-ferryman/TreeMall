<!--
  pages/payment/result.vue -- 支付结果页

  【设计思想】
  支付完成后展示支付结果，包含成功和失败两种状态：
  1. 成功：绿色对勾 + 支付金额 + 成功提示 + 查看订单/返回首页按钮
  2. 失败：红色叉号 + 支付金额 + 失败提示 + 重新支付/返回首页按钮

  【Figma 设计令牌】
  主色 #007AFF | 文字 #1D1D1F | 次要文字 #8E8E93 | 背景 #F2F2F7 | 白色 #FFFFFF
  分隔线 #E5E5EA | 浅蓝 #E5F1FF | 设计宽度 375px | rpx=px*2

  【URL 参数】
  - orderId: 订单 ID
  - amount: 支付金额
  - status: 支付结果（'success' | 'fail'）

  【使用场景】
  - 订单详情页点击"去支付"后跳转
  - 微信支付完成后回调
-->
<template>
  <view class="payment-result-page">
    <!-- ==================== 结果图标区 ==================== -->
    <!--
      成功：绿色圆形背景 + 白色对勾
      失败：红色圆形背景 + 白色叉号
    -->
    <view class="payment-result-page__icon-area">
      <!-- 成功图标 -->
      <view
        v-if="isSuccess"
        class="payment-result-page__icon payment-result-page__icon--success"
      >
        <u-icon name="checkmark" :size="64" color="#ffffff" />
      </view>
      <!-- 失败图标 -->
      <view
        v-else
        class="payment-result-page__icon payment-result-page__icon--fail"
      >
        <u-icon name="close" :size="64" color="#ffffff" />
      </view>
    </view>

    <!-- ==================== 结果文字区 ==================== -->
    <!-- 主标题文字 -->
    <text class="payment-result-page__title">
      {{ isSuccess ? '支付成功' : '支付失败' }}
    </text>

    <!-- ==================== 支付金额区 ==================== -->
    <!-- 成功时显示支付金额 -->
    <view class="payment-result-page__amount" v-if="isSuccess">
      <text class="payment-result-page__amount-symbol">¥</text>
      <text class="payment-result-page__amount-value">{{ formatAmount(amount) }}</text>
    </view>

    <!-- 失败时显示失败原因 -->
    <text class="payment-result-page__desc" v-else>
      {{ failReason || '支付未完成，请重新尝试' }}
    </text>

    <!-- ==================== 成功提示信息 ==================== -->
    <!-- 成功时显示订单号和支付时间 -->
    <view class="payment-result-page__info" v-if="isSuccess">
      <view class="payment-result-page__info-row">
        <text class="payment-result-page__info-label">订单编号</text>
        <text class="payment-result-page__info-value">{{ orderNo || '--' }}</text>
      </view>
      <view class="payment-result-page__info-row">
        <text class="payment-result-page__info-label">支付时间</text>
        <text class="payment-result-page__info-value">{{ payTime || '--' }}</text>
      </view>
    </view>

    <!-- 失败时显示的提示信息 -->
    <view class="payment-result-page__tips" v-else>
      <text class="payment-result-page__tips-text">可能的原因：</text>
      <text class="payment-result-page__tips-text">1. 账户余额不足</text>
      <text class="payment-result-page__tips-text">2. 支付密码错误</text>
      <text class="payment-result-page__tips-text">3. 网络连接异常</text>
    </view>

    <!-- ==================== 底部按钮区 ==================== -->
    <view class="payment-result-page__actions">
      <!-- 成功：查看订单 -->
      <view
        v-if="isSuccess"
        class="payment-result-page__btn payment-result-page__btn--primary"
        @tap="onViewOrder"
      >
        查看订单
      </view>

      <!-- 失败：重新支付 -->
      <view
        v-else
        class="payment-result-page__btn payment-result-page__btn--primary"
        @tap="onRetryPay"
      >
        重新支付
      </view>

      <!-- 返回首页（通用按钮） -->
      <view
        class="payment-result-page__btn payment-result-page__btn--secondary"
        @tap="onGoHome"
      >
        返回首页
      </view>
    </view>
  </view>
</template>

<script setup>
// ==================== 导入依赖 ====================
import { ref, computed } from 'vue'               // Vue 3 响应式 API
import { onLoad } from '@dcloudio/uni-app'        // uni-app 页面生命周期

// ==================== 响应式数据 ====================

// 订单 ID（从 URL 参数获取）
const orderId = ref(null)
// 支付金额（从 URL 参数获取）
const amount = ref(0)
// 支付结果状态（'success' | 'fail'）
const resultStatus = ref('success')
// 订单编号
const orderNo = ref('')
// 支付时间
const payTime = ref('')
// 失败原因（支付失败时使用）
const failReason = ref('')

// ==================== 计算属性 ====================

/**
 * 是否为支付成功
 * 根据 URL 参数中的 status 判断
 */
const isSuccess = computed(() => {
  return resultStatus.value === 'success'
})

// ==================== 页面生命周期 ====================

/**
 * 页面加载时获取 URL 参数
 * 解析支付结果并初始化页面数据
 *
 * @param {Object} options - URL 参数对象
 * @param {string} options.orderId - 订单 ID
 * @param {number} options.amount - 支付金额
 * @param {string} options.status - 支付结果（'success' | 'fail'）
 */
onLoad((options) => {
  // 解析订单 ID
  orderId.value = options.orderId || null
  // 解析支付金额
  amount.value = options.amount ? Number(options.amount) : 0
  // 解析支付结果
  resultStatus.value = options.status === 'fail' ? 'fail' : 'success'

  // 支付成功后生成订单编号和支付时间
  if (isSuccess.value) {
    // 生成模拟订单编号（实际项目中从后端获取）
    orderNo.value = options.orderNo || generateOrderNo()
    // 设置当前时间为支付时间
    payTime.value = formatDateTime(new Date())
  } else {
    // 失败原因（可从 URL 参数获取，或使用默认值）
    failReason.value = options.reason || ''
  }
})

// ==================== 事件处理 ====================

/**
 * 查看订单：跳转到订单详情页
 */
const onViewOrder = () => {
  if (!orderId.value) {
    // 无订单 ID 时跳转到订单列表
    uni.redirectTo({ url: '/pages/order/list' })
    return
  }
  // 跳转到订单详情页
  uni.redirectTo({
    url: `/pages/order/detail?id=${orderId.value}`
  })
}

/**
 * 重新支付：返回上一页重新发起支付
 */
const onRetryPay = () => {
  // 返回上一页（订单详情页），用户可重新点击支付
  uni.navigateBack()
}

/**
 * 返回首页：跳转到 TabBar 首页
 */
const onGoHome = () => {
  // 使用 switchTab 跳转到首页 TabBar 页面
  uni.switchTab({
    url: '/pages/index/index'
  })
}

// ==================== 工具函数 ====================

/**
 * 生成模拟订单编号
 * 格式：TM + 年月日 + 4位随机数
 *
 * @returns {string} 订单编号
 */
const generateOrderNo = () => {
  const now = new Date()
  // 年月日：20260720
  const dateStr = [
    now.getFullYear(),
    String(now.getMonth() + 1).padStart(2, '0'),
    String(now.getDate()).padStart(2, '0')
  ].join('')
  // 4 位随机数
  const random = Math.floor(Math.random() * 10000).toString().padStart(4, '0')
  // 拼接：TM + 日期 + 随机数
  return `TM${dateStr}${random}`
}

/**
 * 格式化日期时间
 *
 * @param {Date} date - 日期对象
 * @returns {string} 格式化后的日期时间字符串：YYYY-MM-DD HH:mm:ss
 */
const formatDateTime = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 格式化金额
 * 保留两位小数
 *
 * @param {number} val - 金额
 * @returns {string} 格式化后的金额字符串
 */
const formatAmount = (val) => {
  return Number(val).toFixed(2)
}
</script>

<style lang="scss" scoped>
// ==================== 页面容器 ====================
.payment-result-page {
  min-height: 100vh;                          // 最小高度撑满屏幕
  background: $bg-white;                      // 白色背景
  @include flex-center;                       // 使用混入：水平垂直居中
  flex-direction: column;                     // 垂直排列
  padding: 0 48rpx;                           // 左右内边距：48rpx
}

// ==================== 结果图标区 ====================
.payment-result-page__icon-area {
  margin-bottom: 48rpx;                       // 底部间距：48rpx（Figma 24px * 2）
}

// 结果图标通用样式
.payment-result-page__icon {
  width: 160rpx;                              // Figma 80px -> 160rpx
  height: 160rpx;
  border-radius: 50%;                         // 圆形
  @include flex-center;                       // 使用混入：水平垂直居中

  // 成功图标：绿色背景
  &--success {
    background: $success-color;               // 成功绿：#34C759
  }

  // 失败图标：红色背景
  &--fail {
    background: $danger-color;                // 危险红：#FF3B30
  }
}

// ==================== 结果文字区 ====================
// 主标题文字
.payment-result-page__title {
  font-size: $font-xxl;                       // 40rpx（Figma 20px * 2）
  font-weight: $font-weight-bold;             // 600 字重
  color: $text-color;                         // 主文字色：#1D1D1F
  margin-bottom: $spacing-md;                 // 底部间距：24rpx
}

// ==================== 支付金额区 ====================
// 金额容器
.payment-result-page__amount {
  display: flex;
  align-items: baseline;                      // 基线对齐
  margin-bottom: $spacing-xl;                 // 底部间距：48rpx
}

// 金额符号（¥）
.payment-result-page__amount-symbol {
  font-size: $font-xl;                        // 36rpx
  font-weight: $font-weight-bold;             // 600 字重
  color: $primary-color;                      // 主题蓝：#007AFF
  margin-right: $spacing-xs;                  // 与数字的间距：8rpx
}

// 金额数字
.payment-result-page__amount-value {
  font-size: $font-title;                     // 56rpx（Figma 28px * 2）
  font-weight: $font-weight-heavy;            // 700 字重
  color: $primary-color;                      // 主题蓝：#007AFF
}

// 失败描述文字
.payment-result-page__desc {
  font-size: $font-md;                        // 28rpx
  color: $text-muted;                         // 次要文字色：#8E8E93
  text-align: center;
  margin-bottom: $spacing-xl;                 // 底部间距：48rpx
  line-height: 1.6;
}

// ==================== 成功提示信息 ====================
// 信息容器
.payment-result-page__info {
  width: 100%;                                // 撑满宽度
  background: $bg-color;                      // 浅灰背景：#F2F2F7
  border-radius: $radius-md;                  // 圆角：16rpx
  padding: 24rpx;                             // 内边距：24rpx
  margin-bottom: 80rpx;                       // 底部间距：80rpx
}

// 信息行
.payment-result-page__info-row {
  display: flex;
  justify-content: space-between;             // 标签在左，值在右
  align-items: center;
  padding: 10rpx 0;                           // 上下内边距：10rpx
}

// 信息标签
.payment-result-page__info-label {
  font-size: $font-sm;                        // 24rpx
  color: $text-muted;                         // 次要文字色：#8E8E93
}

// 信息值
.payment-result-page__info-value {
  font-size: $font-sm;                        // 24rpx
  color: $text-color;                         // 主文字色：#1D1D1F
}

// ==================== 失败提示信息 ====================
.payment-result-page__tips {
  width: 100%;
  background: $bg-color;                      // 浅灰背景：#F2F2F7
  border-radius: $radius-md;                  // 圆角：16rpx
  padding: 24rpx;                             // 内边距：24rpx
  margin-bottom: 80rpx;                       // 底部间距：80rpx
}

// 提示文字
.payment-result-page__tips-text {
  font-size: $font-sm;                        // 24rpx
  color: $text-muted;                         // 次要文字色：#8E8E93
  line-height: 2;                             // 行高：2 倍（宽松排版）
  display: block;                             // 块级元素（每行独立）
}

// ==================== 底部按钮区 ====================
.payment-result-page__actions {
  width: 100%;                                // 撑满宽度
  display: flex;
  flex-direction: column;                     // 垂直排列
  gap: $spacing-md;                           // 按钮间距：24rpx
}

// 按钮通用样式
.payment-result-page__btn {
  width: 100%;                                // 撑满宽度
  height: 88rpx;                              // 按钮高度：88rpx（Figma 44px * 2）
  @include flex-center;                       // 使用混入：水平垂直居中
  font-size: $font-lg;                        // 32rpx
  font-weight: $font-weight-medium;           // 500 字重
  border-radius: $radius-xl;                  // 圆角胶囊：32rpx
  line-height: 1.4;

  // 主要按钮（查看订单、重新支付）
  &--primary {
    background: $primary-color;               // 主题蓝背景：#007AFF
    color: #ffffff;                           // 白色文字
  }

  // 次要按钮（返回首页）
  &--secondary {
    background: $bg-color;                    // 浅灰背景：#F2F2F7
    color: $text-color;                       // 主文字色：#1D1D1F
    border: 1rpx solid $border-color;         // 边框：#E5E5EA
  }
}
</style>