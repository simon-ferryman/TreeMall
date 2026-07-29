<!--
  pages/merchant/order-manage/list.vue — 商户订单管理页（分包）

  【设计思想】
  商户端查看和管理用户订单，支持确认发货等操作。
  只有商户角色的用户才能访问此页面。
-->
<template>
  <view class="merchant-order-page">
    <view class="merchant-order-page__header">
      <text class="merchant-order-page__title">订单管理</text>
    </view>

    <!-- 状态 Tab -->
    <view class="merchant-order-page__tabs">
      <view
        v-for="tab in statusTabs"
        :key="tab.value"
        class="merchant-order-page__tab"
        :class="{ 'merchant-order-page__tab--active': activeTab === tab.value }"
        @tap="onTabChange(tab.value)"
      >
        {{ tab.label }}
      </view>
    </view>

    <scroll-view class="merchant-order-page__scroll" scroll-y @scrolltolower="onLoadMore">
      <EmptyState
        v-if="orders.length === 0 && !loading"
        icon="order"
        text="暂无订单"
      />

      <view v-else class="merchant-order-page__list">
        <view
          v-for="order in orders"
          :key="order.orderId"
          class="merchant-order-page__card"
		  @tap="onOrderClick(order)"
        >
          <view class="merchant-order-page__card-header">
            <text class="merchant-order-page__order-no">订单号：{{ order.orderNo }}</text>
            <StatusBadge :status="order.status" />
          </view>

          <view class="merchant-order-page__goods">
            <image
              v-for="(item, idx) in (order.items || []).slice(0, 3)"
              :key="idx"
              class="merchant-order-page__goods-image"
              :src="getImageUrl(item.productImage) || defaultImage"
              mode="aspectFill"
            />
          </view>
		  <view class="merchant-order-page__goods-info" v-for="(item, idx) in (order.items || []).slice(0, 3)" :key="'info-' + idx">
			<text class="merchant-order-page__goods-name">{{ item.productName }}</text>
			<text class="merchant-order-page__goods-desc" v-if="item.description">{{ item.description }}</text>
			<text class="merchant-order-page__goods-specs" v-if="item.specs">规格：{{ item.specs }}</text>
		  </view>
          <view class="merchant-order-page__card-footer">
            <text class="merchant-order-page__amount">
              共{{ (order.items || []).length }}件 合计：¥{{ formatPrice(order.totalAmount) }}
            </text>
            <view
              v-if="order.status === 'paid'"
              class="merchant-order-page__ship-btn"
			  @tap="onShip(order)"
            >
              发货
            </view>
          </view>
        </view>
      </view>

      <LoadingMore :status="loadMoreStatus" @retry="onLoadMore" />
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getImageUrl } from '@/utils/image-url'
// ==================== 导入依赖 ====================
import StatusBadge from '@/components/business/StatusBadge.vue'
import EmptyState from '@/components/basics/EmptyState.vue'
import LoadingMore from '@/components/basics/LoadingMore.vue'
import { getMerchantOrderList} from '@/api/merchant'

// ==================== 状态 Tab 配置 ====================
const statusTabs = [
  { label: '全部', value: '' },
  { label: '待发货', value: 'paid' },
  { label: '已发货', value: 'shipped' },
  { label: '已完成', value: 'completed' }
]

// ==================== 响应式数据 ====================
const activeTab = ref('')
const orders = ref([])
const loading = ref(true)
const currentPage = ref(1)
const hasMore = ref(true)
const loadMoreStatus = ref('')
const defaultImage = '/static/images/product-default.png'
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
  }

  try {
    const params = { page: currentPage.value, size: 10 }
    if (activeTab.value) params.status = activeTab.value

    const res = await getMerchantOrderList(params)
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
    console.error('[MerchantOrder] 加载失败:', error)
    loadMoreStatus.value = 'error'
  } finally {
    loading.value = false
  }
}

// ==================== 事件处理 ====================

const onTabChange = (value) => {
  if (activeTab.value === value) return
  activeTab.value = value
  loadOrders(true)
}
const onShip = (order) => {
  uni.navigateTo({
    url: `/pages/merchant/order-manage/ship?id=${order.id}`
  })
}


const onLoadMore = () => loadOrders(false)
const onOrderClick = (order) => {
  uni.navigateTo({
    url: `/pages/merchant/order-manage/detail?id=${order.id}`
  })
}
const formatPrice = (price) => {
  return price != null ? Number(price).toFixed(2) : '0.00'
}
</script>

<style lang="scss" scoped>
.merchant-order-page {
  height: 100vh;
  background: #F2F2F7;
  display: flex;
  flex-direction: column;
}

.merchant-order-page__header {
  padding: 20rpx 24rpx;
  background: #ffffff;
  border-bottom: 1rpx solid #E5E5EA;
}

.merchant-order-page__title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1D1D1F;
}

.merchant-order-page__tabs {
  display: flex;
  background: #ffffff;
  flex-shrink: 0;
}

.merchant-order-page__tab {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  font-size: 26rpx;
  color: #8E8E93;
  position: relative;

  &--active {
    color: #007AFF;
    font-weight: 600;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 40rpx;
      height: 4rpx;
      background: #007AFF;
      border-radius: 2rpx;
    }
  }
}

.merchant-order-page__scroll {
  flex: 1;
  padding: 16rpx;
}

.merchant-order-page__card {
  background: #ffffff;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 1rpx 4rpx rgba(0,0,0,0.04);
}

.merchant-order-page__card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.merchant-order-page__order-no {
  font-size: 24rpx;
  color: #8E8E93;
}

.merchant-order-page__goods {
  display: flex;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.merchant-order-page__goods-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 8rpx;
  background: #F2F2F7;
}

.merchant-order-page__card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.merchant-order-page__amount {
  font-size: 24rpx;
  color: #8E8E93;
}

.merchant-order-page__ship-btn {
  background: #007AFF;
  color: #ffffff;
  font-size: 24rpx;
  padding: 8rpx 24rpx;
  border-radius: 24rpx;
}
.merchant-order-page__goods-info {
  margin-bottom: 12rpx;
  padding: 0 4rpx;
}
.merchant-order-page__goods-name {
  font-size: 26rpx;
  color: #1D1D1F;
  font-weight: 500;
  display: block;
  margin-bottom: 4rpx;
}
.merchant-order-page__goods-desc {
  font-size: 22rpx;
  color: #8E8E93;
  display: block;
  margin-bottom: 4rpx;
}
.merchant-order-page__goods-specs {
  font-size: 22rpx;
  color: #8E8E93;
  display: block;
}
</style>
