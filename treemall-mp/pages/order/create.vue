<!--
  pages/order/create.vue — 下单确认页

  【设计思想】
  下单确认页是用户完成购买的最后一步，展示订单摘要并引导用户确认。
  用户在购物车选中商品后进入此页面，确认地址和商品信息后提交订单。

  【页面流程】
  1. 加载购物车已选商品（cartStore.checkedItems）
  2. 加载地址列表，选择收货地址
  3. 填写备注
  4. 确认金额
  5. 提交订单 → 跳转支付或订单列表
-->
<template>
  <view class="order-create-page">
    <scroll-view class="order-create-page__scroll" scroll-y>
      <!-- ==================== 收货地址 ==================== -->
      <view class="order-create-page__section">
        <view v-if="selectedAddress" class="order-create-page__address" @tap="onSelectAddress">
          <view class="order-create-page__address-top">
            <u-icon name="map-fill" :size="36" color="#007AFF" />
            <view class="order-create-page__address-info">
              <text class="order-create-page__address-receiver">{{ selectedAddress.receiverName }}</text>
              <text class="order-create-page__address-phone">{{ selectedAddress.receiverPhone }}</text>
            </view>
            <u-icon name="arrow-right" :size="24" color="#9ca3af" />
          </view>
          <text class="order-create-page__address-detail">
            {{ selectedAddress.province }}{{ selectedAddress.city }}{{ selectedAddress.district }} {{ selectedAddress.detailAddress }}
          </text>
        </view>
        <view v-else class="order-create-page__address-empty" @tap="onSelectAddress">
          <u-icon name="plus-circle" :size="36" color="#007AFF" />
          <text class="order-create-page__address-empty-text">请添加收货地址</text>
          <u-icon name="arrow-right" :size="24" color="#9ca3af" />
        </view>
      </view>

      <!-- ==================== 商品清单 ==================== -->
      <view class="order-create-page__section">
        <view class="order-create-page__section-title">商品清单</view>
        <view v-for="item in checkedItems" :key="item.id" class="order-create-page__goods-item">
          <image class="order-create-page__goods-image" :src="getImageUrl(item.mainImage)" mode="aspectFill" />
          <view class="order-create-page__goods-info">
            <text class="order-create-page__goods-name">{{ item.productName }}</text>
            <view class="order-create-page__goods-bottom">
              <text class="order-create-page__goods-price">¥{{ formatPrice(item.price) }}</text>
              <text class="order-create-page__goods-qty">×{{ item.quantity }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- ==================== 备注 ==================== -->
      <view class="order-create-page__section">
        <view class="order-create-page__remark">
          <text class="order-create-page__remark-label">备注</text>
          <input class="order-create-page__remark-input" v-model="remark" placeholder="选填，如「请放快递柜」" maxlength="50" />
        </view>
      </view>

      <!-- ==================== 优惠券 ==================== -->
      <view class="order-create-page__section">
        <view class="order-create-page__coupon-row" @tap="onOpenCouponPopup">
          <text class="order-create-page__coupon-label">优惠券</text>
          <view class="order-create-page__coupon-right">
            <text class="order-create-page__coupon-value" :class="{ 'order-create-page__coupon-value--selected': selectedCoupon }">
              {{ selectedCoupon ? `${selectedCoupon.name}（-¥${selectedCoupon.discount}）` : '选择优惠券' }}
            </text>
            <u-icon name="arrow-right" :size="24" color="#8E8E93" />
          </view>
        </view>
      </view>

      <!-- ==================== 价格明细 ==================== -->
      <view class="order-create-page__section">
        <view class="order-create-page__section-title">价格明细</view>
        <view class="order-create-page__price-row">
          <text class="order-create-page__price-label">商品合计</text>
          <text class="order-create-page__price-value">¥{{ formatPrice(totalAmount) }}</text>
        </view>
        <view class="order-create-page__price-row">
          <text class="order-create-page__price-label">运费</text>
          <text class="order-create-page__price-value">免运费</text>
        </view>
        <view v-if="couponDiscount > 0" class="order-create-page__price-row">
          <text class="order-create-page__price-label">优惠券</text>
          <text class="order-create-page__price-value order-create-page__price-value--discount">-¥{{ formatPrice(couponDiscount) }}</text>
        </view>
        <view class="order-create-page__price-row order-create-page__price-row--total">
          <text class="order-create-page__price-label">合计</text>
          <text class="order-create-page__price-value order-create-page__price-value--total">¥{{ formatPrice(finalAmount) }}</text>
        </view>
      </view>

      <!-- ==================== 支付方式 ==================== -->
      <view class="order-create-page__section">
        <view class="order-create-page__payment-row">
          <text class="order-create-page__payment-label">支付方式</text>
          <view class="order-create-page__payment-right" @tap="onTogglePayment">
            <view class="order-create-page__payment-icon"><text class="order-create-page__payment-icon-text">微</text></view>
            <text class="order-create-page__payment-name">微信支付</text>
            <text v-if="paymentMethod === 'wechat'" class="order-create-page__payment-check">&#10003;</text>
          </view>
        </view>
      </view>

      <view style="height: 120rpx;" />
    </scroll-view>

    <!-- ==================== 优惠券选择弹窗 ==================== -->
    <view v-if="showCouponPopup" class="order-create-page__popup-mask" @tap="onCloseCouponPopup">
      <view class="order-create-page__popup" @tap.stop>
        <view class="order-create-page__popup-title">选择优惠券</view>
        <view class="order-create-page__popup-list">
          <view
            v-for="coupon in coupons" :key="coupon.id"
            class="order-create-page__coupon-item"
            :class="{
              'order-create-page__coupon-item--active': selectedCoupon && selectedCoupon.id === coupon.id,
              'order-create-page__coupon-item--disabled': totalAmount < coupon.threshold
            }"
            @tap="onSelectCouponItem(coupon)"
          >
            <view class="order-create-page__coupon-item-left">
              <text class="order-create-page__coupon-item-amount">¥{{ coupon.discount }}</text>
            </view>
            <view class="order-create-page__coupon-item-right">
              <text class="order-create-page__coupon-item-name">{{ coupon.name }}</text>
              <text class="order-create-page__coupon-item-threshold">满{{ coupon.threshold }}元可用</text>
            </view>
            <text v-if="selectedCoupon && selectedCoupon.id === coupon.id" class="order-create-page__coupon-item-check">&#10003;</text>
          </view>
        </view>
        <view class="order-create-page__popup-footer">
          <view class="order-create-page__popup-btn" @tap="onCloseCouponPopup">确定</view>
        </view>
      </view>
    </view>

    <!-- ==================== 底部提交栏 ==================== -->
    <view class="order-create-page__footer">
      <view class="order-create-page__footer-left">
        <text class="order-create-page__footer-label">合计：</text>
        <text class="order-create-page__footer-price">¥{{ formatPrice(finalAmount) }}</text>
      </view>
      <view
        class="order-create-page__submit-btn"
        :class="{ 'order-create-page__submit-btn--disabled': !selectedAddress }"
        @tap="onSubmit"
      >提交订单</view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useCartStore } from '@/stores/cart'
