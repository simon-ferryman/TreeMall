<!--
  pages/address/edit.vue — 地址编辑页

  【设计思想】
  新增和编辑共用同一页面，通过 URL 参数 ?id=xxx 区分模式：
  - 无 id：新增模式
  - 有 id：编辑模式，onLoad 时加载已有地址数据

  【表单字段】
  - receiverName：收货人姓名
  - receiverPhone：手机号（11 位）
  - province/city/district：省市区
  - detailAddress：详细地址
  - isDefault：是否默认地址
-->
<template>
  <view class="address-edit-page">
    <view class="address-edit-page__form">
      <!-- 收货人 -->
      <view class="address-edit-page__form-item">
        <text class="address-edit-page__label">收货人</text>
        <input
          class="address-edit-page__input"
          v-model="form.receiverName"
          placeholder="请输入收货人姓名"
          maxlength="20"
        />
      </view>

      <!-- 手机号 -->
      <view class="address-edit-page__form-item">
        <text class="address-edit-page__label">手机号</text>
        <input
          class="address-edit-page__input"
          v-model="form.receiverPhone"
          type="number"
          placeholder="请输入手机号"
          maxlength="11"
        />
      </view>

<!--      省市区
      <view class="address-edit-page__form-item" @tap="onRegionSelect">
        <text class="address-edit-page__label">所在地区</text>
        <view class="address-edit-page__input address-edit-page__input--select">
          <text :class="{ 'address-edit-page__placeholder': !regionText }">
            {{ regionText || '请选择省市区' }}
          </text>
          <u-icon name="arrow-right" :size="24" color="#9ca3af" />
        </view>
      </view> -->
	  <!-- 所在地区：使用三个独立输入框替代 chooseLocation -->
	  <!-- chooseLocation 是地图选点 API，不适合省市区选择，且需要隐私声明 -->
<!-- 所在地区：使用省市区级联选择器 -->
		<view class="address-edit-page__form-item" @tap="onRegionTap">
		  <text class="address-edit-page__label">所在地区</text>
		  <picker
			mode="region"
			:value="regionValue"
			@change="onRegionChange"
			class="address-edit-page__picker"
		  >
			<view class="address-edit-page__input address-edit-page__input--select">
			  <text :class="{ 'address-edit-page__placeholder': !regionText }">
				{{ regionText || '请选择省市区' }}
			  </text>
			  <u-icon name="arrow-right" :size="24" color="#9ca3af" />
			</view>
		  </picker>
		</view>
      <!-- 详细地址 -->
      <view class="address-edit-page__form-item">
        <text class="address-edit-page__label">详细地址</text>
        <input
          class="address-edit-page__input"
          v-model="form.detailAddress"
          placeholder="请输入详细地址（街道、门牌号等）"
          maxlength="100"
        />
      </view>

      <!-- 默认地址开关 -->
      <view class="address-edit-page__form-item address-edit-page__form-item--switch">
        <text class="address-edit-page__label">设为默认地址</text>
        <switch
          :checked="form.isDefault"
          color="#007AFF"
          @change="onSwitchChange"
        />
      </view>
    </view>

    <!-- 保存按钮 -->
    <view class="address-edit-page__footer">
      <view class="address-edit-page__save-btn" @tap="onSave">
        保存
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { computed } from 'vue'                //地区显示文本，计算属性，展示省市区
// ==================== 导入依赖 ====================
import { getAddressDetail, createAddress, updateAddress } from '@/api/address'

// ==================== 响应式数据 ====================

// 是否为编辑模式
const isEdit = ref(false)
// 编辑时的地址 ID
const editId = ref(null)

// 表单数据
const form = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: false
})

// 地区显示文本
//const regionText = ref('')

// ==================== 页面生命周期 ====================

onLoad((options) => {
  if (options.id) {
    // 编辑模式：加载已有地址
    isEdit.value = true
    editId.value = options.id
    loadAddressDetail(editId.value)
  }
  // 否则为新增模式，使用默认空表单
})

// ==================== 数据加载 ====================

