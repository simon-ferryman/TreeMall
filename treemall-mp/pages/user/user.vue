<!--
  pages/user/user.vue — 个人中心页（TabBar 页面）

  【设计思想】
  个人中心是用户的个人信息和功能入口集合页，严格按 Figma 设计稿实现。
  Figma 设计稿关键特征：
  - 白色背景头部，简洁清爽
  - 头像（80×80px 圆形）+ 昵称 + PLUS会员徽章 + 设置按钮
  - 订单状态行：待付款/待发货/待收货/退换货
  - AI 推荐卡片
  - 功能菜单：收藏夹/浏览记录/地址管理/优惠券/帮助中心/消息通知

  【Figma 设计令牌】
  主色 #007AFF | 文字 #1D1D1F | 次要文字 #8E8E93 | 背景 #F2F2F7 | 白色 #FFFFFF
  分隔线 #E5E5EA | 浅蓝 #E5F1FF | 设计宽度 375px | rpx=px*2
-->
<template>
  <view class="user-page">
    <!-- ==================== 可滚动内容 ==================== -->
    <scroll-view class="user-page__scroll" scroll-y :enhanced="true" :show-scrollbar="false">

      <!-- ==================== 1. 用户信息头部 ==================== -->
      <!-- Figma: 白色背景，左侧头像 + 中间昵称&徽章 + 右侧设置按钮 -->
      <view class="user-page__header">
        <!-- 已登录状态 -->
        <template v-if="userStore.isLogin">
          <!-- 头像：80x80px 圆形，浅灰背景，人物图标 -->
          <view class="user-page__avatar">
            <image
              v-if="userStore.userInfo?.avatarUrl"
              class="user-page__avatar-img"
              :src="getImageUrl(userStore.userInfo.avatarUrl)"
              mode="aspectFill"
            />
            <u-icon v-else name="account" :size="40" color="#8E8E93" />
          </view>

          <!-- 昵称 + PLUS会员徽章 -->
          <view class="user-page__user-info">
            <view class="user-page__name-row">
				<text class="user-page__nickname">
				  {{ userStore.userInfo?.nickname || '未设置昵称' }}
				</text>
              <!-- PLUS会员徽章：蓝色背景 + 白色文字 -->
              <view class="user-page__member-badge" v-if="false">
                <text class="user-page__member-badge-text">PLUS会员</text>
              </view>
            </view>
            <!-- 角色标签 -->
            <text class="user-page__role-tag" v-if="userStore.userInfo?.role">
              {{ userStore.userInfo?.role === 'merchant' ? '商户' : '普通用户' }}
            </text>
          </view>
        </template>

        <!-- 未登录状态 -->
        <template v-else>
          <view class="user-page__avatar user-page__avatar--empty" @tap="onLogin">
            <u-icon name="account" :size="40" color="#C7C7CC" />
          </view>
          <view class="user-page__user-info" @tap="onLogin">
            <text class="user-page__login-text">点击登录</text>
            <text class="user-page__login-hint">登录后享受更多权益</text>
          </view>
        </template>

        <!-- 设置按钮（右侧齿轮图标） -->
        <view class="user-page__settings-btn" @tap="onSettings">
          <u-icon name="setting" :size="30" color="#1D1D1F" />
        </view>
      </view>

      <!-- ==================== 2. 订单状态行 ==================== -->
      <!-- Figma: 我的订单标题行 + 4个订单状态图标入口 -->
      <view class="user-page__card">
        <!-- 标题行 -->
        <view class="user-page__card-header">
          <text class="user-page__card-title">我的订单</text>
          <view class="user-page__card-more" @tap="onAllOrders">
            <text>全部订单</text>
            <u-icon name="arrow-right" :size="24" color="#8E8E93" />
          </view>
        </view>

        <!-- 订单状态图标行 -->
        <view class="user-page__order-row">
          <view
            v-for="entry in orderEntries"
            :key="entry.status"
            class="user-page__order-item"
            @tap="onOrderEntry(entry.status)"
          >
            <!-- 订单图标（带背景圆） -->
            <view class="user-page__order-icon-wrap">
              <u-icon :name="entry.icon" :size="44" color="#1D1D1F" />
            </view>
            <text class="user-page__order-label">{{ entry.label }}</text>
          </view>
        </view>
      </view>

      <!-- ==================== 3. AI 推荐卡片 ==================== -->
      <!-- Figma: 根据你的偏好，AI 为你精选了 6 件新品 -->
      <view class="user-page__card user-page__ai-card" @tap="onAiRecommend">
        <view class="user-page__ai-content">
          <view class="user-page__ai-icon-wrap">
            <u-icon name="star" :size="36" color="#007AFF" />
          </view>
          <view class="user-page__ai-text">
            <text class="user-page__ai-title">AI 智能推荐</text>
            <text class="user-page__ai-desc">根据你的偏好，AI 为你精选了 6 件新品</text>
          </view>
          <u-icon name="arrow-right" :size="28" color="#C7C7CC" />
        </view>
      </view>

      <!-- ==================== 4. 功能菜单 ==================== -->
      <!-- Figma: 收藏夹/浏览记录/地址管理/优惠券/帮助中心/消息通知 -->
      <view class="user-page__card">
        <view class="user-page__menu">
          <!-- 收藏夹 -->
          <view class="user-page__menu-item" @tap="onFavorites">
            <view class="user-page__menu-icon-wrap user-page__menu-icon-wrap--favorites">
              <u-icon name="heart" :size="36" color="#FF3B30" />
            </view>
            <text class="user-page__menu-text">收藏夹</text>
            <u-icon name="arrow-right" :size="24" color="#C7C7CC" />
          </view>

          <!-- 浏览记录 -->
          <view class="user-page__menu-item" @tap="onHistory">
            <view class="user-page__menu-icon-wrap user-page__menu-icon-wrap--history">
              <u-icon name="clock" :size="36" color="#FF9500" />
            </view>
            <text class="user-page__menu-text">浏览记录</text>
            <u-icon name="arrow-right" :size="24" color="#C7C7CC" />
          </view>

          <!-- 地址管理 -->
          <view class="user-page__menu-item" @tap="onAddressManage">
            <view class="user-page__menu-icon-wrap user-page__menu-icon-wrap--address">
              <u-icon name="map" :size="36" color="#007AFF" />
            </view>
            <text class="user-page__menu-text">地址管理</text>
            <u-icon name="arrow-right" :size="24" color="#C7C7CC" />
          </view>

          <!-- 优惠券 -->
          <view class="user-page__menu-item" @tap="onCoupon">
            <view class="user-page__menu-icon-wrap user-page__menu-icon-wrap--coupon">
              <u-icon name="coupon" :size="36" color="#34C759" />
            </view>
            <text class="user-page__menu-text">优惠券</text>
            <u-icon name="arrow-right" :size="24" color="#C7C7CC" />
          </view>

          <!-- 帮助中心 -->
          <view class="user-page__menu-item" @tap="onHelp">
            <view class="user-page__menu-icon-wrap user-page__menu-icon-wrap--help">
              <u-icon name="question-circle" :size="36" color="#8E8E93" />
            </view>
            <text class="user-page__menu-text">帮助中心</text>
            <u-icon name="arrow-right" :size="24" color="#C7C7CC" />
          </view>

          <!-- 消息通知 -->
          <view class="user-page__menu-item" @tap="onNotify">
            <view class="user-page__menu-icon-wrap user-page__menu-icon-wrap--notify">
              <u-icon name="bell" :size="36" color="#FF9500" />
            </view>
            <text class="user-page__menu-text">消息通知</text>
            <view class="user-page__menu-right">
              <!-- 未读消息红点 -->
              <view v-if="unreadNotifyCount > 0" class="user-page__badge">
                {{ unreadNotifyCount > 99 ? '99+' : unreadNotifyCount }}
              </view>
              <u-icon name="arrow-right" :size="24" color="#C7C7CC" />
            </view>
          </view>
        </view>
      </view>

      <!-- ==================== 5. 商户端入口（仅商户角色可见） ==================== -->
      <view class="user-page__card" v-if="userStore.isMerchant">
        <view class="user-page__card-header">
          <text class="user-page__card-title">商户管理</text>
        </view>
        <view class="user-page__menu">
          <!-- 商品管理 -->
          <view class="user-page__menu-item" @tap="onMerchantGoods">
            <view class="user-page__menu-icon-wrap user-page__menu-icon-wrap--merchant">
              <u-icon name="bag" :size="36" color="#059669" />
            </view>
            <text class="user-page__menu-text">商品管理</text>
            <u-icon name="arrow-right" :size="24" color="#C7C7CC" />
          </view>

          <!-- 订单管理 -->
          <view class="user-page__menu-item" @tap="onMerchantOrders">
            <view class="user-page__menu-icon-wrap user-page__menu-icon-wrap--merchant">
              <u-icon name="order" :size="36" color="#059669" />
            </view>
            <text class="user-page__menu-text">订单管理</text>
            <u-icon name="arrow-right" :size="24" color="#C7C7CC" />
          </view>
        </view>
      </view>

      <!-- ==================== 6. 退出登录 ==================== -->
      <view class="user-page__card" v-if="userStore.isLogin">
        <view class="user-page__logout-btn" @tap="onLogout">
          <text>退出登录</text>
        </view>
      </view>

      <!-- 底部留白 -->
      <view style="height: 40rpx;" />
    </scroll-view>
  </view>
