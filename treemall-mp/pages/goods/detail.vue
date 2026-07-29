<!--
  pages/goods/detail.vue — 商品详情页

  【设计思想】
  商品详情页是电商转化率最高的页面，需要展示完整的商品信息并引导用户下单。
  核心功能：图片轮播、价格展示、规格选择、加入购物车、立即购买。

  【Figma 设计稿参考】
  - 大图轮播区
  - 价格 + 名称 + 标签 + 评分
  - 颜色选择器（圆形色块）
  - 参数表
  - 商品描述（富文本）
  - 为你推荐（横向滑动）
  - 底部固定操作栏：收藏 + 客服 + 加入购物车 + 立即购买

  【页面参数】通过 URL 参数接收商品 ID：/pages/goods/detail?id=123
-->
<template>
  <view class="goods-detail-page">
    <!-- ==================== 可滚动内容 ==================== -->
    <scroll-view class="goods-detail-page__scroll" scroll-y>
      <!-- 商品图片轮播 -->
      <swiper
        class="goods-detail-page__swiper"
        :indicator-dots="productImages.length > 1"
        indicator-color="rgba(255,255,255,0.5)"
        indicator-active-color="#007AFF"
        circular
        autoplay
      >
        <swiper-item v-for="(img, index) in productImages" :key="index">
          <image class="goods-detail-page__image" :src="img" mode="aspectFill" />
        </swiper-item>
      </swiper>

      <!-- 商品信息区 -->
      <view class="goods-detail-page__info">
        <!-- 价格行 -->
        <view class="goods-detail-page__price-row">
          <view class="goods-detail-page__price">
            <text class="goods-detail-page__price-symbol">¥</text>
            <text class="goods-detail-page__price-value">{{ formatPrice(product.price) }}</text>
          </view>
          <!-- 销量 -->
          <text class="goods-detail-page__sales" v-if="product.salesCount">
            已售 {{ product.salesCount }}
          </text>
        </view>

        <!-- 商品名称 -->
        <text class="goods-detail-page__name">{{ product.productName }}</text>


        <!-- 标签 -->
        <view class="goods-detail-page__tags" v-if="product.tags">
          <text
            v-for="(tag, index) in parseTags(product.tags)"
            :key="index"
            class="goods-detail-page__tag"
          >
            {{ tag }}
          </text>
        </view>
      </view>

      <!-- 颜色选择器 -->
      <view class="goods-detail-page__section" v-if="colorOptions.length > 0">
        <text class="goods-detail-page__section-title">颜色选择</text>
        <view class="goods-detail-page__color-options">
          <view
            v-for="(color, index) in colorOptions"
            :key="index"
            class="goods-detail-page__color-option"
            :class="{ 'goods-detail-page__color-option--active': selectedColor === color.value }"
            @tap="onColorSelect(color.value)"
          >
            <view
              class="goods-detail-page__color-dot"
              :style="{ background: color.hex || '#ccc' }"
            />
            <text class="goods-detail-page__color-name">{{ color.label }}</text>
          </view>
        </view>
      </view>

      <!-- 参数表 -->
      <view class="goods-detail-page__section">
        <text class="goods-detail-page__section-title">详细参数</text>
        <view class="goods-detail-page__params">
          <view
            v-for="(param, index) in productParams"
            :key="index"
            class="goods-detail-page__param-row"
          >
            <text class="goods-detail-page__param-label">{{ param.label }}</text>
            <text class="goods-detail-page__param-value">{{ param.value }}</text>
          </view>
        </view>
      </view>

      <!-- ==================== 商品描述（富文本） ==================== -->
      <!--
        使用 rich-text 组件渲染 HTML 富文本内容
        仅在 description 不为空时显示该区块
      -->
      <view class="goods-detail-page__section" v-if="product.description">
        <text class="goods-detail-page__section-title">商品描述</text>
        <view class="goods-detail-page__description">
          <rich-text
            class="goods-detail-page__rich-text"
            :nodes="product.description"
          />
        </view>
      </view>

      <!-- 为你推荐 -->
      <view class="goods-detail-page__section">
        <text class="goods-detail-page__section-title">为你推荐</text>
        <scroll-view class="goods-detail-page__recommend" scroll-x>
          <view
            v-for="rec in recommendProducts"
            :key="rec.id"
            class="goods-detail-page__recommend-item"
            @tap="onRecommendClick(rec)"
          >
            <image
              class="goods-detail-page__recommend-image"
              :src="getImageUrl(rec.mainImage)"
              mode="aspectFill"
            />
            <text class="goods-detail-page__recommend-name">{{ rec.productName }}</text>
            <text class="goods-detail-page__recommend-price">¥{{ formatPrice(rec.price) }}</text>
          </view>
        </scroll-view>
      </view>

      <!-- 底部安全区域（为固定底部操作栏预留空间） -->
      <view style="height: 140rpx;" />
    </scroll-view>

    <!-- ==================== 底部操作栏 ==================== -->
    <!--
      固定底部，包含收藏、客服、加入购物车、立即购买四个操作项
      加入购物车：白色边框按钮（outline 风格）
      立即购买：蓝色实心按钮（主色 #007AFF）
    -->
    <view class="goods-detail-page__footer">
      <!-- 收藏按钮 -->
      <view class="goods-detail-page__footer-icon" @tap="onFavorite">
        <u-icon name="heart" :size="25" :color="isFavorite ? '#ef4444' : '#8E8E93'" />
        <text class="goods-detail-page__footer-icon-text">收藏</text>
      </view>

      <!-- 客服按钮 -->
      <view class="goods-detail-page__footer-icon" @tap="onService">
        <u-icon name="kefu-ermai" :size="25" color="#8E8E93" />
        <text class="goods-detail-page__footer-icon-text">客服</text>
      </view>

      <!-- 加入购物车按钮（白色边框，点击弹出 SKU 选择弹窗） -->
      <view
        class="goods-detail-page__footer-btn goods-detail-page__footer-btn--cart"
        @tap="onAddToCart"
      >
        加入购物车
      </view>

      <!-- 立即购买按钮（蓝色实心，点击弹出 SKU 选择弹窗后跳转下单页） -->
      <view
        class="goods-detail-page__footer-btn goods-detail-page__footer-btn--buy"
        @tap="onBuyNow"
      >
        立即购买
      </view>
    </view>

    <!-- ==================== SKU 规格选择弹窗 ==================== -->
    <!--
      集成 SkuSelector 组件，用户点击"加入购物车"或"立即购买"时弹出
      通过 v-model:show 控制弹窗显隐
      通过 confirmText 区分"加入购物车"和"立即购买"两种模式的按钮文字
    -->
    <SkuSelector
      v-model:show="showSkuSelector"
      :product="skuProductData"
      :sku-list="skuList"
      :confirm-text="skuConfirmText"
      @confirm="onSkuConfirm"
      @close="onSkuClose"
    />
  </view>
