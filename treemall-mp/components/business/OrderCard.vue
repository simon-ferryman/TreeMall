<!--
  components/OrderCard.vue — 订单卡片组件

  【设计思想】
  订单列表页中每个订单以卡片形式展示，包含：
  1. 订单头部（订单号 + 状态标签）
  2. 商品缩略图（最多显示 3 张）
  3. 订单金额
  4. 操作按钮（取消、付款、确认收货等，根据状态动态显示）

  【Props】
  - order: 订单数据对象 { orderId, orderNo, status, items, totalAmount, createTime }

  【Events】
  - click: 点击整张卡片
  - cancel: 取消订单
  - pay: 去付款
  - confirm: 确认收货
-->
<template>
  <view class="order-card" @tap="onClick">
    <!-- 订单头部 -->
    <view class="order-card__header">
      <text class="order-card__order-no">订单号：{{ formatOrderNo(order.orderNo) }}</text>
      <StatusBadge :status="order.status" />
    </view>

    <!-- 商品缩略图（最多 3 张） -->
    <!-- 商品缩略图（最多 3 张） -->
    <view class="order-card__goods">
      <image
        v-for="(item, index) in displayItems"
        :key="index"
        class="order-card__goods-image"
        :src="getImageUrl(item.productImage) || defaultImage"
        mode="aspectFill"
      />
      <view v-if="order.items && order.items.length > 3" class="order-card__goods-more">
        +{{ order.items.length - 3 }}
      </view>
    </view>

    <!-- 商品描述信息（新增） -->
    <view class="order-card__goods-info" v-for="(item, index) in displayItems" :key="'info-' + index">
      <text class="order-card__goods-name">{{ item.productName }}</text>
      <text class="order-card__goods-desc" v-if="item.description">{{ item.description }}</text>
      <text class="order-card__goods-specs" v-if="item.specs">规格：{{ item.specs }}</text>
    </view>
    <!-- 订单底部 -->
    <view class="order-card__footer">
      <text class="order-card__amount">
        共{{ order.items ? order.items.length : 0 }}件 合计：¥{{ formatAmount(order.totalAmount) }}
      </text>
      <!-- 操作按钮组 -->
      <view class="order-card__actions">
        <view
          v-if="order.status === 'pending'"
          class="order-card__btn order-card__btn--secondary"
          @tap.stop="onCancel"
        >取消订单</view>
        <view
          v-if="order.status === 'pending'"
          class="order-card__btn order-card__btn--primary"
          @tap.stop="onPay"
        >去付款</view>
        <view
          v-if="order.status === 'shipped'"
          class="order-card__btn order-card__btn--primary"
          @tap.stop="onConfirmReceive"
		  @tap="onConfirmReceive"
        >确认收货</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import StatusBadge from './StatusBadge.vue'
import { getImageUrl } from '@/utils/image-url'

const defaultImage = '/static/images/product-default.png'

const props = defineProps({
  order: {
    type: Object,
    required: true,
    default: () => ({ orderId: 0, orderNo: '', status: 'pending', items: [], totalAmount: 0, createTime: '' })
  }
})

const emit = defineEmits(['click', 'cancel', 'pay', 'confirm'])

// 取前 3 个商品缩略图
const displayItems = computed(() => {
  if (!props.order.items || props.order.items.length === 0) return []
  return props.order.items.slice(0, 3)
})

const onClick = () => emit('click', props.order)
const onCancel = () => emit('cancel', props.order)
const onPay = () => emit('pay', props.order)
const onConfirm = () => emit('confirm', props.order)

const formatOrderNo = (no) => {
  if (!no) return '—'
  return no.length > 12 ? no.slice(0, 12) + '...' : no
}
const formatAmount = (a) => a != null ? Number(a).toFixed(2) : '0.00'
</script>

<style lang="scss" scoped>
.order-card {
  background: #ffffff;
  border-radius: 12rpx;
  margin: 0 16rpx 20rpx;
  padding: 24rpx;
  box-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.04);
}

.order-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #F2F2F7;
}

.order-card__order-no { font-size: 24rpx; color: #8E8E93; }

.order-card__goods {
  display: flex;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.order-card__goods-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  background: #F2F2F7;
  flex-shrink: 0;
}

.order-card__goods-more {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  background: rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #8E8E93;
  flex-shrink: 0;
}

.order-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-card__amount { font-size: 24rpx; color: #8E8E93; }

.order-card__actions { display: flex; gap: 16rpx; }

.order-card__btn {
  font-size: 24rpx;
  padding: 10rpx 24rpx;
  border-radius: 24rpx;
  line-height: 1.4;
  &--primary { background: #007AFF; color: #ffffff; }
  &--secondary { background: #F2F2F7; color: #8E8E93; border: 1rpx solid #E5E5EA; }
}
.order-card__goods-info {
  margin-bottom: 12rpx;
  padding: 0 4rpx;
}
.order-card__goods-name {
  font-size: 26rpx;
  color: #1D1D1F;
  font-weight: 500;
  display: block;
  margin-bottom: 4rpx;
}
.order-card__goods-desc {
  font-size: 22rpx;
  color: #8E8E93;
  display: block;
  @include text-ellipsis-multi(2);
  margin-bottom: 4rpx;
}
.order-card__goods-specs {
  font-size: 22rpx;
  color: #8E8E93;
  display: block;
}
</style>
