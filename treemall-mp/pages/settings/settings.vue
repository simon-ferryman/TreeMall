<!--
  pages/settings/settings.vue -- 设置页

  【设计思想】
  设置页是个人中心的功能入口，包含以下菜单：
  1. 个人信息：跳转到个人信息编辑页
  2. 账号安全：跳转到账号安全设置页
  3. 关于我们：跳转到关于页面
  4. 退出登录：底部红色按钮，点击弹出确认弹窗

  使用 iOS 风格的菜单列表布局，每个菜单项包含图标 + 文字 + 右侧箭头。

  【Figma 设计令牌】
  主色 #007AFF | 文字 #1D1D1F | 次要文字 #8E8E93 | 背景 #F2F2F7 | 白色 #FFFFFF
  分隔线 #E5E5EA | 浅蓝 #E5F1FF | 设计宽度 375px | rpx=px*2

  【使用场景】
  - 个人中心页点击"设置"入口
-->
<template>
  <view class="settings-page">
    <!-- ==================== 菜单列表 ==================== -->
    <view class="settings-page__section">
      <!-- 个人信息 -->
      <view class="settings-page__menu-item" @tap="onNavigate('profile')">
        <!-- 左侧图标 -->
        <view class="settings-page__menu-icon settings-page__menu-icon--blue">
          <u-icon name="account" :size="36" color="#007AFF" />
        </view>
        <!-- 菜单文字 -->
        <text class="settings-page__menu-label">个人信息</text>
        <!-- 右侧箭头 -->
        <u-icon name="arrow-right" :size="24" color="#C7C7CC" />
      </view>

      <!-- 分隔线 -->
      <view class="settings-page__divider" />

      <!-- 账号安全 -->
      <view class="settings-page__menu-item" @tap="onNavigate('security')">
        <view class="settings-page__menu-icon settings-page__menu-icon--green">
          <u-icon name="lock" :size="36" color="#34C759" />
        </view>
        <text class="settings-page__menu-label">账号安全</text>
        <u-icon name="arrow-right" :size="24" color="#C7C7CC" />
      </view>

      <!-- 分隔线 -->
      <view class="settings-page__divider" />

      <!-- 关于我们 -->
      <view class="settings-page__menu-item" @tap="onNavigate('about')">
        <view class="settings-page__menu-icon settings-page__menu-icon--orange">
          <u-icon name="info-circle" :size="36" color="#FF9500" />
        </view>
        <text class="settings-page__menu-label">关于我们</text>
        <!-- 版本号 -->
        <view class="settings-page__menu-extra">
          <text class="settings-page__version">v1.0.0</text>
          <u-icon name="arrow-right" :size="24" color="#C7C7CC" />
        </view>
      </view>
    </view>

    <!-- ==================== 退出登录按钮 ==================== -->
    <!--
      退出登录使用红色按钮，与普通菜单区分
      点击弹出确认弹窗，防止误操作
    -->
    <view class="settings-page__section">
      <view class="settings-page__logout-btn" @tap="onLogout">
        退出登录
      </view>
    </view>
  </view>
</template>

<script setup>
// ==================== 导入依赖 ====================
import { onLoad } from '@dcloudio/uni-app'        // uni-app 页面生命周期
import { useUserStore } from '@/stores/user'      // 用户状态管理 Store

// ==================== Store 实例 ====================
// 获取用户 Store 实例，用于退出登录操作
const userStore = useUserStore()

// ==================== 页面生命周期 ====================

onLoad(() => {
  // 页面加载时无需额外操作
  // 所有逻辑由事件处理函数完成
})

// ==================== 事件处理 ====================

/**
 * 导航到对应页面
 *
 * @param {string} target - 目标页面标识
 *   'profile' - 个人信息
 *   'security' - 账号安全
 *   'about' - 关于我们
 */
const onNavigate = (target) => {
  switch (target) {
    case 'profile':
      // 跳转到个人信息编辑页
      // 实际项目中应跳转到 pages/user/profile 或类似页面
      uni.showToast({ title: '个人信息编辑功能开发中', icon: 'none' })
      break

    case 'security':
      // 跳转到账号安全设置页
      // 实际项目中应跳转到 pages/user/security 或类似页面
      uni.showToast({ title: '账号安全设置功能开发中', icon: 'none' })
      break

    case 'about':
      // 跳转到关于页面
      // 实际项目中应跳转到 pages/about/about 或类似页面
      uni.showToast({ title: '关于页面开发中', icon: 'none' })
      break

    default:
      break
  }
}

/**
 * 退出登录
 * 弹出确认弹窗，确认后清除用户状态并跳转到登录页
 */