</template>

<script setup>
// ==================== 导入依赖 ====================
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getImageUrl } from '@/utils/image-url'

// 导入 API 模块
import { getProductDetail } from '@/api/product'
import { getProductList } from '@/api/product'

// 导入 Store
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'

// 导入统一路由跳转工具
import { navTo } from '@/utils/navigator'

// 导入 SKU 选择弹窗组件
import SkuSelector from '@/components/dialogs/SkuSelector.vue'

// ==================== Store 实例 ====================
const cartStore = useCartStore()        // 购物车 Store，用于加入购物车操作
const userStore = useUserStore()        // 用户 Store，用于登录状态检查

// ==================== 响应式数据 ====================

// 商品信息（完整商品详情数据）
const product = ref({})

// 商品图片列表（主图 + 可选副图）
const productImages = computed(() => {
  const list = []
  // 1. 添加主图（转为完整 URL）
  if (product.value.mainImage) {
    list.push(getImageUrl(product.value.mainImage))
  }
  // 2. 解析 images JSON 数组，添加多张副图
  if (product.value.images) {
    try {
      const imagesArr = typeof product.value.images === 'string'
        ? JSON.parse(product.value.images)
        : product.value.images
      if (Array.isArray(imagesArr)) {
        imagesArr.forEach(img => list.push(getImageUrl(img)))
      }
    } catch (e) {
      console.error('[Detail] 图片列表解析失败:', e)
    }
  }
  return list
})

// 颜色选项（模拟数据，实际从后端 specifications 中解析）
const colorOptions = ref([])

// 选中的颜色值
const selectedColor = ref('')

// 是否已收藏
const isFavorite = ref(false)

// 推荐商品列表
const recommendProducts = ref([])

// 商品参数（从商品详情中提取）
const productParams = computed(() => {
  if (!product.value.specs) return []
  try {
    const specs = typeof product.value.specs === 'string'
      ? JSON.parse(product.value.specs)
      : product.value.specs
    // 将 {"内存":"12GB","品牌":"华为"} 转为 [{label:"内存",value:"12GB"}, ...]
    return Object.entries(specs).map(([key, value]) => ({
      label: key,
      value: String(value)
    }))
  } catch (e) {
    console.error('[Detail] 规格参数解析失败:', e)
    return []
  }
})


