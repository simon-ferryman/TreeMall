<!--
  pages/index/index.vue — 首页（TabBar 页面）

  【设计思想】
  首页是用户进入小程序的第一个页面，需要快速展示核心内容并引导用户操作。
  基于 Figma 设计稿，首页包含以下模块：
  1. 顶部搜索栏（搜索入口）
  2. Banner 轮播图（BannerSwiper 组件）
  3. 分类快捷入口（5 图标 CategoryGrid）
  4. 次级 Banner（促销活动入口）
  5. 热门推荐（ProductCard 组件，2×2 网格）
  6. AI推荐（ProductCard 组件，2×2 网格）
  7. FAB 悬浮客服按钮

  【数据来源】
  - Banner 列表：appStore.banners（App.vue onLaunch 预加载）
  - 分类列表：appStore.categories
  - 热门商品/AI推荐商品：onLoad 时调用 getProductList 获取

  【Figma 设计稿参考】
  - 首页顶部大型 Banner，16:9 比例
  - 5 个图标分类宫格
  - 次级 Banner 促销区域
  - 2×2 热门推荐 + AI推荐商品网格
  - 右下角 FAB 按钮
-->
<template>
  <view class="index-page">
    <!-- ==================== 顶部搜索栏 ==================== -->
    <!-- 搜索栏作为入口，点击跳转到搜索页 -->
    <view class="index-page__search-bar" @tap="onSearchTap">
      <u-icon name="search" :size="32" color="#8E8E93" />
      <text class="index-page__search-placeholder">搜索商品</text>
    </view>

    <!-- ==================== 可滚动内容区域 ==================== -->
    <scroll-view
      class="index-page__scroll"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refresherTriggered"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
    >
      <!-- ===== 1. Banner 轮播图 ===== -->
      <BannerSwiper
        :list="banners"
        :height="340"
        @click="onBannerClick"
      />

      <!-- ===== 2. 分类快捷入口 ===== -->
      <CategoryGrid
        :list="categories"
        :columns="5"
        @click="onCategoryClick"
      />

      <!-- ===== 3. 次级 Banner ===== -->
      <!-- 促销活动入口，点击跳转到活动页 -->
<!--      <view class="index-page__sub-banner" @tap="onSubBannerTap">
        <image
          class="index-page__sub-banner-image"
          src="/static/images/banner/bannerTest.jpg"
          mode="aspectFill"
        />
      </view> -->
	  <!-- 修改后的促销入口 -->
	  <view class="index-page__sub-banner" @tap="onSubBannerTap">
	    <image
	      class="index-page__sub-banner-image"
	      :src="getImageUrl('/images/banner/bannerTest.jpg')"
	      mode="aspectFill"
	    />
	  </view>

      <!-- ===== 4. 热门推荐 ===== -->
      <view class="index-page__section">
        <view class="index-page__section-header">
          <text class="index-page__section-title">热门推荐</text>
          <view class="index-page__section-more" @tap="onMoreTap">
            <text>更多</text>
            <u-icon name="arrow-right" :size="24" color="#8E8E93" />
          </view>
        </view>
        <view class="index-page__product-grid">
          <ProductCard
            v-for="product in hotProducts"
            :key="product.id"
            :product="product"
            mode="compact"
            class="index-page__product-card"
            @click="onProductClick"
          />
        </view>
      </view>

      <!-- ===== 5. AI推荐 ===== -->
      <view class="index-page__section">
        <view class="index-page__section-header">
          <view class="index-page__section-title-row">
            <text class="index-page__section-title">AI推荐</text>
            <view class="index-page__ai-badge">
              <u-icon name="star" :size="22" color="#007AFF" />
              <text>AI</text>
            </view>
          </view>
          <view class="index-page__section-more" @tap="onAiMoreTap">
            <text>更多</text>
            <u-icon name="arrow-right" :size="24" color="#8E8E93" />
          </view>
        </view>
        <view class="index-page__product-grid">
          <ProductCard
            v-for="product in aiProducts"
            :key="'ai-' + product.id"
            :product="product"
            mode="compact"
            class="index-page__product-card"
            @click="onProductClick"
          />
        </view>
      </view>

      <LoadingMore :status="loadMoreStatus" @retry="onLoadMore" />
    </scroll-view>

    <!-- ==================== FAB 悬浮客服按钮 ==================== -->
    <view class="index-page__fab" @tap="onFabTap">
      <u-icon name="kefu-ermai" :size="44" color="#ffffff" />
    </view>
  </view>
