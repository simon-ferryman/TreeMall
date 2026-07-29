package com.treemall.mapper;                                            // Mapper 接口包

import com.baomidou.mybatisplus.core.mapper.BaseMapper;                 // MyBatis-Plus 通用 Mapper
import com.treemall.entity.User;                                        // 对应的实体类
import org.apache.ibatis.annotations.Mapper;                            // 标记为 Mapper 接口

/**
 * 用户 Mapper 接口
 *
 * 继承 BaseMapper<User> 后，自动获得以下方法（无需写 SQL）：
 *   insert(user)          — 插入一条用户
 *   selectById(id)        — 根据 ID 查询
 *   selectList(wrapper)   — 条件查询
 *   selectPage(page, ...) — 分页查询
 *   updateById(user)      — 根据 ID 更新
 *   deleteById(id)        — 逻辑删除（自动转 UPDATE SET deleted=1）
 *
 * 如果有复杂查询需要自定义 SQL，可以在 resources/mapper/ 下创建同名 XML 文件
 */
@Mapper                                                                 // 告诉 Spring 这是一个 Mapper 接口
public interface UserMapper extends BaseMapper<User> {                  // 继承 BaseMapper，泛型指定为 User
    // 这里暂时不需要自定义方法，BaseMapper 提供的方法够用
    // 后续需要按 openid 查询时，可以用 MyBatis-Plus 的 LambdaQueryWrapper
}