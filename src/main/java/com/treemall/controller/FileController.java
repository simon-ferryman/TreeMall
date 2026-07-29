package com.treemall.controller;                                        // 控制器包

import cn.hutool.core.util.IdUtil;                                      // Hutool 工具：生成随机文件名
import com.treemall.common.Result;                                      // 统一响应格式
import lombok.extern.slf4j.Slf4j;                                       // 日志
import org.springframework.beans.factory.annotation.Value;              // 读取配置
import org.springframework.web.bind.annotation.*;                       // Spring MVC 注解
import org.springframework.web.multipart.MultipartFile;                 // 上传文件对象

import java.io.File;                                                    // 文件操作
import java.io.IOException;                                             // IO 异常
import java.nio.file.Files;                                             // 文件工具类
import java.nio.file.Path;                                              // 文件路径
import java.nio.file.Paths;                                             // 路径工具
import java.util.HashMap;                                               // 返回结果 Map
import java.util.Map;

/**
 * 文件上传控制器
 *
 * 使用场景：商户在后台添加/编辑商品时，上传商品图片
 * 流程：商户选择图片 → 前端调用上传接口 → 后端保存到本地磁盘 → 返回图片 URL
 *       商户拿到 URL 后，填入商品的 mainImage 或 images 字段
 *
 * 存储策略：
 *   V1：本地文件系统 → /data/images/ 目录
 *   V2：迁移到腾讯云 COS（对象存储），只需改此 Controller，不影响其他代码
 *
 * 请求路径：/api/v1/file/upload
 * 需要 Token + 商户角色（MerchantInterceptor 拦截 /api/v1/merchant/**）
 */
@Slf4j                                                                  // 自动生成 log 对象
@RestController
@RequestMapping("/api/v1/merchant")                                       // 放在商户端路径下，受 MerchantInterceptor 保护
public class FileController {

    /**
     * 从配置文件读取上传目录
     * application.yml 中：file.upload-dir: /data/images/
     */
    @Value("${file.upload-dir:/data/images/}")                           // 读取配置，默认值 /data/images/
    private String uploadDir;

    /**
     * 上传图片
     *
     * 请求方式：POST /api/v1/merchant/file/upload
     * 请求格式：multipart/form-data（表单上传）
     * 参数名：  file（图片文件）
     *
     * 响应体示例：
     * {
     *   "code": 200,
     *   "data": {
     *     "url": "/images/product/20260716_a1b2c3d4.jpg"
     *   }
     * }
     *
     * 实现逻辑：
     *   1. 校验文件是否为空
     *   2. 校验文件类型（只允许图片格式）
     *   3. 校验文件大小（不超过配置的最大值）
     *   4. 生成唯一文件名（日期 + UUID 前8位 + 原扩展名）
     *   5. 保存到本地磁盘
     *   6. 返回访问 URL
     */
    @PostMapping("/file/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        // 第1步：校验文件是否为空
        if (file == null || file.isEmpty()) {
            return Result.error(400, "请选择文件");
        }

        // 第2步：校验文件类型（只允许常见图片格式）
        String originalFilename = file.getOriginalFilename();            // 获取原始文件名
        if (originalFilename == null) {
            return Result.error(400, "文件名不能为空");
        }
        // 获取文件扩展名（如 .jpg、.png）
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");                 // 找到最后一个 . 的位置
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex).toLowerCase(); // 转小写，如 ".jpg"
        }
        // 白名单校验：只允许 jpg、jpeg、png、gif、webp
        if (!extension.matches("^\\.(jpg|jpeg|png|gif|webp)$")) {
            return Result.error(400, "不支持的文件类型，仅允许 jpg/jpeg/png/gif/webp");
        }

        // 第3步：校验文件大小（application.yml 配置了 max-size: 20MB，Spring 会自动拦截超大文件）
        // 这里做二次校验
        if (file.getSize() > 20 * 1024 * 1024) {                          // 20MB
            return Result.error(400, "文件大小不能超过 20MB");
        }

        // 第4步：生成唯一文件名（防止重名覆盖）
        // 格式：日期_随机8位.扩展名，如 20260716_a1b2c3d4.jpg
        String dateStr = java.time.LocalDate.now()
                .toString().replace("-", "");                            // 当前日期，如 "20260716"
        String randomStr = IdUtil.fastSimpleUUID().substring(0, 8);      // UUID 前8位，如 "a1b2c3d4"
        String newFileName = dateStr + "_" + randomStr + extension;      // 最终文件名

        // 第5步：创建目录并保存文件
        try {
            // 确保上传目录存在（如果不存在则创建）
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);                      // 递归创建目录
            }

            // 保存文件到磁盘
            File destFile = new File(uploadDir + newFileName);            // 目标文件路径
            file.transferTo(destFile);                                    // 将上传的文件写入磁盘

            log.info("图片上传成功: {}", newFileName);
        } catch (IOException e) {
            log.error("图片上传失败", e);
            return Result.error(500, "图片上传失败，请稍后重试");
        }

        // 第6步：返回访问 URL
        // 前端拿到这个 URL 后，填入商品的 mainImage 或 images 字段
        String imageUrl = "/images/" + newFileName;                      // 相对路径，前端拼接域名
        Map<String, String> result = new HashMap<>();
        result.put("url", imageUrl);                                      // 返回图片 URL

        return Result.success(result);
    }
}