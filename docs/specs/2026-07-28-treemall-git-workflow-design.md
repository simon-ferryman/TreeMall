# TreeMall 企业级 Git 工作流设计文档

> 版本: v1.0  
> 日期: 2026-07-28  
> 状态: 已批准

---

## 一、概述

本文档定义 TreeMall 电商项目从"本地开发"到"企业级团队协作"的完整 Git 工作流方案。基于 Git Flow 分支策略、GitHub 双仓库协作模式、Conventional Commits 规范和 SemVer 版本管理，模拟真实企业开发全流程。

### 当前状态

- 后端（Spring Boot 3.2.0）：63 个 Java 文件，11 个 Controller，已完成
- 前端（uni-app + Vue 3）：20+ 页面，基础设施完成，进行中
- 数据库：MySQL 8.0 + Redis 7，9 张表
- 版本管理：未初始化 Git 仓库
- 规划文档：从 PRD V1.0 到 V1.8 技术设计文档，已在 TRAE 工作区

---

## 二、核心决策

| # | 决策项 | 选择 | 理由 |
|---|--------|------|------|
| 1 | Git 托管平台 | GitHub（主）+ Gitee（镜像） | GitHub 生态全球标准，Gitee 镜像解决国内访问速度 |
| 2 | 分支策略 | Git Flow | 项目有明确版本规划（MVP → V2 → V3），与 Git Flow 天然匹配 |
| 3 | 团队协作模式 | 双仓库（Fork + PR） | 企业 Git 协作真实形态，单人可模拟完整流程 |
| 4 | 仓库结构 | Monorepo（前后端合一） | 聚焦学习 Git Flow 流程，避免多仓库管理复杂度 |
| 5 | Commit 规范 | Conventional Commits | 业界标准，可自动生成 Changelog，与 Git Flow 生态配套 |
| 6 | 分支命名 | 混合型 | 日常功能用简洁名，Bug 修复关联 Issue 编号 |
| 7 | 版本号 | SemVer（v1.0.0） | 全球标准，与 Git Flow 的 release 分支、Tag 管理配套 |
| 8 | 合并策略 | `--no-ff` + Squash Merge | release/hotfix 合并用 `--no-ff` 保留版本边界；feature PR 用 Squash 保持历史干净 |
| 9 | 工作项管理 | GitHub Issues + Labels + Milestones + Projects | 需求→Issue→分支→PR→合并，全链路可追溯 |
| 10 | 镜像同步 | GitHub Actions 自动推送 | 自动化，无需手动维护 |

---

## 三、仓库拓扑

```
                    GitHub.com
   ┌─────────────────────────────────────────┐
   │                                         │
   │  TreeMall (中央仓库)                     │
   │  ├── main         (生产分支)              │
   │  ├── develop      (开发集成分支)           │
   │  ├── release/*    (发布分支)              │
   │  └── hotfix/*     (紧急修复分支)           │
   │         ↑                               │
   │     PR Merge                            │
   │         │                               │
   │  your-fork/TreeMall (个人 Fork 仓库)      │
   │  ├── develop      (同步上游)              │
   │  └── feature/*    (功能开发分支)           │
   │                                         │
   └─────────────────────────────────────────┘
                    ⇅ Actions 自动同步
   ┌─────────────────────────────────────────┐
   │  Gitee (镜像仓库 - 只读)                  │
   │  TreeMall-Mirror                        │
   └─────────────────────────────────────────┘
```

---

## 四、Git Flow 完整工作流

### 4.1 功能开发流程（feature）

```
1. 从 develop 拉分支
   git checkout -b feature/product-review develop

2. 开发 + 多次提交（Conventional Commits）
   git commit -m "feat(review): 添加评价实体和数据库表"
   git commit -m "feat(review): 实现评价创建接口"

3. 推送并创建 PR
   git push origin feature/product-review
   → GitHub: feature/product-review → develop (中央仓库)

4. Code Review → Approve → Squash Merge
```

### 4.2 版本发布流程（release）

```
1. 拉 release 分支
   git checkout -b release/1.1.0 develop

2. 只修 Bug，不加新功能
   git commit -m "fix(review): 修复评价列表分页错误"

3. 测试通过后合并
   git checkout main && git merge --no-ff release/1.1.0
   git tag -a v1.1.0 -m "Release v1.1.0"
   git checkout develop && git merge --no-ff release/1.1.0
   git branch -d release/1.1.0
```

### 4.3 紧急修复流程（hotfix）

```
1. 从 main 拉 hotfix 分支（不是 develop！）
   git checkout -b hotfix/ISSUE-99-payment-crash main

2. 修复 Bug
   git commit -m "fix(payment): 修复微信回调签名验证"

3. 同时合到 main 和 develop（关键！）
   git checkout main && git merge --no-ff hotfix/ISSUE-99-payment-crash
   git tag -a v1.1.1 -m "Hotfix v1.1.1"
   git checkout develop && git merge --no-ff hotfix/ISSUE-99-payment-crash
   git branch -d hotfix/ISSUE-99-payment-crash
```

---

## 五、分支命名规范

