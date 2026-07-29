<!--
  components/CategoryGrid.vue — 分类宫格组件

  【设计思想】
  首页 Banner 下方的快捷分类入口，用图标 + 文字的形式展示常用分类。
  用户点击图标可直接跳转到对应分类的商品列表。

  【Props】
  - list: 分类数据数组 [{ id, name, icon }]
  - columns: 每行显示的图标数量（默认 5）
  - activeId: 当前选中的分类 ID
-->
<template>
  <view class="category-grid">
    <view
      v-for="item in list"
      :key="item.id"
      class="category-grid__item"
      :style="{ width: itemWidth }"
      @tap="onCategoryClick(item)"
    >
      <view class="category-grid__icon-wrapper">
		<u-icon
		  :name="getCategoryIcon(item)"
		  :size="44"
		  :color="isActive(item.id) ? '#007AFF' : '#8E8E93'"
		/>
      </view>
      <text class="category-grid__name">{{ item.name }}</text>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  list: { type: Array, default: () => [] },
  columns: { type: Number, default: 5 },
  activeId: { type: Number, default: null }
})
/**
 * 分类图标映射表
 * 当后端没有返回 icon 字段时，根据分类名称自动匹配图标
 * uView UI 3.x 图标名称参考：https://uview-plus.jiangruyi.com/components/icon.html
 *
 * 【设计原则】
 * - 使用 uView UI 内置图标，无需额外引入图片资源
 * - 映射表可根据实际业务扩展
 * - 未匹配到的分类使用 'bag'（购物袋）作为默认图标
 */
const CATEGORY_ICON_MAP = {
  '冰箱': 'star',           // 冰箱 → 星形图标
  '空调': 'thumb-up',       // 空调 → 拇指图标
  '电视机': 'play-circle',  // 电视机 → 播放图标
  '洗衣机': 'grid',         // 洗衣机 → 宫格图标
  '电饭煲': 'gift',         // 电饭煲 → 礼物图标
  '手机': 'phone',          // 手机 → 手机图标
  '电脑': 'server',         // 电脑 → 服务器图标
  '服装': 'bag',            // 服装 → 购物袋图标
  '食品': 'gift',           // 食品 → 礼物图标
  '家居': 'home',           // 家居 → 首页图标
  '美妆': 'heart',          // 美妆 → 心形图标
  '运动': 'thumb-up',       // 运动 → 拇指图标
  '图书': 'file-text',      // 图书 → 文件图标
  '数码': 'grid',           // 数码 → 宫格图标
  '母婴': 'heart',          // 母婴 → 心形图标
  '汽车': 'car',            // 汽车 → 汽车图标
  '其他': 'bag'             // 其他 → 购物袋图标（兜底）
}

/**
 * 获取分类图标名称
 * 优先使用后端返回的 icon 字段，否则从映射表中查找，最终兜底为 'bag'
 *
 * @param {Object} item - 分类数据对象 { id, name, icon }
 * @returns {string} uView UI 图标名称
 */
const getCategoryIcon = (item) => {
  // 优先使用后端返回的 icon 字段
  if (item.icon) return item.icon
  // 从映射表中查找，未找到则使用 'bag' 作为默认图标
  return CATEGORY_ICON_MAP[item.name] || 'bag'
}

const emit = defineEmits(['click'])

// 每个分类项宽度：100% / 列数
const itemWidth = 100 / props.columns + '%'

const isActive = (id) => props.activeId === id
const onCategoryClick = (item) => emit('click', item)
</script>

<style lang="scss" scoped>
.category-grid {
  display: flex;
  flex-wrap: wrap;
  background: #ffffff;
  padding: 24rpx 16rpx;
  border-radius: 12rpx;
  margin: 0 16rpx;
}

.category-grid__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12rpx 0;
  gap: 10rpx;
}

.category-grid__icon-wrapper {
  width: 84rpx;
  height: 84rpx;
  border-radius: 50%;
  background: #F2F2F7;
  display: flex;
  align-items: center;
  justify-content: center;
}

.category-grid__icon-placeholder {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: #E5E5EA;
}

.category-grid__name {
  font-size: 22rpx;
  color: #1D1D1F;
  text-align: center;
  line-height: 1.3;
}
</style>
