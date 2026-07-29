<!--
  pages/notify/notify.vue -- 通知页

  【设计思想】
  通知页展示系统通知列表，每条通知包含：
  1. 通知类型图标（系统通知/订单通知/活动通知/物流通知）
  2. 通知标题
  3. 通知时间
  4. 内容摘要（1 行截断）
  5. 未读标记（蓝色圆点）

  支持空状态展示。

  【Figma 设计令牌】
  主色 #007AFF | 文字 #1D1D1F | 次要文字 #8E8E93 | 背景 #F2F2F7 | 白色 #FFFFFF
  分隔线 #E5E5EA | 浅蓝 #E5F1FF | 设计宽度 375px | rpx=px*2

  【使用场景】
  - 个人中心页点击"消息通知"入口
  - 首页顶部消息图标
-->
<template>
  <view class="notify-page">
    <!-- ==================== 通知列表 ==================== -->
    <scroll-view
      class="notify-page__scroll"
      scroll-y
      @scrolltolower="onLoadMore"
    >
      <!-- 加载中状态 -->
      <view v-if="loading" class="notify-page__loading">
        <u-loading-icon :size="36" color="#007AFF" mode="circle" />
        <text class="notify-page__loading-text">加载中...</text>
      </view>

      <!-- 空状态：无通知 -->
      <EmptyState
        v-else-if="notifyList.length === 0 && !loading"
        icon="bell"
        text="暂无通知"
        button-text=""
        :padding="240"
      />

      <!-- 通知列表 -->
      <view v-else class="notify-page__list">
        <!--
          每条通知是一个卡片
          点击可查看通知详情（或标记为已读）
        -->
        <view
          v-for="item in notifyList"
          :key="item.id"
          class="notify-page__item"
          :class="{ 'notify-page__item--unread': !item.isRead }"
          @tap="onNotifyClick(item)"
        >
          <!-- 左侧：通知类型图标 -->
          <view
            class="notify-page__icon"
            :class="'notify-page__icon--' + item.type"
          >
            <u-icon
              :name="getNotifyIcon(item.type)"
              :size="36"
              :color="getNotifyColor(item.type)"
            />
          </view>

          <!-- 中间：通知内容 -->
          <view class="notify-page__content">
            <!-- 标题行：标题 + 未读标记 -->
            <view class="notify-page__title-row">
              <text class="notify-page__title">{{ item.title }}</text>
              <!-- 未读标记：蓝色圆点 -->
              <view
                v-if="!item.isRead"
                class="notify-page__unread-dot"
              />
            </view>
            <!-- 内容摘要：1 行截断 -->
            <text class="notify-page__summary">{{ item.summary }}</text>
            <!-- 时间 -->
            <text class="notify-page__time">{{ formatTime(item.createTime) }}</text>
          </view>

          <!-- 右侧：箭头 -->
          <u-icon
            name="arrow-right"
            :size="24"
            color="#C7C7CC"
            class="notify-page__arrow"
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
import { ref } from 'vue'                              // Vue 3 响应式 API
import { onLoad } from '@dcloudio/uni-app'             // uni-app 页面生命周期
import EmptyState from '@/components/basics/EmptyState.vue'   // 空状态组件
import LoadingMore from '@/components/basics/LoadingMore.vue' // 加载更多组件

// ==================== 响应式数据 ====================

// 通知列表数据
const notifyList = ref([])
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
 * 页面加载时获取通知列表
 * 支持下拉刷新和上拉加载更多
 */
onLoad(() => {
  // 加载通知列表
  loadNotifyList(true)
})

// ==================== 数据加载 ====================

/**
 * 加载通知列表
 * 注意：实际项目中应使用专门的 API（如 getNotifyList）
 * 此处使用模拟数据展示
 *
 * @param {boolean} isRefresh - 是否为刷新（true 时重置分页）
 */
const loadNotifyList = async (isRefresh = false) => {
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
    // 模拟数据（实际项目中调用后端 API）
    // 示例：const res = await getNotifyList({ page: currentPage.value, size: 10 })
    const mockData = generateMockNotifies(currentPage.value)

    if (isRefresh) {
      notifyList.value = mockData
    } else {
      notifyList.value = [...notifyList.value, ...mockData]
    }

    // 模拟只有 2 页数据
    hasMore.value = currentPage.value < 2
    loadMoreStatus.value = hasMore.value ? '' : 'noMore'
    currentPage.value++

  } catch (error) {
    console.error('[Notify] 加载通知列表失败:', error)
    loadMoreStatus.value = 'error'
  } finally {
    loading.value = false
  }
}

// ==================== 模拟数据 ====================

/**
 * 生成模拟通知数据
 * 实际项目中应替换为后端 API 调用
 *
 * @param {number} page - 页码
 * @returns {Object[]} 通知列表
 */
