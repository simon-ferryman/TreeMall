<!--
  pages/merchant/order-manage/detail.vue — 商户订单详情页

  【设计思想】
  商户端查看订单完整信息，只读展示，无操作按钮。
  与用户端订单详情页的区别：无支付/取消/确认收货等操作按钮。
-->
<template>
  <view class="merchant-detail-page">
    <view v-if="loading" class="merchant-detail-page__loading">
      <text class="merchant-detail-page__loading-text">加载中...</text>
    </view>

    <template v-else-if="order">
      <scroll-view class="merchant-detail-page__scroll" scroll-y>
        <!-- 订单状态区 -->
        <view class="merchant-detail-page__status" :class="'merchant-detail-page__status--' + order.status">
          <text class="merchant-detail-page__status-text">{{ statusMap[order.status] || '未知状态' }}</text>
          <text class="merchant-detail-page__status-desc">{{ statusDesc }}</text>
        </view>

        <!-- 收货地址区 -->
        <view class="merchant-detail-page__card" v-if="address">
          <view class="merchant-detail-page__card-header">
            <text class="merchant-detail-page__card-title">收货信息</text>
          </view>
          <view class="merchant-detail-page__address-contact">
            <text class="merchant-detail-page__address-name">{{ address.receiverName || '--' }}</text>
            <text class="merchant-detail-page__address-phone">{{ address.receiverPhone || '--' }}</text>
          </view>
          <text class="merchant-detail-page__address-full">
            {{ address.province || '' }}{{ address.city || '' }}{{ address.district || '' }} {{ address.detailAddress || '' }}
          </text>
        </view>

        <!-- 商品列表区 -->
        <view class="merchant-detail-page__card">
          <view class="merchant-detail-page__card-header">
            <text class="merchant-detail-page__card-title">商品信息</text>
          </view>
          <view v-for="(item, index) in order.items" :key="index" class="merchant-detail-page__goods-item">
            <image
              class="merchant-detail-page__goods-image"
              :src="getImageUrl(item.productImage) || defaultImage"
              mode="aspectFill"
            />
            <view class="merchant-detail-page__goods-info">
              <text class="merchant-detail-page__goods-name">{{ item.productName || '商品名称' }}</text>
			  <text class="merchant-detail-page__goods-specs">{{ formatSpecs(item.specs)}}</text>
              <view class="merchant-detail-page__goods-bottom">
                <text class="merchant-detail-page__goods-price">¥{{ formatPrice(item.price) }}</text>
                <text class="merchant-detail-page__goods-quantity">x{{ item.quantity || 1 }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 价格明细区 -->
        <view class="merchant-detail-page__card">
          <view class="merchant-detail-page__card-header">
            <text class="merchant-detail-page__card-title">价格明细</text>
          </view>
          <view class="merchant-detail-page__price-row">
            <text class="merchant-detail-page__price-label">实付款</text>
            <text class="merchant-detail-page__price-value merchant-detail-page__price-value--total">
              ¥{{ formatPrice(order.totalAmount) }}
            </text>
          </view>
        </view>

        <!-- 订单信息区 -->
        <view class="merchant-detail-page__card">
          <view class="merchant-detail-page__card-header">
            <text class="merchant-detail-page__card-title">订单信息</text>
          </view>
          <view class="merchant-detail-page__info-row">
            <text class="merchant-detail-page__info-label">订单编号</text>
            <text class="merchant-detail-page__info-text">{{ order.orderNo || '--' }}</text>
          </view>
          <view class="merchant-detail-page__info-row">
            <text class="merchant-detail-page__info-label">创建时间</text>
            <text class="merchant-detail-page__info-text">{{ formatTime(order.createdAt) }}</text>
          </view>
          <view class="merchant-detail-page__info-row" v-if="order.payTime">
            <text class="merchant-detail-page__info-label">支付时间</text>
            <text class="merchant-detail-page__info-text">{{ formatTime(order.payTime) }}</text>
          </view>
          <view class="merchant-detail-page__info-row" v-if="order.remark">
            <text class="merchant-detail-page__info-label">订单备注</text>
            <text class="merchant-detail-page__info-text">{{ order.remark }}</text>
          </view>
        </view>

        <view style="height: 40rpx;" />
      </scroll-view>
    </template>

    <view v-else class="merchant-detail-page__error">
      <text>订单信息加载失败</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getMerchantOrderDetail } from '@/api/merchant'
import { getImageUrl } from '@/utils/image-url'

const defaultImage = '/static/images/product-default.png'

const statusMap = {
  pending: '待付款',
  paid: '待发货',
  shipped: '已发货',
  completed: '已完成',
  cancelled: '已取消'
}

const order = ref(null)
const loading = ref(true)
const address = ref(null)

const statusDesc = computed(() => {
  if (!order.value) return ''
  const descMap = {
    pending: '等待买家付款',
    paid: '买家已付款，请尽快发货',
    shipped: '商品已发出，等待买家收货',
    completed: '订单已完成',
    cancelled: '订单已取消'
  }
  return descMap[order.value.status] || ''
})

