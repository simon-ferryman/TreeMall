<!--
  App.vue — 应用根组件

  【设计思想】
  App.vue 是 uni-app 的根组件，所有页面都是它的子组件。
  它负责：
  1. 全局生命周期管理（onLaunch、onShow、onHide）
  2. 全局样式定义
  3. 应用启动时的数据预加载

  【注意】App.vue 中不能使用 <template>，因为它是逻辑层而非视图层
-->
<script setup>
  // 导入 uni-app 生命周期钩子
  import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
  // 导入全局 Store
  import { useAppStore } from '@/stores/app'
  import { useUserStore } from '@/stores/user'

  /**
   * onLaunch — 应用初始化（仅触发一次）
   *
   * 【触发时机】小程序首次打开时
   * 【执行内容】
   * 1. 预加载全局数据（分类列表、轮播图）
   * 2. 检查登录状态
   *
   * 【为什么在这里预加载？】
   * 分类列表和轮播图是首页必须的数据，在应用启动时预加载可以：
   * - 避免首页打开时出现空白/加载中
   * - 多个页面共享数据，避免重复请求
   */
  onLaunch(() => {
    console.log('[App] 应用启动，开始预加载全局数据...')

    // 获取全局 Store 实例
    const appStore = useAppStore()

    // 并行预加载分类列表和轮播图数据
    // Promise.all 同时发起两个请求，总耗时 = max(请求1耗时, 请求2耗时)
    // 而非 请求1耗时 + 请求2耗时
    Promise.all([
      appStore.fetchCategories(),  // 预加载分类列表
      appStore.fetchBanners()      // 预加载轮播图
    ]).then(() => {
      console.log('[App] 全局数据预加载完成')
    }).catch(err => {
      console.error('[App] 全局数据预加载失败:', err)
    })
  })

  /**
   * onShow — 应用从后台进入前台
   *
   * 【触发时机】
   * - 小程序从后台切换到前台
   * - 从其他小程序返回
   * - 每次打开小程序时（在 onLaunch 之后）
   */
  onShow(() => {
    console.log('[App] 应用进入前台')
  })

  /**
   * onHide — 应用从前台进入后台
   *
   * 【触发时机】用户切换到其他小程序或回到微信主界面
   * 【使用场景】可以在这里保存临时数据、停止定时器等
   */
  onHide(() => {
    console.log('[App] 应用进入后台')
  })
  // 在 App.vue 的 <script setup> 中，onLaunch 函数内增加：
  import { getUserInfo } from '@/api/auth'

  onLaunch(() => {
      console.log('[App] 应用启动，开始预加载全局数据...')

      const appStore = useAppStore()
      const userStore = useUserStore()

      // 如果已有 token，主动获取最新用户信息
      if (userStore.isLogin) {
        getUserInfo().then(userInfo => {
          userStore.setUserInfo(userInfo)
          console.log('[App] 用户信息已刷新')
        }).catch(err => {
          console.error('[App] 刷新用户信息失败:', err)
          // token 可能已过期，清除登录状态
          if (err.message && err.message.includes('401')) {
            userStore.logout()
          }
        })
      }

      Promise.all([
        appStore.fetchCategories(),
        appStore.fetchBanners()
      ]).then(() => {
        console.log('[App] 全局数据预加载完成')
      }).catch(err => {
        console.error('[App] 全局数据预加载失败:', err)
      })
    })
</script>

<!-- ==================== 全局样式 ==================== -->
<style lang="scss">
  /* ============================================
   *  全局样式定义
   *
   *  【设计原则】
   *  - 这里只放全局通用样式（页面容器、通用组件等）
   *  - 页面级样式放在各自的 .vue 文件中
   *  - 颜色变量统一在 uni.scss 中定义
   *  ============================================ */

  /* 页面容器通用样式 */
  page {
    /* 小程序中 page 是根元素，设置背景色和字体 */
    background-color: #F2F2F7;  /* 页面背景色：iOS 系统分组背景 */
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
                 'Helvetica Neue', Arial, sans-serif;  /* 系统默认字体栈 */
    font-size: 28rpx;           /* 默认字体大小（rpx 是响应式像素单位） */
    color: #1D1D1F;             /* 默认文字颜色：iOS 深色文字 */
    line-height: 1.6;           /* 行高 */
  }

  /* 全局重置：去除默认边距 */
  view, text, image, scroll-view, swiper, swiper-item {
    box-sizing: border-box;     /* 统一盒模型：border-box（边距计入宽高） */
    margin: 0;
    padding: 0;
  }

  /* 图片默认样式 */
  image {
    display: block;             /* 去除图片底部间隙（默认 inline 会有 3px 间隙） */
    width: 100%;                /* 默认占满父容器宽度 */
    height: auto;               /* 高度自适应 */
  }
</style>
