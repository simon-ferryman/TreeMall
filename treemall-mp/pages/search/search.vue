<!--
  pages/search/search.vue -- 搜索页

  【设计思想】
  搜索页是电商小程序的核心功能入口，包含以下功能区域：
  1. 顶部搜索框：输入关键词，实时搜索或点击搜索按钮
  2. 搜索历史：本地存储最近搜索关键词，支持一键清空
  3. 热搜列表：展示热门搜索关键词，点击直接搜索
  4. 搜索结果列表：使用 ProductCard 组件展示搜索结果，支持上拉加载更多

  【Figma 设计令牌】
  主色 #007AFF | 文字 #1D1D1F | 次要文字 #8E8E93 | 背景 #F2F2F7 | 白色 #FFFFFF
  分隔线 #E5E5EA | 浅蓝 #E5F1FF | 设计宽度 375px | rpx=px*2

  【使用场景】
  - 首页搜索框点击后跳转此页面
  - 分类页搜索入口
-->
<template>
  <view class="search-page">
    <!-- ==================== 顶部搜索框区域 ==================== -->
    <!--
      搜索框 + 取消按钮
      输入时实时显示清除按钮，点击取消返回上一页
    -->
    <view class="search-page__header">
      <!-- 搜索框容器 -->
      <view class="search-page__search-bar">
        <!-- 搜索图标（uView 内置图标） -->
        <u-icon
          name="search"
          :size="20"
          color="#9c9ca1"
          class="search-page__search-icon"
        />
        <!-- 输入框：绑定 keyword 响应式变量 -->
        <input
          class="search-page__input"
          v-model="keyword"
          type="text"
          :placeholder="hotKeyword || '搜索商品'"
          placeholder-style="color: #C7C7CC; font-size: 28rpx;"
          confirm-type="search"
          @confirm="onSearch">                 <!-- 键盘回车触发搜索 -->
		</input