const generateMockNotifies = (page) => {
  // 第一页数据
  if (page === 1) {
    return [
      {
        id: 1,
        type: 'order',                           // 通知类型：订单
        title: '订单已发货',
        summary: '您购买的商品【iPhone 15 Pro Max】已由顺丰快递发出，运单号：SF1234567890，请注意查收。',
        createTime: '2026-07-20 10:30:00',
        isRead: false                            // 未读
      },
      {
        id: 2,
        type: 'system',                          // 通知类型：系统
        title: '系统维护通知',
        summary: '系统将于2026年7月21日凌晨2:00-4:00进行维护升级，届时部分功能可能暂时无法使用，敬请谅解。',
        createTime: '2026-07-19 18:00:00',
        isRead: true                             // 已读
      },
      {
        id: 3,
        type: 'activity',                        // 通知类型：活动
        title: '夏日大促活动上线',
        summary: '全场商品满299减50，满599减120！新品首发享8折优惠，限时抢购，错过再等一年！',
        createTime: '2026-07-18 09:00:00',
        isRead: false                            // 未读
      },
      {
        id: 4,
        type: 'order',                           // 通知类型：订单
        title: '订单支付成功',
        summary: '您已成功支付订单 TM20260718001，金额 ¥6,999.00，商家正在准备发货中。',
        createTime: '2026-07-18 08:45:00',
        isRead: true                             // 已读
      },
      {
        id: 5,
        type: 'logistics',                       // 通知类型：物流
        title: '包裹已签收',
        summary: '您的包裹【蓝牙耳机】已于2026年7月17日签收，感谢您的购买，期待您的评价。',
        createTime: '2026-07-17 14:20:00',
        isRead: true                             // 已读
      },
      {
        id: 6,
        type: 'system',                          // 通知类型：系统
        title: '账号安全提醒',
        summary: '检测到您的账号在新设备上登录，如非本人操作，请及时修改密码保障账号安全。',
        createTime: '2026-07-16 11:00:00',
        isRead: true                             // 已读
      }
    ]
  }
  // 第二页数据
  return [
    {
      id: 7,
      type: 'activity',
      title: '新品首发预告',
      summary: '下周将有重磅新品发布，敬请期待！预约可享首发优惠价。',
      createTime: '2026-07-10 10:00:00',
      isRead: true
    },
    {
      id: 8,
      type: 'logistics',
      title: '快递已到达',
      summary: '您的快递已到达菜鸟驿站，请凭取件码 3-2-5678 在24小时内取件。',
      createTime: '2026-07-10 08:30:00',
      isRead: true
    }
  ]
}

// ==================== 工具函数 ====================

/**
 * 获取通知类型对应的图标名称（uView 图标库）
 *
 * @param {string} type - 通知类型
 * @returns {string} 图标名称
 */
const getNotifyIcon = (type) => {
  const iconMap = {
    system: 'info-circle',           // 系统通知：信息图标
    order: 'order',                  // 订单通知：订单图标
    activity: 'volume',              // 活动通知：喇叭图标
    logistics: 'car'                 // 物流通知：车辆图标
  }
  return iconMap[type] || 'bell'     // 默认：铃铛图标
}

/**
 * 获取通知类型对应的图标颜色
 *
 * @param {string} type - 通知类型
 * @returns {string} 颜色值
 */
const getNotifyColor = (type) => {
  const colorMap = {
    system: '#007AFF',               // 系统通知：主题蓝
    order: '#FF9500',                // 订单通知：橙色
    activity: '#FF3B30',             // 活动通知：红色
    logistics: '#34C759'             // 物流通知：绿色
  }
  return colorMap[type] || '#8E8E93' // 默认：灰色
}

/**
 * 格式化通知时间
 * 根据时间距离现在的远近显示不同格式：
 * - 今天：显示 HH:mm
 * - 昨天：显示"昨天 HH:mm"
 * - 今年内：显示 MM-DD HH:mm
 * - 更早：显示 YYYY-MM-DD
 *
 * @param {string} timeStr - 时间字符串
 * @returns {string} 格式化后的时间
 */