const loadAddressDetail = async (id) => {
  try {
    uni.showLoading({ title: '加载中...' })
    const data = await getAddressDetail(id)
    // 填充表单
    form.receiverName = data.receiverName || ''
    form.receiverPhone = data.receiverPhone || ''
    form.province = data.province || ''
    form.city = data.city || ''
    form.district = data.district || ''
    form.detailAddress = data.detailAddress || ''
    form.isDefault = data.isDefault === 1 || data.isDefault === true
    //regionText.value = `${form.province} ${form.city} ${form.district}`
  } catch (error) {
    console.error('[Address] 加载地址详情失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

// ==================== 事件处理 ====================

/**
 * 选择省市区
 * 使用微信原生地区选择器

const onRegionSelect = () => {
  uni.chooseLocation({
    success: (res) => {
      // 微信 chooseLocation 返回的是完整地址
      // 需要解析省市信息（简化处理）
      form.province = res.name || ''
      form.city = ''
      form.district = ''
      form.detailAddress = res.address || ''
      regionText.value = res.address || res.name
    },
    fail: (err) => {
      console.log('[Address] 选择地址取消:', err)
    }
  })
}*/
// ==================== 省市区选择器 ====================

// 地区显示文本（计算属性，用于展示"省-市-区"）
const regionText = computed(() => {
  if (form.province && form.city && form.district) {
    return `${form.province} ${form.city} ${form.district}`
  }
  return ''
})

// picker 的 value 属性（用于编辑模式回显）
const regionValue = computed(() => {
  if (form.province && form.city && form.district) {
    return [form.province, form.city, form.district]
  }
  return []
})

/**
 * 省市区选择器变化回调
 * picker mode="region" 返回 { value: ['省','市','区'], code: ['编码1','编码2','编码3'] }
 */
const onRegionChange = (e) => {
  const region = e.detail.value
  form.province = region[0] || ''
  form.city = region[1] || ''
  form.district = region[2] || ''
}
/**
 * 默认地址开关
 */
const onSwitchChange = (e) => {
  form.isDefault = e.detail.value
}

/**
 * 保存地址
 */
const onSave = async () => {
  // 表单校验
  if (!form.receiverName.trim()) {
    uni.showToast({ title: '请输入收货人姓名', icon: 'none' })
    return
  }
  if (!form.receiverPhone.trim() || form.receiverPhone.length !== 11) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (!form.detailAddress.trim()) {
    uni.showToast({ title: '请输入详细地址', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '保存中...' })
	// 构建请求数据，将 isDefault 从 boolean 转为 integer
	const requestData = {
	    ...form,
	    isDefault: form.isDefault ? 1 : 0   // boolean → integer
	}

    if (isEdit.value) {
      // 编辑模式
      await updateAddress({
        id: editId.value,
        ...requestData
      })
    } else {
      // 新增模式
      await createAddress(requestData)
    }

    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)

  } catch (error) {
    console.error('[Address] 保存地址失败:', error)
    uni.showToast({ title: '保存失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}
</script>

<style lang="scss" scoped>
.address-edit-page {
  min-height: 100vh;
  background: #F2F2F7;
  padding-bottom: 120rpx;
}

// ==================== 表单 ====================
.address-edit-page__form {
  background: #ffffff;
  margin: 16rpx;
  border-radius: 12rpx;
  overflow: hidden;
}

// 表单项
.address-edit-page__form-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid #F2F2F7;

  &:last-child {
    border-bottom: none;
  }

  &--switch {
    justify-content: space-between;
  }
}

// 标签
.address-edit-page__label {
  width: 140rpx;
  font-size: 28rpx;
  color: #1D1D1F;
  flex-shrink: 0;
}

// 输入框
.address-edit-page__input {
  flex: 1;
  font-size: 28rpx;
  color: #1D1D1F;
  height: 48rpx;
  line-height: 48rpx;

// ==================== 省市区输入组 ====================
.address-edit-page__region-inputs {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex: 1;
}

// 省市区输入框
.address-edit-page__region-input {
  flex: 1;
  height: 48rpx;
  line-height: 48rpx;
  font-size: 28rpx;
  color: #1D1D1F;
  text-align: center;
  background: #F2F2F7;
  border-radius: 8rpx;
  padding: 0 8rpx;
}

// 分隔符
.address-edit-page__region-sep {
  font-size: 24rpx;
  color: #9ca3af;
  flex-shrink: 0;
}
}

// 占位文字
.address-edit-page__placeholder {
  color: #9ca3af;
}

// ==================== 底部按钮 ====================
.address-edit-page__footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16rpx 24rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom, 0));
  background: #ffffff;
  border-top: 1rpx solid #E5E5EA;
}

.address-edit-page__save-btn {
  background: #007AFF;
  color: #ffffff;
  border-radius: 44rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 600;
}
// 选择器样式（含箭头图标）
.address-edit-page__input--select {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

// picker 包裹层
.address-edit-page__picker {
  flex: 1;
}
</style>
