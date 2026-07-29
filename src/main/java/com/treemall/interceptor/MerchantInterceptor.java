package com.treemall.interceptor;

import com.treemall.common.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 商户角色拦截器
 * 仅拦截 /api/v1/merchant/** 路径
 * 校验当前用户角色是否为 "merchant"，不是则返回 403
 */
@Component
public class MerchantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String role = UserContext.getRole();

        if (!"merchant".equals(role)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(200);
            response.getWriter().write(
                    "{\"code\":403,\"message\":\"无权限，仅商户可操作\",\"data\":null}");
            return false;
        }

        return true;  // 角色校验通过，放行
    }
}