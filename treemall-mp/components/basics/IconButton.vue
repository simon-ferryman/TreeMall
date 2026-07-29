<!--
  components/IconButton.vue — 图标+文字按钮组件

  【设计思想】
  图标+文字按钮是小程序中常见的 UI 元素，用于底部导航栏操作按钮、
  列表项操作入口等场景。本组件支持自定义图标、文字、颜色和尺寸，
  以及禁用状态，覆盖项目中的各种按钮需求。

  【Figma 设计令牌】
  - 默认图标色：主色 #007AFF
  - 默认文字色：主文字 #1D1D1F
  - 禁用态：浅灰 #C7C7CC

  【使用场景】
  - 底部操作栏：分享、收藏、客服等按钮
  - 列表项行内操作按钮
  - 导航栏右侧按钮
  - 空状态引导按钮

  【Props】
  - icon: uView 图标名称
  - iconSize: 图标大小（默认 40rpx）
  - text: 按钮文字
  - color: 主题色（默认 #007AFF）
  - disabled: 是否禁用
  - direction: 排列方向（'horizontal' | 'vertical'）

  【Events】
  - click: 点击事件
-->
<template>
  <!--
    图标+文字按钮容器
    点击触发 click 事件
    禁用状态下不响应点击
  -->
  <view
    class="icon-button"
    :class="[
      'icon-button--' + direction,
      { 'icon-button--disabled': disabled }
    ]"
    :style="{
      color: disabled ? '#C7C7CC' : color
    }"
    @tap="onClick"
  >
    <!--
      图标
      使用 uView UI 的 u-icon 组件
      颜色继承父级 color 样式
    -->
    <view class="icon-button__icon">
      <u-icon
        :name="icon"
        :size="iconSize"
        :color="disabled ? '#C7C7CC' : color"
      />
    </view>

    <!--
      文字
      仅在 text 有值时显示
    -->
    <text
      v-if="text"
      class="icon-button__text"
      :class="'icon-button__text--' + direction"
      :style="{
        fontSize: fontSize + 'rpx'
      }"
    >
      {{ text }}
    </text>
  </view>
</template>

<script setup>
// ==================== Props 定义 ====================
const props = defineProps({
  // 图标名称：uView UI 图标库中的图标名
  icon: {
    type: String,
    required: true                   // 必填，图标是组件的核心元素
  },
  // 图标大小：单位 rpx
  iconSize: {
    type: [Number, String],
    default: 40                     // 默认 40rpx（Figma 20px × 2）
  },
  // 按钮文字：可选，不传则只显示图标
  text: {
    type: String,
    default: ''                      // 默认空字符串，不显示文字
  },
  // 主题色：图标和文字的颜色
  color: {
    type: String,
    default: '#007AFF'              // 默认主题蓝色
  },
  // 是否禁用：禁用时颜色变灰，不响应点击
  disabled: {
    type: Boolean,
    default: false                   // 默认不禁用
  },
  // 排列方向：horizontal 水平排列（图标在左，文字在右），vertical 垂直排列（图标在上，文字在下）
  direction: {
    type: String,
    default: 'vertical',            // 默认纵向排列，适合底部操作栏
    validator: (value) => {
      return ['horizontal', 'vertical'].includes(value)
    }
  },
  // 文字大小：单位 rpx
  fontSize: {
    type: [Number, String],
    default: 22                     // 默认 22rpx（Figma 11px × 2）
  }
})

// ==================== Events 定义 ====================
const emit = defineEmits(['click'])

// ==================== 事件处理 ====================

/**
 * 按钮点击事件
 * 禁用状态下不触发 click 事件
 */
const onClick = () => {
  // 禁用状态直接返回，不触发事件
  if (props.disabled) return

  // 触发 click 事件，通知父组件
  emit('click')
}
</script>

<style lang="scss" scoped>
// ==================== 按钮容器 ====================
.icon-button {
  // 使用 flex 布局，方便控制子元素排列
  display: flex;
  align-items: center;              // 交叉轴居中
  justify-content: center;          // 主轴居中
  // 最小点击区域 44pt（微信小程序无障碍标准）
  min-width: 88rpx;
  min-height: 88rpx;
  // 过渡动画
  transition: opacity 0.2s ease;

  // ==================== 排列方向变体 ====================

  // 纵向排列：图标在上，文字在下（适用于底部操作栏）
  &--vertical {
    flex-direction: column;         // 纵向排列
    gap: 4rpx;                      // 图标和文字之间的间距
  }

  // 横向排列：图标在左，文字在右（适用于行内操作）
  &--horizontal {
    flex-direction: row;            // 横向排列
    gap: 8rpx;                      // 图标和文字之间的间距
  }

  // ==================== 禁用状态 ====================
  &--disabled {
    // 禁用时透明度降低，视觉上表示不可点击
    opacity: 0.5;
    // 禁用点击事件穿透
    pointer-events: none;
  }

  // ==================== 按下态（仅在非禁用时生效） ====================
  // 使用 :active 伪类实现按下反馈
  &:not(&--disabled):active {
    opacity: 0.7;                   // 按下时降低透明度，提供触觉反馈
  }
}

// ==================== 图标区域 ====================
.icon-button__icon {
  // 图标容器，保证图标居中
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;                   // 不压缩图标
}

// ==================== 文字样式 ====================
.icon-button__text {
  line-height: 1.4;                 // 舒适的行高
  white-space: nowrap;              // 不换行，保持单行
  flex-shrink: 0;                   // 不压缩文字

  // 纵向排列时文字样式
  &--vertical {
    // 纵向排列时文字居中
    text-align: center;
  }

  // 横向排列时文字样式
  &--horizontal {
    // 横向排列时文字左对齐
    text-align: left;
  }
}
</style>