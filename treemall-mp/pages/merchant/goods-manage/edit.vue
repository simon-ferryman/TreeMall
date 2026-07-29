<!--
  pages/merchant/goods-manage/edit.vue -- 商品添加/编辑页（商户端分包）

  【设计思想】
  新增和编辑商品共用同一页面，通过 URL 参数 ?id=xxx 区分模式：
  - 无 id：新增模式，表单为空
  - 有 id：编辑模式，onLoad 时加载已有商品数据填充表单

  【表单字段】
  - productName：商品名称（必填，最多 50 字）
  - price：商品价格（必填，数字，保留两位小数）
  - stock：商品库存（必填，正整数）
  - categoryId：商品分类（选择器）
  - mainImage：商品主图（图片上传）
  - description：商品描述（富文本/多行文本）
  - status：商品状态（上架/下架）

  【Figma 设计令牌】
  主色 #007AFF | 文字 #1D1D1F | 次要文字 #8E8E93 | 背景 #F2F2F7 | 白色 #FFFFFF
  分隔线 #E5E5EA | 浅蓝 #E5F1FF | 设计宽度 375px | rpx=px*2

  【使用场景】
  - 商户商品管理列表页点击"新增"按钮
  - 商户商品管理列表页点击"编辑"按钮
-->
<template>
  <view class="goods-edit-page">
    <!-- ==================== 加载中状态 ==================== -->
    <view v-if="loading" class="goods-edit-page__loading">
      <u-loading-icon :size="48" color="#007AFF" mode="circle" />
      <text class="goods-edit-page__loading-text">加载中...</text>
    </view>

    <!-- ==================== 表单区域 ==================== -->
    <template v-else>
      <scroll-view class="goods-edit-page__scroll" scroll-y>
        <!-- ==================== 商品主图上传 ==================== -->
        <view class="goods-edit-page__card">
          <view class="goods-edit-page__card-title">商品主图</view>
          <view class="goods-edit-page__upload-area">
            <!-- 已上传图片预览 -->
            <view
              v-if="form.mainImage"
              class="goods-edit-page__image-preview"
              @tap="onChooseImage"
            >
              <image
                class="goods-edit-page__image"
                :src="getImageUrl(form.mainImage)"
                mode="aspectFill"
              />
              <!-- 删除按钮 -->
              <view class="goods-edit-page__image-delete" @tap.stop="onDeleteImage">
                <u-icon name="close-circle-fill" :size="36" color="#FF3B30" />
              </view>
            </view>
            <!-- 上传按钮：未选择图片时显示 -->
            <view
              v-else
              class="goods-edit-page__upload-btn"
              @tap="onChooseImage"
            >
              <u-icon name="camera" :size="48" color="#C7C7CC" />
              <text class="goods-edit-page__upload-text">点击上传主图</text>
            </view>
          </view>
        </view>

        <!-- ==================== 基本信息表单 ==================== -->
        <view class="goods-edit-page__card">
          <view class="goods-edit-page__card-title">基本信息</view>

          <!-- 商品名称 -->
          <view class="goods-edit-page__form-item">
            <text class="goods-edit-page__form-label">
              <text class="goods-edit-page__required">*</text>商品名称
            </text>
            <input
              class="goods-edit-page__form-input"
              v-model="form.productName"
              placeholder="请输入商品名称"
              placeholder-style="color: #C7C7CC; font-size: 28rpx;"
              maxlength="50"
            />
          </view>

          <!-- 商品分类 -->
          <view class="goods-edit-page__form-item" @tap="onSelectCategory">
            <text class="goods-edit-page__form-label">
              <text class="goods-edit-page__required">*</text>商品分类
            </text>
            <view class="goods-edit-page__form-selector">
              <text :class="{ 'goods-edit-page__placeholder': !selectedCategoryName }">
                {{ selectedCategoryName || '请选择分类' }}
              </text>
              <u-icon name="arrow-right" :size="24" color="#C7C7CC" />
            </view>
          </view>

          <!-- 商品价格 -->
          <view class="goods-edit-page__form-item">
            <text class="goods-edit-page__form-label">
              <text class="goods-edit-page__required">*</text>商品价格
            </text>
            <view class="goods-edit-page__form-input-group">
              <text class="goods-edit-page__input-prefix">¥</text>
              <input
                class="goods-edit-page__form-input goods-edit-page__form-input--price"
                v-model="form.price"
                type="digit"
                placeholder="0.00"
                placeholder-style="color: #C7C7CC; font-size: 28rpx;"
              />
            </view>
          </view>

          <!-- 商品库存 -->
          <view class="goods-edit-page__form-item">
            <text class="goods-edit-page__form-label">
              <text class="goods-edit-page__required">*</text>商品库存
            </text>
            <input
              class="goods-edit-page__form-input"
              v-model="form.stock"
              type="number"
              placeholder="请输入库存数量"
              placeholder-style="color: #C7C7CC; font-size: 28rpx;"
            />
          </view>

          <!-- 商品状态（仅编辑模式显示） -->
          <view class="goods-edit-page__form-item" v-if="isEdit">
            <text class="goods-edit-page__form-label">商品状态</text>
            <view class="goods-edit-page__form-selector">
              <switch
                :checked="form.status === 1"
                color="#007AFF"
                @change="onStatusChange"
              />
            </view>
          </view>
        </view>

        <!-- ==================== 商品描述 ==================== -->
        <view class="goods-edit-page__card">
          <view class="goods-edit-page__card-title">商品描述</view>
          <textarea
            class="goods-edit-page__textarea"
            v-model="form.description"
            placeholder="请输入商品描述（规格、材质、使用说明等）"
            placeholder-style="color: #C7C7CC; font-size: 28rpx;"
            maxlength="500"
            auto-height
          />
          <!-- 字数统计 -->
          <text class="goods-edit-page__char-count">
            {{ form.description.length }}/500
          </text>
        </view>

        <!-- 底部留白 -->
        <view style="height: 140rpx;" />
      </scroll-view>

      <!-- ==================== 底部保存按钮 ==================== -->
      <view class="goods-edit-page__footer">
        <view
          class="goods-edit-page__save-btn"
          :class="{ 'goods-edit-page__save-btn--disabled': saving }"
          @tap="onSave"
        >
          {{ saving ? '保存中...' : (isEdit ? '保存修改' : '发布商品') }}
        </view>
      </view>
    </template>
  </view>
