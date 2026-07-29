<!--
  components/PriceSummary.vue — 价格汇总结算栏组件

  【设计思想】
  购物车页面底部固定结算栏，是用户下单前的最后一步操作。
  包含三个核心元素：
  1. 全选按钮（勾选/取消所有商品）
  2. 合计金额（显示已选商品的总价）
  3. 去结算按钮（跳转到下单确认页）

  【Props】
  - total: 合计金额
  - checkedCount: 已选商品数量
  - isAllChecked: 是否全选

  【Events】
  - toggle-all: 切换全选
  - settle: 去结算
-->
<template>
  <view class="price-summary">
    <!-- 左侧：全选 + 合计 -->
    <view class="price-summary__left">
      <view class="price-summary__checkbox" @tap="onToggleAll">
        <u-icon
          :name="isAllChecked ? 'checkmark-circle-fill' : 'checkmark-circle-fill'"
          :size="30"
          :color="isAllChecked ? '#007AFF' : '#d1d5db'"
        />
        <text class="price-summary__checkbox-label">全选</text>
      </view>
      <view class="price-summary__total">
        <text class="price-summary__total-label">合计：</text>
        <text class="price-summary__total-price">¥{{ formattedTotal }}</text>
      </view>
    </view>

    <!-- 右侧：结算按钮 -->
    <view
      class="price-summary__settle-btn"
      :class="{ 'price-summary__settle-btn--disabled': checkedCount === 0 }"
      @tap="onSettle"
    >
      去结算{{ checkedCount > 0 ? `(${checkedCount})` : '' }}
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  total: { type: Number, default: 0 },
  checkedCount: { type: Number, default: 0 },
  isAllChecked: { type: Boolean, default: false }
})

const emit = defineEmits(['toggle-all', 'settle'])

// 格式化总金额：保留两位小数
const formattedTotal = computed(() => Number(props.total).toFixed(2))

const onToggleAll = () => emit('toggle-all', !props.isAllChecked)

const onSettle = () => {
  if (props.checkedCount > 0) emit('settle')
}
</script>

<style lang="scss" scoped>
.price-summary {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #ffffff;
  border-top: 1rpx solid #E5E5EA;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16rpx;
  height: 100rpx;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.04);
  z-index: 100;
}

.price-summary__left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
}

.price-summary__checkbox {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.price-summary__checkbox-label { font-size: 26rpx; color: #8E8E93; }

.price-summary__total { display: flex; align-items: baseline; }

.price-summary__total-label { font-size: 26rpx; color: #1D1D1F; }

.price-summary__total-price {
  font-size: 32rpx;
  font-weight: 700;
  color: #007AFF;
}

.price-summary__settle-btn {
  background: #007AFF;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 600;
  padding: 16rpx 40rpx;
  border-radius: 40rpx;
  flex-shrink: 0;
  text-align: center;
  line-height: 1.2;
  &--disabled { background: #d1d5db; color: #9ca3af; }
}
</style>
