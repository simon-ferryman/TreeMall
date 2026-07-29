package com.treemall.controller;                                         // 控制器包

import com.treemall.common.Result;                                      // 统一响应格式
import com.treemall.common.UserContext;                                 // 当前用户上下文
import com.treemall.entity.User;                                        // 用户实体
import com.treemall.service.UserService;                                // 用户服务
import lombok.RequiredArgsConstructor;                                   // 构造器注入
import org.springframework.web.bind.annotation.*;                       // REST 注解
import com.treemall.common.JwtUtil;
import java.util.HashMap;                                               // 用于构建返回数据
import java.util.Map;


/**
 * 认证控制器 — 处理登录和用户信息相关接口
 *
 * REST API 路径设计：
 *   POST   /api/v1/auth/login      — 微信登录
 *   GET    /api/v1/auth/userinfo   — 获取当前用户信息（需要 Token）
 *   PUT    /api/v1/auth/userinfo   — 更新用户信息（需要 Token）
 */
@RestController                                                        // = @Controller + @ResponseBody（所有方法返回 JSON）
@RequestMapping("/api/v1/auth")                                        // 统一路径前缀
@RequiredArgsConstructor                                                 // 构造器注入
public class AuthController {

    private final UserService userService;                              // 用户服务
    private final JwtUtil jwtUtil;
    /**
     * 微信登录接口
     *
     * 请求方式：POST /api/v1/auth/login
     * 请求体：  { "code": "0a3Xyz..." }  ← 前端 wx.login() 返回的临时凭证
     * 响应体：  { "code": 200, "message": "成功", "data": { "token": "eyJhbG..." } }
     *
     * 注意：此接口不需要 Token（已在 WebConfig 中排除拦截）
     *
     * @param </code> 微信登录凭证（前端通过 wx.login() 获取）
     * @return 包含 JWT Token 的响应
     */
    @PostMapping("/login")                                             // POST /api/v1/auth/login
    public Result<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String code = body.get("code");                                  // 从请求体中取 code
        if (code == null || code.isEmpty()) {
            return Result.error(400, "登录凭证不能为空");               // 参数校验
        }

        String token = userService.login(code);                         // 调用 Service 完成登录

        Map<String, String> data = new HashMap<>();                     // 构建返回数据
        data.put("token", token);                                       // 前端拿到 Token 后存入本地缓存
        return Result.success(data);                                    // 返回成功响应
    }

    /**
     * 获取当前用户信息
     *
     * 请求方式：GET /api/v1/auth/userinfo
     * 请求头：  Authorization: Bearer <token>
     * 响应体：  { "code": 200, "message": "成功", "data": { "id": 1, "nickname": "张三", ... } }
     *
     * 注意：此接口需要 Token（JwtInterceptor 会拦截并解析出 userId）
     */
    @GetMapping("/userinfo")                                           // GET /api/v1/auth/userinfo
    public Result<User> getUserInfo() {
        Long userId = UserContext.getUserId();                           // 从拦截器存入的上下文获取 userId
        User user = userService.getUserById(userId);                    // 查数据库
        user.setOpenid(null);                                           // 安全：不返回 openid 给前端
        return Result.success(user);                                    // 返回用户信息
    }

    /**
     * 更新用户信息
     *
     * 请求方式：PUT /api/v1/auth/userinfo
     * 请求头：  Authorization: Bearer <token>
     * 请求体：  { "nickname": "新昵称", "avatarUrl": "https://...", "phone": "13800138000" }
     * 响应体：  { "code": 200, "message": "成功", "data": null }
     *
     * 注意：只更新前端传来的字段，其他字段保持不变
     */
    @PutMapping("/userinfo")                                           // PUT /api/v1/auth/userinfo
    public Result<Void> updateUserInfo(@RequestBody Map<String, Object> body) {
        Long userId = UserContext.getUserId();                           // 获取当前用户 ID

        User user = new User();                                         // 创建临时 User 对象
        user.setId(userId);                                             // 设置要更新的用户 ID

        // 只更新前端传来的字段（nickname、avatarUrl、phone）
        if (body.containsKey("nickname")) {
            user.setNickname((String) body.get("nickname"));            // 更新昵称
        }
        if (body.containsKey("avatarUrl")) {
            user.setAvatarUrl((String) body.get("avatarUrl"));          // 更新头像
        }
        if (body.containsKey("phone")) {
            user.setPhone((String) body.get("phone"));                  // 更新手机号
        }

        userService.updateUser(user);                                   // 执行更新
        return Result.success();                                        // 返回成功（无数据）
    }
    /**
     * 开发阶段测试登录（DEV ONLY）
     *
     * 请求方式：POST /api/v1/auth/dev-login
     * 请求体：  { "userId": 1 }
     * 响应体：  { "code": 200, "message": "成功", "data": { "token": "eyJhbG..." } }
     *
     * 用途：在没有真实微信 code 的情况下，用 userId 直接生成 Token 进行接口测试
     * 注意：此接口仅在开发环境使用，生产环境应删除或通过配置禁用
     */
    @PostMapping("/dev-login")
    public Result<Map<String, Object>> devLogin(@RequestBody Map<String, Object> body) {
        // 从请求体取 userId，默认 1
        Long userId = body.containsKey("userId")
                ? Long.valueOf(body.get("userId").toString())
                : 1L;
        String role = body.containsKey("role")
                ? (String) body.get("role")
                : "consumer";
        // 查询用户是否存在
        User user = userService.getUserById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在，请检查用户ID");
        }
        // 直接生成 Token，不调用微信 API
        String token = jwtUtil.generateToken(userId, role);
        // 构建用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("nickname", user.getNickname() != null ? user.getNickname() : "");
        userInfo.put("avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
        userInfo.put("phone", user.getPhone() != null ? user.getPhone() : "");
        userInfo.put("role", user.getRole());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userInfo", userInfo);

        return Result.success(data);
    }
}