</template>

<script setup>
/**
 * 【页面级别说明】
 * 首页是 TabBar 页面，生命周期：
 * - onLoad：首次进入时加载热门商品
 * - onShow：从其他 Tab 切换回来时刷新数据
 */
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getImageUrl } from '@/utils/image-url'
// ==================== 导入依赖 ====================
import BannerSwiper from '@/components/business/BannerSwiper.vue'
import CategoryGrid from '@/components/business/CategoryGrid.vue'
import ProductCard from '@/components/business/ProductCard.vue'
import LoadingMore from '@/components/basics/LoadingMore.vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { getProductList } from '@/api/product'
import { navTo } from '@/utils/navigator'

// ==================== Store 实例 ====================
// 获取全局 Store 实例，用于读取预加载的 Banner 和分类数据
const appStore = useAppStore()
const userStore = useUserStore()

// ==================== 响应式数据 ====================
const hotProducts = ref([])           // 热门商品列表
const aiProducts = ref([])            // AI推荐商品列表
const currentPage = ref(1)            // 当前页码
const pageSize = ref(10)              // 每页数量
const hasMore = ref(true)             // 是否还有更多数据
const loadMoreStatus = ref('')        // 加载更多状态：'' | 'loading' | 'noMore' | 'error'
const refresherTriggered = ref(false) // 下拉刷新触发状态

// ==================== 计算属性 ====================

/**
 * Banner 列表
 * 从 appStore 获取（App.vue onLaunch 已预加载）
 * 如果预加载失败，返回空数组避免报错
 */
const banners = computed(() => appStore.banners || [])

/**
 * 分类列表
 * 取前 5 个一级分类展示在首页分类宫格中
 */
const categories = computed(() => (appStore.categories || []).slice(0, 5))

// ==================== 页面生命周期 ====================

/**
 * onLoad：首次加载
 * 并行加载热门商品和 AI 推荐商品
 */
onLoad(() => {
  loadHotProducts()
  loadAiProducts()
})

/**
 * onShow：每次显示页面时触发
 * 页面首次加载时 onLoad 已经加载了数据，不需要重复加载
 */
onShow(() => {
  // 可在此处添加需要刷新的逻辑
})

// ==================== 数据加载 ====================

/**
 * 加载热门商品
 * 按销量降序排序，支持分页（上拉加载更多）
 *
 * @param {boolean} isRefresh - 是否刷新（重置分页）
 */
const loadHotProducts = async (isRefresh = false) => {
  // 防止重复加载：如果正在加载中或没有更多数据则跳过
  if (loadMoreStatus.value === 'loading') return
  if (!isRefresh && !hasMore.value) return

  loadMoreStatus.value = 'loading'

  // 刷新时重置分页参数
  if (isRefresh) {
    currentPage.value = 1
    hasMore.value = true
  }

  try {
    // 调用后端商品列表接口，按销量降序排序
    const res = await getProductList({
      page: currentPage.value,
      pageSize: pageSize.value,       // 注意：后端参数名是 pageSize
      sortBy: 'salesCount',
      sortOrder: 'desc'
    })

    const records = res.records || []
    const total = res.total || 0

    // 刷新时替换数据，否则追加数据
    if (isRefresh) {
      hotProducts.value = records
    } else {
      hotProducts.value = [...hotProducts.value, ...records]
    }

    // 判断是否还有更多数据
    hasMore.value = hotProducts.value.length < total
    loadMoreStatus.value = hasMore.value ? '' : 'noMore'
    currentPage.value++

  } catch (error) {
    console.error('[Index] 加载热门商品失败:', error)
    loadMoreStatus.value = 'error'

    // 首次加载失败时提示用户
    if (hotProducts.value.length === 0) {
      uni.showToast({ title: '加载失败，下拉刷新重试', icon: 'none', duration: 2000 })
    }
  }
}

/**
 * 加载 AI 推荐商品
 * 区别于热门商品，使用不同的排序策略（按评分排序）
 */
const loadAiProducts = async () => {
  try {
    const res = await getProductList({
      page: 1,
      pageSize: 4,
      sortBy: 'rating',
      sortOrder: 'desc'
    })
    aiProducts.value = res.records || []
  } catch (error) {
    console.error('[Index] 加载AI推荐商品失败:', error)
  }
}

// ==================== 事件处理 ====================

