package com.treemall.mapper;                                            // Mapper 接口包

import com.baomidou.mybatisplus.core.mapper.BaseMapper;                 // 通用 Mapper
import com.treemall.entity.Address;                                    // 对应的实体类
import org.apache.ibatis.annotations.Mapper;                            // Mapper 标记

/**
 * 收货地址 Mapper 接口
 * 继承 BaseMapper，自动获得 insert/update/delete/selectList 等方法
 */
@Mapper
public interface AddressMapper extends BaseMapper<Address> {           // 继承 BaseMapper，泛型指定 Address
    // BaseMapper 够用，暂不需要自定义 SQL
}