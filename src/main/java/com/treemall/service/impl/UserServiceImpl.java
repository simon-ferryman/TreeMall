package com.treemall.service.impl;                                      // 服务实现包

import cn.hutool.core.util.StrUtil;                                     // Hutool 字符串工具：判断空字符串
import cn.hutool.json.JSONObject;                                       // Hutool JSON 工具：解析微信返回
import cn.hutool.json.JSONUtil;                                         // Hutool JSON 工具
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // MyBatis-Plus 条件构造器
import com.treemall.common.BusinessException;                            // 业务异常
import com.treemall.common.JwtUtil;                                      // JWT 工具
import com.treemall.entity.User;                                        // 用户实体
import com.treemall.mapper.UserMapper;                                   // 用户 Mapper
import com.treemall.service.UserService;                                 // 用户服务接口
import lombok.RequiredArgsConstructor;                                   // Lombok：构造器注入
import lombok.extern.slf4j.Slf4j;                                       // 日志

import com.treemall.config.WxPayConfig;                                 // 微信配置类

import org.springframework.stereotype.Service;                          // 标记为 Service
import org.springframework.transaction.annotation.Transactional;        // 事务注解
import org.springframework.web.client.RestTemplate;                      // 发送 HTTP 请求


/**
 * 用户服务实现类
 *
 * 设计要点：
 *   1. 微信登录：code → openid → 查库 → 注册/登录 → 返回 Token
 *   2. 登录和注册合并为一个接口（微信小程序标准做法）
 *   3. 首次登录自动创建用户，后续登录只更新 Token
 */
@Slf4j                                                                  // 自动生成 log 对象
@Service                                                                 // 标记为 Service，Spring 会扫描并管理
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;                                // 用户 Mapper
    private final JwtUtil jwtUtil;                                      // JWT 工具
    private final RestTemplate restTemplate;                             // HTTP 客户端
    private final WxPayConfig wxPayConfig;                                // 微信配置（集中管理 appId、appSecret 等）

    /**
     * 构造器注入
     *
     * 设计改进：appId 和 appSecret 改为从 WxPayConfig 统一获取，
     * 不再使用 @Value 注解分散注入，微信相关配置集中在一个类中管理。
     */
    public UserServiceImpl(UserMapper userMapper, JwtUtil jwtUtil,
                           RestTemplate restTemplate, WxPayConfig wxPayConfig) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
        this.wxPayConfig = wxPayConfig;
    }
    /**
     * 微信登录
     * 完整流程：code → 微信 API → openid → 查库 → 注册/登录 → 返回 Token
     *
     * @param code 微信登录凭证
     * @return JWT Token 字符串
     */
    @Override
    @Transactional                                                      // 事务：确保用户创建和 Token 生成的一致性
    public String login(String code) {
        // 1. 用 code 换 openid（调用微信官方接口）
        String openid = getOpenidFromWechat(code);                      // 调用微信 API

        // 2. 根据 openid 查数据库，看用户是否已注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();   // 条件构造器
        wrapper.eq(User::getOpenid, openid);                            // 条件：openid = ?

        User user = userMapper.selectOne(wrapper);                      // 查数据库

        // 3. 如果用户不存在 → 自动注册新用户
        if (user == null) {
            user = new User();                                          // 创建新用户
            user.setOpenid(openid);                                     // 设置 openid
            user.setRole("consumer");                                   // 默认角色：消费者
            user.setStatus(1);                                          // 默认状态：正常
//            user.setCreatedAt(LocalDateTime.now());                     // 创建时间  @TableField()已处理
//            user.setUpdatedAt(LocalDateTime.now());                     // 更新时间  @TableField()已处理
            userMapper.insert(user);                                    // 插入数据库
            log.info("新用户注册成功，openid={}", openid);
        }

        // 4. 生成 JWT Token（带上 userId 和 role）
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        log.info("用户登录成功，userId={}, role={}", user.getId(), user.getRole());
        return token;
    }

    @Override
    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);                      // 根据 ID 查询
        if (user == null) {
            throw new BusinessException("用户不存在");                   // 抛出业务异常
        }
        return user;
    }

    @Override
    @Transactional                                                      // 更新操作加事务
    public void updateUser(User user) {
        if (user.getId() == null) {
            throw new BusinessException(400, "用户 ID 不能为空");       // 参数校验
        }
//      user.setUpdatedAt(LocalDateTime.now());                         // 更新时间  实体类中@TableField()已处理。
        userMapper.updateById(user);                                    // 只更新非 null 字段
    }

    /**
     * 调用微信官方 API，用 code 换取 openid
     *
     * 微信 API 文档：https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html
     *
     * @param code 前端 wx.login() 返回的临时凭证
     * @return 用户的 openid
     */
    private String getOpenidFromWechat(String code) {
        // 拼接微信 API 请求 URL
//        String url = "https://api.weixin.qq.com/sns/jscode2session" +
//                "?appid=" + appId +                                     // 小程序 appId
//                "&secret=" + appSecret +                                 // 小程序 appSecret
//                "&js_code=" + code +                                    // 前端传来的临时 code
//                "&grant_type=authorization_code";                       // 固定值：授权码模式
        String url = "https://api.weixin.qq.com/sns/jscode2session" +
                "?appid=" + wxPayConfig.getAppId() +                     // 从 WxPayConfig 获取 appId
                "&secret=" + wxPayConfig.getAppSecret() +                 // 从 WxPayConfig 获取 appSecret
                "&js_code=" + code +
                "&grant_type=authorization_code";

        // 发送 GET 请求，获取微信返回的 JSON
        String response = restTemplate.getForObject(url, String.class);  // 调用微信 API

        if (StrUtil.isBlank(response)) {
            throw new BusinessException("微信服务器响应异常");           // 微信 API 调用失败
        }

        // 解析微信返回的 JSON
        JSONObject json = JSONUtil.parseObj(response);                  // 字符串 → JSON 对象

        // 微信 API 返回格式：{ "openid": "xxx", "session_key": "xxx", "errcode": 0 }
        // 如果 errcode 存在且不为 0，说明调用失败
        Integer errcode = json.getInt("errcode");
        if (errcode != null && errcode != 0) {
            log.error("微信登录失败，errcode={}, errmsg={}", errcode, json.getStr("errmsg"));
            throw new BusinessException("微信登录失败，请重试");         // 微信返回错误
        }

        return json.getStr("openid");                                   // 返回 openid
    }
}