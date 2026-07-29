<!--
  pages/favorites/favorites.vue -- 收藏页

  【设计思想】
  收藏页展示用户收藏的商品列表，使用 ProductCard 组件展示：
  1. 商品列表：2 列网格布局，每项显示商品卡片
  2. 空状态：无收藏时显示空状态提示，引导用户去浏览商品
  3. 支持上拉加载更多

  【Figma 设计令牌】
  主色 #007AFF | 文字 #1D1D1F | 次要文字 #8E8E93 | 背景 #F2F2F7 | 白色 #FFFFFF
  分隔线 #E5E5EA | 浅蓝 #E5F1FF | 设计宽度 375px | rpx=px*2

  【使用场景】
  - 个人中心页点击"我的收藏"入口
  - 商品详情页点击收藏按钮
-->
<template>
  <view class="favorites-page">
    <!-- ==================== 收藏列表 ==================== -->
    <scroll-view
      class="favorites-page__scroll"
      scroll-y
      @scrolltolower="onLoadMore"
    >
      <!-- 加载中状态 -->
      <view v-if="loading" class="favorites-page__loading">
        <u-loading-icon :size="36" color="#007AFF" mode="circle" />
        <text class="favorites-page__loading-text">加载中...</text>
      </view>

      <!-- 空状态：无收藏商品 -->
      <EmptyState
        v-else-if="favoriteList.length === 0 && !loading"
        icon="heart"
        text="还没有收藏商品"
        button-text="去逛逛"
        @action="onGoHome"
      />

      <!-- 收藏商品列表：2 列网格布局 -->
      <view v-else class="favorites-page__grid">
        <view
          v-for="product in favoriteList"
          :key="product.id"
          class="favorites-page__grid-item"
        >
          <ProductCard
            :product="product"
            mode="compact"
            @click="onProductClick"
          />
        </view>
      </view>

      <!-- 加载更多 -->
      <LoadingMore :status="loadMoreStatus" @retry="onLoadMore" />

      <!-- 底部留白 -->
      <view style="height: 40rpx;" />
    </scroll-view>
  </view>
</template>

<script setup>
// ==================== 导入依赖 ====================
import { ref } from 'vue'                                // Vue 3 响应式 API
import { onLoad } from '@dcloudio/uni-app'               // uni-app 页面生命周期
import ProductCard from '@/components/business/ProductCard.vue'   // 商品卡片组件
import EmptyState from '@/components/basics/EmptyState.vue'     // 空状态组件
import LoadingMore from '@/components/basics/LoadingMore.vue'   // 加载更多组件
import { getProductList } from '@/api/product'           // 商品列表 API（用于获取收藏商品）

// ==================== 响应式数据 ====================

// 收藏商品列表
const favoriteList = ref([])
// 是否正在加载
const loading = ref(true)
// 当前页码
const currentPage = ref(1)
// 是否还有更多数据
const hasMore = ref(true)
// 加载更多状态
const loadMoreStatus = ref('')

// ==================== 页面生命周期 ====================

/**
 * 页面加载时获取收藏商品列表
 * 注意：实际项目中应使用专门的收藏 API（如 getFavoriteList）
 * 此处使用商品列表 API 进行模拟，实际应替换为 @/api/favorites 或类似接口
 */
onLoad(() => {
  // 加载收藏列表
  loadFavorites(true)
})

// ==================== 数据加载 ====================

/**
 * 加载收藏商品列表
 * 使用 localStorage 存储收藏的商品 ID，再通过商品列表 API 获取详情
 *
 * @param {boolean} isRefresh - 是否为下拉刷新（true 时重置分页）
 */
const loadFavorites = async (isRefresh = false) => {
  // 防重复请求
  if (loadMoreStatus.value === 'loading') return
  // 加载更多时如果没有更多数据，不再请求
  if (!isRefresh && !hasMore.value) return

  loadMoreStatus.value = 'loading'

  if (isRefresh) {
    currentPage.value = 1
    hasMore.value = true
  }

  try {
    // 从本地 Storage 获取收藏的商品 ID 列表
    const favIds = getFavoriteIds()

    // 如果没有收藏，直接返回空列表
    if (favIds.length === 0) {
      favoriteList.value = []
      hasMore.value = false
      loadMoreStatus.value = ''
      loading.value = false
      return
    }

    // 调用商品列表 API 获取商品详情
    // 注意：实际项目中应使用专门的收藏 API，支持分页
    const res = await getProductList({
      page: currentPage.value,
      pageSize: 10
    })

    const records = (res.records || []).filter(item => favIds.includes(item.id))

    if (isRefresh) {
      favoriteList.value = records
    } else {
      favoriteList.value = [...favoriteList.value, ...records]
    }

    // 判断是否还有更多数据
    hasMore.value = favoriteList.value.length < favIds.length
    loadMoreStatus.value = hasMore.value ? '' : 'noMore'
    currentPage.value++

  } catch (error) {
    console.error('[Favorites] 加载收藏列表失败:', error)
    loadMoreStatus.value = 'error'
  } finally {
    loading.value = false
  }
}

// ==================== 收藏管理 ====================

/**
 * 从本地 Storage 获取收藏的商品 ID 列表
 * 使用 localStorage 作为简单的收藏数据存储
 *
 * @returns {number[]} 收藏的商品 ID 数组
 */
const getFavoriteIds = () => {
  try {
    const favStr = uni.getStorageSync('favorite_ids')
    return favStr ? JSON.parse(favStr) : []
  } catch (e) {
    console.error('[Favorites] 读取收藏数据失败:', e)
    return []
  }
}

// ==================== 事件处理 ====================

/**
 * 上拉加载更多
 */
const onLoadMore = () => {
  loadFavorites(false)
}

/**
 * 商品卡片点击：跳转到商品详情页
 *
 * @param {Object} product - 商品数据
 */
const onProductClick = (product) => {
  uni.navigateTo({
    url: `/pages/goods/detail?id=${product.id}`
  })
}

/**
 * 去逛逛：跳转到首页
 */
const onGoHome = () => {
  uni.switchTab({
    url: '/pages/index/index'
  })
}
</script>

<style lang="scss" scoped>
// ==================== 页面容器 ====================
.favorites-page {
  min-height: 100vh;                          // 最小高度撑满屏幕
  background: $bg-color;                      // 页面背景色：#F2F2F7
  display: flex;
  flex-direction: column;
}

// ==================== 滚动区域 ====================
.favorites-page__scroll {
  flex: 1;                                    // 填充剩余空间
  padding: 0 16rpx;                           // 左右内边距
}

// ==================== 加载中状态 ====================
.favorites-page__loading {
  @include flex-center;                       // 使用混入：水平垂直居中
  flex-direction: column;
  padding: 200rpx 0;                          // 上下留白
}

.favorites-page__loading-text {
  font-size: $font-sm;                        // 24rpx
  color: $text-muted;                         // 次要文字色：#8E8E93
  margin-top: $spacing-md;                    // 顶部间距：24rpx
}

// ==================== 商品网格布局 ====================
// 2 列网格
.favorites-page__grid {
  display: flex;
  flex-wrap: wrap;                            // 自动换行
  gap: 16rpx;                                 // 卡片间距
  padding-top: 16rpx;                         // 顶部间距
}

// 网格项：每行 2 列，等宽
.favorites-page__grid-item {
  width: calc(50% - 8rpx);                    // 两列等宽，减去 gap 的一半
}
</style>