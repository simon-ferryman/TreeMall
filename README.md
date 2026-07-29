# TreeMall - 电商小程序

基于 Spring Boot 3.2 + uni-app (Vue 3) 的全栈电商项目。

## 技术栈

**后端**
- Java 17 + Spring Boot 3.2.0
- MyBatis-Plus 3.5.7
- MySQL 8.0 + Redis 7
- JWT 无状态认证
- 微信支付 V3

**前端**
- uni-app (Vue 3 + Composition API)
- Pinia 2.1 状态管理
- uView Plus 3.1 UI 组件库
- 目标平台: 微信小程序

## 快速开始

### 1. 启动基础设施

```bash
docker-compose -f docker-compose.dev.yml up -d
```

### 2. 初始化数据库

执行 `db/init/01-schema.sql` 和 `db/init/02-data.sql`

### 3. 启动后端

```bash
./mvnw spring-boot:run
```
或者如下方式启动
```bash
mvn clean compile
mvn spring-boot:run '-Dspring-boot.run.profiles=dev'
```

或使用 IDE 运行 `TreemallApplication.java`

### 4. 启动前端

```bash
cd treemall-mp
npm install
npm run dev:mp-weixin
```

## 项目结构
```bash
TreeMall/ 
├── src/ # 后端源码 (Spring Boot) 
├── treemall-mp/ # 前端源码 (uni-app) 
├── db/init/ # 数据库初始化脚本 
├── docker-compose.dev.yml # 本地开发环境 
└── docs/ # 设计文档

```