// ==================== SKU 弹窗相关状态 ====================

/**
 * 是否显示 SKU 选择弹窗
 * 点击"加入购物车"或"立即购买"时设为 true
 */
const showSkuSelector = ref(false)

/**
 * SKU 确认模式
 * 'cart' — 加入购物车模式
 * 'buy'  — 立即购买模式
 */
const skuConfirmMode = ref('cart')

/**
 * SKU 确认按钮文字
 * 根据 skuConfirmMode 动态切换：加入购物车 / 立即购买
 */
const skuConfirmText = computed(() => {
  return skuConfirmMode.value === 'cart' ? '加入购物车' : '立即购买'
})

/**
 * 传给 SkuSelector 组件的商品数据
 * 包含缩略图、价格、库存等基础信息
 */
const skuProductData = computed(() => {
  return {
    mainImage: getImageUrl(product.value.mainImage),      // 商品缩略图
    price: product.value.price || 0,               // 商品价格
    stock: product.value.stock || 0,               // 商品库存
    productName: product.value.productName || ''   // 商品名称
  }
})

/**
 * SKU 规格列表
 * 从商品详情的 specifications 字段解析，转换为 SkuSelector 组件所需格式
 * 格式：[{ name: '颜色', values: ['红色', '蓝色'] }, { name: '尺寸', values: ['S', 'M', 'L'] }]
 */
const skuList = computed(() => {
  // 如果商品没有规格数据，返回空数组
  if (!product.value.specs) return []

  try {
    // 解析 specifications（可能是 JSON 字符串或已解析的对象）
    const specs = typeof product.value.specs === 'string'
      ? JSON.parse(product.value.specs)
      : product.value.specs

    // 构建 SkuSelector 组件所需格式的规格列表
    const list = []

    // 如果有颜色选项，添加颜色规格
    if (specs.colors && specs.colors.length > 0) {
      list.push({
        name: '颜色',                                               // 规格名称
        values: specs.colors.map(c => c.label || c.value || c)      // 提取颜色值
      })
    }

    // 如果有尺寸选项，添加尺寸规格
    if (specs.sizes && specs.sizes.length > 0) {
      list.push({
        name: '尺寸',                                               // 规格名称
        values: specs.sizes.map(s => s.label || s.value || s)       // 提取尺寸值
      })
    }

    // 如果有其他自定义规格，逐一添加
    if (specs.customSpecs && specs.customSpecs.length > 0) {
      specs.customSpecs.forEach(spec => {
        list.push({
          name: spec.name || '规格',                                // 规格名称
          values: Array.isArray(spec.values) ? spec.values : []     // 规格值数组
        })
      })
    }

    return list
  } catch (e) {
    // 解析失败时返回空数组，避免页面崩溃
    console.error('[Detail] SKU 规格解析失败:', e)
    return []
  }
})

// ==================== 页面生命周期 ====================

onLoad(async (options) => {
  // 从 URL 参数中获取商品 ID
  const productId = options.id
  if (!productId) {
    // 没有商品 ID 时提示用户并返回上一页
    uni.showToast({ title: '商品不存在', icon: 'none' })
    setTimeout(() => uni.navigateBack(), 1500)
    return
  }

  // 加载商品详情和推荐商品
  await loadProductDetail(productId)
  await loadRecommendProducts()
})

// ==================== 数据加载 ====================

/**
 * 加载商品详情
 * 根据商品 ID 从后端获取商品完整信息，并解析规格数据
 *
 * @param {string|number} productId - 商品 ID
 */
