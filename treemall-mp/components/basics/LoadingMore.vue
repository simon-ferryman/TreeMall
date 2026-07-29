<!--
  components/LoadingMore.vue — 加载更多组件

  【设计思想】
  列表页上拉加载更多时，底部显示加载状态指示器。
  三种状态：
  1. loading：正在加载（显示加载动画 + "加载中..."）
  2. noMore：没有更多数据（显示"—— 没有更多了 ——"）
  3. error：加载失败（显示"加载失败，点击重试"）

  【Props】
  - status: 'loading' | 'noMore' | 'error' | ''

  【Events】
  - retry: 点击重试（仅 error 状态有效）
-->
<template>
  <view class="loading-more">
    <!-- 加载中 -->
    <view v-if="status === 'loading'" class="loading-more__loading">
      <u-loading-icon :size="28" color="#007AFF" mode="circle" />
      <text class="loading-more__text">加载中...</text>
    </view>

    <!-- 没有更多 -->
    <view v-else-if="status === 'noMore'" class="loading-more__no-more">
      <view class="loading-more__line" />
      <text class="loading-more__text">没有更多了</text>
      <view class="loading-more__line" />
    </view>

    <!-- 加载失败 -->
    <view v-else-if="status === 'error'" class="loading-more__error" @tap="onRetry">
      <text class="loading-more__text loading-more__text--error">
        加载失败，点击重试
      </text>
    </view>
  </view>
</template>

<script setup>
// ==================== Props 定义 ====================
defineProps({
  status: {
    type: String,
    default: '',
    validator: (value) => ['', 'loading', 'noMore', 'error'].includes(value)
  }
})

// ==================== Events 定义 ====================
const emit = defineEmits(['retry'])

// ==================== 事件处理 ====================
const onRetry = () => emit('retry')
</script>

<style lang="scss" scoped>
.loading-more {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 32rpx 0 48rpx;
}

.loading-more__loading {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.loading-more__no-more {
  display: flex;
  align-items: center;
  gap: 16rpx;
  width: 100%;
  padding: 0 32rpx;
}

.loading-more__line {
  flex: 1;
  height: 1rpx;
  background: #E5E5EA;
}

.loading-more__error {
  padding: 16rpx 0;
}

.loading-more__text {
  font-size: 24rpx;
  color: #9ca3af;
  flex-shrink: 0;

  &--error {
    color: #007AFF;
  }
}
</style>