>
        <!-- 清除按钮：当输入框有内容时显示 -->

        <view
          v-if="keyword.length > 0"
          class="search-page__clear-btn"
          @tap="onClearKeyword"
        >
          <u-icon name="close-circle-fill" :size="28" color="#C7C7CC" />
        </view>
      </view>
      <!-- 取消按钮：点击返回上一页 -->
      <text class="search-page__cancel-btn" @tap="onCancel">取消</text>
    </view>

    <!-- ==================== 搜索历史 / 热搜区域 ==================== -->
    <!--
      未输入关键词时显示搜索历史和热搜列表
      输入关键词后隐藏，转而显示搜索结果
    -->
    <view class="search-page__body" v-if="!keyword.trim()">
      <!-- ==================== 搜索历史 ==================== -->
      <!--
        搜索历史存储在本地 Storage 中
        最多保存 10 条，支持一键清空
      -->
      <view class="search-page__section" v-if="searchHistory.length > 0">
        <!-- 标题行：搜索历史 + 清空按钮 -->
        <view class="search-page__section-header">
          <text class="search-page__section-title">搜索历史</text>
          <view class="search-page__clear-history" @tap="onClearHistory">
            <u-icon name="trash" :size="24" color="#8E8E93" />
          </view>
        </view>
        <!-- 历史标签流式布局 -->
        <view class="search-page__tags">
          <view
            v-for="(item, index) in searchHistory"
            :key="index"
            class="search-page__tag search-page__tag--history"
            @tap="onHistoryTap(item)"
          >
            {{ item }}
          </view>
        </view>
      </view>

      <!-- ==================== 热搜列表 ==================== -->
      <!--
        热搜关键词从后端获取，或使用本地默认数据
        每个关键词带热度序号，前 3 名使用红色序号
      -->
      <view class="search-page__section">
        <view class="search-page__section-header">
          <text class="search-page__section-title">热搜推荐</text>
        </view>
        <!-- 热搜列表 -->
        <view class="search-page__hot-list">
          <view
            v-for="(item, index) in hotList"
            :key="index"
            class="search-page__hot-item"
            @tap="onHotTap(item)"
          >
            <!-- 排名序号：前 3 名红色高亮 -->
            <text
              class="search-page__hot-rank"
              :class="{ 'search-page__hot-rank--top': index < 3 }"
            >
              {{ index + 1 }}
            </text>
            <!-- 关键词文字 -->
            <text class="search-page__hot-text">{{ item }}</text>
            <!-- 热度标识（HOT 标签） -->
            <text
              v-if="index < 3"
              class="search-page__hot-badge"
              :class="'search-page__hot-badge--' + (index + 1)"
            >
              {{ index === 0 ? 'HOT' : index === 1 ? '热' : '新' }}
            </text>
          </view>
        </view>
      </view>
    </view>

    <!-- ==================== 搜索结果区域 ==================== -->
    <!--
      输入关键词并搜索后显示此区域
      使用 ProductCard 组件展示商品列表
      支持上拉加载更多
    -->
    <scroll-view
      v-else
      class="search-page__results"
      scroll-y
      @scrolltolower="onLoadMore"
    >
      <!-- 搜索状态提示 -->
      <view v-if="loading" class="search-page__loading">
        <u-loading-icon :size="36" color="#007AFF" mode="circle" />
        <text class="search-page__loading-text">搜索中...</text>
      </view>

      <!-- 空状态：搜索无结果 -->
      <EmptyState
        v-else-if="resultList.length === 0 && !loading"
        icon="search"
        text="没有找到相关商品"
        button-text="换个关键词试试"
        @action="onFocus"
      />

      <!-- 搜索结果商品列表：2 列网格布局 -->
      <view v-else class="search-page__result-grid">
        <view
          v-for="product in resultList"
          :key="product.id"
          class="search-page__result-item"
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
import { onLoad } from '@dcloudio/uni-app'         // uni-app 生命周期钩子
import { ref } from 'vue'
import ProductCard from '@/components/business/ProductCard.vue'     // 商品卡片组件
import EmptyState from '@/components/basics/EmptyState.vue'       // 空状态组件
import LoadingMore from '@/components/basics/LoadingMore.vue'     // 加载更多组件
import { getProductList } from '@/api/product'             // 商品列表 API（支持 keyword 搜索）

// ==================== 常量配置 ====================
// 搜索历史本地存储 key
const HISTORY_KEY = 'search_history'
// 最大历史记录条数
const MAX_HISTORY = 10

// ==================== 响应式数据 ====================

// 搜索关键词（双向绑定输入框）
const keyword = ref('')
// 搜索历史列表（从本地 Storage 加载）
const searchHistory = ref([])
// 当前热搜词（用于 placeholder 显示）
const hotKeyword = ref('')
// 热搜列表数据
const hotList = ref(['手机', '笔记本电脑', '耳机', '蓝牙音箱', '运动鞋', '连衣裙', '零食大礼包', '充电宝'])
// 搜索结果列表
const resultList = ref([])
// 是否正在加载
const loading = ref(false)
// 当前页码
const currentPage = ref(1)
// 是否还有更多数据
const hasMore = ref(true)
// 加载更多状态
const loadMoreStatus = ref('')

// ==================== 页面生命周期 ====================

onLoad(() => {
  // 页面加载时从本地存储读取搜索历史
  loadSearchHistory()
})

// ==================== 搜索历史管理 ====================

/**
 * 从本地 Storage 加载搜索历史
 * 使用 uni.getStorageSync 同步读取
 */
const loadSearchHistory = () => {
  try {
    // 读取 Storage 中的历史记录 JSON 字符串
    const historyStr = uni.getStorageSync(HISTORY_KEY)
    // 解析 JSON，如果为空则使用空数组
    searchHistory.value = historyStr ? JSON.parse(historyStr) : []
  } catch (e) {
    // 解析失败时（如数据损坏）使用空数组
    console.error('[Search] 读取搜索历史失败:', e)
    searchHistory.value = []
  }
}

