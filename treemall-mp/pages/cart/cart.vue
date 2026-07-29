<!--
  pages/cart/cart.vue — 购物车页

  【设计思想】
  购物车是用户整理待购商品的核心页面，所有操作由 cartStore 统一管理。
  页面通过 cartStore 的 getters 获取展示数据，通过 actions 执行操作。
  这种设计确保购物车数据在多个页面间保持一致（商品详情页加入购物车后，
  切换到购物车页能立即看到最新数据）。

  【Figma 设计稿参考】
  - CartItem 商品行（缩略图 + 名称 + 规格 + 数量调节 + 价格）
  - 底部 PriceSummary 结算栏（全选 + 合计 + 去结算）
-->
<template>
  <view class="cart-page">
    <!-- ==================== 空状态 ==================== -->
    <EmptyState
      v-if="!cartStore.items || cartStore.items.length === 0"
      icon="shopping-cart"
      text="购物车还是空的"
      button-text="去首页逛逛"
      @action="onGoHome"
    />

    <!-- ==================== 购物车列表 ==================== -->
    <template v-else>
      <scroll-view
        class="cart-page__scroll"
        scroll-y
        @scrolltolower="onLoadMore"
      >
        <!-- 商品列表 -->
        <CartItem
          v-for="item in cartStore.items"
          :key="item.id"
          :item="item"
          @toggle-check="onToggleCheck"
          @increase="onIncrease"
          @decrease="onDecrease"
          @delete="onDelete"
          @click="onItemClick"
        />

        <!-- ==================== AI 推荐区域 ==================== -->
        <!-- 仅在购物车有商品且推荐数据加载成功时显示 -->
        <view
          v-if="showRecommend && recommendProducts.length > 0"
          class="cart-page__recommend"
        >
          <!-- 推荐标题行 -->
          <view class="cart-page__recommend-header">
            <!-- 主标题：为你推荐 -->
            <text class="cart-page__recommend-title">为你推荐</text>
            <!-- 副标题：AI 智能推荐，使用蓝色标签样式 -->
            <text class="cart-page__recommend-subtitle">AI 智能推荐</text>
          </view>

          <!-- 推荐商品 2 列网格 -->
          <view class="cart-page__recommend-grid">
            <!-- 使用 ProductCard compact 模式，紧凑展示推荐商品 -->
            <ProductCard
              v-for="product in recommendProducts"
              :key="product.id"
              :product="product"
              mode="compact"
              @click="onRecommendClick"
            />
          </view>
        </view>

        <!-- 底部留白（防止被结算栏遮挡） -->
        <view style="height: 120rpx;" />
      </scroll-view>

      <!-- ==================== 底部结算栏 ==================== -->
      <PriceSummary
        :total="cartStore.checkedTotal"
        :checked-count="cartStore.checkedItems.length"
        :is-all-checked="cartStore.isAllChecked"
        @toggle-all="onToggleAll"
        @settle="onSettle"
      />
    </template>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'

// ==================== 导入依赖 ====================
import CartItem from '@/components/business/CartItem.vue'
import PriceSummary from '@/components/business/PriceSummary.vue'
import ProductCard from '@/components/business/ProductCard.vue'
import EmptyState from '@/components/basics/EmptyState.vue'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { getProductList } from '@/api/product'

// ==================== Store 实例 ====================
const cartStore = useCartStore()
const userStore = useUserStore()

// ==================== 响应式数据 ====================

// 推荐商品列表（AI 智能推荐，最多 4 个）
const recommendProducts = ref([])

// ==================== 计算属性 ====================

// 是否显示推荐区域：仅在购物车有商品时显示
const showRecommend = computed(() => cartStore.items && cartStore.items.length > 0)

// ==================== 页面生命周期 ====================

/**
 * onShow：每次显示购物车页时刷新数据
 * 确保从其他页面加入购物车后返回时数据是最新的
 */
onShow(async () => {
  // 未登录：跳转登录页
  if (!userStore.isLogin) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => {
      uni.navigateTo({ url: '/pages/login/login' })
    }, 1500)
    return
  }

  // 刷新购物车列表
  await loadCartData()

  // 加载 AI 推荐商品
  await loadRecommendProducts()
})

// ==================== 数据加载 ====================

