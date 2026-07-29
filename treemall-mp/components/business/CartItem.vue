<!--
  components/CartItem.vue — 购物车商品行组件

  【设计思想】
  购物车中每个商品通过 CartItem 组件渲染，核心交互包括：
  1. 勾选/取消勾选（切换商品是否参与结算）
  2. 数量调节（+/- 按钮，最少 1，最多库存上限）
  3. 删除

  【Figma 设计稿参考】
  左侧勾选框 → 缩略图(140×140) → 名称+价格(中) → 数量调节器(右) → 删除(右)

  【Props】
  - item: 购物车项数据 { id, productId, productName, price, quantity, mainImage, checked }
-->
<template>
  <view class="cart-item">
    <!-- 勾选框 -->
    <view class="cart-item__checkbox" @tap.stop="onToggleCheck">
      <u-icon
        :name="item.checked ? 'checkmark-circle-fill' : 'checkmark-circle-fill'"
        :size="30"
        :color="item.checked ? '#007AFF' : '#d1d5db'"
      />
    </view>
    <!-- 商品图片 -->
    <image
      class="cart-item__image"
      :src="getImageUrl(item.mainImage) || defaultImage"
      mode="aspectFill"
      @tap="onClick"
    />
    <!-- 商品信息 -->
    <view class="cart-item__info" @tap="onClick">
      <text class="cart-item__name">{{ item.productName }}</text>
      <text class="cart-item__price">¥{{ formatPrice(item.price) }}</text>
    </view>
    <!-- 数量调节器 -->
    <view class="cart-item__quantity">
      <view
        class="cart-item__qty-btn"
        :class="{ 'cart-item__qty-btn--disabled': item.quantity <= 1 }"
        @tap.stop="onDecrease"
      >
        <u-icon name="minus" :size="20" :color="item.quantity <= 1 ? '#d1d5db' : '#1D1D1F'" />
      </view>
      <text class="cart-item__qty-value">{{ item.quantity }}</text>
      <view class="cart-item__qty-btn" @tap.stop="onIncrease">
        <u-icon name="plus" :size="20" color="#1D1D1F" />
      </view>
    </view>
    <!-- 删除按钮 -->
    <view class="cart-item__delete" @tap.stop="onDelete">
      <u-icon name="trash" :size="36" color="#ef4444" />
    </view>
  </view>
</template>

<script setup>
const defaultImage = '/static/images/product-default.png'
import { getImageUrl } from '@/utils/image-url'

const props = defineProps({
  item: {
    type: Object,
    required: true,
    default: () => ({ id: 0, productId: 0, productName: '', price: 0, quantity: 1, mainImage: '', checked: true })
  }
})

const emit = defineEmits(['toggle-check', 'increase', 'decrease', 'delete', 'click'])

const onToggleCheck = () => emit('toggle-check', props.item)
const onIncrease = () => emit('increase', props.item)
const onDecrease = () => { if (props.item.quantity > 1) emit('decrease', props.item) }
const onDelete = () => emit('delete', props.item)
const onClick = () => emit('click', props.item)

const formatPrice = (price) => price != null ? Number(price).toFixed(2) : '0.00'
</script>

<style lang="scss" scoped>
.cart-item {
  display: flex;
  align-items: center;
  background: #ffffff;
  padding: 20rpx 16rpx;
  border-radius: 12rpx;
  margin: 0 16rpx 16rpx;
  box-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.04);
  gap: 16rpx;
}

.cart-item__checkbox {
  flex-shrink: 0;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cart-item__image {
  width: 140rpx;
  height: 140rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
  background: #F2F2F7;
}

.cart-item__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
  overflow: hidden;
}

.cart-item__name {
  font-size: 26rpx;
  color: #1D1D1F;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cart-item__price {
  font-size: 28rpx;
  font-weight: 700;
  color: #007AFF;
  line-height: 1;
}

.cart-item__quantity {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  gap: 0;
  border: 1rpx solid #E5E5EA;
  border-radius: 8rpx;
  overflow: hidden;
}

.cart-item__qty-btn {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9fafb;
  &--disabled { opacity: 0.4; }
}

.cart-item__qty-value {
  width: 64rpx;
  text-align: center;
  font-size: 26rpx;
  color: #1D1D1F;
  font-weight: 500;
  border-left: 1rpx solid #E5E5EA;
  border-right: 1rpx solid #E5E5EA;
  line-height: 56rpx;
}

.cart-item__delete {
  flex-shrink: 0;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