</template>

<script setup>
// ==================== 导入依赖 ====================
import { ref, reactive, computed } from 'vue'       // Vue 3 响应式 API
import { onLoad } from '@dcloudio/uni-app'          // uni-app 页面生命周期
import { getCategoryList } from '@/api/category'    // 分类 API
import { addProduct, updateProduct, getMerchantProductDetail } from '@/api/merchant'
import { useUserStore } from '@/stores/user'
import { getImageUrl } from '@/utils/image-url'
// ==================== 响应式数据 ====================

// 是否为编辑模式（有 id 为编辑，无 id 为新增）
const isEdit = ref(false)
// 编辑时的商品 ID
const editId = ref(null)
// 是否正在加载数据
const loading = ref(false)
// 是否正在保存
const saving = ref(false)
// 分类列表数据
const categoryList = ref([])

// 表单数据
const form = reactive({
  productName: '',           // 商品名称
  categoryId: null,          // 分类 ID
  price: '',                 // 价格（字符串，方便输入框绑定）
  stock: '',                 // 库存（字符串，方便输入框绑定）
  mainImage: '',             // 商品主图 URL
  description: '',           // 商品描述
  status: 1                  // 商品状态：1=上架，0=下架（默认上架）
})

// 选中的分类名称
const selectedCategoryName = computed(() => {
  if (!form.categoryId || !categoryList.value.length) return ''
  // 在分类列表中查找匹配的分类
  const cat = categoryList.value.find(c => c.id === form.categoryId)
  return cat ? cat.name : ''
})

// ==================== 页面生命周期 ====================

/**
 * 页面加载时获取 URL 参数
 * 根据是否有 id 参数判断新增/编辑模式
 */
onLoad((options) => {
  // 加载分类列表（供选择器使用）
  loadCategories()

  if (options.id) {
    // 编辑模式：加载已有商品数据
    isEdit.value = true
    editId.value = options.id
    // 动态设置导航栏标题
    uni.setNavigationBarTitle({ title: '编辑商品' })
    // 加载商品详情
    loadProductDetail(editId.value)
  } else {
    // 新增模式：使用默认空表单
    isEdit.value = false
    // 动态设置导航栏标题
    uni.setNavigationBarTitle({ title: '新增商品' })
  }
})

// ==================== 数据加载 ====================

/**
 * 加载分类列表
 * 用于分类选择器
 */
