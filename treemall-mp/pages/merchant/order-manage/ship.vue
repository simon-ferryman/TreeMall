<!--
  pages/merchant/order-manage/ship.vue — 商户发货页

  【设计思想】
  商户点击"确认发货"后进入此页面，页面展示订单关键摘要信息，
  提供物流公司和物流单号两个输入框，商户填写后提交完成发货。
-->
<template>
  <view class="ship-page">
    <view v-if="loading" class="ship-page__loading">
      <text class="ship-page__loading-text">加载中...</text>
    </view>

    <template v-else-if="order">
      <scroll-view class="ship-page__scroll" scroll-y>
        <!-- 订单摘要区 -->
        <view class="ship-page__card">
          <view class="ship-page__card-header">
            <text class="ship-page__card-title">订单信息</text>
          </view>

          <!-- 订单编号 -->
          <view class="ship-page__info-row">
            <text class="ship-page__info-label">订单编号</text>
            <text class="ship-page__info-value">{{ order.orderNo || '--' }}</text>
          </view>

          <!-- 商品列表 -->
          <view class="ship-page__goods-list">
            <view v-for="(item, index) in order.items" :key="index" class="ship-page__goods-item">
              <image
                class="ship-page__goods-image"
                :src="getImageUrl(item.productImage) || defaultImage"
                mode="aspectFill"
              />
              <view class="ship-page__goods-info">
                <text class="ship-page__goods-name">{{ item.productName }}</text>
                <text class="ship-page__goods-meta">¥{{ formatPrice(item.price) }} x{{ item.quantity }}</text>
              </view>
            </view>
          </view>

          <!-- 收货地址 -->
          <view class="ship-page__info-row" v-if="address">
            <text class="ship-page__info-label">收货地址</text>
            <text class="ship-page__info-value ship-page__info-value--address">
              {{ address.receiverName }} {{ address.receiverPhone }}
              {{ address.province }}{{ address.city }}{{ address.district }} {{ address.detailAddress }}
            </text>
          </view>

          <!-- 实付金额 -->
          <view class="ship-page__info-row">
            <text class="ship-page__info-label">实付金额</text>
            <text class="ship-page__info-value ship-page__info-value--price">¥{{ formatPrice(order.totalAmount) }}</text>
          </view>
        </view>

        <!-- 物流信息表单区 -->
        <view class="ship-page__card">
          <view class="ship-page__card-header">
            <text class="ship-page__card-title">物流信息</text>
          </view>

          <view class="ship-page__form-item">
            <text class="ship-page__form-label">物流公司</text>
            <input
              class="ship-page__form-input"
              v-model="companyName"
              placeholder="请输入物流公司名称，如：顺丰速运"
              placeholder-style="color: #C7C7CC; font-size: 28rpx;"
            />
          </view>

          <view class="ship-page__form-item">
            <text class="ship-page__form-label">物流单号</text>
            <input
              class="ship-page__form-input"
              v-model="trackingNo"
              placeholder="请输入物流单号，如：SF1234567890"
              placeholder-style="color: #C7C7CC; font-size: 28rpx;"
            />
          </view>
        </view>

        <view style="height: 140rpx;" />
      </scroll-view>

      <!-- 底部提交按钮 -->
      <view class="ship-page__footer">
        <view
          class="ship-page__submit-btn"
          :class="{ 'ship-page__submit-btn--loading': submitting }"
          @tap="onSubmit"
        >
          {{ submitting ? '提交中...' : '确认发货' }}
        </view>
      </view>
    </template>

    <view v-else class="ship-page__error">
      <text>订单信息加载失败</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getMerchantOrderDetail, deliverOrder } from '@/api/merchant'
import { getImageUrl } from '@/utils/image-url'

const defaultImage = '/static/images/product-default.png'

const order = ref(null)
const loading = ref(true)
const submitting = ref(false)
const address = ref(null)

// 表单字段
const companyName = ref('')
const trackingNo = ref('')

onLoad((options) => {
  const orderId = options.id
  if (orderId) {
    loadOrderSummary(orderId)
  } else {
    loading.value = false
    uni.showToast({ title: '订单不存在', icon: 'none' })
  }
})

