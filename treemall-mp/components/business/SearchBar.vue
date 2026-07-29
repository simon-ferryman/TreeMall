<!--
  components/SearchBar.vue — 搜索栏组件

  【设计思想】
  搜索栏是电商小程序的标配组件，用户通过搜索快速找到目标商品。
  本组件提供搜索图标 + 输入框的核心交互，支持输入时实时触发搜索，
  以及可选的取消按钮。

  【Props】
  - modelValue: 双向绑定搜索关键词
  - placeholder: 占位提示文字
  - showCancel: 是否显示取消按钮
  - autofocus: 是否自动聚焦

  【Events】
  - search: 用户点击搜索/确认时触发
  - cancel: 用户点击取消按钮时触发
  - update:modelValue: v-model 双向绑定
-->
<template>
  <view class="search-bar">
    <!-- 搜索输入区域 -->
    <view class="search-bar__input-wrapper">
      <u-icon class="search-bar__icon" name="search" size="36" color="#8E8E93" />
      <input
        class="search-bar__input"
        :value="modelValue"
        type="text"
        :placeholder="placeholder"
        :focus="autofocus"
        placeholder-class="search-bar__placeholder"
        confirm-type="search"
        @input="onInput"
        @confirm="onConfirm"
      />
    </view>
    <!-- 取消按钮 -->
    <text v-if="showCancel" class="search-bar__cancel" @tap="onCancel">取消</text>
  </view>
</template>

<script setup>
const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '搜索商品' },
  showCancel: { type: Boolean, default: false },
  autofocus: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'search', 'cancel'])

// 输入框变化：通过 emit('update:modelValue') 实现 v-model 双向绑定
const onInput = (e) => emit('update:modelValue', e.detail.value)
// 键盘确认搜索：将当前关键词传递给父组件
const onConfirm = () => emit('search', props.modelValue)
// 取消按钮：清空输入内容并通知父组件
const onCancel = () => {
  emit('update:modelValue', '')
  emit('cancel')
}
</script>

<style lang="scss" scoped>
.search-bar {
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
  background-color: #FFFFFF;
  box-sizing: border-box;
}

.search-bar__input-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  height: 64rpx;
  background-color: #F2F2F7;
  border-radius: 32rpx;
  padding: 0 24rpx;
  box-sizing: border-box;
}

.search-bar__icon {
  margin-right: 12rpx;
  flex-shrink: 0;
}

.search-bar__input {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
  color: #1D1D1F;
  background: transparent;
  border: none;
  outline: none;
}

.search-bar__placeholder {
  font-size: 28rpx;
  color: #8E8E93;
}

.search-bar__cancel {
  flex-shrink: 0;
  margin-left: 20rpx;
  font-size: 28rpx;
  color: #007AFF;
  line-height: 64rpx;
  white-space: nowrap;
}
</style>