const loadCategories = async () => {
  try {
    const data = await getCategoryList()
    categoryList.value = data || []
  } catch (error) {
    console.error('[GoodsEdit] 加载分类列表失败:', error)
    categoryList.value = []
  }
}

/**
 * 加载商品详情（编辑模式）
 * 将后端数据填充到表单中
 *
 * @param {number} id - 商品 ID
 */
const loadProductDetail = async (id) => {
  loading.value = true
  try {
    // 调用商品详情 API
    const data = await getMerchantProductDetail(id)
    // 填充表单数据
    form.productName = data.name || ''
    form.categoryId = data.categoryId || null
    form.price = data.price != null ? String(data.price) : ''
    form.stock = data.stock != null ? String(data.stock) : ''
    form.mainImage = data.mainImage || ''
    form.description = data.description || ''
    form.status = data.status != null ? data.status : 1
  } catch (error) {
    console.error('[GoodsEdit] 加载商品详情失败:', error)
    uni.showToast({ title: '加载商品信息失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// ==================== 事件处理 ====================

/**
 * 选择商品图片
 * 使用 uni.chooseImage 选择图片后上传
 */
const onChooseImage = () => {
  // 选择图片
  uni.chooseImage({
    count: 1,                                      // 只选一张
    sizeType: ['compressed'],                      // 压缩图
    sourceType: ['album', 'camera'],               // 相册 + 拍照
    success: (res) => {
      // 获取临时文件路径
      const tempFilePath = res.tempFilePaths[0]
      // 上传图片
      uploadImage(tempFilePath)
    }
  })
}

/**
 * 上传图片到服务器
 *
 * @param {string} filePath - 图片临时路径
 */
const uploadImage = async (filePath) => {
  try {
    // 显示上传中提示
    uni.showLoading({ title: '上传中...', mask: true })

    // 使用 uni.uploadFile 上传图片
    // 注意：此处使用项目已有的 upload API 模式
	const token = useUserStore().token

    // 发起上传请求
    const uploadResult = await new Promise((resolve, reject) => {
      uni.uploadFile({
        url: 'http://localhost:8080/api/v1/merchant/file/upload',
        filePath: filePath,
        name: 'file',
        header: {
          'Authorization': token ? `Bearer ${token}` : ''
        },
        success: (res) => {
          try {
            const data = JSON.parse(res.data)
            if (res.statusCode === 200 && data.code === 200) {
              resolve(data.data)
            } else {
              reject(new Error(data.message || '上传失败'))
            }
          } catch (e) {
            reject(new Error('响应格式异常'))
          }
        },
        fail: (err) => {
          reject(err)
        }
      })
    })

    // 更新表单中的图片 URL
    form.mainImage = uploadResult.url || uploadResult
    uni.showToast({ title: '上传成功', icon: 'success' })

  } catch (error) {
    console.error('[GoodsEdit] 上传图片失败:', error)
    uni.showToast({ title: '上传失败，请重试', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

/**
 * 删除已上传的图片
 */
const onDeleteImage = () => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除该图片吗？',
    success: (res) => {
      if (res.confirm) {
        // 清空图片 URL
        form.mainImage = ''
      }
    }
  })
}

/**
 * 选择商品分类
 * 使用 uni.showActionSheet 展示分类列表
 */
const onSelectCategory = () => {
  if (!categoryList.value.length) {
    uni.showToast({ title: '分类列表加载中', icon: 'none' })
    return
  }

  // 提取分类名称列表
  const categoryNames = categoryList.value.map(c => c.name)

  // 显示操作菜单（底部弹出选择器）
  uni.showActionSheet({
    itemList: categoryNames,
    success: (res) => {
      // 根据索引获取选中的分类
      const selected = categoryList.value[res.tapIndex]
      form.categoryId = selected.id
    }
  })
}

/**
 * 商品状态开关
 * 切换上架/下架
 */
const onStatusChange = (e) => {
  form.status = e.detail.value ? 1 : 0
}

// ==================== 表单校验 ====================

/**
 * 校验表单数据
 *
 * @returns {boolean} 校验是否通过
 */
const validateForm = () => {
  // 商品名称：必填，最多 50 字
  if (!form.productName.trim()) {
    uni.showToast({ title: '请输入商品名称', icon: 'none' })
    return false
  }
  if (form.productName.trim().length > 50) {
    uni.showToast({ title: '商品名称不能超过50字', icon: 'none' })
    return false
  }

  // 商品分类：必选
  if (!form.categoryId) {
    uni.showToast({ title: '请选择商品分类', icon: 'none' })
    return false
  }

  // 商品价格：必填，大于 0
  const price = Number(form.price)
  if (isNaN(price) || price <= 0) {
    uni.showToast({ title: '请输入正确的价格', icon: 'none' })
    return false
  }

  // 商品库存：必填，非负整数
  const stock = Number(form.stock)
  if (isNaN(stock) || stock < 0 || !Number.isInteger(stock)) {
    uni.showToast({ title: '请输入正确的库存数量', icon: 'none' })
    return false
  }

  // 商品主图：必传
  if (!form.mainImage) {
    uni.showToast({ title: '请上传商品主图', icon: 'none' })
    return false
  }

  return true
}

// ==================== 保存操作 ====================

/**
 * 保存商品（新增或编辑）
 * 校验表单后提交数据
 */
const onSave = async () => {
  if (saving.value) return
  if (!validateForm()) return

  saving.value = true
  uni.showLoading({ title: '保存中...', mask: true })

  try {
    // 注意：后端 ProductSaveRequest 字段名是 name，不是 productName
    const params = {
      name: form.productName.trim(),          // 后端字段名是 name
      categoryId: form.categoryId,
      price: Number(form.price),
      stock: Number(form.stock),
      mainImage: form.mainImage,
      description: form.description.trim(),
      status: form.status
    }

    if (isEdit.value) {
      params.id = editId.value                // 字符串类型，后端自动解析为 Long
      await updateProduct(params)
    } else {
      await addProduct(params)
    }

    uni.showToast({ title: isEdit.value ? '保存成功' : '发布成功', icon: 'success' })
    setTimeout(() => { uni.navigateBack() }, 1500)
  } catch (error) {
    console.error('[GoodsEdit] 保存商品失败:', error)
    uni.showToast({ title: '保存失败，请重试', icon: 'none' })
  } finally {
    saving.value = false
    uni.hideLoading()
  }
}

</script>

<style lang="scss" scoped>
// ==================== 页面容器 ====================
.goods-edit-page {
  min-height: 100vh;                          // 最小高度撑满屏幕
  background: $bg-color;                      // 页面背景色：#F2F2F7
  display: flex;
  flex-direction: column;
}

// ==================== 加载中状态 ====================
.goods-edit-page__loading {
  @include flex-center;                       // 使用混入：水平垂直居中
  flex-direction: column;
  padding: 200rpx 0;
}

.goods-edit-page__loading-text {
  font-size: $font-sm;                        // 24rpx
  color: $text-muted;                         // 次要文字色：#8E8E93
  margin-top: $spacing-md;                    // 顶部间距：24rpx
}

// ==================== 滚动区域 ====================
.goods-edit-page__scroll {
  flex: 1;                                    // 填充剩余空间
}

// ==================== 卡片容器 ====================
.goods-edit-page__card {
  background: $bg-white;                      // 白色背景
  margin: 16rpx;                              // 四周边距：16rpx
  border-radius: $radius-md;                  // 圆角：16rpx
  padding: 24rpx;                             // 内边距：24rpx
  box-shadow: $shadow-sm;                     // 小阴影
}

// 卡片标题
.goods-edit-page__card-title {
  font-size: $font-md;                        // 28rpx
  font-weight: $font-weight-bold;             // 600 字重
  color: $text-color;                         // 主文字色：#1D1D1F
  margin-bottom: $spacing-md;                 // 底部间距：24rpx
}

// ==================== 图片上传区域 ====================
.goods-edit-page__upload-area {
  display: flex;
  justify-content: center;                    // 水平居中
}

// 图片预览容器
.goods-edit-page__image-preview {
  position: relative;                         // 相对定位（删除按钮绝对定位）
  width: 320rpx;                              // Figma 160px -> 320rpx
  height: 320rpx;
}

// 图片
.goods-edit-page__image {
  width: 100%;
  height: 100%;
  border-radius: $radius-sm;                  // 圆角：12rpx
  background: $bg-color;                      // 浅灰背景（加载前）
}

// 删除按钮
.goods-edit-page__image-delete {
  position: absolute;                         // 绝对定位
  top: -12rpx;                                // 右上角偏移
  right: -12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;                        // 白色背景
  border-radius: 50%;                         // 圆形
}

// 上传按钮
.goods-edit-page__upload-btn {
  width: 320rpx;                              // 与预览区域同宽
  height: 320rpx;
  border: 2rpx dashed $border-color;          // 虚线边框
  border-radius: $radius-sm;                  // 圆角：12rpx
  background: $bg-color;                      // 浅灰背景
  @include flex-center;                       // 使用混入：水平垂直居中
  flex-direction: column;                     // 垂直排列
  gap: $spacing-sm;                           // 间距：16rpx
}

// 上传文字
.goods-edit-page__upload-text {
  font-size: $font-sm;                        // 24rpx
  color: $text-muted;                         // 次要文字色：#8E8E93
}

// ==================== 表单项 ====================
.goods-edit-page__form-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;                           // 上下内边距：20rpx
  border-bottom: 1rpx solid $border-light;    // 底部分隔线

  // 最后一条无分隔线
  &:last-child {
    border-bottom: none;
  }
}

// 表单标签
.goods-edit-page__form-label {
  width: 160rpx;                              // 固定宽度
  font-size: $font-md;                        // 28rpx
  color: $text-color;                         // 主文字色：#1D1D1F
  flex-shrink: 0;                             // 不压缩
}

// 必填标志（红色星号）
.goods-edit-page__required {
  color: $danger-color;                       // 危险红：#FF3B30
  margin-right: 4rpx;                         // 与标签文字的间距
}

// 表单输入框
.goods-edit-page__form-input {
  flex: 1;                                    // 填充剩余空间
  font-size: $font-md;                        // 28rpx
  color: $text-color;                         // 主文字色：#1D1D1F
  height: 48rpx;                              // 输入框高度
  line-height: 48rpx;
  text-align: right;                          // 右对齐（与标签对称）

  // 价格输入框
  &--price {
    flex: 1;
    text-align: left;                         // 价格左对齐
  }
}

// 输入框组（价格 ¥ 前缀）
.goods-edit-page__form-input-group {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;                  // 右对齐
}

// 输入前缀（¥ 符号）
.goods-edit-page__input-prefix {
  font-size: $font-lg;                        // 32rpx
  font-weight: $font-weight-bold;             // 600 字重
  color: $primary-color;                      // 主题蓝：#007AFF
  margin-right: $spacing-xs;                  // 与输入框的间距：8rpx
}

// 选择器容器
.goods-edit-page__form-selector {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;                  // 右对齐
  gap: $spacing-xs;                           // 间距：8rpx
  font-size: $font-md;                        // 28rpx
  color: $text-color;                         // 主文字色：#1D1D1F
}

// 占位文字
.goods-edit-page__placeholder {
  color: #C7C7CC;                             // 浅色占位文字
}

// ==================== 文本域 ====================
.goods-edit-page__textarea {
  width: 100%;
  min-height: 200rpx;                         // 最小高度
  font-size: $font-md;                        // 28rpx
  color: $text-color;                         // 主文字色：#1D1D1F
  line-height: 1.6;
  padding: 16rpx;                             // 内边距
  background: $bg-color;                      // 浅灰背景
  border-radius: $radius-sm;                  // 圆角：12rpx
  box-sizing: border-box;                     // 盒模型
}

// 字数统计
.goods-edit-page__char-count {
  display: block;                             // 块级元素
  text-align: right;                          // 右对齐
  font-size: $font-xs;                        // 20rpx
  color: $text-muted;                         // 次要文字色：#8E8E93
  margin-top: $spacing-xs;                    // 顶部间距：8rpx
}

// ==================== 底部保存按钮 ====================
.goods-edit-page__footer {
  position: fixed;                            // 固定在页面底部
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16rpx 24rpx;                       // 上下 16rpx，左右 24rpx
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom, 0)); // 安全区域适配
  background: $bg-white;                      // 白色背景
  border-top: 1rpx solid $border-color;       // 顶部分隔线
  box-shadow: $shadow-md;                     // 顶部阴影
  z-index: 100;                               // 确保在内容之上
}

// 保存按钮
.goods-edit-page__save-btn {
  background: $primary-color;                 // 主题蓝背景：#007AFF
  color: #ffffff;                             // 白色文字
  border-radius: $radius-xl;                  // 圆角胶囊：32rpx
  height: 88rpx;                              // 按钮高度：88rpx
  @include flex-center;                       // 使用混入：水平垂直居中
  font-size: $font-lg;                        // 32rpx
  font-weight: $font-weight-bold;             // 600 字重

  // 禁用状态（保存中）
  &--disabled {
    opacity: 0.6;                             // 降低透明度
  }
}
</style>
