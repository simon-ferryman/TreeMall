package com.treemall.common;                                            // 声明包路径

import lombok.extern.slf4j.Slf4j;                                       // Lombok 日志注解，自动生成 log 对象
import org.springframework.web.bind.annotation.ExceptionHandler;         // 标记方法为异常处理器
import org.springframework.web.bind.annotation.RestControllerAdvice;     // 全局异常通知 + REST 响应

/**
 * 全局异常处理器
 * 使用 @RestControllerAdvice 拦截所有 Controller 抛出的异常，
 * 统一转换为 Result.error() 格式返回给前端
 *
 * 设计思路：
 * 1. 业务异常（BusinessException）：返回给用户看，显示具体错误信息
 * 2. 系统异常（Exception）：不暴露细节给用户，只记录日志，返回通用提示
 * 3. 所有异常都返回 Result 格式，前端只需处理一种响应结构
 */
@Slf4j                                                                  // 编译时自动生成 log 对象，等价于写 private static final Logger log = ...
@RestControllerAdvice                                                   // = @ControllerAdvice + @ResponseBody（返回 JSON）
public class GlobalExceptionHandler {

    /**
     * 处理业务异常（我们自己抛的）
     * 捕获 BusinessException，提取 code 和 message，包装成 Result 返回
     *
     * 示例：throw new BusinessException(400, "库存不足")
     *       → 前端收到 {"code":400, "message":"库存不足", "data":null}
     */
    @ExceptionHandler(BusinessException.class)                          // 声明这个方法只处理 BusinessException
    public Result<Void> handleBusinessException(BusinessException e) {   // 参数 e 就是被抛出的异常对象
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage()); // 记录警告日志，方便排查
        return Result.error(e.getCode(), e.getMessage());               // 用异常自带的 code 和 message 构造 Result
    }

    /**
     * 处理参数校验异常（@Valid 校验失败时自动抛出）
     * 示例：用户提交的表单中，手机号格式不对
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException e) {
        // 从异常中提取第一条校验失败信息
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        log.warn("参数校验失败: {}", message);                           // 记录警告日志
        return Result.error(400, message);                              // 返回 400 错误码
    }

    /**
     * 处理所有未被捕获的异常（兜底处理器）
     * 任何上面没匹配到的异常都会到这里，防止直接返回 500 错误堆栈给前端
     *
     * 为什么不能返回真实错误信息？
     * —— 防止暴露数据库结构、代码路径等敏感信息给攻击者
     */
    @ExceptionHandler(Exception.class)                                  // 匹配所有 Exception 及其子类
    public Result<Void> handleException(Exception e) {                   // 参数 e 是原始异常
        log.error("系统异常: ", e);                                     // 记录完整错误堆栈到日志文件，供开发者排查
        return Result.error("系统繁忙，请稍后再试");                     // 返回通用提示，不暴露真实错误
    }
}