/**
 * 保存搜索历史到本地 Storage
 * 新关键词插入到数组最前面，最多保留 MAX_HISTORY 条
 * 去重：如果关键词已存在，先移除旧记录再插入到最前面
 *
 * @param {string} kw - 搜索关键词
 */
const saveSearchHistory = (kw) => {
  if (!kw || !kw.trim()) return                        // 空关键词不保存
  // 过滤掉新旧重复的关键词（去重）
  searchHistory.value = searchHistory.value.filter(item => item !== kw)
  // 插入到数组最前面
  searchHistory.value.unshift(kw)
  // 超过最大条数时截断
  if (searchHistory.value.length > MAX_HISTORY) {
    searchHistory.value = searchHistory.value.slice(0, MAX_HISTORY)
  }
  // 持久化到本地 Storage
  uni.setStorageSync(HISTORY_KEY, JSON.stringify(searchHistory.value))
}

/**
 * 清空搜索历史
 */
const onClearHistory = () => {
  // 确认弹窗：防止误操作
  uni.showModal({
    title: '确认清空',
    content: '确定要清空搜索历史吗？',
    success: (res) => {
      if (res.confirm) {
        // 清空数组
        searchHistory.value = []
        // 清除 Storage 中的数据
        uni.removeStorageSync(HISTORY_KEY)
        // 提示用户
        uni.showToast({ title: '已清空', icon: 'success', duration: 1500 })
      }
    }
  })
}

// ==================== 搜索逻辑 ====================

/**
 * 执行搜索
 * 重置分页、调用 API 获取搜索结果
 *
 * @param {boolean} isLoadMore - 是否为加载更多（true 时不重置页码）
 */
const doSearch = async (isLoadMore = false) => {
  // 去除首尾空格
  const kw = keyword.value.trim()
  // 空关键词不搜索
  if (!kw) return

  // 防重复请求：正在加载中不再发起新请求
  if (loading.value) return
  // 加载更多时如果没有更多数据，不再请求
  if (isLoadMore && !hasMore.value) return

  // 设置加载状态
  loading.value = true
  loadMoreStatus.value = 'loading'

  // 首次搜索（非加载更多）时重置分页
  if (!isLoadMore) {
    currentPage.value = 1
    hasMore.value = true
    resultList.value = []
  }

  try {
    // 调用商品列表 API，传入关键词和分页参数
    const res = await getProductList({
      keyword: kw,                              // 搜索关键词
      page: currentPage.value,                   // 当前页码
      pageSize: 10                              // 每页数量（注意：product API 使用 pageSize）
    })

    // 提取记录列表
    const records = res.records || []

    if (isLoadMore) {
      // 加载更多：追加到现有列表末尾
      resultList.value = [...resultList.value, ...records]
    } else {
      // 首次搜索：替换列表
      resultList.value = records
    }

    // 判断是否还有更多数据
    hasMore.value = resultList.value.length < (res.total || 0)
    // 更新加载更多状态
    loadMoreStatus.value = hasMore.value ? '' : 'noMore'
    // 页码 +1
    currentPage.value++

  } catch (error) {
    // 加载失败：显示错误状态
    console.error('[Search] 搜索失败:', error)
    loadMoreStatus.value = 'error'
  } finally {
    // 无论成功或失败，结束加载状态
    loading.value = false
  }
}

// ==================== 事件处理 ====================

/**
 * 搜索按钮点击 / 键盘回车
 * 保存搜索历史并执行搜索
 */
const onSearch = () => {
  // 保存到搜索历史
  saveSearchHistory(keyword.value.trim())
  // 执行搜索
  doSearch(false)
}

/**
 * 输入框获得焦点
 * 清空搜索结果，回到历史/热搜视图
 */
const onFocus = () => {
  // 如果当前有搜索结果，清空关键词回到历史/热搜视图
  // 用户可能想重新搜索
}

/**
 * 输入框内容变化
 * 可用于实时搜索建议（当前版本暂不实现）
 */
