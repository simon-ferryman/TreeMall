package com.treemall.dto.request;                                       // 请求 DTO 包

import lombok.Data;                                                     // Lombok：自动生成 getter/setter
import java.math.BigDecimal;                                             // 价格：精确计算

/**
 * 商品保存请求 DTO（添加 + 编辑共用）
 *
 * 使用场景：
 *   商户 POST /api/v1/merchant/product 添加商品
 *   商户 PUT  /api/v1/merchant/product 编辑商品（编辑时多传一个 id）
 *
 * 请求体示例（添加）：
 * {
 *   "categoryId": 5,
 *   "name": "华为 Mate 70 Pro",
 *   "description": "<p>旗舰手机，麒麟芯片</p>",
 *   "price": 6999.00,
 *   "originalPrice": 7999.00,
 *   "stock": 100,
 *   "mainImage": "/images/product/huawei_mate70.jpg",
 *   "images": "[\"/images/product/huawei1.jpg\",\"/images/product/huawei2.jpg\"]",
 *   "specs": "{\"品牌\":\"华为\",\"屏幕\":\"6.82英寸\",\"内存\":\"12GB\"}"
 * }
 *
 * 设计原则：DTO 和 Entity 分离
 *   - 前端不需要传 merchantId、status、salesCount 等字段（后端自动设置）
 *   - 编辑时，前端只传要修改的字段，不传的字段保持不变
 */
@Data                                                                   // 自动生成 getter/setter/toString
public class ProductSaveRequest {

    /**
     * 商品 ID（编辑时必传，添加时为空）
     * 后端根据此字段判断是添加还是编辑：
     *   id == null → 新增商品
     *   id != null → 编辑商品
     */
    private Long id;

    private Long categoryId;                                              // 所属分类 ID（必填）

    private String name;                                                  // 商品名称（必填）

    private String description;                                           // 商品描述（可选，富文本 HTML）

    private BigDecimal price;                                             // 售价（元，必填）

    private BigDecimal originalPrice;                                    // 原价（元，可选，用于展示划线价）

    private Integer stock;                                                // 库存数量（必填）

    private String mainImage;                                            // 商品主图 URL（必填，先上传图片获得 URL 再提交）

    /**
     * 商品图片列表（JSON 数组字符串）
     * 内容示例：["/images/product/p1.jpg","/images/product/p2.jpg"]
     * 用于商品详情页轮播图展示
     */
    private String images;

    /**
     * 规格参数（JSON 字符串）
     * 内容示例：{"品牌":"华为","屏幕":"6.82英寸","内存":"12GB"}
     * 用于商品详情页规格参数展示
     */
    private String specs;
}