import { getAddressList } from '@/api/address'
import { createOrder } from '@/api/order'
import { getImageUrl } from '@/utils/image-url'

const cartStore = useCartStore()

const addressList = ref([])
const selectedAddress = ref(null)
const remark = ref('')

// 优惠券模拟数据（满100减10 / 满200减30 / 满500减80）
const coupons = ref([
  { id: 1, name: '满100减10', threshold: 100, discount: 10 },
  { id: 2, name: '满200减30', threshold: 200, discount: 30 },
  { id: 3, name: '满500减80', threshold: 500, discount: 80 }
])
const selectedCoupon = ref(null)
const showCouponPopup = ref(false)
const paymentMethod = ref('wechat')

// 已选商品列表（从 cartStore 获取）
const checkedItems = computed(() => cartStore.checkedItems || [])
// 合计金额
const totalAmount = computed(() => cartStore.checkedTotal || 0)
// 优惠券抵扣金额（仅在满足门槛时生效）
const couponDiscount = computed(() => {
  if (!selectedCoupon.value) return 0
  if (totalAmount.value >= selectedCoupon.value.threshold) return selectedCoupon.value.discount
  return 0
})
// 最终应付金额（不小于 0）
const finalAmount = computed(() => Math.max(0, totalAmount.value - couponDiscount.value))

onShow(async () => { await loadAddressList() })

const loadAddressList = async () => {
  try {
    const res = await getAddressList()
    addressList.value = res || []
    if (!selectedAddress.value && addressList.value.length > 0) {
      const defaultAddr = addressList.value.find(a => a.isDefault)
      selectedAddress.value = defaultAddr || addressList.value[0]
    }
  } catch (error) {
    console.error('[Order] 加载地址列表失败:', error)
  }
}

