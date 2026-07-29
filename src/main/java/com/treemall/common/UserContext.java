package com.treemall.common;

/**
 * 用户上下文（ThreadLocal）
 *
 * 作用：在请求处理过程中，任何地方都可以通过 UserContext 获取当前用户信息，
 * 不需要在方法参数中层层传递 userId。
 *
 * 数据流向：
 * JwtInterceptor（解析 Token）→ UserContext.set(userId, role)
 *      → Controller / Service 任意位置 → UserContext.getUserId()
 * 请求结束后 → UserContext.remove()（防止内存泄漏）
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ROLE = new ThreadLocal<>();

    /** 设置当前用户ID（由 JwtInterceptor 调用） */
    public static void set(Long userId, String role) {
        USER_ID.set(userId);
        USER_ROLE.set(role);
    }

    /** 获取当前用户ID */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /** 获取当前用户角色 */
    public static String getRole() {
        return USER_ROLE.get();
    }

    /**
     * 清除 ThreadLocal（必须调用！）
     * 原因：Tomcat 使用线程池，线程会被复用。
     * 如果不清理，下一个请求可能读到上一个请求的用户信息。
     */
    public static void remove() {
        USER_ID.remove();
        USER_ROLE.remove();
    }
}