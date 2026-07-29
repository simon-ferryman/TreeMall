<!--
  components/PageContainer.vue — 页面容器组件

  【设计思想】
  页面容器是所有页面的最外层包裹组件，统一管理页面的 padding 和背景色，
  确保整个小程序的页面间距和底色保持一致，避免每个页面重复书写相同的样式。

  【Figma 设计令牌】
  - 背景色：$bg-color (#F2F2F7)
  - 统一 padding：24rpx（Figma 12px × 2 = 24rpx）

  【使用场景】
  - 所有页面作为最外层容器使用
  - 包裹页面内的主要内容区域

  【Props】
  - padding: 自定义内边距（默认 24rpx）
  - bgColor: 自定义背景色（默认 #F2F2F7）

  【Slots】
  - default: 插入页面主体内容
-->
<template>
  <!--
    页面容器
    使用 view 标签包裹，通过默认插槽插入子内容
    默认背景色为 $bg-color，padding 为 24rpx
  -->
  <view
    class="page-container"
    :style="{
      padding: padding + 'rpx',
      backgroundColor: bgColor
    }"
  >
    <!--
      默认插槽
      父组件传入的所有内容将渲染在此处
    -->
    <slot />
  </view>
</template>

<script setup>
// ==================== Props 定义 ====================
defineProps({
  // 内边距：统一控制页面内容区域的 padding（单位 rpx）
  padding: {
    type: Number,
    default: 24                      // 默认 24rpx，对应 Figma 12px
  },
  // 背景色：支持自定义页面背景色
  bgColor: {
    type: String,
    default: '#F2F2F7'               // 默认使用 Figma 设计令牌中的页面背景色
  }
})
</script>

<style lang="scss" scoped>
// ==================== 容器基础样式 ====================
.page-container {
  // 使用 flex 纵向布局，方便子元素垂直排列
  display: flex;
  flex-direction: column;
  // 最小高度撑满整个视口，避免内容不足时背景色不覆盖
  min-height: 100vh;
  // 盒模型使用 border-box，确保 padding 包含在 width/height 内
  box-sizing: border-box;
}
</style>