<!--
  components/ProductCard.vue — 商品卡片组件

  【设计思想】
  商品卡片是小程序中最常见的展示组件，出现在首页、分类页、搜索结果等多个场景。
  不同场景需要不同的展示密度，因此通过 mode 属性控制两种模式：
  - compact：紧凑模式（首页 2×2 网格，仅显示图片+名称+价格）
  - list：列表模式（分类页 2 列网格，显示图片+名称+价格+评价数）

  【Props】
  - product: 商品数据对象 { id, productName, price, mainImage, salesCount }
  - mode: 展示模式（'compact' | 'list'）
-->
<template>
  <view class="product-card" :class="'product-card--' + mode" @tap="onClick">
    <!-- 商品图片 -->
    <view class="product-card__image-wrapper">
      <image
        class="product-card__image"
        :src="getImageUrl(product.mainImage)"
        mode="aspectFill"
        lazy-load
      />
      <!-- NEW 标签（仅紧凑模式 + 有标记时显示） -->
      <view v-if="product.isNew && mode === 'compact'" class="product-card__tag-new">NEW</view>
    </view>
    <!-- 商品信息 -->
    <view class="product-card__info">
      <text class="product-card__name" :class="'product-card__name--' + mode">
        {{ product.name || '商品名称' }}
      </text>
      <!-- 价格行 -->
      <view class="product-card__price-row">
        <view class="product-card__price">
          <text class="product-card__price-symbol">¥</text>
          <text class="product-card__price-value">
            {{ product.price != null ? product.price.toFixed(2) : '0.00' }}
          </text>
        </view>
        <!-- 销量/评价数（仅列表模式 + 有数据时显示） -->
        <text v-if="mode === 'list' && product.salesCount" class="product-card__sales">
          {{ formatSales(product.salesCount) }}人评价
        </text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { getImageUrl } from '@/utils/image-url'
const defaultImage = '/static/images/product-default.png'

const props = defineProps({
  product: { type: Object, required: true, default: () => ({}) },
  mode: { type: String, default: 'compact', validator: (v) => ['compact', 'list'].includes(v) }
})

const emit = defineEmits(['click'])

// 卡片点击：向父组件传递商品数据
const onClick = () => emit('click', props.product)

// 格式化销量：>=10000 显示"万"单位
const formatSales = (count) => {
  if (count >= 10000) return (count / 10000).toFixed(1) + '万'
  return String(count)
}
</script>

<style lang="scss" scoped>
.product-card {
  background: #ffffff;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);

  &--compact {
    .product-card__image-wrapper {
      width: 100%;
      height: 0;
      padding-bottom: 100%;
      position: relative;
    }
    .product-card__image {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
    }
    .product-card__info { padding: 16rpx; }
  }

  &--list {
    .product-card__image-wrapper {
      width: 100%;
      height: 0;
      padding-bottom: 100%;
      position: relative;
    }
    .product-card__image {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
    }
    .product-card__info { padding: 20rpx; }
  }
}

.product-card__image-wrapper { background: #F2F2F7; }
.product-card__image { display: block; }

.product-card__tag-new {
  position: absolute;
  top: 12rpx;
  left: 12rpx;
  background: #007AFF;
  color: #ffffff;
  font-size: 20rpx;
  font-weight: 600;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  line-height: 1.4;
}

.product-card__info {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.product-card__name {
  font-size: 26rpx;
  color: #1D1D1F;
  line-height: 1.4;

  &--compact {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  &--list {
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
}

.product-card__price-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.product-card__price {
  color: #007AFF;
  font-weight: 700;
  line-height: 1;
}

.product-card__price-symbol { font-size: 22rpx; font-weight: 500; }
.product-card__price-value { font-size: 32rpx; }

.product-card__sales {
  font-size: 22rpx;
  color: #ffffff;
  flex-shrink: 0;
}
</style>