</template>

<script setup>
// ==================== 导入依赖 ====================
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { getImageUrl } from '@/utils/image-url'
// ==================== Store 实例 ====================
const userStore = useUserStore()

// ==================== 订单入口配置 ====================
// Figma: 待付款/待发货/待收货/退换货
const orderEntries = [
  { status: 'pending', label: '待付款', icon: 'clock' },
  { status: 'paid', label: '待发货', icon: 'car' },
  { status: 'shipped', label: '待收货', icon: 'map' },
  { status: 'completed', label: '已完成', icon: 'file-text' }
]

// ==================== 未读通知数量 ====================
const unreadNotifyCount = ref(0)

// ==================== 页面生命周期 ====================

onShow(() => {
  // 每次显示时刷新用户信息
  // 可在此处刷新未读通知数量
})

// ==================== 事件处理 ====================

/** 点击登录 */
const onLogin = () => {
  uni.navigateTo({ url: '/pages/login/login' })
}

/** 点击设置 */
const onSettings = () => {
  uni.navigateTo({ url: '/pages/settings/settings' })
}

/** 查看全部订单 */
const onAllOrders = () => {
  uni.navigateTo({ url: '/pages/order/list' })
}

/** 点击订单状态入口 */
const onOrderEntry = (status) => {
  uni.navigateTo({ url: `/pages/order/list?status=${status}` })
}

