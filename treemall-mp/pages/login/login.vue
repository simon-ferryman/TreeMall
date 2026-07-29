<!--
  pages/login/login.vue — 登录页

  【设计思想】
  小程序的登录流程与 Web 端不同，依赖于微信的 wx.login() 获取临时 code，
  然后用 code 换取后端的 JWT Token。开发阶段使用 dev-login 接口模拟登录。

  【页面流程】
  1. 用户点击"微信一键登录" → wx.login() 获取 code
  2. code 发送到后端 → 后端返回 Token + 用户信息
  3. Token 存入 userStore → 跳转首页或来源页

  【对应后端】AuthController
  - 生产环境：POST /auth/login { code }
  - 开发环境：POST /auth/dev-login { userId, role }
-->
<template>
  <view class="login-page">
    <!-- ==================== Logo 区域 ==================== -->
    <view class="login-page__logo-area">
      <!-- 应用 Logo（使用文字占位，后续替换为图片） -->
      <view class="login-page__logo">
        <text class="login-page__logo-text">TM</text>
      </view>
      <!-- 应用名称 -->
      <text class="login-page__app-name">TreeMall 小商城</text>
      <!-- 应用描述 -->
      <text class="login-page__app-desc">品质生活，从这里开始</text>
    </view>

    <!-- ==================== 登录方式 ==================== -->
    <view class="login-page__actions">
      <!--
        微信一键登录按钮
        绿色按钮，符合微信官方设计规范
      -->
      <button
        class="login-page__wx-btn"
        open-type="getUserInfo"
        @getuserinfo="onWxLogin"
        :loading="wxLoading"
        :disabled="wxLoading"
      >
        <u-icon name="weixin-fill" :size="36" color="#ffffff" />
        <text>微信一键登录</text>
      </button>

      <!-- 登录协议提示 -->
      <text class="login-page__agreement">
        登录即表示同意《用户协议》和《隐私政策》
      </text>
    </view>

    <!-- ==================== 开发环境模拟登录 ==================== -->
    <!--
      开发阶段使用 dev-login 接口绕过微信登录
      生产环境需要移除整个区块
    -->
    <view class="login-page__dev-area">
      <view class="login-page__dev-divider">
        <view class="login-page__dev-line" />
        <text class="login-page__dev-text">开发环境模拟登录</text>
        <view class="login-page__dev-line" />
      </view>

      <!-- 用户ID输入 -->
      <view class="login-page__dev-form">
        <view class="login-page__dev-form-item">
          <text class="login-page__dev-label">用户ID</text>
          <input
            class="login-page__dev-input"
            v-model="devUserId"
            type="number"
            placeholder="请输入用户ID（如 1）"
          />
        </view>

        <!-- 角色选择 -->
        <view class="login-page__dev-form-item">
          <text class="login-page__dev-label">角色</text>
          <view class="login-page__dev-radio-group">
            <view
              class="login-page__dev-radio"
              :class="{ 'login-page__dev-radio--active': devRole === 'consumer' }"
              @tap="devRole = 'consumer'"
            >
              <text>普通用户</text>
            </view>
            <view
              class="login-page__dev-radio"
              :class="{ 'login-page__dev-radio--active': devRole === 'merchant' }"
              @tap="devRole = 'merchant'"
            >
              <text>商户</text>
            </view>
          </view>
        </view>

        <!-- 模拟登录按钮 -->
        <button
          class="login-page__dev-btn"
          :loading="devLoading"
          :disabled="devLoading || !devUserId"
          @tap="onDevLogin"
        >
          模拟登录
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
/**
 * 【页面级别说明】
 * 登录页使用 onLoad 检查是否已登录（已登录直接跳转首页）
 * 使用 onUnload 清理不必要的状态
 */
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

// ==================== 导入依赖 ====================
// 用户 Store：管理登录状态和 Token
import { useUserStore } from '@/stores/user'
// 认证 API：调用后端登录接口
import { wxLogin, devLogin } from '@/api/auth'

