<!--
  pages/category/category.vue — 分类页

  【设计思想】
  分类页采用经典的"左右联动"布局，左侧一级分类，右侧二级分类+商品列表。
  这是电商小程序的标准布局模式，适合分类层级较深（两级）的场景。

  【Figma 设计稿参考】
  左侧：6 个一级分类垂直菜单（选中态：蓝色文字 + 左侧蓝色竖线）
  右侧：搜索栏 + 子分类 + 排序栏 + 商品 2 列网格

  【设计令牌】
  主色：#007AFF，文字：#1D1D1F，次要文字：#8E8E93
  背景：#F2F2F7，白色：#FFFFFF，分隔线：#E5E5EA
-->
<template>
  <view class="category-page">
    <!-- ==================== 左侧：一级分类菜单 ==================== -->
    <scroll-view class="category-page__sidebar" scroll-y>
      <view
        v-for="cat in categories" :key="cat.id"
        class="category-page__sidebar-item"
        :class="{ 'category-page__sidebar-item--active': activeCategoryId === cat.id }"
        @tap="onCategoryChange(cat)"
      >
        <view v-if="activeCategoryId === cat.id" class="category-page__sidebar-indicator" />
        <text class="category-page__sidebar-name">{{ cat.name }}</text>
      </view>
    </scroll-view>

    <!-- ==================== 右侧：内容区域 ==================== -->
    <scroll-view class="category-page__content" scroll-y @scrolltolower="onLoadMore">
      <view class="category-page__search-wrapper" @tap="onSearchTap">
        <SearchBar v-model="searchKeyword" placeholder="搜索商品" :showCancel="false" :autofocus="false" />
      </view>
      <view class="category-page__sub-categories" v-if="subCategories.length > 0">
        <view
          v-for="sub in subCategories" :key="sub.id"
          class="category-page__sub-category"
          :class="{ 'category-page__sub-category--active': activeSubCategoryId === sub.id }"
          @tap="onSubCategoryChange(sub)"
        >{{ sub.name }}</view>
      </view>
      <view class="category-page__sort-bar">
        <view v-for="(sortItem, index) in sortOptions" :key="index"
          class="category-page__sort-item"
          :class="{ 'category-page__sort-item--active': activeSortIndex === index }"
          @tap="onSortChange(index)"
        >
          <text class="category-page__sort-label">{{ sortItem.label }}</text>
        </view>
      </view>
      <view class="category-page__product-grid">
        <ProductCard v-for="product in products" :key="product.id" :product="product" mode="list" class="category-page__product-card" @click="onProductClick" />
      </view>
      <LoadingMore :status="loadMoreStatus" @retry="onLoadMore" />
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import ProductCard from '@/components/business/ProductCard.vue'
import SearchBar from '@/components/business/SearchBar.vue'
import LoadingMore from '@/components/basics/LoadingMore.vue'
import { useAppStore } from '@/stores/app'
import { getProductList } from '@/api/product'
import { navTo } from '@/utils/navigator'

const appStore = useAppStore()
const activeCategoryId = ref(null)
const activeSubCategoryId = ref(null)
const products = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)
const loadMoreStatus = ref('')
const searchKeyword = ref('')
const sortOptions = [
  { label: '综合', field: '', order: '' },
  { label: '价格升序', field: 'price', order: 'asc' },
  { label: '价格降序', field: 'price', order: 'desc' },
  { label: '销量', field: 'sales', order: 'desc' }
]
const activeSortIndex = ref(0)
const categories = computed(() => appStore.categories || [])
const subCategories = computed(() => {
  const active = categories.value.find(c => c.id === activeCategoryId.value)
  return active?.children || []
})

onLoad(() => {
  const preselectedId = appStore.activeCategoryId
  if (preselectedId && categories.value.some(c => c.id === preselectedId)) {
    activeCategoryId.value = preselectedId
    appStore.setActiveCategory(null)
  } else if (categories.value.length > 0) {
    activeCategoryId.value = categories.value[0].id
  }
  if (activeCategoryId.value) loadProducts(true)
})