/**
 * 加载购物车数据
 */
const loadCartData = async () => {
  try {
    await cartStore.fetchCart()
  } catch (error) {
    console.error('[Cart] 加载购物车失败:', error)
  }
}

/**
 * 加载 AI 智能推荐商品
 * 获取 4 个推荐商品展示在购物车底部
 */
const loadRecommendProducts = async () => {
  try {
    // 调用商品列表接口，获取第 1 页的 4 个商品
    const res = await getProductList({ page: 1, pageSize: 4 })
    // 取接口返回的 records 数组作为推荐商品数据
    recommendProducts.value = res.records || []
  } catch (error) {
    console.error('[Cart] 加载推荐商品失败:', error)
    // 失败时清空推荐列表，不阻塞页面正常展示
    recommendProducts.value = []
  }
}

// ==================== 事件处理 ====================

/**
 * 切换商品勾选状态
 */
const onToggleCheck = (item) => {
  cartStore.toggleCheck(item.id)
}

/**
 * 增加数量
 */
const onIncrease = (item) => {
  cartStore.updateQuantity(item.id, item.quantity + 1)
}

/**
 * 减少数量
 */
const onDecrease = (item) => {
  if (item.quantity > 1) {
    cartStore.updateQuantity(item.id, item.quantity - 1)
  }
}

/**
 * 删除商品
 */
const onDelete = (item) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除该商品吗？',
    success: (res) => {
      if (res.confirm) {
        cartStore.removeItem(item.id)
      }
    }
  })
}

/**
 * 点击商品行跳转详情
 */
const onItemClick = (item) => {
  uni.navigateTo({
    url: `/pages/goods/detail?id=${item.productId}`
  })
}

/**
 * 全选/取消全选
 */
const onToggleAll = (checked) => {
  cartStore.toggleAllCheck(checked)
}

/**
 * 去结算
 */
const onSettle = () => {
  if (cartStore.checkedItems.length === 0) {
    uni.showToast({ title: '请选择商品', icon: 'none' })
    return
  }
  uni.navigateTo({
    url: '/pages/order/create'
  })
}

/**
 * 去首页
 */
const onGoHome = () => {
  uni.switchTab({ url: '/pages/index/index' })
}

/**
 * 点击推荐商品，跳转到商品详情页
 */
const onRecommendClick = (product) => {
  uni.navigateTo({
    url: `/pages/goods/detail?id=${product.id}`
  })
}

/**
 * 上拉加载更多
 */
const onLoadMore = () => {
  // 购物车数据通常一次性加载，不需要分页
}
</script>

<style lang="scss" scoped>
.cart-page {
  height: 100vh;
  background: #F2F2F7;
  display: flex;
  flex-direction: column;
}

.cart-page__scroll {
  flex: 1;
  padding-top: 16rpx;
}

// ==================== AI 推荐区域 ====================
.cart-page__recommend {
  margin: 16rpx 16rpx 0;           // 左右边距，与商品列表对齐
  padding: 24rpx 20rpx;             // 内边距
  background: #FFFFFF;              // 白色背景卡片
  border-radius: 12rpx;             // 圆角卡片
}

// 推荐标题行
.cart-page__recommend-header {
  display: flex;                    // 水平排列
  align-items: center;              // 垂直居中
  margin-bottom: 20rpx;             // 与商品网格间距
}

// 主标题：为你推荐
.cart-page__recommend-title {
  font-size: 30rpx;                 // 较大字号
  font-weight: 700;                 // 加粗
  color: #1D1D1F;                   // 主文字色
}

// 副标题：AI 智能推荐标签
.cart-page__recommend-subtitle {
  margin-left: 12rpx;               // 与主标题间距
  font-size: 22rpx;                 // 小字号标签
  color: #007AFF;                   // 主题蓝色
  background: rgba(0, 122, 255, 0.08); // 浅蓝色背景
  padding: 4rpx 16rpx;              // 标签内边距
  border-radius: 20rpx;             // 圆角标签
  font-weight: 500;                 // 中等粗细
}

// 推荐商品 2 列网格
.cart-page__recommend-grid {
  display: grid;                    // 网格布局
  grid-template-columns: 1fr 1fr;   // 等宽两列
  gap: 16rpx;                       // 卡片间距
}
</style>