// ==================== Store 实例 ====================
const userStore = useUserStore()

// ==================== 响应式数据 ====================

// 微信登录加载状态
const wxLoading = ref(false)

// 开发环境模拟登录
const devUserId = ref('1')          // 默认用户ID为 1
const devRole = ref('consumer')         // 默认角色为普通用户
const devLoading = ref(false)

// ==================== 页面生命周期 ====================

/**
 * onLoad：页面加载时检查登录状态
 * 如果已登录，直接跳转到首页（避免重复登录）
 */
onLoad(() => {
  // 如果已有 Token，说明已登录，直接跳转首页
  if (userStore.isLogin) {
    // reLaunch：关闭所有页面，打开首页（防止用户按返回键回到登录页）
    uni.reLaunch({
      url: '/pages/index/index'
    })
  }
})

// ==================== 事件处理 ====================

/**
 * 微信一键登录
 *
 * 流程：
 * 1. 调用 wx.login() 获取临时 code
 * 2. 调用后端 wxLogin(code) 换取 Token
 * 3. 将 Token 和用户信息存入 userStore
 * 4. 跳转到来源页或首页
 *
 * 注意：getuserinfo 事件在微信新版 API 中已废弃，
 * 生产环境建议使用 wx.getUserProfile() 或 button 的 open-type="getPhoneNumber"
 */
const onWxLogin = async (e) => {
  wxLoading.value = true

  try {
    // 1. 调用微信登录获取临时 code
    const loginRes = await uni.login({
      provider: 'weixin'            // 指定微信登录
    })

    // 2. 将 code 发送到后端，换取 JWT Token
    const res = await wxLogin(loginRes.code)

    // 3. 存储 Token 和用户信息到 Pinia Store
    // 后端返回 { token, userInfo }，与 store.login() 参数格式一致
    userStore.login(res)

    // 4. 登录成功提示
    uni.showToast({
      title: '登录成功',
      icon: 'success',
      duration: 1500
    })

    // 5. 延迟跳转（等待 Toast 显示）
    setTimeout(() => {
      // 跳转到来源页或首页
      navigateAfterLogin()
    }, 1500)

  } catch (error) {
    // 登录失败处理
    console.error('[Login] 微信登录失败:', error)
    uni.showToast({
      title: '登录失败，请重试',
      icon: 'none',
      duration: 2000
    })
  } finally {
    wxLoading.value = false
  }
}

/**
 * 开发环境模拟登录
 *
 * 使用 dev-login 接口，直接传入 userId 和 role 获取 Token
 * 生产环境需要移除此功能
 */
const onDevLogin = async () => {
  // 参数校验
  if (!devUserId.value) {
    uni.showToast({ title: '请输入用户ID', icon: 'none' })
    return
  }

  devLoading.value = true

  try {
    // 调用 dev-login 接口
    const res = await devLogin({
      userId: Number(devUserId.value),  // 转换为数字类型
      role: devRole.value
    })

    // 存储 Token 和用户信息
    // 后端返回 { token, userInfo }，与 store.login() 参数格式一致
    userStore.login(res)

    uni.showToast({
      title: '模拟登录成功',
      icon: 'success',
      duration: 1500
    })

    setTimeout(() => {
      navigateAfterLogin()
    }, 1500)

  } catch (error) {
    console.error('[Login] 模拟登录失败:', error)
    uni.showToast({
      title: '登录失败：' + (error.message || '未知错误'),
      icon: 'none',
      duration: 2000
    })
  } finally {
    devLoading.value = false
  }
}

/**
 * 登录成功后跳转
 *
 * 跳转逻辑：
 * 1. 如果有来源页（redirect），跳转到来源页
 * 2. 否则跳转到首页
 *
 * 使用 reLaunch 关闭所有页面，防止用户按返回键回到登录页
 */
