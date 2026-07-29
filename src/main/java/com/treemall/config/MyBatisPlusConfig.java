package com.treemall.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置类
 *
 * 两大核心功能：
 * 1. 注册分页插件 → 让 Page 对象的分页查询生效
 * 2. 注册自动填充处理器 → 让 @TableField(fill = ...) 注解生效
 *    - INSERT 时自动填 createdAt 和 updatedAt 为当前时间
 *    - UPDATE 时自动填 updatedAt 为当前时间
 *    - 逻辑删除时也会触发 UPDATE，自动填充 updatedAt
 */
@Slf4j                                                                  // 日志注解：自动生成 log 对象，方便调试
@Configuration                                                          // 标记为 Spring 配置类，容器启动时加载
public class MyBatisPlusConfig {

    /**
     * 分页插件
     * 使用场景：商品列表分页、订单列表分页等
     * 不加这个 Bean，Page 对象不会生效，分页查询会返回全部数据

     * MyBatis-Plus 配置类
     * 注册分页插件，让分页查询（Page 对象）生效
     */
    @Bean                                                               // 把这个方法的返回值注册为 Spring Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor(); // 创建拦截器对象
        // 添加 MySQL 分页拦截器：自动识别方言，生成正确的 LIMIT 语句
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 自动填充处理器
     *
     * 作用：当实体类字段标注了 @TableField(fill = ...) 时，
     *       这个处理器会自动在 INSERT 或 UPDATE 时填入指定的值
     *
     * 使用场景：
     *   - 所有表的 created_at 和 updated_at 字段自动填充
     *   - 避免每个 Service 方法都要手动 setCreatedAt(new Date())
     *
     * 设计原则：
     *   - strictInsertFill：严格模式，只有当字段值为 null 时才填充
     *   - strictUpdateFill：严格模式，只有当字段值为 null 时才填充
     *   - 严格模式的好处：如果代码中手动设置了值，不会覆盖
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {

            /**
             * INSERT 时自动填充
             * 触发时机：执行 mapper.insert(entity) 时
             * 填充策略：如果 createdAt 为 null → 填当前时间
             *          如果 updatedAt 为 null → 填当前时间
             */
            @Override
            public void insertFill(MetaObject metaObject) {
                log.debug("自动填充 INSERT 字段: createdAt, updatedAt");     // 调试日志
                // strictInsertFill(元对象, 字段名, 字段类型, 填充值)
                // 参数1：metaObject — MyBatis 的元对象，可以动态设置字段值
                // 参数2：字段名 — 对应实体类中的 Java 字段名（驼峰）
                // 参数3：字段类型 — 用于类型匹配
                // 参数4：填充值 — 如果字段为 null，就填这个值
                this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }

            /**
             * UPDATE 时自动填充
             * 触发时机：执行 mapper.updateById(entity) 或逻辑删除时
             * 填充策略：如果 updatedAt 为 null → 填当前时间
             *
             * 注意：逻辑删除（@TableLogic）本质上也是 UPDATE 操作，
             *       所以也会触发这个填充处理器
             */
            @Override
            public void updateFill(MetaObject metaObject) {
                log.debug("自动填充 UPDATE 字段: updatedAt");              // 调试日志
                // strictUpdateFill：仅当字段为 null 时才填充，避免覆盖手动设置的值
                this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}