const formatTime = (timeStr) => {
  if (!timeStr) return '--'

  const now = new Date()
  const time = new Date(timeStr.replace(/-/g, '/')) // 兼容 iOS Safari

  // 计算时间差（毫秒）
  const diffMs = now - time
  const diffMinutes = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  // 1 小时内：显示"X分钟前"
  if (diffMinutes < 60) {
    if (diffMinutes < 1) return '刚刚'
    return `${diffMinutes}分钟前`
  }

  // 今天内：显示"X小时前"
  if (diffHours < 24 && now.getDate() === time.getDate()) {
    return `${diffHours}小时前`
  }

  // 昨天：显示"昨天 HH:mm"
  if (diffDays < 2) {
    const h = String(time.getHours()).padStart(2, '0')
    const m = String(time.getMinutes()).padStart(2, '0')
    return `昨天 ${h}:${m}`
  }

  // 今年内：显示 MM-DD
  if (now.getFullYear() === time.getFullYear()) {
    const month = String(time.getMonth() + 1).padStart(2, '0')
    const day = String(time.getDate()).padStart(2, '0')
    return `${month}-${day}`
  }

  // 更早：显示 YYYY-MM-DD
  const year = time.getFullYear()
  const month = String(time.getMonth() + 1).padStart(2, '0')
  const day = String(time.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// ==================== 事件处理 ====================

/**
 * 点击通知条目
 * 标记为已读 + 跳转详情（或执行对应操作）
 *
 * @param {Object} item - 通知数据
 */
const onNotifyClick = (item) => {
  // 标记为已读
  if (!item.isRead) {
    item.isRead = true
  }

  // 根据不同类型执行不同操作
  switch (item.type) {
    case 'order':
      // 跳转到订单详情（需要从 summary 中提取订单 ID）
      uni.showToast({ title: '跳转到订单详情', icon: 'none' })
      break
    case 'logistics':
      // 查看物流详情
      uni.showToast({ title: '查看物流详情', icon: 'none' })
      break
    case 'activity':
      // 跳转到活动页面
      uni.showToast({ title: '查看活动详情', icon: 'none' })
      break
    case 'system':
      // 系统通知：仅标记已读
      uni.showToast({ title: '已标记为已读', icon: 'success', duration: 1500 })
      break
    default:
      break
  }
}

/**
 * 上拉加载更多
 */
const onLoadMore = () => {
  loadNotifyList(false)
}
</script>

<style lang="scss" scoped>
// ==================== 页面容器 ====================
.notify-page {
  min-height: 100vh;                          // 最小高度撑满屏幕
  background: $bg-color;                      // 页面背景色：#F2F2F7
  display: flex;
  flex-direction: column;
}

// ==================== 滚动区域 ====================
.notify-page__scroll {
  flex: 1;                                    // 填充剩余空间
}

// ==================== 加载中状态 ====================
.notify-page__loading {
  @include flex-center;                       // 使用混入：水平垂直居中
  flex-direction: column;
  padding: 200rpx 0;                          // 上下留白
}

.notify-page__loading-text {
  font-size: $font-sm;                        // 24rpx
  color: $text-muted;                         // 次要文字色：#8E8E93
  margin-top: $spacing-md;                    // 顶部间距：24rpx
}

// ==================== 通知列表 ====================
.notify-page__list {
  padding: 0 16rpx;                           // 左右内边距
}

// 通知条目
.notify-page__item {
  display: flex;
  align-items: flex-start;                    // 顶部对齐（多行内容）
  gap: $spacing-sm;                           // 间距：16rpx
  background: $bg-white;                      // 白色背景
  border-radius: $radius-md;                  // 圆角：16rpx
  padding: 24rpx;                             // 内边距：24rpx
  margin-top: 16rpx;                          // 顶部间距
  box-shadow: $shadow-sm;                     // 小阴影
  position: relative;                         // 相对定位（未读标记）

  // 未读状态：浅蓝背景
  &--unread {
    background: $primary-light;               // 浅蓝背景：#E5F1FF
  }
}

// ==================== 通知图标 ====================
.notify-page__icon {
  width: 72rpx;                               // Figma 36px -> 72rpx
  height: 72rpx;
  border-radius: 50%;                         // 圆形
  @include flex-center;                       // 使用混入：水平垂直居中
  flex-shrink: 0;                             // 不压缩

  // 系统通知：浅蓝背景
  &--system {
    background: $primary-light;               // 浅蓝背景：#E5F1FF
  }

  // 订单通知：浅橙背景
  &--order {
    background: #fff7ed;                      // 浅橙背景
  }

  // 活动通知：浅红背景
  &--activity {
    background: #fef2f2;                      // 浅红背景
  }

  // 物流通知：浅绿背景
  &--logistics {
    background: #f0fdf4;                      // 浅绿背景
  }
}

// ==================== 通知内容 ====================
.notify-page__content {
  flex: 1;                                    // 填充剩余空间
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;                           // 间距：8rpx
  min-width: 0;                               // 允许内容收缩
}

// 标题行
.notify-page__title-row {
  display: flex;
  align-items: center;
  gap: $spacing-xs;                           // 间距：8rpx
}

// 标题文字
.notify-page__title {
  font-size: $font-md;                        // 28rpx
  font-weight: $font-weight-bold;             // 600 字重
  color: $text-color;                         // 主文字色：#1D1D1F
  @include text-ellipsis;                     // 使用混入：单行截断
}

// 未读标记（蓝色圆点）
.notify-page__unread-dot {
  width: 12rpx;                               // 圆点大小
  height: 12rpx;
  border-radius: 50%;                         // 圆形
  background: $primary-color;                 // 主题蓝：#007AFF
  flex-shrink: 0;                             // 不压缩
}

// 内容摘要
.notify-page__summary {
  font-size: $font-sm;                        // 24rpx
  color: $text-muted;                         // 次要文字色：#8E8E93
  line-height: 1.6;
  @include text-ellipsis-multi(2);            // 使用混入：2 行截断
}

// 时间
.notify-page__time {
  font-size: $font-xs;                        // 20rpx
  color: $text-light;                         // 浅色文字：#C7C7CC
}

// ==================== 右侧箭头 ====================
.notify-page__arrow {
  flex-shrink: 0;                             // 不压缩
  align-self: center;                         // 垂直居中
}
</style>