const loadProducts = async (isRefresh = false) => {
  if (loadMoreStatus.value === 'loading') return
  if (!isRefresh && !hasMore.value) return
  loadMoreStatus.value = 'loading'
  if (isRefresh) { currentPage.value = 1; hasMore.value = true }
  try {
    const params = { page: currentPage.value, pageSize: pageSize.value }
    if (activeSubCategoryId.value) params.categoryId = activeSubCategoryId.value
    else if (activeCategoryId.value) params.categoryId = activeCategoryId.value
    const currentSort = sortOptions[activeSortIndex.value]
    if (currentSort.field) { params.sortField = currentSort.field; params.sortOrder = currentSort.order }
    const res = await getProductList(params)
    const records = res.records || []
    if (isRefresh) products.value = records
    else products.value = [...products.value, ...records]
    hasMore.value = products.value.length < (res.total || 0)
    loadMoreStatus.value = hasMore.value ? '' : 'noMore'
    currentPage.value++
  } catch (error) {
    console.error('[Category] 加载商品失败:', error)
    loadMoreStatus.value = 'error'
  }
}

const onCategoryChange = (cat) => {
  if (activeCategoryId.value === cat.id) return
  activeCategoryId.value = cat.id
  activeSubCategoryId.value = null
  activeSortIndex.value = 0
  loadProducts(true)
}
const onSubCategoryChange = (sub) => {
  activeSubCategoryId.value = activeSubCategoryId.value === sub.id ? null : sub.id
  loadProducts(true)
}
const onSearchTap = () => navTo.search()
const onSortChange = (index) => {
  if (activeSortIndex.value === index) return
  activeSortIndex.value = index
  loadProducts(true)
}
const onProductClick = (product) => uni.navigateTo({ url: `/pages/goods/detail?id=${product.id}` })
const onLoadMore = () => loadProducts(false)
</script>

<style lang="scss" scoped>
.category-page { display: flex; height: 100vh; background: #F2F2F7; }
.category-page__sidebar { width: 180rpx; background: #F2F2F7; flex-shrink: 0; }
.category-page__sidebar-item { position: relative; height: 88rpx; display: flex; align-items: center; justify-content: center; font-size: 26rpx; color: #8E8E93; transition: all 0.2s; &--active { background: #FFFFFF; color: #007AFF; font-weight: 600; } }
.category-page__sidebar-indicator { position: absolute; left: 0; top: 50%; transform: translateY(-50%); width: 6rpx; height: 40rpx; background: #007AFF; border-radius: 0 3rpx 3rpx 0; }
.category-page__sidebar-name { line-height: 1.4; text-align: center; }
.category-page__content { flex: 1; background: #FFFFFF; }
.category-page__search-wrapper { }
.category-page__sub-categories { display: flex; flex-wrap: wrap; gap: 12rpx; padding: 0 16rpx 16rpx; }
.category-page__sub-category { font-size: 24rpx; color: #8E8E93; background: #F2F2F7; padding: 8rpx 20rpx; border-radius: 24rpx; line-height: 1.4; &--active { background: #E5F1FF; color: #007AFF; font-weight: 500; } }
.category-page__sort-bar { display: flex; align-items: center; justify-content: space-around; padding: 0 16rpx 16rpx; border-bottom: 2rpx solid #E5E5EA; }
.category-page__sort-item { position: relative; display: flex; align-items: center; justify-content: center; padding: 12rpx 0; }
.category-page__sort-label { font-size: 26rpx; color: #8E8E93; line-height: 1.4; transition: color 0.2s; }
.category-page__sort-item--active .category-page__sort-label { color: #007AFF; font-weight: 600; }
.category-page__product-grid { display: flex; flex-wrap: wrap; gap: 16rpx; padding: 0 16rpx 32rpx; }
.category-page__product-card { width: calc(50% - 8rpx); }
</style>