const onSelectAddress = () => uni.navigateTo({ url: '/pages/address/list' })
const onOpenCouponPopup = () => { showCouponPopup.value = true }
const onCloseCouponPopup = () => { showCouponPopup.value = false }
const onSelectCouponItem = (coupon) => {
  if (totalAmount.value < coupon.threshold) {
    uni.showToast({ title: `满${coupon.threshold}元可用`, icon: 'none' })
    return
  }
  if (selectedCoupon.value && selectedCoupon.value.id === coupon.id) {
    selectedCoupon.value = null
    return
  }
  selectedCoupon.value = coupon
}
const onTogglePayment = () => uni.showToast({ title: '当前仅支持微信支付', icon: 'none' })

// 提交订单：校验地址 → 调用 API → 清空已选 → 跳转订单列表
const onSubmit = async () => {
  if (!selectedAddress.value) { uni.showToast({ title: '请选择收货地址', icon: 'none' }); return }
  if (checkedItems.value.length === 0) { uni.showToast({ title: '请选择商品', icon: 'none' }); return }
  try {
    uni.showLoading({ title: '提交中...' })
    await createOrder({
      addressId: selectedAddress.value.id,
      cartIds: checkedItems.value.map(item => item.id),
      remark: remark.value
    })
    uni.hideLoading()
    await cartStore.clearChecked()
    uni.showToast({ title: '下单成功', icon: 'success' })
    setTimeout(() => { uni.redirectTo({ url: '/pages/order/list' }) }, 1500)
  } catch (error) {
    console.error('[Order] 提交订单失败:', error)
    uni.hideLoading()
    uni.showToast({ title: '下单失败，请重试', icon: 'none' })
  }
}

const formatPrice = (price) => price != null ? Number(price).toFixed(2) : '0.00'
</script>

