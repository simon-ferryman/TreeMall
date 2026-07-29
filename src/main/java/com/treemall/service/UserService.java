package com.treemall.service;                                           // 服务接口包

import com.treemall.entity.User;                                        // 用户实体

/**
 * 用户服务接口
 * 定义用户相关的业务方法，由 UserServiceImpl 实现
 */
public interface UserService {

    /**
     * 微信登录 — 根据 openid 查找用户，不存在则自动注册
     *
     * 流程：
     *   1. 用微信 code 换取 openid（调用微信 API）
     *   2. 根据 openid 查数据库
     *   3. 如果用户不存在 → 自动创建新用户
     *   4. 生成 JWT Token 返回
     *
     * @param code 微信登录凭证（前端调用 wx.login() 获取）
     * @return JWT Token 字符串
     */
    String login(String code);                                          // 返回值是 JWT Token

    /**
     * 根据 ID 获取用户信息
     * @param userId 用户 ID
     * @return 用户实体
     */
    User getUserById(Long userId);                                      // 简单查询

    /**
     * 更新用户信息（昵称、头像、手机号）
     * @param user 用户实体（只需填充要更新的字段）
     */
    void updateUser(User user);                                         // 更新操作
}