<!--
  components/StatusBadge.vue — 订单状态标签组件

  【设计思想】
  订单状态标签是订单系统的核心 UI 元素，不同状态使用不同颜色区分：
  - 待付款：橙色（需要用户操作）
  - 待发货：蓝色（商家操作中）
  - 已发货：绿色（物流中）
  - 已完成：灰色（终态）
  - 已取消：红色（终态）

  【Props】
  - status: 订单状态字符串（pending | paid | shipped | completed | cancelled）
-->
<template>
  <view class="status-badge" :class="'status-badge--' + status">
    {{ statusMap[status] || '未知' }}
  </view>
</template>

<script setup>
defineProps({
  status: {
    type: String,
    required: true,
    default: 'pending',
    validator: (v) => ['pending', 'paid', 'shipped', 'completed', 'cancelled'].includes(v)
  }
})

// 状态映射表：后端枚举值 → 用户可读中文标签
const statusMap = {
  pending: '待付款',
  paid: '待发货',
  shipped: '已发货',
  completed: '已完成',
  cancelled: '已取消'
}
</script>

<style lang="scss" scoped>
.status-badge {
  display: inline-block;
  font-size: 22rpx;
  font-weight: 500;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
  line-height: 1.6;
  flex-shrink: 0;

  &--pending { background: #fff7ed; color: #ea580c; }
  &--paid { background: #E5F1FF; color: #007AFF; }
  &--shipped { background: #f0fdf4; color: #16a34a; }
  &--completed { background: #F2F2F7; color: #8E8E93; }
  &--cancelled { background: #fef2f2; color: #ef4444; }
}
</style>