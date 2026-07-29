-- ============================================
-- TreeMall 数据库建表脚本 V2（修正版）
-- 执行顺序：01-schema-v2.sql → 02-data-v2.sql
-- 说明：
--   1. 表名以 t_ 开头，与 application.yml 中 table-prefix: t_ 对应
--   2. MyBatis-Plus 自动映射：Entity 类名 User → 表 t_user
--   3. 金额 DECIMAL(10,2) 存储，Java 代码运算统一转分为单位 int
--   4. 逻辑删除字段 deleted，与 MyBatis-Plus logic-delete-field: deleted 对应
--   5. 不建数据库外键，关系在应用层维护（生产环境惯例）
--   6. t_order 加反引号（MySQL 保留字）
--   7. 订单地址使用 JSON 快照，下单时复制，后续地址修改不影响历史订单
-- ============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS tree_mall
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE tree_mall;

-- ============================================
-- 1. 用户表 (t_user)
-- 存储小程序用户和商户的基本信息
-- 角色设计：consumer=消费者, merchant=商户, admin=最高管理员
-- V1 单商户不拆分，V2 多商户时可新增 t_merchant 表
-- 后续新增 admin 角色只需插入一条 role='admin' 的用户记录，零迁移成本
-- ============================================
CREATE TABLE IF NOT EXISTS t_user (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '用户ID，主键',
    openid          VARCHAR(64)     NOT NULL                 COMMENT '微信 openid，小程序唯一标识',
    nickname        VARCHAR(64)     DEFAULT NULL             COMMENT '用户昵称',
    avatar_url      VARCHAR(512)    DEFAULT NULL             COMMENT '头像 URL',
    phone           VARCHAR(20)     DEFAULT NULL             COMMENT '手机号',
    role            VARCHAR(20)     NOT NULL DEFAULT 'consumer' COMMENT '角色：consumer=消费者, merchant=商户, admin=最高管理员',
    status          TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：1=正常, 0=禁用',
    deleted         TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除：0=未删除, 1=已删除',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_openid (openid),
    KEY idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 商品分类表 (t_category)
-- 树形结构，通过 parent_id 实现父子关系
-- V1 限制为二级分类，但结构支持多级扩展
-- 分类标签可自定义（管理员/商户可自行添加分类名称）
-- 示例：家用电器(parent_id=0) → 冰箱(parent_id=3)
-- ============================================
CREATE TABLE IF NOT EXISTS t_category (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '分类ID',
    name            VARCHAR(64)     NOT NULL                 COMMENT '分类名称',
    parent_id       BIGINT          NOT NULL DEFAULT 0       COMMENT '父分类ID，0 表示顶级分类',
    sort_order      INT             NOT NULL DEFAULT 0       COMMENT '排序值，越小越靠前',
    icon            VARCHAR(256)    DEFAULT NULL             COMMENT '分类图标 URL',
    deleted         TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除：0=未删除, 1=已删除',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id),
    KEY idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- ============================================
-- 3. 商品表 (t_product)
-- 核心业务表，存储商品完整信息
-- 价格：数据库 DECIMAL(10,2) 存储，Java 代码运算统一转分为单位 int
-- 图片：MySQL JSON 类型存 URL 数组，如 ["url1","url2"]
-- V1 不做多规格 SKU，V2 扩展时新增 t_sku 表，价格/库存可迁移
-- ============================================
CREATE TABLE IF NOT EXISTS t_product (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '商品ID',
    category_id     BIGINT          NOT NULL                 COMMENT '所属分类ID',
    merchant_id     BIGINT          NOT NULL                 COMMENT '商户ID（关联 t_user.id，V1 固定为商户用户ID）',
    name            VARCHAR(128)    NOT NULL                 COMMENT '商品名称',
    description     TEXT            DEFAULT NULL             COMMENT '商品描述（富文本 HTML）',
    price           DECIMAL(10,2)   NOT NULL                 COMMENT '售价（元），Java端运算统一转分为单位int',
    original_price  DECIMAL(10,2)   DEFAULT NULL             COMMENT '原价（元），用于展示划线价',
    stock           INT             NOT NULL DEFAULT 0       COMMENT '库存数量',
    main_image      VARCHAR(512)    DEFAULT NULL             COMMENT '主图 URL',
    images          JSON            DEFAULT NULL             COMMENT '商品图片列表（JSON 数组），如 ["/images/p1.jpg","/images/p2.jpg"]',
    specs           JSON            DEFAULT NULL             COMMENT '规格参数（JSON 格式），如 {"品牌":"华为","屏幕":"6.7英寸"}',
    sales_count     INT             NOT NULL DEFAULT 0       COMMENT '累计销量',
    status          TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：1=上架, 0=下架',
    deleted         TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除：0=未删除, 1=已删除',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_merchant_id (merchant_id),
    KEY idx_status (status),
    FULLTEXT KEY ft_name_desc (name, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ============================================
-- 4. 购物车表 (t_cart)
-- 每个用户对每个商品只有一条记录，再次加入则更新数量
-- 唯一约束 (user_id, product_id) 保证不重复
-- ============================================
CREATE TABLE IF NOT EXISTS t_cart (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '购物车项ID',
    user_id         BIGINT          NOT NULL                 COMMENT '用户ID',
    product_id      BIGINT          NOT NULL                 COMMENT '商品ID',
    quantity        INT             NOT NULL DEFAULT 1       COMMENT '购买数量',
    checked         TINYINT         NOT NULL DEFAULT 1       COMMENT '是否选中：1=选中, 0=未选中',
    deleted         TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除：0=未删除, 1=已删除',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_product (user_id, product_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- ============================================
-- 5. 收货地址表 (t_address)
-- 一个用户可以有多个收货地址，只有一个为默认地址
-- is_default 由 Service 层保证唯一性（设置前先清除其他默认）
-- ============================================
CREATE TABLE IF NOT EXISTS t_address (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '地址ID',
    user_id         BIGINT          NOT NULL                 COMMENT '用户ID',
    receiver_name   VARCHAR(32)     NOT NULL                 COMMENT '收货人姓名',
    receiver_phone  VARCHAR(20)     NOT NULL                 COMMENT '收货人电话',
    province        VARCHAR(32)     NOT NULL                 COMMENT '省份',
    city            VARCHAR(32)     NOT NULL                 COMMENT '城市',
    district        VARCHAR(32)     NOT NULL                 COMMENT '区/县',
    detail_address  VARCHAR(256)    NOT NULL                 COMMENT '详细地址',
    is_default      TINYINT         NOT NULL DEFAULT 0       COMMENT '是否默认地址：1=是, 0=否',
    deleted         TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除：0=未删除, 1=已删除',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货地址表';

-- ============================================
-- 6. 订单表 (t_order)
-- 订单主表：一笔订单的汇总信息
-- 【注意】order 是 MySQL 保留字，必须加反引号 `t_order`
-- 地址快照：下单时把地址 JSON 序列化存入，防止用户后续修改影响历史订单
-- 金额：数据库 DECIMAL(10,2) 存储，Java 端运算统一转分为单位 int
-- 订单号：雪花 ID 生成（19位数字），保证全局唯一
-- 订单状态机：PENDING_PAYMENT → PENDING_DELIVERY → DELIVERED → RECEIVED（不可逆跳）
-- ============================================
CREATE TABLE IF NOT EXISTS `t_order` (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '订单ID',
    order_no        VARCHAR(32)     NOT NULL                 COMMENT '订单编号（唯一，雪花ID生成）',
    user_id         BIGINT          NOT NULL                 COMMENT '下单用户ID',
    total_amount    DECIMAL(10,2)   NOT NULL                 COMMENT '订单总金额（元），Java端运算统一转分为单位int',
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING_PAYMENT' COMMENT '订单状态：PENDING_PAYMENT=待支付, PENDING_DELIVERY=待发货, DELIVERED=已发货, RECEIVED=已收货, CANCELLED=已取消',
    address_snapshot JSON           NOT NULL                 COMMENT '收货地址快照（JSON格式），如 {"receiver_name":"张三","receiver_phone":"13800138000","province":"广东省","city":"广州市","district":"天河区","detail_address":"体育西路100号"}。下单时复制，后续地址修改不影响历史订单',
    remark          VARCHAR(256)    DEFAULT NULL             COMMENT '用户备注',
    transaction_id  VARCHAR(64)     DEFAULT NULL             COMMENT '微信支付流水号（支付成功后填入，用于回调幂等判断）',
    pay_time        DATETIME        DEFAULT NULL             COMMENT '支付时间',
    deliver_time    DATETIME        DEFAULT NULL             COMMENT '发货时间',
    receive_time    DATETIME        DEFAULT NULL             COMMENT '收货时间',
    deleted         TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除：0=未删除, 1=已删除',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_transaction_id (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================
-- 7. 订单项表 (t_order_item)
-- 一笔订单包含多个商品，每个商品一条记录
-- 商品名称/图片/价格全部快照存储，不关联 t_product 的当前值
-- ============================================
CREATE TABLE IF NOT EXISTS t_order_item (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '订单项ID',
    order_id        BIGINT          NOT NULL                 COMMENT '所属订单ID',
    product_id      BIGINT          NOT NULL                 COMMENT '商品ID（关联查询用，不依赖其当前价格/名称）',
    product_name    VARCHAR(128)    NOT NULL                 COMMENT '商品名称（快照：下单时的名称）',
    product_image   VARCHAR(512)    DEFAULT NULL             COMMENT '商品主图（快照：下单时的图片）',
    price           DECIMAL(10,2)   NOT NULL                 COMMENT '下单时单价（快照，元），Java端运算统一转分为单位int',
    quantity        INT             NOT NULL DEFAULT 1       COMMENT '购买数量',
    deleted         TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除：0=未删除, 1=已删除',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项表';

-- ============================================
-- 8. 物流表 (t_logistics)
-- 一个订单一条物流记录
-- 物流状态：PENDING=待揽收, IN_TRANSIT=运输中, DELIVERED=已签收
-- ============================================
CREATE TABLE IF NOT EXISTS t_logistics (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '物流ID',
    order_id        BIGINT          NOT NULL                 COMMENT '关联订单ID',
    company_name    VARCHAR(64)     NOT NULL                 COMMENT '物流公司名称',
    tracking_no     VARCHAR(64)     NOT NULL                 COMMENT '物流单号',
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '物流状态：PENDING=待揽收, IN_TRANSIT=运输中, DELIVERED=已签收',
    ship_time       DATETIME        DEFAULT NULL             COMMENT '发货时间',
    arrive_time     DATETIME        DEFAULT NULL             COMMENT '签收时间',
    deleted         TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除：0=未删除, 1=已删除',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_id (order_id),
    KEY idx_tracking_no (tracking_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物流表';

-- ============================================
-- 9. 轮播图表 (t_banner)
-- 首页轮播图，由商户后台管理
-- link_type：PRODUCT=商品详情, CATEGORY=分类页, NONE=无跳转
-- ============================================
CREATE TABLE IF NOT EXISTS t_banner (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '轮播图ID',
    title           VARCHAR(64)     NOT NULL                 COMMENT '轮播图标题',
    image_url       VARCHAR(512)    NOT NULL                 COMMENT '轮播图图片 URL',
    link_type       VARCHAR(20)     DEFAULT NULL             COMMENT '跳转类型：PRODUCT=商品, CATEGORY=分类, NONE=无跳转',
    link_target     VARCHAR(256)    DEFAULT NULL             COMMENT '跳转目标（商品ID或分类ID）',
    sort_order      INT             NOT NULL DEFAULT 0       COMMENT '排序值，越小越靠前',
    status          TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：1=启用, 0=禁用',
    deleted         TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除：0=未删除, 1=已删除',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_sort_order (sort_order),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表';