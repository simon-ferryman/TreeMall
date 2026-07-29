package com.treemall.common;                                            // 声明包路径

import lombok.Data;                                                     // Lombok 自动生成 getter/setter
import lombok.NoArgsConstructor;                                        // 自动生成无参构造器
import lombok.AllArgsConstructor;                                       // 自动生成全参构造器

/**
 * 统一响应结果类
 * 所有 Controller 接口都返回这个格式，前端可以统一处理
 * 格式：{ "code": 200, "message": "成功", "data": {...} }
 */
@Data                                                                   // 自动生成 getXxx()、setXxx()、toString()、equals()、hashCode()
@NoArgsConstructor                                                      // 生成无参构造器：new Result()
@AllArgsConstructor                                                     // 生成全参构造器：new Result(200, "成功", data)
public class Result<T> {                                                // <T> 是泛型：data 可以是任意类型

    private Integer code;                                               // 状态码：200 成功，400 参数错误，500 服务端异常
    private String message;                                             // 提示信息：给前端展示用
    private T data;                                                     // 响应数据：泛型，可以是对象、列表、null

    // ===== 静态工厂方法：快速创建 Result 对象 =====

    /**
     * 成功响应（带数据）
     * 用法：Result.success(userObject) → { code:200, message:"成功", data:userObject }
     */
    public static <T> Result<T> success(T data) {                      // <T> 声明这个方法使用泛型
        return new Result<>(200, "成功", data);                         // 状态码 200，返回数据
    }

    /**
     * 成功响应（无数据）
     * 用法：Result.success() → { code:200, message:"成功", data:null }
     * 场景：删除操作、修改操作不需要返回数据时
     */
    public static Result<Void> success() {                              // Void 是空类型，表示 data 为 null
        return new Result<>(200, "成功", null);
    }

    /**
     * 失败响应
     * 用法：Result.error(400, "用户名不能为空") → { code:400, message:"用户名不能为空", data:null }
     */
    public static <T> Result<T> error(Integer code, String message) {   // 自定义错误码和提示信息
        return new Result<>(code, message, null);
    }

    /**
     * 失败响应（快捷版，默认 500 服务端异常）
     * 用法：Result.error("系统异常") → { code:500, message:"系统异常", data:null }
     */
    public static <T> Result<T> error(String message) {                 // 默认错误码 500
        return new Result<>(500, message, null);
    }
}