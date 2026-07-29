<!--
  pages/address/list.vue — 地址管理页

  【设计思想】
  地址管理是下单流程的前置条件，用户需要至少有一个地址才能下单。
  支持新增、编辑、删除、设为默认地址。
  地址数据不需要跨页面共享，直接调用 API 即可。

  【页面交互】
  - 列表展示所有地址
  - 默认地址显示"默认"标签
  - 左滑删除
  - 点击编辑跳转编辑页
  - 底部新增按钮
-->
<template>
  <view class="address-list-page">
    <!-- ==================== 空状态 ==================== -->
    <EmptyState
      v-if="addressList.length === 0 && !loading"
      icon="empty-address"
      text="还没有收货地址"
      button-text="新增地址"
      @action="onAddAddress"
    />

    <!-- ==================== 地址列表 ==================== -->
    <view v-else class="address-list-page__list">
      <view
        v-for="addr in addressList"
        :key="addr.id"
        class="address-list-page__card"
        @tap="onEditAddress(addr)"
      >
        <!-- 默认标签 -->
        <view class="address-list-page__card-header">
          <text class="address-list-page__receiver">{{ addr.receiverName }}</text>
          <text class="address-list-page__phone">{{ addr.receiverPhone }}</text>
          <view v-if="addr.isDefault" class="address-list-page__default-tag">
            默认
          </view>
        </view>

        <!-- 地址详情 -->
        <text class="address-list-page__detail">
          {{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detailAddress }}
        </text>

        <!-- 操作按钮 -->
        <view class="address-list-page__actions">
          <!-- 设为默认 -->
          <view
            v-if="!addr.isDefault"
            class="address-list-page__action-btn"
            @tap.stop="onSetDefault(addr)"
          >
            <u-icon name="checkmark-circle" :size="28" color="#007AFF" />
            <text>设为默认</text>
          </view>
          <!-- 编辑 -->
          <view class="address-list-page__action-btn" @tap.stop="onEditAddress(addr)">
            <u-icon name="edit-pen" :size="28" color="#8E8E93" />
            <text>编辑</text>
          </view>
          <!-- 删除 -->
          <view class="address-list-page__action-btn" @tap.stop="onDeleteAddress(addr)">
            <u-icon name="trash" :size="28" color="#ef4444" />
            <text>删除</text>
          </view>
        </view>
      </view>
    </view>

    <!-- ==================== 底部新增按钮 ==================== -->
    <view class="address-list-page__footer">
      <view class="address-list-page__add-btn" @tap="onAddAddress">
        <u-icon name="plus" :size="32" color="#ffffff" />
        <text>新增收货地址</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'

// ==================== 导入依赖 ====================
import EmptyState from '@/components/basics/EmptyState.vue'
import { getAddressList, setDefaultAddress, deleteAddress } from '@/api/address'

// ==================== 响应式数据 ====================
const addressList = ref([])         // 地址列表
const loading = ref(true)           // 加载状态

// ==================== 页面生命周期 ====================

onShow(() => {
  loadAddressList()
})

// ==================== 数据加载 ====================

const loadAddressList = async () => {
  loading.value = true
  try {
    const res = await getAddressList()
    addressList.value = res || []
  } catch (error) {
    console.error('[Address] 加载地址列表失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// ==================== 事件处理 ====================

const onAddAddress = () => {
  uni.navigateTo({ url: '/pages/address/edit' })
}

const onEditAddress = (addr) => {
  uni.navigateTo({ url: `/pages/address/edit?id=${addr.id}` })
}

const onSetDefault = async (addr) => {
  try {
    await setDefaultAddress(addr.id)
    uni.showToast({ title: '已设为默认地址', icon: 'success' })
    loadAddressList()
  } catch (error) {
    uni.showToast({ title: '设置失败', icon: 'none' })
  }
}

const onDeleteAddress = (addr) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除该地址吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteAddress(addr.id)
          uni.showToast({ title: '已删除', icon: 'success' })
          loadAddressList()
        } catch (error) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.address-list-page {
  min-height: 100vh;
  background: #F2F2F7;
  padding-bottom: 120rpx;
}

// ==================== 地址列表 ====================
.address-list-page__list {
  padding: 16rpx;
}

// 地址卡片
.address-list-page__card {
  background: #ffffff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 1rpx 4rpx rgba(0,0,0,0.04);
}

// 卡片头部
.address-list-page__card-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

// 收货人
.address-list-page__receiver {
  font-size: 28rpx;
  font-weight: 600;
  color: #1D1D1F;
}

// 手机号
.address-list-page__phone {
  font-size: 26rpx;
  color: #8E8E93;
}

// 默认标签
.address-list-page__default-tag {
  font-size: 20rpx;
  color: #007AFF;
  background: #E5F1FF;
  padding: 2rpx 12rpx;
  border-radius: 6rpx;
  margin-left: auto;
}

// 地址详情
.address-list-page__detail {
  font-size: 26rpx;
  color: #1D1D1F;
  line-height: 1.5;
  display: block;
  margin-bottom: 16rpx;
}

// 操作按钮
.address-list-page__actions {
  display: flex;
  gap: 32rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #F2F2F7;
}

.address-list-page__action-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 24rpx;
  color: #8E8E93;
}

// ==================== 底部按钮 ====================
.address-list-page__footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16rpx 24rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom, 0));
  background: #ffffff;
  border-top: 1rpx solid #E5E5EA;
}

.address-list-page__add-btn {
  background: #007AFF;
  color: #ffffff;
  border-radius: 44rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  font-size: 28rpx;
  font-weight: 600;
}
</style>
