<!--
  components/BannerSwiper.vue — 轮播图组件

  【设计思想】
  首页 Banner 轮播图是电商小程序最重要的视觉焦点，需要支持：
  1. 自动轮播（5 秒间隔）
  2. 手动滑动切换
  3. 指示器圆点（当前页高亮）
  4. 点击跳转（支持商品详情、活动页等链接）

  【使用场景】
  首页顶部 Banner 区域，通过 appStore.banners 获取数据

  【Props】
  - list: Banner 数据数组 [{ id, imageUrl, linkUrl }]
  - height: 轮播图高度（默认 340rpx，对应 16:9 比例）
  - autoplay: 是否自动播放（默认 true）
  - interval: 自动播放间隔（默认 5000ms）
-->
<template>
  <view class="banner-swiper-container">
    <swiper
      class="banner-swiper"
      :style="{ height: height + 'rpx' }"
      :circular="true"
      :autoplay="autoplay"
      :interval="interval"
      :duration="500"
      :indicator-dots="list.length > 1"
      indicator-color="rgba(255, 255, 255, 0.5)"
      indicator-active-color="#007AFF"
      @change="onSwiperChange"
    >
      <swiper-item v-for="(item, index) in list" :key="item.id || index">
        <image
          class="banner-image"
          :src="getImageUrl(item.imageUrl)"
          mode="aspectFill"
          @tap="onBannerClick(item)"
        />
      </swiper-item>
    </swiper>
  </view>
</template>

<script setup>
import { getImageUrl } from '@/utils/image-url'
const props = defineProps({
  list: { type: Array, default: () => [] },
  height: { type: Number, default: 340 },
  autoplay: { type: Boolean, default: true },
  interval: { type: Number, default: 5000 }
})

const emit = defineEmits(['change', 'click'])

// 轮播切换事件：向父组件传递当前页索引
const onSwiperChange = (e) => emit('change', e.detail.current)

// Banner 点击事件：如果配置了 linkUrl，触发 click 事件
const onBannerClick = (item) => {
  if (item.linkUrl) emit('click', item)
}
</script>

<style lang="scss" scoped>
.banner-swiper-container {
  width: 100%;
  overflow: hidden;
  // 左右外边距（与边框的距离）
  margin: 0 10px;          // 左右各 16px
  // 因为设置了 margin，必须把宽度改成计算值，否则会溢出
  width: calc(100% - 32px); // 100% 减去左右 margin 总和
  // 圆角
  border-radius: 12px;     // 圆角大小，按需要修改

}
.banner-swiper {
  width: 100%;
}
.banner-image {
  width: 100%;
  height: 100%;
  display: block;
}
</style>
