<!--
  components/OrderStatusTag.vue — 订单状态标签组件（增强版）

  【设计思想】
  与 StatusBadge 类似，但额外支持尺寸变体（small/default/large），
  使用更细腻的 iOS 风格颜色方案（与 constants.js 中的 ORDER_STATUS_MAP 对齐）。

  【Props】
  - status: 订单状态字符串（pending | paid | shipped | completed | cancelled）
  - size: 标签尺寸（'small' | 'default' | 'large'）
-->
<template>
  <view
    class="order-status-tag"
    :class="['order-status-tag--' + status, 'order-status-tag--' + size]"
  >
    {{ statusMap[status] || '未知状态' }}
  </view>
</template>

<script setup>
defineProps({
  status: {
    type: String,
    required: true,
    default: 'pending',
    validator: (v) => ['pending', 'paid', 'shipped', 'completed', 'cancelled'].includes(v)
  },
  size: {
    type: String,
    default: 'default',
    validator: (v) => ['small', 'default', 'large'].includes(v)
  }
})

const statusMap = {
  pending: '待付款',
  paid: '待发货',
  shipped: '待收货',
  completed: '已完成',
  cancelled: '已取消'
}
</script>

<style lang="scss" scoped>
.order-status-tag {
  display: inline-block;
  font-weight: 500;
  border-radius: 20rpx;
  line-height: 1.6;
  flex-shrink: 0;
  text-align: center;
  white-space: nowrap;

  // 尺寸变体
  &--small { font-size: 20rpx; padding: 2rpx 12rpx; }
  &--default { font-size: 22rpx; padding: 4rpx 16rpx; }
  &--large { font-size: 28rpx; padding: 6rpx 24rpx; }

  // 状态颜色变体（iOS 风格）
  &--pending { background-color: #FFF2E5; color: #FF9500; }
  &--paid { background-color: #E5F1FF; color: #007AFF; }
  &--shipped { background-color: #E8F8EE; color: #34C759; }
  &--completed { background-color: #F2F2F7; color: #8E8E93; }
  &--cancelled { background-color: #FFEBEA; color: #FF3B30; }
}
</style>