const loadProductDetail = async (productId) => {
  try {
    uni.showLoading({ title: '加载中...' })
    const res = await getProductDetail(productId)
    product.value = res

    // 提取颜色选项（如果后端返回了规格数据）
    if (res.specs) {
      try {
        const specs = typeof res.specifications === 'string'
          ? JSON.parse(res.specs)
          : res.specs
	// V1 规格为扁平 key-value，暂不支持颜色选项
	// 如果未来扩展为 {colors: [...]} 格式，在此解析
        colorOptions.value = specs.colors || []
      } catch (e) {
        colorOptions.value = []
      }
    }
  } catch (error) {
    console.error('[Detail] 加载商品详情失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

/**
 * 加载推荐商品
 * 获取商品列表作为"为你推荐"的数据源
 */
const loadRecommendProducts = async () => {
  try {
    const res = await getProductList({ page: 1, pageSize: 6 })
    recommendProducts.value = res.records || []
  } catch (error) {
    console.error('[Detail] 加载推荐商品失败:', error)
  }
}

// ==================== 事件处理 ====================

/**
 * 颜色选择事件
 * 用户点击某个颜色选项时触发
 *
 * @param {string} value - 选中的颜色值
 */
const onColorSelect = (value) => {
  selectedColor.value = value
}

/**
 * 收藏/取消收藏事件
 * 切换商品收藏状态，未登录时跳转到登录页
 */
const onFavorite = () => {
  // 未登录时跳转登录页
  if (!userStore.isLogin) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  // 切换收藏状态
  isFavorite.value = !isFavorite.value
  uni.showToast({
    title: isFavorite.value ? '已收藏' : '已取消收藏',
    icon: 'none'
  })
}

/**
 * 客服按钮点击事件
 * 打开客服会话（功能开发中，暂时提示）
 */
const onService = () => {
  uni.showToast({ title: '客服功能开发中', icon: 'none' })
}

/**
 * 加入购物车按钮点击事件
 * 弹出 SKU 选择弹窗，让用户选择商品规格后加入购物车
 */
const onAddToCart = () => {
  // 未登录时跳转登录页
  if (!userStore.isLogin) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }

  // 设置 SKU 确认模式为"加入购物车"
  skuConfirmMode.value = 'cart'
  // 显示 SKU 选择弹窗
  showSkuSelector.value = true
}

/**
 * 立即购买按钮点击事件
 * 弹出 SKU 选择弹窗，让用户选择商品规格后跳转下单确认页
 */
const onBuyNow = () => {
  // 未登录时跳转登录页
  if (!userStore.isLogin) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }

  // 设置 SKU 确认模式为"立即购买"
  skuConfirmMode.value = 'buy'
  // 显示 SKU 选择弹窗
  showSkuSelector.value = true
}

/**
 * SKU 弹窗确认事件
 * 用户在 SKU 选择弹窗中点击确认按钮后触发
 * 根据当前的确认模式（加入购物车 / 立即购买）执行不同操作
 *
 * @param {Object} payload - 确认数据 { sku: Object, quantity: number }
 * @param {Object} payload.sku - 选中的规格组合，如 { 颜色: '红色', 尺寸: 'M' }
 * @param {number} payload.quantity - 选择的购买数量
 */
const onSkuConfirm = async (payload) => {
  console.log('[Detail] SKU 确认:', payload)

  // 根据模式执行不同操作
  if (skuConfirmMode.value === 'cart') {
    // ---- 加入购物车模式 ----
    try {
      await cartStore.addItem({
        productId: product.value.id,      // 商品 ID
        quantity: payload.quantity,        // 购买数量
        sku: JSON.stringify(payload.sku)   // 选中的规格组合（序列化为 JSON 字符串）
      })
      uni.showToast({ title: '已加入购物车', icon: 'success' })
    } catch (error) {
      console.error('[Detail] 加入购物车失败:', error)
      uni.showToast({ title: '加入购物车失败', icon: 'none' })
    }
  } else {
    // ---- 立即购买模式 ----
    // 构建下单页参数：商品 ID + 数量 + 规格
    const params = {
      productId: product.value.id,        // 商品 ID
      quantity: payload.quantity,          // 购买数量
      sku: JSON.stringify(payload.sku)     // 选中的规格组合
    }
    // 使用 navTo 工具跳转到下单确认页，并传递参数
    uni.navigateTo({
      url: `/pages/order/create?productId=${params.productId}&quantity=${params.quantity}&sku=${encodeURIComponent(params.sku)}`
    })
  }
}

/**
 * SKU 弹窗关闭事件
 * 弹窗关闭时不需要额外处理，仅重置弹窗显示状态
 */
const onSkuClose = () => {
  // 弹窗关闭时无需额外操作，v-model:show 已自动更新为 false
}

/**
 * 推荐商品点击事件
 * 点击推荐商品卡片时跳转到对应商品详情页
 *
 * @param {Object} item - 推荐商品数据
 */
const onRecommendClick = (item) => {
  uni.navigateTo({ url: `/pages/goods/detail?id=${item.id}` })
}

// ==================== 工具函数 ====================

/**
 * 格式化价格
 * 将价格数字转为保留两位小数的字符串
 *
 * @param {number} price - 价格数值
 * @returns {string} 格式化后的价格字符串，如 "99.00"
 */
const formatPrice = (price) => {
  return price != null ? Number(price).toFixed(2) : '0.00'
}

/**
 * 解析标签数据
 * 支持字符串（JSON 或逗号分隔）、数组等多种格式
 *
 * @param {string|Array} tags - 标签数据
 * @returns {Array} 标签数组
 */
const parseTags = (tags) => {
  if (!tags) return []
  if (typeof tags === 'string') {
    try { return JSON.parse(tags) } catch (e) { return tags.split(',') }
  }
  return Array.isArray(tags) ? tags : []
}
</script>

<style lang="scss" scoped>
// ==================== 页面容器 ====================
.goods-detail-page {
  height: 100vh;                     // 撑满整个视口
  background: #F2F2F7;               // 页面背景色
  display: flex;
  flex-direction: column;
}

// 可滚动区域
.goods-detail-page__scroll {
  flex: 1;                           // 占据剩余空间（底部操作栏之外）
}

// ==================== 图片轮播 ====================
.goods-detail-page__swiper {
  width: 100%;
  height: 750rpx;                    // 正方形图片区域
}

.goods-detail-page__image {
  width: 100%;
  height: 100%;
  display: block;
  background: #F2F2F7;               // 图片加载前占位背景色
}

// ==================== 商品信息 ====================
.goods-detail-page__info {
  background: #FFFFFF;               // 白色卡片背景
  padding: 24rpx;
  margin-bottom: 16rpx;              // 区块间距
}

// 价格行（价格 + 销量）
.goods-detail-page__price-row {
  display: flex;
  align-items: baseline;             // 基线对齐，使 ¥ 符号与价格数字底部对齐
  justify-content: space-between;    // 价格在左，销量在右
  margin-bottom: 12rpx;
}

// 价格区域
.goods-detail-page__price {
  color: #007AFF;                    // 主题蓝色价格
  font-weight: 700;
  line-height: 1;
}

// 价格符号 ¥
.goods-detail-page__price-symbol {
  font-size: 28rpx;
}

// 价格数字
.goods-detail-page__price-value {
  font-size: 44rpx;                  // 大号价格突出显示
}

// 销量文字
.goods-detail-page__sales {
  font-size: 24rpx;
  color: #9ca3af;                    // 淡灰色销量
}

// 商品名称
.goods-detail-page__name {
  font-size: 30rpx;
  color: #1D1D1F;                    // 主文字色
  font-weight: 600;
  line-height: 1.5;
  display: block;
  margin-bottom: 12rpx;
}

// ==================== 标签 ====================
.goods-detail-page__tags {
  display: flex;
  flex-wrap: wrap;                   // 标签换行排列
  gap: 8rpx;                         // 标签间距
}

.goods-detail-page__tag {
  font-size: 20rpx;
  color: #007AFF;                    // 主题蓝色文字
  background: #E5F1FF;               // 浅蓝背景
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
}

// ==================== 通用区块 ====================
.goods-detail-page__section {
  background: #FFFFFF;               // 白色卡片背景
  padding: 24rpx;
  margin-bottom: 16rpx;              // 区块间距
}

// 区块标题
.goods-detail-page__section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1D1D1F;                    // 主文字色
  margin-bottom: 16rpx;
  display: block;
}