/** AI 推荐 */
const onAiRecommend = () => {
  uni.navigateTo({ url: '/pages/category/category?tab=ai' })
}

/** 收藏夹 */
const onFavorites = () => {
  uni.navigateTo({ url: '/pages/favorites/favorites' })
}

/** 浏览记录 */
const onHistory = () => {
  uni.showToast({ title: '浏览记录功能开发中', icon: 'none' })
}

/** 地址管理 */
const onAddressManage = () => {
  uni.navigateTo({ url: '/pages/address/list' })
}

/** 优惠券 */
const onCoupon = () => {
  uni.showToast({ title: '优惠券功能开发中', icon: 'none' })
}

/** 帮助中心 */
const onHelp = () => {
  uni.showToast({ title: '帮助中心开发中', icon: 'none' })
}

/** 消息通知 */
const onNotify = () => {
  uni.navigateTo({ url: '/pages/notify/notify' })
}

/** 商户商品管理 */
const onMerchantGoods = () => {
  uni.navigateTo({ url: '/pages/merchant/goods-manage/list' })
}

/** 商户订单管理 */
const onMerchantOrders = () => {
  uni.navigateTo({ url: '/pages/merchant/order-manage/list' })
}

/** 退出登录 */
const onLogout = () => {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        uni.showToast({ title: '已退出登录', icon: 'success' })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
// ==================== 页面容器 ====================
// Figma: 页面背景 #F2F2F7
.user-page {
  height: 100vh;
  background: #F2F2F7;
  display: flex;
  flex-direction: column;
}

.user-page__scroll {
  flex: 1;
}

// ==================== 1. 用户信息头部 ====================
// Figma: 白色背景头部，80px 头像，深色昵称 + PLUS会员徽章，右侧设置按钮
.user-page__header {
  background: #FFFFFF;
  padding: 32rpx 32rpx 28rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
}

// 头像：80x80px 圆形，浅灰背景 #F2F2F7
.user-page__avatar {
  width: 160rpx;               // Figma: 80px -> 160rpx
  height: 160rpx;
  border-radius: 50%;          // Figma: cornerRadius 999
  background: #F2F2F7;         // Figma: r:0.949, g:0.949, b:0.969
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;

  &--empty {
    background: #F2F2F7;
  }
}

.user-page__avatar-img {
  width: 100%;
  height: 100%;
  display: block;
}

// 用户信息区域
.user-page__user-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

// 昵称行：昵称 + PLUS会员徽章
.user-page__name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

// 昵称：20px, 600 字重，深色 #1D1D1F
.user-page__nickname {
  font-size: 40rpx;            // Figma: 20px -> 40rpx
  font-weight: 600;
  color: #1D1D1F;              // Figma: r:0.114, g:0.114, b:0.122
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

// PLUS会员徽章：蓝色背景 #007AFF + 白色文字
.user-page__member-badge {
  background: #007AFF;
  border-radius: 6rpx;
  padding: 4rpx 16rpx;
  flex-shrink: 0;

  &-text {
    font-size: 22rpx;          // Figma: 11px -> 22rpx
    font-weight: 700;
    color: #FFFFFF;
    line-height: 1.4;
    letter-spacing: 0.44rpx;
  }
}

// 角色标签
.user-page__role-tag {
  font-size: 24rpx;
  color: #8E8E93;
  line-height: 1.4;
}

// 未登录状态
.user-page__login-text {
  font-size: 40rpx;
  font-weight: 600;
  color: #1D1D1F;
  line-height: 1.3;
  display: block;
}

.user-page__login-hint {
  font-size: 24rpx;
  color: #8E8E93;
  line-height: 1.4;
  display: block;
}

// 设置按钮（右侧齿轮图标）
.user-page__settings-btn {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

// ==================== 通用卡片 ====================
.user-page__card {
  background: #FFFFFF;
  margin: 16rpx 16rpx 0;
  border-radius: 16rpx;
  overflow: hidden;
}

// 卡片标题行
.user-page__card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 24rpx 0;
}

// 卡片标题
.user-page__card-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1D1D1F;
}

// "全部订单" / "更多" 按钮
.user-page__card-more {
  display: flex;
  align-items: center;
  gap: 4rpx;
  font-size: 24rpx;
  color: #8E8E93;
}

// ==================== 2. 订单状态行 ====================
// Figma: 4 个图标均匀分布
.user-page__order-row {
  display: flex;
  justify-content: space-around;
  padding: 28rpx 16rpx 24rpx;
}

// 单个订单入口
.user-page__order-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

// 订单图标背景圆
.user-page__order-icon-wrap {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: #F2F2F7;
  display: flex;
  align-items: center;
  justify-content: center;
}

// 订单标签
.user-page__order-label {
  font-size: 22rpx;
  color: #8E8E93;
}

// ==================== 3. AI 推荐卡片 ====================
.user-page__ai-card {
  background: linear-gradient(135deg, #E5F1FF 0%, #F0F7FF 100%);
}

.user-page__ai-content {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 28rpx 24rpx;
}

.user-page__ai-icon-wrap {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: rgba(0, 122, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-page__ai-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.user-page__ai-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1D1D1F;
}

.user-page__ai-desc {
  font-size: 24rpx;
  color: #8E8E93;
  line-height: 1.4;
}

// ==================== 4. 功能菜单 ====================
.user-page__menu {
  display: flex;
  flex-direction: column;
}

// 菜单项
.user-page__menu-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 26rpx 24rpx;
  border-bottom: 1rpx solid #F2F2F7;

  &:last-child {
    border-bottom: none;
  }
}

// 菜单图标背景
.user-page__menu-icon-wrap {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &--favorites {
    background: #FEF2F2;
  }

  &--history {
    background: #FFF7ED;
  }

  &--address {
    background: #E5F1FF;
  }

  &--coupon {
    background: #F0FDF4;
  }

  &--help {
    background: #F2F2F7;
  }

  &--notify {
    background: #FFF7ED;
  }

  &--merchant {
    background: #ECFDF5;
  }
}

// 菜单文字
.user-page__menu-text {
  flex: 1;
  font-size: 28rpx;
  color: #1D1D1F;
}

// 菜单右侧区域
.user-page__menu-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

// 红点徽章
.user-page__badge {
  min-width: 36rpx;
  height: 36rpx;
  border-radius: 18rpx;
  background: #FF3B30;
  color: #FFFFFF;
  font-size: 20rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
  line-height: 1;
}

// ==================== 6. 退出登录按钮 ====================
.user-page__logout-btn {
  padding: 28rpx 24rpx;
  text-align: center;
  font-size: 28rpx;
  color: #FF3B30;
  font-weight: 500;
}
</style>