<style lang="scss" scoped>
.order-create-page { height: 100vh; background: #F2F2F7; display: flex; flex-direction: column; }
.order-create-page__scroll { flex: 1; }
.order-create-page__section { background: #ffffff; margin: 0 16rpx 16rpx; border-radius: 12rpx; padding: 24rpx; }
.order-create-page__section-title { font-size: 28rpx; font-weight: 600; color: #1D1D1F; margin-bottom: 16rpx; }
.order-create-page__address { padding: 0; }
.order-create-page__address-top { display: flex; align-items: center; gap: 12rpx; margin-bottom: 12rpx; }
.order-create-page__address-info { flex: 1; display: flex; gap: 16rpx; }
.order-create-page__address-receiver { font-size: 28rpx; font-weight: 600; color: #1D1D1F; }
.order-create-page__address-phone { font-size: 24rpx; color: #8E8E93; }
.order-create-page__address-detail { font-size: 24rpx; color: #8E8E93; padding-left: 48rpx; line-height: 1.5; }
.order-create-page__address-empty { display: flex; align-items: center; gap: 12rpx; padding: 16rpx 0; }
.order-create-page__address-empty-text { flex: 1; font-size: 28rpx; color: #007AFF; }
.order-create-page__goods-item { display: flex; gap: 16rpx; padding: 16rpx 0; border-bottom: 1rpx solid #F2F2F7; &:last-child { border-bottom: none; } }
.order-create-page__goods-image { width: 120rpx; height: 120rpx; border-radius: 8rpx; background: #F2F2F7; flex-shrink: 0; }
.order-create-page__goods-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.order-create-page__goods-name { font-size: 26rpx; color: #1D1D1F; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.order-create-page__goods-bottom { display: flex; justify-content: space-between; align-items: center; }
.order-create-page__goods-price { font-size: 28rpx; color: #007AFF; font-weight: 600; }
.order-create-page__goods-qty { font-size: 24rpx; color: #8E8E93; }
.order-create-page__remark { display: flex; align-items: center; }
.order-create-page__remark-label { width: 100rpx; font-size: 28rpx; color: #1D1D1F; }
.order-create-page__remark-input { flex: 1; font-size: 26rpx; color: #1D1D1F; height: 48rpx; }
.order-create-page__coupon-row { display: flex; align-items: center; justify-content: space-between; }
.order-create-page__coupon-label { font-size: 28rpx; color: #1D1D1F; }
.order-create-page__coupon-right { display: flex; align-items: center; gap: 8rpx; }
.order-create-page__coupon-value { font-size: 26rpx; color: #8E8E93; &--selected { color: #007AFF; } }
.order-create-page__price-row { display: flex; justify-content: space-between; padding: 12rpx 0; border-bottom: 1rpx solid #F2F2F7; &:last-child { border-bottom: none; } &--total { padding-top: 16rpx; } }
.order-create-page__price-label { font-size: 26rpx; color: #8E8E93; }
.order-create-page__price-value { font-size: 26rpx; color: #1D1D1F; &--total { font-size: 30rpx; font-weight: 700; color: #007AFF; } &--discount { color: #34C759; font-weight: 600; } }
.order-create-page__footer { position: fixed; bottom: 0; left: 0; right: 0; background: #ffffff; border-top: 1rpx solid #E5E5EA; display: flex; align-items: center; justify-content: space-between; padding: 0 24rpx; height: 100rpx; padding-bottom: env(safe-area-inset-bottom, 0); z-index: 100; }
.order-create-page__footer-left { display: flex; align-items: baseline; }
.order-create-page__footer-label { font-size: 26rpx; color: #1D1D1F; }
.order-create-page__footer-price { font-size: 32rpx; font-weight: 700; color: #007AFF; }
.order-create-page__submit-btn { background: #007AFF; color: #ffffff; font-size: 28rpx; font-weight: 600; padding: 16rpx 40rpx; border-radius: 40rpx; &--disabled { background: #d1d5db; color: #9ca3af; } }
.order-create-page__payment-row { display: flex; align-items: center; justify-content: space-between; }
.order-create-page__payment-label { font-size: 28rpx; color: #1D1D1F; }
.order-create-page__payment-right { display: flex; align-items: center; gap: 12rpx; }
.order-create-page__payment-icon { width: 40rpx; height: 40rpx; border-radius: 50%; background: #34C759; display: flex; align-items: center; justify-content: center; }
.order-create-page__payment-icon-text { font-size: 22rpx; color: #FFFFFF; font-weight: 700; }
.order-create-page__payment-name { font-size: 26rpx; color: #1D1D1F; }
.order-create-page__payment-check { font-size: 28rpx; color: #34C759; font-weight: 700; }
.order-create-page__popup-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.5); z-index: 999; display: flex; align-items: flex-end; }
.order-create-page__popup { width: 100%; background: #FFFFFF; border-radius: 24rpx 24rpx 0 0; max-height: 60vh; display: flex; flex-direction: column; }
.order-create-page__popup-title { font-size: 32rpx; font-weight: 700; color: #1D1D1F; text-align: center; padding: 32rpx 24rpx 20rpx; border-bottom: 1rpx solid #E5E5EA; }
.order-create-page__popup-list { flex: 1; overflow-y: auto; padding: 16rpx 24rpx; }
.order-create-page__popup-footer { padding: 20rpx 24rpx; padding-bottom: calc(20rpx + env(safe-area-inset-bottom, 0)); border-top: 1rpx solid #E5E5EA; }
.order-create-page__popup-btn { width: 100%; height: 80rpx; background: #007AFF; color: #FFFFFF; font-size: 30rpx; font-weight: 600; border-radius: 40rpx; display: flex; align-items: center; justify-content: center; }
.order-create-page__coupon-item { display: flex; align-items: center; padding: 20rpx 16rpx; background: #F2F2F7; border-radius: 12rpx; margin-bottom: 16rpx; border: 2rpx solid transparent; position: relative; &:last-child { margin-bottom: 0; } &--active { border-color: #007AFF; background: rgba(0, 122, 255, 0.04); } &--disabled { opacity: 0.45; } }
.order-create-page__coupon-item-left { width: 120rpx; display: flex; align-items: center; justify-content: center; border-right: 2rpx dashed #E5E5EA; margin-right: 20rpx; padding-right: 20rpx; }
.order-create-page__coupon-item-amount { font-size: 36rpx; font-weight: 700; color: #007AFF; }
.order-create-page__coupon-item-right { flex: 1; display: flex; flex-direction: column; gap: 6rpx; }
.order-create-page__coupon-item-name { font-size: 28rpx; color: #1D1D1F; font-weight: 600; }
.order-create-page__coupon-item-threshold { font-size: 22rpx; color: #8E8E93; }
.order-create-page__coupon-item-check { position: absolute; top: 8rpx; right: 12rpx; font-size: 28rpx; color: #007AFF; font-weight: 700; }
</style>
