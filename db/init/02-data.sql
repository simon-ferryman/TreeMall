-- ============================================
-- TreeMall 初始数据 V2（修正版）
-- 执行顺序：先执行 01-schema-v2.sql 建表，再执行本脚本插数据
-- 说明：
--   1. V1 限制为二级分类，不再插入三级分类
--   2. 分类标签可自定义（通过商户后台添加/修改）
--   3. 轮播图数据不变
-- ============================================

USE tree_mall;

-- ============================================
-- 商品分类（二级结构）
-- 顶级分类 → 二级分类
-- 商户/管理员可在后台自定义添加分类名称
-- ============================================

-- 一级分类（parent_id=0）
INSERT INTO t_category (id, name, parent_id, sort_order) VALUES
(1, '手机通讯', 0, 1),
(2, '电脑办公', 0, 2),
(3, '家用电器', 0, 3),
(4, '数码配件', 0, 4);
(5, '家电', 0, 4);
-- 二级分类（parent_id 指向一级分类的 id）
INSERT INTO t_category (id, name, parent_id, sort_order) VALUES
(6,  '智能手机', 1, 1),
(7,  '老年手机', 1, 2),
(8,  '笔记本',   2, 1),
(9,  '台式机',   2, 2),
(10,  '平板电脑', 2, 3),

-- ============================================
-- 首页轮播图
-- 默认 3 张轮播图，跳转到具体分类
-- ============================================
INSERT INTO t_banner (title, image_url, link_type, link_target, sort_order) VALUES
('夏季空调大促',  '/images/banner/banner1.jpg', 'CATEGORY', '13', 1),
('智能手机新品上市', '/images/banner/banner2.jpg', 'CATEGORY', '5',  2),
('笔记本以旧换新', '/images/banner/banner3.jpg', 'CATEGORY', '7',  3);


INSERT INTO t_product (id, category_id, merchant_id, name, description, price, original_price, stock, main_image, images, specs, sales_count, status)
VALUES
    (1, 1, 2, '华为 Mate 60 Pro', '搭载麒麟芯片的旗舰手机', 6999.00, 7999.00, 100,
     '/images/product/product1.jpg',
     '["/images/product/product1.jpg","/images/product/product2.jpg","/images/product/product3.jpg"]',
     '{"品牌":"华为","屏幕":"6.82英寸","内存":"12GB","存储":"512GB"}', 258, 1),

    (2, 2, 2, 'iPhone 15 Pro Max', 'Apple 最新旗舰手机', 8999.00, 9999.00, 50,
     '/images/product/product2.jpg',
     '["/images/product/product2.jpg","/images/product/product4.jpg"]',
     '{"品牌":"Apple","屏幕":"6.7英寸","内存":"8GB","存储":"256GB"}', 189, 1),

    (3, 3, 2, '联想 ThinkPad X1 Carbon', '轻薄商务笔记本', 9999.00, 12999.00, 30,
     '/images/product/product3.jpg',
     '["/images/product/product3.jpg","/images/product/product5.jpg"]',
     '{"品牌":"联想","屏幕":"14英寸","处理器":"i7-1365U","内存":"16GB"}', 76, 1),

    (4, 4, 2, 'MacBook Pro 14英寸', 'Apple M3 Pro 芯片', 14999.00, 16999.00, 20,
     '/images/product/product4.jpg',
     '["/images/product/product4.jpg","/images/product/product1.jpg"]',
     '{"品牌":"Apple","屏幕":"14.2英寸","处理器":"M3 Pro","内存":"18GB"}', 132, 1),

    (5, 5, 2, '小米电视 ES Pro 65英寸', '4K 超高清智能电视', 3299.00, 3999.00, 60,
     '/images/product/product5.jpg',
     '["/images/product/product5.jpg","/images/product/product2.jpg"]',
     '{"品牌":"小米","尺寸":"65英寸","分辨率":"3840×2160","类型":"智能电视"}', 421, 1);