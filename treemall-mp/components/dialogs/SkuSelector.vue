<!--
  components/SkuSelector.vue — SKU 规格选择弹窗组件

  【设计思想】
  SKU（Stock Keeping Unit）选择是电商商品详情页的核心交互。
  用户通过此弹窗选择商品的规格组合（如颜色、尺寸），
  查看当前 SKU 的库存和价格，选择数量后加入购物车或立即购买。

  本组件包含以下功能模块：
  1. 顶部商品信息栏（缩略图 + 价格 + 库存）
  2. 规格选择列表（每个规格一行，显示该规格所有可选值）
  3. 数量选择器（使用 u-number-box 组件）
  4. 底部确认按钮（加入购物车 / 立即购买）

  【Props】
  - show: 是否显示弹窗
  - product: 商品基础信息 { id, productName, price, mainImage, stock }
  - skuList: 规格列表 [{ name: '颜色', values: ['红', '蓝', '绿'] }, ...]
  - confirmText: 确认按钮文字

  【Events】
  - update:show: 弹窗显示/隐藏切换（支持 v-model:show）
  - confirm: 用户点击确认按钮，传递 { sku, quantity }
  - close: 弹窗关闭事件
-->
<template>
  <u-popup
    :show="show"
    mode="bottom"
    round="32"
    :safe-area-inset-bottom="true"
    :close-on-click-overlay="true"
    @close="onClose"
  >
    <view class="sku-selector">
      <!-- 顶部商品信息栏 -->
      <view class="sku-selector__header">
        <image class="sku-selector__thumb" :src="product.mainImage || defaultImage" mode="aspectFill" />
        <view class="sku-selector__info">
          <view class="sku-selector__price">
            <text class="sku-selector__price-symbol">¥</text>
            <text class="sku-selector__price-value">{{ currentPrice.toFixed(2) }}</text>
          </view>
          <text class="sku-selector__stock">库存：{{ currentStock }}件</text>
        </view>
        <view class="sku-selector__close" @tap="onClose">
          <u-icon name="close" size="32" color="#8E8E93" />
        </view>
      </view>

      <!-- 规格选择区域（可滚动） -->
      <scroll-view class="sku-selector__body" scroll-y>
        <view v-for="(spec, specIndex) in skuList" :key="specIndex" class="sku-selector__spec-group">
          <text class="sku-selector__spec-name">{{ spec.name }}</text>
          <view class="sku-selector__spec-values">
            <view
              v-for="(value, valueIndex) in spec.values"
              :key="valueIndex"
              class="sku-selector__spec-value"
              :class="{ 'sku-selector__spec-value--active': selectedSpecs[specIndex] === valueIndex }"
              @tap="onSelectSpec(specIndex, valueIndex)"
            >
              {{ value }}
            </view>
          </view>
        </view>
      </scroll-view>
	  <!-- 数量选择区域 -->
	  <view class="sku-selector__quantity">
	    <text class="sku-selector__quantity-label">数量</text>
	    <u-number-box
	      v-model="quantity"
	      :min="1"
	      :max="currentStock"
	      :step="1"
	      integer
	      :disabled="currentStock <= 0"
	    />
	  </view>

      <!-- 底部确认按钮 -->
      <view class="sku-selector__footer">
        <view
          class="sku-selector__confirm-btn"
          :class="{ 'sku-selector__confirm-btn--disabled': currentStock <= 0 }"
          @tap="onConfirm"
        >
          {{ confirmText }}
        </view>
      </view>
    </view>
  </u-popup>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import uNumberBox from 'uview-plus/components/u-number-box/u-number-box.vue'
const defaultImage = '/static/images/product-default.png'

const props = defineProps({
  show: { type: Boolean, default: false },
  product: { type: Object, required: true, default: () => ({}) },
  skuList: { type: Array, default: () => [] },
  confirmText: { type: String, default: '确定' }
})

const emit = defineEmits(['update:show', 'confirm', 'close'])

