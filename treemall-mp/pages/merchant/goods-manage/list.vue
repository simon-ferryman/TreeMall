<!--
  pages/merchant/goods-manage/list.vue — 商户商品管理页（分包）

  【设计思想】
  商户端商品管理，支持查看、上下架、编辑商品。
  只有商户角色的用户才能访问此页面。
-->
<template>
  <view class="merchant-goods-page">
    <!-- 顶部操作栏 -->
    <view class="merchant-goods-page__header">
      <text class="merchant-goods-page__title">商品管理</text>
      <view class="merchant-goods-page__add-btn" @tap="onAddGoods">
        <u-icon name="plus" :size="28" color="#ffffff" />
        <text>新增</text>
      </view>
    </view>

    <scroll-view class="merchant-goods-page__scroll" scroll-y @scrolltolower="onLoadMore">
      <EmptyState
        v-if="goodsList.length === 0 && !loading"
        icon="bag"
        text="暂无商品"
        button-text="新增商品"
        @action="onAddGoods"
      />

      <view v-else class="merchant-goods-page__list">
        <view
          v-for="goods in goodsList"
          :key="goods.id"
          class="merchant-goods-page__card"
        >
          <image
            class="merchant-goods-page__image"
           :src="getImageUrl(goods.mainImage) || '/static/images/product-default.png'"
            mode="aspectFill"
          />
          <view class="merchant-goods-page__info">
            <text class="merchant-goods-page__name">{{ goods.name }}</text>
            <text class="merchant-goods-page__price">¥{{ formatPrice(goods.price) }}</text>
            <text class="merchant-goods-page__stock">库存：{{ goods.stock || 0 }}</text>
          </view>
          <view class="merchant-goods-page__actions">
            <view
              class="merchant-goods-page__action-btn"
              :class="goods.status === 1 ? 'merchant-goods-page__action-btn--down' : 'merchant-goods-page__action-btn--up'"
              @tap="onToggleStatus(goods)"
            >
              {{ goods.status === 1 ? '下架' : '上架' }}
            </view>
            <view
              class="merchant-goods-page__action-btn merchant-goods-page__action-btn--edit"
              @tap="onEditGoods(goods)"
            >
              编辑
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

// ==================== 导入依赖 ====================
import EmptyState from '@/components/basics/EmptyState.vue'
import LoadingMore from '@/components/basics/LoadingMore.vue'
import { getMerchantProductList, updateProductStatus } from '@/api/merchant'
import { getImageUrl } from '@/utils/image-url'

// ==================== 响应式数据 ====================
const goodsList = ref([])
const loading = ref(true)
const currentPage = ref(1)
const hasMore = ref(true)
const loadMoreStatus = ref('')

// ==================== 页面生命周期 ====================

onLoad(() => {
  loadGoodsList(true)
})

// ==================== 数据加载 ====================

const loadGoodsList = async (isRefresh = false) => {
  if (loadMoreStatus.value === 'loading') return
  if (!isRefresh && !hasMore.value) return

  loadMoreStatus.value = 'loading'
  if (isRefresh) {
    currentPage.value = 1
    hasMore.value = true
  }

  try {
    const res = await getMerchantProductList({
      page: currentPage.value,
      size: 10
    })
    const records = res.records || []

    if (isRefresh) {
      goodsList.value = records
    } else {
      goodsList.value = [...goodsList.value, ...records]
    }

    hasMore.value = goodsList.value.length < (res.total || 0)
    loadMoreStatus.value = hasMore.value ? '' : 'noMore'
    currentPage.value++

  } catch (error) {
    console.error('[Merchant] 加载商品列表失败:', error)
    loadMoreStatus.value = 'error'
  } finally {
    loading.value = false
  }
}

// ==================== 事件处理 ====================

const onAddGoods = () => {
  uni.navigateTo({ url: '/pages/merchant/goods-manage/edit' })
}

const onEditGoods = (goods) => {
  uni.navigateTo({ url: `/pages/merchant/goods-manage/edit?id=${goods.id}` })
}

const onToggleStatus = (goods) => {
  const newStatus = goods.status === 1 ? 0 : 1
  const actionText = newStatus === 0 ? '下架' : '上架'
  uni.showModal({
    title: '确认操作',
    content: `确定要${actionText}该商品吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await updateProductStatus(goods.id, newStatus)
          uni.showToast({ title: `${actionText}成功`, icon: 'success' })
          // 刷新列表
          loadGoodsList(true)
        } catch (error) {
          console.error('[Merchant] 状态更新失败:', error)
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

const onLoadMore = () => loadGoodsList(false)

const formatPrice = (price) => {
  return price != null ? Number(price).toFixed(2) : '0.00'
}
</script>

<style lang="scss" scoped>
.merchant-goods-page {
  height: 100vh;
  background: #F2F2F7;
  display: flex;
  flex-direction: column;
}

.merchant-goods-page__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 24rpx;
  background: #ffffff;
  border-bottom: 1rpx solid #E5E5EA;
}

.merchant-goods-page__title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1D1D1F;
}

.merchant-goods-page__add-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  background: #007AFF;
  color: #ffffff;
  font-size: 24rpx;
  padding: 10rpx 24rpx;
  border-radius: 24rpx;
}

.merchant-goods-page__scroll {
  flex: 1;
}

.merchant-goods-page__list {
  padding: 16rpx;
}

.merchant-goods-page__card {
  background: #ffffff;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
  display: flex;
  gap: 16rpx;
  box-shadow: 0 1rpx 4rpx rgba(0,0,0,0.04);
}

.merchant-goods-page__image {
  width: 140rpx;
  height: 140rpx;
  border-radius: 8rpx;
  background: #F2F2F7;
  flex-shrink: 0;
}

.merchant-goods-page__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.merchant-goods-page__name {
  font-size: 28rpx;
  color: #1D1D1F;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.merchant-goods-page__price {
  font-size: 28rpx;
  color: #007AFF;
  font-weight: 700;
}

.merchant-goods-page__stock {
  font-size: 22rpx;
  color: #8E8E93;
}

.merchant-goods-page__actions {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  flex-shrink: 0;
}

.merchant-goods-page__action-btn {
  font-size: 22rpx;
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  text-align: center;

  &--up {
    background: #f0fdf4;
    color: #16a34a;
  }

  &--down {
    background: #fef2f2;
    color: #ef4444;
  }

  &--edit {
    background: #F2F2F7;
    color: #8E8E93;
  }
}
</style>
