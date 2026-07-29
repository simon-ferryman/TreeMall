/**
 * main.js — 应用入口文件
 *
 * 【设计思想】
 * main.js 是 uni-app 的入口文件，负责：
 * 1. 创建 Vue 3 应用实例
 * 2. 注册全局插件（Pinia、uView UI）
 * 3. 导出 createApp 函数（uni-app 引擎会调用它来创建应用）
 *
 * 【与普通 Vue 项目的区别】
 * 普通 Vue 项目：main.js 直接调用 createApp(App).mount('#app')
 * uni-app：导出 createApp 函数，由 uni-app 引擎在合适的时机调用
 * 这种设计是为了适配多个平台（小程序、H5、App）不同的启动流程
 *
 * 【文件夹依赖关系】
 * main.js
 *   ├── App.vue（根组件）
 *   ├── uview-plus（UI 组件库）
 *   ├── pinia（状态管理）
 *   └── stores/（各 Store 模块）
 *        ├── stores/user.js
 *        ├── stores/app.js
 *        └── stores/cart.js
 */

// 导入 Vue 3 的 createSSRApp 函数
// createSSRApp 是 uni-app 提供的 SSR（服务端渲染）兼容版本
// 在小程序中，uni-app 使用 SSR 模式渲染页面，所以必须用 createSSRApp 而非 createApp
import { createSSRApp } from 'vue'

// 导入根组件
import App from './App.vue'

// 导入 uView UI 3.x（专门为 Vue 3 + uni-app 适配的版本）
// uview-plus 提供了 60+ 个高质量组件（按钮、输入框、列表、弹窗等）
import uviewPlus from 'uview-plus'

// 导入 Pinia 状态管理库
// createPinia 创建 Pinia 实例，将被挂载到 Vue 应用上
import { createPinia } from 'pinia'

/**
 * createApp — 导出应用创建函数
 *
 * uni-app 引擎会在启动时调用这个函数来创建应用实例
 * 返回 { app } 对象，uni-app 引擎会用它来挂载和渲染页面
 *
 * 【为什么导出函数而不是直接创建？】
 * uni-app 需要在不同平台（小程序/H5/App）使用不同的应用实例创建方式，
 * 导出函数让 uni-app 引擎在内部控制创建时机和参数
 */
export function createApp() {
  // 1. 创建 Vue 3 应用实例（SSR 兼容版本）
  const app = createSSRApp(App)

  // 2. 注册 Pinia 状态管理
  // Pinia 是 Vue 3 官方推荐的状态管理库，替代 Vuex
  const pinia = createPinia()          // 创建 Pinia 实例
  app.use(pinia)                       // 将 Pinia 挂载到 Vue 应用

  // 3. 注册 uView UI 组件库
  // uView 注册后，所有 u- 前缀的组件（如 <u-button>、<u-input>）都可以全局使用
  app.use(uviewPlus)

  // 4. 返回应用实例
  // uni-app 引擎会使用这个 app 实例来渲染页面
  return { app }
}
