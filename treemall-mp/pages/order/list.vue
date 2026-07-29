<!--
  pages/order/list.vue — 订单列表页

  【设计思想】
  订单列表使用 Tab 切换不同状态的订单，每个 Tab 对应一个数据列表。
  使用 OrderCard 组件展示订单卡片，StatusBadge 显示状态标签。
  支持上拉加载更多和下拉刷新。

  【Tab 状态】
  - 全部：不传 status
  - 待付款：status=pending
  - 待发货：status=paid
  - 已完成：status=completed
-->
<template>
  <view class="order-list-page">
    <!-- ==================== 状态 Tab ==================== -->
    <view class="order-list-page__tabs">
      <view
        v-for="tab in statusTabs"
        :key="tab.value"
        class="order-list-page__tab"
        :class="{ 'order-list-page__tab--active': activeTab === tab.value }"
        @tap="onTabChange(tab.value)"
      >
        {{ tab.label }}
      </view>
    </view>

    <!-- ==================== 订单列表 ==================== -->
    <scroll-view
      class="order-list-page__scroll"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refresherTriggered"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
    >
     <!-- 空状态：初始加载失败时显示加载失败，数据为空时显示暂无订单 -->
     <EmptyState
       v-if="orders.length === 0 && !loading"
       :icon="loadError ? 'error' : 'order'"
       :text="loadError ? '加载失败，请重试' : '暂无订单'"
       :button-text="loadError ? '点击重试' : '去首页'"
       @action="loadError ? onLoadMore : onGoHome"
     />

      <!-- 订单卡片 -->
      <OrderCard
        v-for="order in orders"
        :key="order.id"
        :order="order"
        @click="onOrderClick"
        @cancel="onCancelOrder"
        @pay="onPayOrder"
        @confirm="onConfirmOrder"
      />

      <!-- 加载更多 -->
      <LoadingMore v-if="orders.length > 0" :status="loadMoreStatus" @retry="onLoadMore" />

      <!-- 底部留白 -->
      <view style="height: 20rpx;" />
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

// ==================== 导入依赖 ====================
import OrderCard from '@/components/business/OrderCard.vue'
import EmptyState from '@/components/basics/EmptyState.vue'
import LoadingMore from '@/components/basics/LoadingMore.vue'
import { getOrderList, cancelOrder } from '@/api/order'

// ==================== 状态 Tab 配置 ====================
const statusTabs = [
  { label: '全部', value: '' },
  { label: '待付款', value: 'pending' },
  { label: '待发货', value: 'paid' },
  { label: '已完成', value: 'completed' }
]

// ==================== 响应式数据 ====================
const activeTab = ref('')
const orders = ref([])
const loading = ref(true)
const currentPage = ref(1)
const hasMore = ref(true)
const loadMoreStatus = ref('')
const refresherTriggered = ref(false)
const loadError = ref(false) // 初始加载是否失败（用于区分"暂无数据"和"加载失败"）

// ==================== 页面生命周期 ====================

onLoad(() => {
  loadOrders(true)
})

// ==================== 数据加载 ====================

const loadOrders = async (isRefresh = false) => {
  if (loadMoreStatus.value === 'loading') return
  if (!isRefresh && !hasMore.value) return

  loadMoreStatus.value = 'loading'
  if (isRefresh) {
    currentPage.value = 1
    hasMore.value = true
	loadError.value = false  // 清除初始加载错误标记
  }

  try {
    const params = { page: currentPage.value, size: 10 }
    if (activeTab.value) params.status = activeTab.value

    const res = await getOrderList(params)
    const records = res.records || []

    if (isRefresh) {
      orders.value = records
    } else {
      orders.value = [...orders.value, ...records]
    }

    hasMore.value = orders.value.length < (res.total || 0)
    loadMoreStatus.value = hasMore.value ? '' : 'noMore'
    currentPage.value++

	} catch (error) {
	  console.error('[Order] 加载订单列表失败:', error)
	  // 区分初始加载失败和分页加载失败
	  if (isRefresh) {
		loadError.value = true          // 初始加载失败：标记错误状态，让 EmptyState 显示重试按钮
	  } else {
		loadMoreStatus.value = 'error'  // 分页加载失败：显示"加载失败，点击重试"
	  }
	}finally {
    loading.value = false
	}
}

// ==================== 事件处理 ====================

const onTabChange = (value) => {
  if (activeTab.value === value) return
  activeTab.value = value
  loadError.value = false  // 切换 Tab 时清除错误状态
  loadOrders(true)
}

const onRefresh = () => {
  refresherTriggered.value = true
  loadOrders(true).finally(() => {
    refresherTriggered.value = false
  })
}

const onLoadMore = () => loadOrders(false)

/**
 * 订单卡片点击 — 跳转到订单详情页
 * 携带订单 ID 作为 URL 参数
 */
const onOrderClick = (order) => {
  uni.navigateTo({
    url: `/pages/order/detail?id=${order.id}`
  })
}

const onCancelOrder = (order) => {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消该订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await cancelOrder(order.id)
          uni.showToast({ title: '已取消', icon: 'success' })
          loadOrders(true)
        } catch (error) {
          uni.showToast({ title: '取消失败', icon: 'none' })
        }
      }
    }
  })
}

/**
 * 去支付 — 跳转到订单详情页执行支付
 * 订单详情页中有完整的支付流程（预支付 → 调起支付 → 结果页）
 */
const onPayOrder = (order) => {
  uni.navigateTo({
    url: `/pages/order/detail?id=${order.id}`
  })
}

/**
 * 确认收货 — 弹窗确认后更新订单状态
 * 用户确认后更新本地状态为"已完成"
 */
const onConfirmOrder = (order) => {
  uni.showModal({
    title: '确认收货',
    content: '确定已收到商品吗？确认后无法退款。',
    success: (res) => {
      if (res.confirm) {
        // 更新本地订单状态为已完成
        const target = orders.value.find(o => o.id === order.id)
        if (target) {
          target.status = 'completed'
        }
        uni.showToast({ title: '已确认收货', icon: 'success' })
      }
    }
  })
}

const onGoHome = () => {
  uni.switchTab({ url: '/pages/index/index' })
}
</script>

<style lang="scss" scoped>
.order-list-page {
  height: 100vh;
  background: #F2F2F7;
  display: flex;
  flex-direction: column;
}

// ==================== Tab 标签栏 ====================
.order-list-page__tabs {
  display: flex;
  background: #ffffff;
  border-bottom: 1rpx solid #E5E5EA;
  flex-shrink: 0;
}

.order-list-page__tab {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 28rpx;
  color: #8E8E93;
  position: relative;
  transition: all 0.2s;

  &--active {
    color: #007AFF;
    font-weight: 600;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 48rpx;
      height: 4rpx;
      background: #007AFF;
      border-radius: 2rpx;
    }
  }
}

// ==================== 滚动区域 ====================
.order-list-page__scroll {
  flex: 1;
  padding-top: 16rpx;
}
</style>