/** 下拉刷新：刷新 Banner、分类和商品数据 */
const onRefresh = async () => {
  refresherTriggered.value = true
  await Promise.all([
    appStore.fetchBanners(),
    appStore.fetchCategories()
  ])
  await Promise.all([
    loadHotProducts(true),
    loadAiProducts()
  ])
  refresherTriggered.value = false
}

/** 上拉加载更多 */
const onLoadMore = () => loadHotProducts(false)

/** 搜索栏点击：跳转到搜索页 */
const onSearchTap = () => navTo.search()

/** Banner 点击：跳转到 Banner 配置的链接 */
const onBannerClick = (item) => {
  if (item.linkUrl) uni.navigateTo({ url: item.linkUrl })
}

/** 分类点击：跳转到分类页并选中对应分类 */
const onCategoryClick = (item) => {
  uni.switchTab({ url: '/pages/category/category' })
  appStore.setActiveCategory(item.id)
}

/** 次级 Banner 点击 */
const onSubBannerTap = () => uni.switchTab({ url: '/pages/category/category' })

/** 商品卡片点击：跳转到商品详情页 */
const onProductClick = (product) => {
  uni.navigateTo({ url: `/pages/goods/detail?id=${product.id}` })
}

/** "更多"按钮点击 */
const onMoreTap = () => uni.switchTab({ url: '/pages/category/category' })

/** AI推荐 "更多" 按钮点击 */
const onAiMoreTap = () => uni.navigateTo({ url: '/pages/category/category?tab=ai' })

/** FAB 客服按钮点击 */
const onFabTap = () => {
  uni.showToast({ title: '客服功能开发中', icon: 'none', duration: 1500 })
}
</script>

<style lang="scss" scoped>
// ==================== 页面容器 ====================
.index-page {
  height: 100vh;
  background: #F2F2F7;
  display: flex;
  flex-direction: column;
  position: relative;
}

// ==================== 顶部搜索栏 ====================
.index-page__search-bar {
  display: flex;
  align-items: center;
  gap: 12rpx;
  height: 72rpx;
  background: #ffffff;
  border-radius: 36rpx;             // 圆角搜索栏
  margin: 16rpx 24rpx;
  padding: 0 28rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  flex-shrink: 0;                   // 不压缩，保持固定高度
}

.index-page__search-placeholder {
  font-size: 26rpx;
  color: #9ca3af;
  flex: 1;
}

// ==================== 可滚动区域 ====================
.index-page__scroll {
  flex: 1;                          // 占据剩余空间
}

// ==================== 次级 Banner ====================
.index-page__sub-banner {
  margin: 20rpx 16rpx;
  border-radius: 12rpx;
  overflow: hidden;
  height: 300rpx;
}

.index-page__sub-banner-image {
  width: 100%;
  height: 100%;
  display: block;
  background: #E5E5EA;              // 灰色占位背景
}

// ==================== 通用区块 ====================
.index-page__section {
  padding: 0 16rpx 32rpx;
}

// 区块标题行
.index-page__section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0 16rpx;
}

// 区块标题
.index-page__section-title {
  font-size: 45rpx;
  font-weight: 700;
  color: #373739;
}

// 标题行（含 AI 徽章）
.index-page__section-title-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

// AI 标签徽章
.index-page__ai-badge {
  display: flex;
  align-items: center;
  gap: 4rpx;
  background: #E5F1FF;
  border-radius: 8rpx;
  padding: 4rpx 12rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #007AFF;
}

// "更多"按钮
.index-page__section-more {
  display: flex;
  align-items: center;
  gap: 4rpx;
  font-size: 40rpx;
  font-weight:700;
  color: #373739;
}

// ==================== 商品网格 ====================
.index-page__product-grid {
  display: flex;
  flex-wrap: wrap;                  // 允许换行
  gap: 16rpx;                       // 卡片间距
}

// 商品卡片（每行 2 个，宽度 = (100% - 间距) / 2）
.index-page__product-card {
  width: calc(50% - 8rpx);          // 50% 减去一半间距
}

// ==================== FAB 悬浮按钮 ====================
.index-page__fab {
  position: fixed;                  // 固定定位
  right: 32rpx;
  bottom: 160rpx;                   // 在 TabBar 上方
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;               // 圆形
  background: #007AFF;              // 主题蓝
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;                      // 在内容之上，TabBar 之下

  &:active { transform: scale(0.92); }
}
</style>