const onInput = () => {
  // 实时搜索建议可在此实现
  // 调用建议 API 获取下拉提示
}

/**
 * 清除输入框内容
 */
const onClearKeyword = () => {
  keyword.value = ''           // 清空关键词
  resultList.value = []        // 清空搜索结果
  currentPage.value = 1        // 重置页码
  hasMore.value = true         // 重置"是否有更多"
  loadMoreStatus.value = ''    // 重置加载状态
}

/**
 * 取消按钮：返回上一页
 */
const onCancel = () => {
  uni.navigateBack()
}

/**
 * 点击搜索历史标签
 *
 * @param {string} kw - 历史关键词
 */
const onHistoryTap = (kw) => {
  keyword.value = kw           // 填充到输入框
  saveSearchHistory(kw)        // 更新历史记录（提到最前面）
  doSearch(false)              // 执行搜索
}

/**
 * 点击热搜关键词
 *
 * @param {string} kw - 热搜关键词
 */
const onHotTap = (kw) => {
  keyword.value = kw           // 填充到输入框
  saveSearchHistory(kw)        // 保存到搜索历史
  doSearch(false)              // 执行搜索
}

/**
 * 上拉加载更多
 */
const onLoadMore = () => {
  doSearch(true)
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
</script>

<style lang="scss" scoped>
// ==================== 页面容器 ====================
.search-page {
  min-height: 100vh;                     // 最小高度撑满屏幕
  background: $bg-color;                 // 页面背景色：#F2F2F7
  display: flex;
  flex-direction: column;                // 从上到下排列
}

// ==================== 顶部搜索框区域 ====================
.search-page__header {
  display: flex;                         // 横向排列：搜索框 + 取消按钮
  align-items: center;                   // 垂直居中
  padding: 16rpx 24rpx;                  // 上下 16rpx，左右 24rpx
  background: $bg-white;                 // 白色背景
  border-bottom: 1rpx solid $border-color; // 底部分隔线
  flex-shrink: 0;                        // 不压缩
}

// 搜索框容器
.search-page__search-bar {
  flex: 1;                               // 自动填充剩余空间
  display: flex;
  align-items: center;
  height: 64rpx;                         // Figma 32px -> 64rpx
  background: $bg-color;                 // 浅灰背景
  border-radius: $radius-xl;             // 圆角：32rpx
  padding: 0 20rpx;                      // 左右内边距
  margin-right: 20rpx;                   // 与取消按钮的间距
}

// 搜索图标
.search-page__search-icon {
  flex-shrink: 0;                        // 不压缩
  margin-right: 12rpx;                   // 与输入框的间距
}

// 搜索输入框
.search-page__input {
  flex: 1;                               // 自动填充剩余空间
  height: 64rpx;                         // 与搜索框等高
  font-size: $font-md;                   // 28rpx 正文字体
  color: $text-color;                    // 主文字色：#1D1D1F
  line-height: 64rpx;
}

// 清除按钮
.search-page__clear-btn {
  flex-shrink: 0;                        // 不压缩
  margin-left: 12rpx;                    // 与输入内容的间距
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40rpx;
  height: 40rpx;
}

// 取消按钮
.search-page__cancel-btn {
  font-size: $font-md;                   // 28rpx
  color: $primary-color;                 // 主题蓝：#007AFF
  flex-shrink: 0;                        // 不压缩
  line-height: 1.4;
}

// ==================== 搜索历史 / 热搜区域 ====================
.search-page__body {
  flex: 1;                               // 填充剩余空间
  padding: 0 24rpx;                      // 左右内边距
}

// 区域容器
.search-page__section {
  margin-top: $spacing-lg;               // 顶部间距：32rpx
}

// 区域标题行
.search-page__section-header {
  display: flex;
  justify-content: space-between;         // 标题在左，清空按钮在右
  align-items: center;
  margin-bottom: $spacing-md;            // 底部间距：24rpx
}

// 区域标题
.search-page__section-title {
  font-size: $font-lg;                   // 32rpx
  font-weight: $font-weight-bold;        // 600 字重
  color: $text-color;                    // 主文字色：#1D1D1F
}

// 清空历史按钮
.search-page__clear-history {
  padding: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

// ==================== 标签流式布局 ====================
.search-page__tags {
  display: flex;
  flex-wrap: wrap;                       // 自动换行
  gap: $spacing-sm;                      // 标签间距：16rpx
}

// 标签通用样式
.search-page__tag {
  font-size: $font-sm;                   // 24rpx
  padding: 10rpx 24rpx;                  // 上下 10rpx，左右 24rpx
  border-radius: $radius-xl;             // 圆角胶囊：32rpx
  line-height: 1.4;

  // 历史标签：浅灰背景
  &--history {
    background: $bg-color;               // 浅灰背景：#F2F2F7
    color: $text-color;                  // 主文字色：#1D1D1F
  }
}

// ==================== 热搜列表 ====================
.search-page__hot-list {
  display: flex;
  flex-direction: column;                // 垂直排列
}

// 热搜条目
.search-page__hot-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;                      // 上下内边距
  border-bottom: 1rpx solid $border-color; // 底部分隔线

  // 最后一条无分隔线
  &:last-child {
    border-bottom: none;
  }
}

// 热搜排名序号
.search-page__hot-rank {
  width: 40rpx;                          // 固定宽度
  font-size: $font-sm;                   // 24rpx
  font-weight: $font-weight-bold;        // 600 字重
  color: $text-muted;                    // 次要文字色：#8E8E93
  text-align: center;
  flex-shrink: 0;

  // 前三名：红色高亮
  &--top {
    color: $danger-color;                // 危险红：#FF3B30
  }
}

// 热搜关键词文字
.search-page__hot-text {
  flex: 1;                               // 填充剩余空间
  font-size: $font-md;                   // 28rpx
  color: $text-color;                    // 主文字色：#1D1D1F
  margin-left: $spacing-sm;              // 左间距：16rpx
}

// 热搜标识（HOT/热/新）
.search-page__hot-badge {
  font-size: $font-xs;                   // 20rpx
  font-weight: $font-weight-bold;        // 600 字重
  padding: 4rpx 12rpx;
  border-radius: $radius-sm;             // 小圆角：12rpx
  flex-shrink: 0;
  margin-left: $spacing-sm;              // 左间距：16rpx

  // 第一名：红色
  &--1 {
    background: #fef2f2;                 // 浅红背景
    color: #ef4444;                      // 红色文字
  }

  // 第二名：橙色
  &--2 {
    background: #fff7ed;                 // 浅橙背景
    color: #ea580c;                      // 橙色文字
  }

  // 第三名：蓝色
  &--3 {
    background: $primary-light;          // 浅蓝背景：#E5F1FF
    color: $primary-color;               // 主题蓝：#007AFF
  }
}

// ==================== 搜索结果区域 ====================
.search-page__results {
  flex: 1;                               // 填充剩余空间
  padding: 0 16rpx;                      // 左右内边距
}

// 加载中状态
.search-page__loading {
  @include flex-center;                  // 使用混入：水平垂直居中
  flex-direction: column;
  padding: 120rpx 0;                     // 上下留白
}

// 加载文字
.search-page__loading-text {
  font-size: $font-sm;                   // 24rpx
  color: $text-muted;                    // 次要文字色：#8E8E93
  margin-top: $spacing-md;              // 顶部间距：24rpx
}

// 搜索结果网格：2 列布局
.search-page__result-grid {
  display: flex;
  flex-wrap: wrap;                       // 自动换行
  gap: 16rpx;                            // 卡片间距
  padding-top: 16rpx;                    // 顶部间距
}

// 搜索结果卡片容器：80rpx = 750rpx / 2 - gap - padding
.search-page__result-item {
  width: calc(50% - 8rpx);               // 两列等宽，减去 gap 的一半
}
</style>