const loadOrderSummary = async (orderId) => {
  loading.value = true
  try {
    const data = await getMerchantOrderDetail(orderId)
    order.value = data
    // 解析地址快照
    if (data.addressSnapshot && typeof data.addressSnapshot === 'string') {
      try {
        address.value = JSON.parse(data.addressSnapshot)
      } catch (e) {
        console.error('[Ship] 解析地址快照失败:', e)
      }
    }
  } catch (error) {
    console.error('[Ship] 加载订单失败:', error)
    order.value = null
  } finally {
    loading.value = false
  }
}

const onSubmit = async () => {
  // 表单校验
  if (!companyName.value || !companyName.value.trim()) {
    uni.showToast({ title: '请输入物流公司名称', icon: 'none' })
    return
  }
  if (!trackingNo.value || !trackingNo.value.trim()) {
    uni.showToast({ title: '请输入物流单号', icon: 'none' })
    return
  }

  if (submitting.value) return
  submitting.value = true

  try {
    await deliverOrder(order.value.id, {
      companyName: companyName.value.trim(),
      trackingNo: trackingNo.value.trim()
    })
    uni.showToast({ title: '发货成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (error) {
    console.error('[Ship] 发货失败:', error)
    uni.showToast({ title: '发货失败，请重试', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

const formatPrice = (price) => {
  return price != null ? Number(price).toFixed(2) : '0.00'
}
</script>

<style lang="scss" scoped>
.ship-page {
  min-height: 100vh;
  background: #F2F2F7;
  display: flex;
  flex-direction: column;
}

.ship-page__loading {
  display: flex;
  justify-content: center;
  padding: 200rpx 0;
}

.ship-page__loading-text {
  font-size: 28rpx;
  color: #8E8E93;
}

.ship-page__scroll {
  flex: 1;
}

.ship-page__card {
  background: #FFFFFF;
  margin: 0 16rpx 16rpx;
  border-radius: 16rpx;
  padding: 24rpx;
}

.ship-page__card-header {
  margin-bottom: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #F2F2F7;
}

.ship-page__card-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1D1D1F;
}

.ship-page__info-row {
  display: flex;
  padding: 10rpx 0;
}

.ship-page__info-label {
  width: 140rpx;
  font-size: 26rpx;
  color: #8E8E93;
  flex-shrink: 0;
}

.ship-page__info-value {
  flex: 1;
  font-size: 26rpx;
  color: #1D1D1F;

  &--address {
    line-height: 1.6;
  }

  &--price {
    font-weight: 600;
    color: #007AFF;
  }
}

.ship-page__goods-list {
  padding: 8rpx 0;
}

.ship-page__goods-item {
  display: flex;
  gap: 16rpx;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #F2F2F7;

  &:last-child {
    border-bottom: none;
  }
}

.ship-page__goods-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 8rpx;
  background: #F2F2F7;
  flex-shrink: 0;
}

.ship-page__goods-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4rpx;
}

.ship-page__goods-name {
  font-size: 26rpx;
  color: #1D1D1F;
  font-weight: 500;
}

.ship-page__goods-meta {
  font-size: 24rpx;
  color: #8E8E93;
}

.ship-page__form-item {
  padding: 16rpx 0;
  border-bottom: 1rpx solid #F2F2F7;

  &:last-child {
    border-bottom: none;
  }
}

.ship-page__form-label {
  font-size: 26rpx;
  color: #1D1D1F;
  margin-bottom: 12rpx;
  display: block;
}

.ship-page__form-input {
  width: 100%;
  height: 80rpx;
  background: #F2F2F7;
  border-radius: 12rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #1D1D1F;
  box-sizing: border-box;
}

.ship-page__footer {
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #FFFFFF;
  border-top: 1rpx solid #E5E5EA;
}

.ship-page__submit-btn {
  width: 100%;
  height: 88rpx;
  background: #007AFF;
  color: #FFFFFF;
  font-size: 32rpx;
  font-weight: 600;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;

  &--loading {
    background: #7CB8FF;
    pointer-events: none;
  }
}

.ship-page__error {
  display: flex;
  justify-content: center;
  padding: 200rpx 0;
  font-size: 28rpx;
  color: #8E8E93;
}
</style>
