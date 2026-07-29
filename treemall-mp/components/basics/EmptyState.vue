<!--
  components/EmptyState.vue — 空状态组件

  【设计思想】
  当列表数据为空时（购物车为空、订单为空、地址为空），
  使用空状态组件替代空白页面，给予用户明确的视觉反馈和操作引导。

  【Props】
  - icon: 图标名称（uView 图标库）
  - text: 提示文字
  - buttonText: 按钮文字（可选，不传则不显示按钮）
  - padding: 上下内边距（默认 200rpx）

  【Events】
  - action: 按钮点击事件

  【使用场景】
  - 购物车为空："购物车还是空的，去逛逛吧" + "去首页"按钮
  - 订单为空："暂无订单" + "去首页"按钮
  - 地址为空："还没有收货地址" + "新增地址"按钮
-->
<template>
  <!-- 空状态容器，使用 flex 垂直居中布局 -->
  <view class="empty-state" :style="{ paddingTop: padding + 'rpx', paddingBottom: padding + 'rpx' }">
    <!-- 空状态图标 -->
    <view class="empty-state__icon-wrapper">
      <u-icon :name="icon" :size="100" color="#d1d5db" />
    </view>
    <!-- 提示文字 -->
    <text class="empty-state__text">{{ text }}</text>
    <!-- 操作按钮（可选） -->
    <view v-if="buttonText" class="empty-state__btn" @tap="onAction">
      {{ buttonText }}
    </view>
  </view>
</template>

<script setup>
// ==================== Props 定义 ====================
defineProps({
  icon: { type: String, default: 'shopping-cart' },  // 默认购物车图标
  text: { type: String, default: '暂无数据' },
  buttonText: { type: String, default: '' },          // 空字符串表示不显示按钮
  padding: { type: Number, default: 200 }             // 默认上下 200rpx 内边距
})

// ==================== Events 定义 ====================
const emit = defineEmits(['action'])

// ==================== 事件处理 ====================
const onAction = () => emit('action')
</script>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-left: 32rpx;
  padding-right: 32rpx;
}

.empty-state__icon-wrapper {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: #F2F2F7;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;
}

.empty-state__text {
  font-size: 28rpx;
  color: #9ca3af;
  text-align: center;
  line-height: 1.6;
  margin-bottom: 40rpx;
}

.empty-state__btn {
  background: #007AFF;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 500;
  padding: 16rpx 48rpx;
  border-radius: 40rpx;
  line-height: 1.4;
}
</style>