onLoad((options) => {
  const orderId = options.id
  if (orderId) {
    loadDetail(orderId)
  } else {
    loading.value = false
    uni.showToast({ title: '订单不存在', icon: 'none' })
  }
})

const loadDetail = async (orderId) => {
  loading.value = true
  try {
    const data = await getMerchantOrderDetail(orderId)
    order.value = data
    // 解析地址快照
    if (data.addressSnapshot && typeof data.addressSnapshot === 'string') {
      try {
        address.value = JSON.parse(data.addressSnapshot)
      } catch (e) {
        console.error('[MerchantDetail] 解析地址快照失败:', e)
      }
    }
  } catch (error) {
    console.error('[MerchantDetail] 加载订单详情失败:', error)
    order.value = null
  } finally {
    loading.value = false
  }
}

const formatPrice = (price) => {
  return price != null ? Number(price).toFixed(2) : '0.00'
}

const formatTime = (timeStr) => {
  if (!timeStr) return '--'
  // 处理 ISO 8601 格式：2026-07-24T10:19:30 → 2026-07-24 10:19:30
  return timeStr.replace('T', ' ')
}
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
</script>

<style lang="scss" scoped>
.merchant-detail-page {
  min-height: 100vh;
  background: #F2F2F7;
}

.merchant-detail-page__loading {
  display: flex;
  justify-content: center;
  padding: 200rpx 0;
}

.merchant-detail-page__loading-text {
  font-size: 28rpx;
  color: #8E8E93;
}

.merchant-detail-page__scroll {
  height: 100vh;
}

.merchant-detail-page__status {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48rpx 24rpx;
  background: #FFFFFF;
  margin-bottom: 16rpx;

  &--pending { background: linear-gradient(180deg, #fff7ed 0%, #ffffff 100%); }
  &--paid { background: linear-gradient(180deg, #E5F1FF 0%, #ffffff 100%); }
  &--shipped { background: linear-gradient(180deg, #f0fdf4 0%, #ffffff 100%); }
  &--completed { background: linear-gradient(180deg, #F2F2F7 0%, #ffffff 100%); }
  &--cancelled { background: linear-gradient(180deg, #fef2f2 0%, #ffffff 100%); }
}

.merchant-detail-page__status-text {
  font-size: 36rpx;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 8rpx;
}

.merchant-detail-page__status-desc {
  font-size: 24rpx;
  color: #8E8E93;
}

.merchant-detail-page__card {
  background: #FFFFFF;
  margin: 0 16rpx 16rpx;
  border-radius: 16rpx;
  padding: 24rpx;
}

.merchant-detail-page__card-header {
  margin-bottom: 16rpx;
}

.merchant-detail-page__card-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #1D1D1F;
}

.merchant-detail-page__address-contact {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-bottom: 8rpx;
}

.merchant-detail-page__address-name {
  font-size: 32rpx;
  font-weight: 500;
  color: #1D1D1F;
}

.merchant-detail-page__address-phone {
  font-size: 28rpx;
  color: #8E8E93;
}

.merchant-detail-page__address-full {
  font-size: 24rpx;
  color: #8E8E93;
  line-height: 1.6;
}

.merchant-detail-page__goods-item {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #F2F2F7;

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
}

.merchant-detail-page__goods-image {
  width: 140rpx;
  height: 140rpx;
  border-radius: 12rpx;
  background: #F2F2F7;
  flex-shrink: 0;
}

.merchant-detail-page__goods-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.merchant-detail-page__goods-name {
  font-size: 28rpx;
  color: #1D1D1F;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.merchant-detail-page__goods-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.merchant-detail-page__goods-price {
  font-size: 28rpx;
  font-weight: 600;
  color: #007AFF;
}

.merchant-detail-page__goods-quantity {
  font-size: 24rpx;
  color: #8E8E93;
}

.merchant-detail-page__price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
}

.merchant-detail-page__price-label {
  font-size: 28rpx;
  color: #8E8E93;
}

.merchant-detail-page__price-value {
  font-size: 28rpx;
  color: #1D1D1F;

  &--total {
    font-size: 36rpx;
    font-weight: 600;
    color: #007AFF;
  }
}

.merchant-detail-page__info-row {
  display: flex;
  padding: 12rpx 0;
}

.merchant-detail-page__info-label {
  width: 140rpx;
  font-size: 24rpx;
  color: #8E8E93;
  flex-shrink: 0;
}

.merchant-detail-page__info-text {
  flex: 1;
  font-size: 24rpx;
  color: #1D1D1F;
  word-break: break-all;
}

.merchant-detail-page__error {
  display: flex;
  justify-content: center;
  padding: 200rpx 0;
  font-size: 28rpx;
  color: #8E8E93;
}
.merchant-detail-page__goods-specs {
  font-size: 22rpx;
  color: #8E8E93;
  margin-top: 4rpx;
}
</style>