// 已选规格索引数组：索引对应 skuList 顺序，-1 表示未选择
const selectedSpecs = ref(props.skuList.map(() => -1))
// 选择数量：默认 1 件
const quantity = ref(1)

// 当前价格：使用商品基础价
const currentPrice = computed(() => props.product.price || 0)
// 当前库存：使用商品基础库存
const currentStock = computed(() => props.product.stock || 0)

// 弹窗打开时重置数量
watch(() => props.show, (newVal) => {
  if (newVal) quantity.value = 1
})

// skuList 变化时重新初始化选择状态
watch(() => props.skuList, () => {
  selectedSpecs.value = props.skuList.map(() => -1)
}, { deep: true })

// 选择规格值：点击已选中的取消选中，否则选中
const onSelectSpec = (specIndex, valueIndex) => {
  if (selectedSpecs.value[specIndex] === valueIndex) {
    selectedSpecs.value[specIndex] = -1
  } else {
    selectedSpecs.value[specIndex] = valueIndex
  }
}

// 确认按钮：收集选中的规格和数量，触发 confirm 事件
const onConfirm = () => {
  if (currentStock.value <= 0) return

  const selectedSku = {}
  props.skuList.forEach((spec, specIndex) => {
    const valueIndex = selectedSpecs.value[specIndex]
    if (valueIndex >= 0) selectedSku[spec.name] = spec.values[valueIndex]
  })

  emit('confirm', { sku: selectedSku, quantity: quantity.value })
  emit('update:show', false)
}

// 弹窗关闭
const onClose = () => {
  emit('update:show', false)
  emit('close')
}
</script>

<style lang="scss" scoped>
.sku-selector {
  background-color: #FFFFFF;
  border-radius: 32rpx 32rpx 0 0;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sku-selector__header {
  display: flex;
  align-items: flex-start;
  padding: 32rpx 24rpx 24rpx;
  position: relative;
  border-bottom: 1rpx solid #E5E5EA;
}

.sku-selector__thumb {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  background-color: #F2F2F7;
  flex-shrink: 0;
  margin-right: 24rpx;
}

.sku-selector__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding-top: 16rpx;
}

.sku-selector__price { color: #007AFF; font-weight: 700; line-height: 1; margin-bottom: 12rpx; }
.sku-selector__price-symbol { font-size: 28rpx; font-weight: 500; }
.sku-selector__price-value { font-size: 40rpx; }
.sku-selector__stock { font-size: 24rpx; color: #8E8E93; line-height: 1.4; }

.sku-selector__close {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sku-selector__body {
  flex: 1;
  overflow-y: auto;
  padding: 24rpx;
}

.sku-selector__spec-group { margin-bottom: 32rpx; &:last-child { margin-bottom: 0; } }

.sku-selector__spec-name {
  font-size: 28rpx;
  color: #1D1D1F;
  font-weight: 500;
  line-height: 1.4;
  margin-bottom: 20rpx;
  display: block;
}

.sku-selector__spec-values {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.sku-selector__spec-value {
  padding: 12rpx 32rpx;
  font-size: 26rpx;
  color: #1D1D1F;
  background-color: #F2F2F7;
  border-radius: 32rpx;
  line-height: 1.4;
  transition: all 0.2s ease;
  box-sizing: border-box;

  &--active {
    color: #007AFF;
    background-color: #E5F1FF;
    border: 2rpx solid #007AFF;
    padding: 10rpx 30rpx;
  }
}

.sku-selector__quantity {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  margin-top: 8rpx;
  border-top: 1rpx solid #E5E5EA;
}

.sku-selector__quantity-label { font-size: 28rpx; color: #1D1D1F; line-height: 1.4; }

.sku-selector__footer {
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #E5E5EA;
}

.sku-selector__confirm-btn {
  width: 100%;
  height: 88rpx;
  background-color: #007AFF;
  color: #FFFFFF;
  font-size: 32rpx;
  font-weight: 600;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;

  &--disabled {
    background-color: #C7C7CC;
    color: #FFFFFF;
    pointer-events: none;
  }
}
</style>