// ==================== 颜色选择器 ====================
.goods-detail-page__color-options {
  display: flex;
  flex-wrap: wrap;                   // 颜色选项换行排列
  gap: 16rpx;
}

.goods-detail-page__color-option {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 12rpx 20rpx;
  border: 1rpx solid #E5E5EA;        // 默认灰色边框
  border-radius: 8rpx;
  background: #FFFFFF;

  // 选中状态
  &--active {
    border-color: #007AFF;           // 主题蓝色边框
    background: #E5F1FF;             // 浅蓝背景
  }
}

// 颜色圆点
.goods-detail-page__color-dot {
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;                // 圆形
  border: 1rpx solid rgba(0,0,0,0.1);
}

// 颜色名称
.goods-detail-page__color-name {
  font-size: 24rpx;
  color: #1D1D1F;                    // 主文字色
}

// ==================== 参数表 ====================
.goods-detail-page__params {
  background: #f9fafb;               // 浅灰背景
  border-radius: 8rpx;
}

// 参数行
.goods-detail-page__param-row {
  display: flex;
  padding: 16rpx 20rpx;
  border-bottom: 1rpx solid #E5E5EA; // 分隔线

  // 最后一行不需要底部边框
  &:last-child {
    border-bottom: none;
  }
}

// 参数标签（左侧固定宽度）
.goods-detail-page__param-label {
  width: 140rpx;
  font-size: 24rpx;
  color: #8E8E93;                    // 次要文字色
  flex-shrink: 0;                    // 不压缩
}