| 分支类型 | 命名格式 | 示例 |
|----------|----------|------|
| 功能开发 | `feature/<功能名>` | `feature/product-review` |
| 发布分支 | `release/<版本号>` | `release/1.1.0` |
| 紧急修复 | `hotfix/<编号>-<描述>` | `hotfix/ISSUE-99-payment-crash` |
| 长期分支 | `main` / `develop` | 永久存在 |

---

## 六、Commit 规范（Conventional Commits）

| 类型 | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat(product): 添加商品搜索接口` |
| `fix` | Bug 修复 | `fix(cart): 修复购物车数量计算错误` |
| `docs` | 文档变更 | `docs(api): 更新订单接口文档` |
| `refactor` | 重构 | `refactor(order): 抽取订单状态机` |
| `test` | 测试 | `test(payment): 添加支付回调单元测试` |
| `chore` | 构建/工具 | `chore(deps): 升级 Spring Boot 至 3.2.1` |

---

## 七、PR 规范

### PR 描述模板

```
## 变更概述
一句话描述本次 PR 做了什么

## 关联 Issue
Closes #12

## 变更类型
- [ ] feat: 新功能
- [ ] fix: Bug 修复
- [ ] refactor: 重构
- [ ] docs: 文档

## 影响范围
- 后端: 新增 ReviewController、ReviewService
- 数据库: 新增 t_review 表
- 前端: 无变更

## 测试情况
- [ ] 单元测试通过
- [ ] 接口测试通过

## Checklist
- [ ] 代码已自测
- [ ] 无硬编码密钥/密码
- [ ] 无调试代码残留
```

### Code Review 审查清单

| 维度 | 检查项 |
|------|--------|
| 代码规范 | 命名清晰、格式一致、符合项目风格 |
| 业务逻辑 | 边界条件处理、异常覆盖 |
| 安全 | 无 SQL 注入、敏感信息未硬编码、权限校验完整 |
| 性能 | 无 N+1 查询、循环中无 DB 调用、缓存合理 |
| 测试 | 核心逻辑有单元测试、异常路径覆盖 |

---

## 八、Issue 管理

### Issue 模板

```
## 需求描述
作为「消费者」，我希望在商品详情页看到其他用户的评价。

## 验收标准
- [ ] 商品详情页展示评价列表（分页，每页 10 条）
- [ ] 支持按评分筛选
- [ ] 空状态展示

## 技术要点
- 后端: 新增 ReviewController
- 数据库: 新增 t_review 表
- 前端: 商品详情页新增评价 Tab

## 关联
- 所属版本: v1.1.0
```

### Label 体系

| Label | 颜色 | 用途 |
|-------|------|------|
| `feature` | 绿色 | 新功能需求 |
| `bug` | 红色 | Bug 报告 |
| `enhancement` | 蓝色 | 改进优化 |
| `documentation` | 灰色 | 文档相关 |
| `backend` | 黄色 | 仅影响后端 |
| `frontend` | 紫色 | 仅影响前端 |
| `priority:high` | 橙红 | 高优先级 |

---

## 九、SemVer 版本号

| 变更类型 | 版本号变化 | 示例 |
|----------|------------|------|
| 修复 Bug | 修订号 +1 | v1.0.0 → v1.0.1 |
| 新增功能（兼容） | 次版本号 +1，修订号归零 | v1.0.1 → v1.1.0 |
| 不兼容 API 变更 | 主版本号 +1，其余归零 | v1.1.0 → v2.0.0 |
| 紧急修复 | 修订号 +1 | v1.1.0 → v1.1.1 |

TreeMall 首次发布版本号: **v1.0.0**

---

## 十、企业流程映射

| 企业流程 | 设计对应 |
|----------|----------|
| 1. 需求评审与估时 | GitHub Issue + Labels + Milestone |
| 2. 技术方案设计 | PRD/技术文档 → `docs/` 目录 |
| 3. 编码与每日站会 | `feature/*` 分支 + Conventional Commits + Projects 看板 |
| 4. 代码审查 | PR + Review Checklist + Squash Merge |
| 5. 联调与提测 | `release/*` 分支 = 测试冻结窗口 |
| 6. 部署上线 | `release` → `main` + Tag；`hotfix/*` 应急 |

---

## 十一、仓库文件规划

| 文件/目录 | 是否提交 | 原因 |
|-----------|----------|------|
| `src/` | 是 | 后端源代码 |
| `treemall-mp/` | 是 | 前端源代码 |
| `pom.xml` | 是 | Maven 构建定义 |
| `db/init/` | 是 | 数据库初始化脚本 |
| `docker-compose.dev.yml` | 是 | 本地开发环境 |
| `README.md` | 是 | 项目说明 |
| `docs/` | 是 | 设计文档、PRD 等 |
| `target/` | 否 | Maven 编译产物 |
| `node_modules/` | 否 | npm 依赖 |
| `application-prod.yml` | 否 | 含生产密钥 |
| `.idea/` | 否 | IDE 个人配置 |

---

## 十二、角色分配

| 角色 | 职责 | 账号 |
|------|------|------|
| 开发者 | Fork 仓库，feature 分支开发，提交 PR | 个人 GitHub 账号 |
| 审查者 | Review PR，提出意见，Approve | 个人 GitHub 账号 |
| 发布经理 | 创建 release 分支，合并到 main，打 Tag | 个人 GitHub 账号 |
| AI 助手 | 辅助 Code Review、提供技术建议 | TRAE |