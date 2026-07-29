package com.treemall.interceptor;

import com.treemall.common.UserContext;
import com.treemall.common.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 认证拦截器
 *
 * 执行流程：
 * 1. 从请求头中取出 Authorization: Bearer <token>
 * 2. 解析 Token，验证签名和有效期
 * 3. 将 userId 和 role 存入 UserContext
 * 4. 放行请求到 Controller
 *
 * 如果 Token 无效或过期，直接返回 401，不进入 Controller
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 提取 Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, 401, "未登录，请先登录");
            return false;
        }
        String token = authHeader.substring(7);  // 去掉 "Bearer " 前缀

        // 2. 解析 Token
        try {
            Claims claims = jwtUtil.parseToken(token);
            Long userId = Long.valueOf(claims.getSubject());
            String role = claims.get("role", String.class);

            // 3. 存入 UserContext
            UserContext.set(userId, role);

            return true;  // 放行
        } catch (JwtException e) {
            log.warn("Token 无效或过期: {}", e.getMessage());
            sendError(response, 401, "登录已过期，请重新登录");
            return false;
        }
    }

    /**
     * 请求结束后清理 ThreadLocal，防止内存泄漏
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.remove();
    }

    /**
     * 向前端返回 JSON 格式的错误信息
     */
    private void sendError(HttpServletResponse response, int code, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);  // 业务状态码用 200，code 字段区分错误类型
        response.getWriter().write(String.format(
                "{\"code\":%d,\"message\":\"%s\",\"data\":null}", code, message));
    }
}