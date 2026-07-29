package com.treemall.common;                                            // 声明包路径

/**
 * 业务异常类
 * 当业务逻辑出现错误时（库存不足、订单状态非法、权限不足等），
 * Service 层抛出此异常，由 GlobalExceptionHandler 统一捕获并返回 Result.error()
 *
 * 为什么不用普通的 RuntimeException？
 * —— 因为我们需要区分"业务异常"（应该返回给用户看的）和"系统异常"（应该隐藏的）
 *
 * 使用示例：SS
 *   throw new BusinessException(400, "库存不足");
 *   throw new BusinessException("订单不存在");  // 默认 code=500
 */
public class BusinessException extends RuntimeException {               // 继承运行时异常，不需要在方法签名上声明 throws

    private Integer code;                                               // 错误码，与 Result 的 code 对应

    /**
     * 带错误码的构造器
     * 用法：throw new BusinessException(400, "用户名不能为空");
     *
     * @param code    错误码，将直接传给前端的 Result.code
     * @param message 错误信息，将直接传给前端的 Result.message
     */
    public BusinessException(Integer code, String message) {            // 双参数构造器
        super(message);                                                 // 调用父类 RuntimeException 的构造器，设置异常消息
        this.code = code;                                               // 保存错误码
    }

    /**
     * 默认错误码 500 的构造器（快捷版）
     * 用法：throw new BusinessException("系统繁忙，请稍后再试");
     *
     * @param message 错误信息
     */
    public BusinessException(String message) {                          // 单参数构造器
        super(message);                                                 // 设置异常消息
        this.code = 500;                                                // 默认错误码 500
    }

    /**
     * 获取错误码
     * GlobalExceptionHandler 用这个方法取 code，组装 Result.error(code, message)
     */
    public Integer getCode() {                                          // getter 方法
        return code;
    }
}