const onLogout = () => {
  // 弹出确认弹窗，防止误操作
  uni.showModal({
    title: '退出登录',
    content: '确定要退出登录吗？退出后需要重新登录。',
    confirmText: '退出',                      // 确认按钮文字
    cancelText: '取消',                       // 取消按钮文字
    confirmColor: '#FF3B30',                  // 确认按钮颜色：红色（危险操作）
    success: (res) => {
      if (res.confirm) {
        // 用户点击"退出"
        handleLogout()
      }
      // 取消则不做任何操作
    }
  })
}

/**
 * 执行退出登录操作
 * 1. 清除用户 Store 状态（Token、用户信息）
 * 2. 跳转到登录页
 * 3. 清空页面栈，防止用户通过返回键回到需要登录的页面
 */
const handleLogout = () => {
  // 调用 Store 的 logout action 清除用户状态
  // 此操作会清除 token、userId、role、userInfo，并移除 Storage 中的 token
  userStore.logout()

  // 提示用户已退出
  uni.showToast({
    title: '已退出登录',
    icon: 'success',
    duration: 1500
  })

  // 延迟跳转，让用户看到提示信息
  setTimeout(() => {
    // 使用 reLaunch 跳转到登录页
    // reLaunch 会关闭所有页面，打开新页面
    // 这样可以防止用户通过返回键回到需要登录的页面
    uni.reLaunch({
      url: '/pages/login/login'
    })
  }, 1500)
}
</script>

<style lang="scss" scoped>
// ==================== 页面容器 ====================
.settings-page {
  min-height: 100vh;                          // 最小高度撑满屏幕
  background: $bg-color;                      // 页面背景色：#F2F2F7
  padding: 16rpx;                             // 四周内边距：16rpx
}

// ==================== 菜单分区 ====================
// 白色圆角卡片容器
.settings-page__section {
  background: $bg-white;                      // 白色背景
  border-radius: $radius-md;                  // 圆角：16rpx
  overflow: hidden;                           // 隐藏超出圆角的内容
  margin-bottom: 16rpx;                       // 底部间距：16rpx
  box-shadow: $shadow-sm;                     // 小阴影
}

// ==================== 菜单项 ====================
// 菜单项：图标 + 文字 + 箭头
.settings-page__menu-item {
  display: flex;
  align-items: center;
  padding: 24rpx;                             // 内边距：24rpx
  gap: $spacing-sm;                           // 间距：16rpx
  min-height: 96rpx;                          // 最小高度：96rpx（Figma 48px * 2）
}

// 菜单图标
.settings-page__menu-icon {
  width: 64rpx;                               // 图标容器宽度：64rpx
  height: 64rpx;                              // 图标容器高度：64rpx
  border-radius: $radius-sm;                  // 圆角：12rpx
  @include flex-center;                       // 使用混入：水平垂直居中
  flex-shrink: 0;                             // 不压缩

  // 蓝色：个人信息
  &--blue {
    background: $primary-light;               // 浅蓝背景：#E5F1FF
  }

  // 绿色：账号安全
  &--green {
    background: #f0fdf4;                      // 浅绿背景
  }

  // 橙色：关于我们
  &--orange {
    background: #fff7ed;                      // 浅橙背景
  }
}

// 菜单文字
.settings-page__menu-label {
  flex: 1;                                    // 填充剩余空间
  font-size: $font-md;                        // 28rpx
  color: $text-color;                         // 主文字色：#1D1D1F
  font-weight: $font-weight-medium;           // 500 字重
}

// 菜单右侧额外信息（版本号 + 箭头）
.settings-page__menu-extra {
  display: flex;
  align-items: center;
  gap: $spacing-xs;                           // 间距：8rpx
}

// 版本号
.settings-page__version {
  font-size: $font-sm;                        // 24rpx
  color: $text-muted;                         // 次要文字色：#8E8E93
}

// ==================== 分隔线 ====================
// 菜单项之间的分隔线（左侧留白，与图标对齐）
.settings-page__divider {
  height: 1rpx;
  background: $border-color;                  // 分隔线色：#E5E5EA
  margin-left: 104rpx;                        // 左侧留白：24rpx（padding）+ 64rpx（icon）+ 16rpx（gap）
}

// ==================== 退出登录按钮 ====================
// 红色按钮，与普通菜单区分，突出危险操作
.settings-page__logout-btn {
  @include flex-center;                       // 使用混入：水平垂直居中
  height: 96rpx;                              // 按钮高度：96rpx
  font-size: $font-lg;                        // 32rpx
  font-weight: $font-weight-medium;           // 500 字重
  color: $danger-color;                       // 危险红：#FF3B30
  background: $bg-white;                      // 白色背景
}
</style>