const navigateAfterLogin = () => {
  // 获取来源页参数（如果有）
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const redirect = currentPage?.options?.redirect

  if (redirect) {
    // 有来源页：跳转到来源页
    uni.reLaunch({
      url: decodeURIComponent(redirect)  // URL 解码
    })
  } else {
    // 无来源页：跳转到首页
    uni.reLaunch({
      url: '/pages/index/index'
    })
  }
}
</script>

<style lang="scss" scoped>
// ==================== 页面容器 ====================
.login-page {
  min-height: 100vh;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 48rpx;
}

// ==================== Logo 区域 ====================
.login-page__logo-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 160rpx;               // 顶部留白
  margin-bottom: 80rpx;
}

// Logo 圆形图标
.login-page__logo {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;               // 圆形
  background: #007AFF;              // 主题蓝背景
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.3);  // 蓝色阴影
}

// Logo 文字
.login-page__logo-text {
  font-size: 48rpx;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: 2rpx;             // 字间距
}

// 应用名称
.login-page__app-name {
  font-size: 36rpx;
  font-weight: 700;
  color: #1D1D1F;
  margin-bottom: 12rpx;
}

// 应用描述
.login-page__app-desc {
  font-size: 26rpx;
  color: #9ca3af;
}

// ==================== 登录按钮区域 ====================
.login-page__actions {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

// 微信登录按钮
.login-page__wx-btn {
  width: 100%;
  background: #07c160;              // 微信绿色
  color: #ffffff;
  font-size: 30rpx;
  font-weight: 600;
  border-radius: 48rpx;             // 大圆角按钮
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;                       // 图标和文字间距
  border: none;
  margin-bottom: 24rpx;

  // 按钮按下效果
  &:active {
    opacity: 0.85;
  }

  // 加载中/禁用状态
  &[disabled] {
    opacity: 0.6;
  }
}

// 协议文字
.login-page__agreement {
  font-size: 22rpx;
  color: #9ca3af;
  text-align: center;
  line-height: 1.6;
}

// ==================== 开发环境区域 ====================
.login-page__dev-area {
  width: 100%;
  margin-top: 80rpx;
}

// 分割线
.login-page__dev-divider {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 32rpx;
}

.login-page__dev-line {
  flex: 1;
  height: 1rpx;
  background: #E5E5EA;
}

.login-page__dev-text {
  font-size: 22rpx;
  color: #9ca3af;
  flex-shrink: 0;
}

// 表单
.login-page__dev-form {
  background: #f9fafb;
  border-radius: 12rpx;
  padding: 24rpx;
}

// 表单项
.login-page__dev-form-item {
  margin-bottom: 20rpx;
}

// 标签
.login-page__dev-label {
  font-size: 26rpx;
  color: #1D1D1F;
  font-weight: 500;
  margin-bottom: 12rpx;
  display: block;
}

// 输入框
.login-page__dev-input {
  width: 100%;
  height: 72rpx;
  background: #ffffff;
  border: 1rpx solid #E5E5EA;
  border-radius: 8rpx;
  padding: 0 20rpx;
  font-size: 26rpx;
  color: #1D1D1F;
  box-sizing: border-box;
}

// 角色选择器
.login-page__dev-radio-group {
  display: flex;
  gap: 16rpx;
}

// 角色选项
.login-page__dev-radio {
  flex: 1;
  height: 72rpx;
  background: #ffffff;
  border: 1rpx solid #E5E5EA;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  color: #8E8E93;
  transition: all 0.2s;

  // 选中状态
  &--active {
    background: #E5F1FF;            // 浅蓝背景
    border-color: #007AFF;          // 蓝色边框
    color: #007AFF;                 // 蓝色文字
    font-weight: 600;
  }
}

// 模拟登录按钮
.login-page__dev-btn {
  width: 100%;
  background: #007AFF;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 600;
  border-radius: 40rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  margin-top: 8rpx;

  &:active {
    opacity: 0.85;
  }

  &[disabled] {
    background: #d1d5db;
    color: #9ca3af;
  }
}
</style>