// 参数值（右侧自适应）
.goods-detail-page__param-value {
  font-size: 24rpx;
  color: #1D1D1F;                    // 主文字色
  flex: 1;
}

// ==================== 商品描述（富文本） ====================
// 商品描述容器
.goods-detail-page__description {
  padding: 8rpx 0;                   // 上下轻微留白
}

// 富文本组件样式
// 通过 rich-text 组件渲染 HTML 内容，图片自适应宽度
.goods-detail-page__rich-text {
  font-size: 28rpx;                  // 富文本基础字号
  color: #1D1D1F;                    // 主文字色
  line-height: 1.8;                  // 舒适的行高
  word-break: break-all;             // 长单词换行
}

// ==================== 推荐商品 ====================
// 横向滚动容器
.goods-detail-page__recommend {
  white-space: nowrap;               // 不换行，实现横向滚动
}

// 推荐商品卡片
.goods-detail-page__recommend-item {
  display: inline-block;             // 行内块，横向排列
  width: 200rpx;
  margin-right: 16rpx;               // 卡片间距
  vertical-align: top;               // 顶部对齐

  // 最后一个不需要右边距
  &:last-child {
    margin-right: 0;
  }
}

// 推荐商品图片
.goods-detail-page__recommend-image {
  width: 200rpx;
  height: 200rpx;                    // 正方形图片
  border-radius: 8rpx;
  display: block;
  background: #F2F2F7;               // 占位背景色
  margin-bottom: 8rpx;
}

// 推荐商品名称
.goods-detail-page__recommend-name {
  font-size: 24rpx;
  color: #1D1D1F;                    // 主文字色
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;           // 文字溢出省略号
  white-space: nowrap;               // 单行显示
  margin-bottom: 4rpx;
}

// 推荐商品价格
.goods-detail-page__recommend-price {
  font-size: 24rpx;
  color: #007AFF;                    // 主题蓝色价格
  font-weight: 600;
}

// ==================== 底部操作栏 ====================
.goods-detail-page__footer {
  position: fixed;                   // 固定定位
  bottom: 0;
  left: 0;
  right: 0;
  background: #FFFFFF;               // 白色背景
  border-top: 1rpx solid #E5E5EA;    // 顶部分隔线
  display: flex;
  align-items: center;               // 垂直居中
  padding: 0 16rpx;                  // 左右内边距
  height: 100rpx;
  gap: 12rpx;                        // 元素间距
  z-index: 100;                      // 确保在最上层
  // iPhone 底部安全区域适配
  padding-bottom: env(safe-area-inset-bottom, 0);
}

// 底部图标按钮（收藏、客服）
.goods-detail-page__footer-icon {
  display: flex;
  flex-direction: column;            // 纵向排列图标 + 文字
  align-items: center;
  gap: 4rpx;
  flex-shrink: 0;                    // 不压缩
  padding: 0 8rpx;                   // 减小左右内边距，为按钮留出更多空间
}

// 图标按钮文字
.goods-detail-page__footer-icon-text {
  font-size: 20rpx;
  color: #8E8E93;                    // 次要文字色
}

// 底部操作按钮（加入购物车 / 立即购买）
.goods-detail-page__footer-btn {
  flex: 1;                           // 弹性占满剩余空间，两个按钮等宽
  height: 72rpx;
  border-radius: 36rpx;              // 圆角胶囊按钮（高度的一半）
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 600;
  line-height: 1;
  // 过渡动画
  transition: opacity 0.2s ease;

  // 加入购物车按钮：白色背景 + 蓝色边框（outline 风格）
  &--cart {
    background: #FFFFFF;             // 白色背景
    color: #007AFF;                  // 主题蓝色文字
    border: 2rpx solid #007AFF;      // 主题蓝色边框
    box-sizing: border-box;          // 边框计入总宽高
  }

  // 立即购买按钮：蓝色实心背景 + 白色文字
  &--buy {
    background: #007AFF;             // 主题蓝色背景
    color: #FFFFFF;                  // 白色文字
    border: none;                    // 无边框
  }
}
</style>
