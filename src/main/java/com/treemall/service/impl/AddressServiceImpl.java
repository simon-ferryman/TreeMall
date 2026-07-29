package com.treemall.service.impl;                                      // 服务实现包

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // 条件构造器
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper; // 更新条件构造器
import com.treemall.common.BusinessException;                            // 业务异常
import com.treemall.common.UserContext;                                 // 当前用户上下文
import com.treemall.entity.Address;                                    // 地址实体
import com.treemall.mapper.AddressMapper;                              // 地址 Mapper
import com.treemall.service.AddressService;                            // 地址服务接口
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收货地址服务实现
 *
 * 核心业务逻辑：设默认地址
 *   1. 先清空当前用户所有地址的 is_default = 0（UPDATE t_address SET is_default=0 WHERE user_id=?）
 *   2. 再设置目标地址 is_default = 1（UPDATE t_address SET is_default=1 WHERE id=?）
 *   三步操作在一个事务中，要么全成功，要么全失败
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;                             // 地址 Mapper

    @Override
    public Address addAddress(Address address) {                           // 新增地址
        Long userId = UserContext.getUserId();                             // 从上下文拿当前用户 ID
        address.setUserId(userId);                                         // 强制设置 userId（防止前端传别人的 userId）

        // 查询当前用户是否已有地址，如果没有，自动设为默认
        LambdaQueryWrapper<Address> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Address::getUserId, userId);                      // 条件：当前用户
        Long count = addressMapper.selectCount(countWrapper);              // 统计数量
        if (count == 0) {                                                 // 如果是第一个地址
            address.setIsDefault(1);                                       // 自动设为默认地址
        } else {
            address.setIsDefault(0);                                       // 否则不是默认
        }

        addressMapper.insert(address);                                     // 插入数据库
        return address;                                                    // 返回带 ID 的地址对象
    }
    public Address getAddressById(Long addressId) {
        Long currentUserId = UserContext.getUserId();
        Address address = addressMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException(404, "地址不存在");
        }
        if (!address.getUserId().equals(currentUserId)) {
            throw new BusinessException(403, "无权查看该地址");
        }
        return address;
    }


//    @Override
//    public Address updateAddress(Address address) {                        // 更新地址
//        if (address.getId() == null) {                                    // 参数校验：必须传 ID
//            throw new BusinessException(400, "地址 ID 不能为空");
//        }
//
//        // 安全校验：验证地址是否属于当前用户
//        Address exist = addressMapper.selectById(address.getId());
//        if (exist == null || !exist.getUserId().equals(UserContext.getUserId())) {
//            throw new BusinessException(403, "无权修改他人的地址");
//        }
//
//        addressMapper.updateById(address);                                 // 只更新非 null 字段
//        return addressMapper.selectById(address.getId());                  // 返回最新数据
//    }
@Override
public Address updateAddress(Address address) {
    if (address.getId() == null) {
        throw new BusinessException(400, "地址 ID 不能为空");
    }

    Long currentUserId = UserContext.getUserId();
    log.info("更新地址请求: addressId={}, currentUserId={}", address.getId(), currentUserId);

    Address exist = addressMapper.selectById(address.getId());
    if (exist == null) {
        log.warn("地址不存在或已被删除: addressId={}", address.getId());
        throw new BusinessException(404, "地址不存在或已被删除");
    }

    log.info("地址所属用户: addressUserId={}, 当前用户: {}", exist.getUserId(), currentUserId);

    if (!exist.getUserId().equals(currentUserId)) {
        log.warn("用户权限不匹配: addressUserId={}, currentUserId={}", exist.getUserId(), currentUserId);
        throw new BusinessException(403, "无权修改他人的地址");
    }

    addressMapper.updateById(address);
    return addressMapper.selectById(address.getId());
}

    @Override
    public boolean deleteAddress(Long addressId) {
        Long currentUserId = UserContext.getUserId();
        log.info("删除地址请求: addressId={}, currentUserId={}", addressId, currentUserId);

        Address exist = addressMapper.selectById(addressId);
        if (exist == null) {
            log.warn("地址不存在或已被删除: addressId={}", addressId);
            throw new BusinessException(404, "地址不存在或已被删除");
        }

        log.info("地址所属用户: addressUserId={}, 当前用户: {}", exist.getUserId(), currentUserId);

        if (!exist.getUserId().equals(currentUserId)) {
            log.warn("用户权限不匹配: addressUserId={}, currentUserId={}", exist.getUserId(), currentUserId);
            throw new BusinessException(403, "无权删除他人的地址");
        }

        addressMapper.deleteById(addressId);
        log.info("地址删除成功: addressId={}", addressId);
        return true;
    }

    @Override
    public boolean setDefault(Long addressId) {                            // 设为默认地址
        Long userId = UserContext.getUserId();

        // 安全校验：地址必须存在且属于当前用户
        Address exist = addressMapper.selectById(addressId);
        if (exist == null || !exist.getUserId().equals(userId)) {
            throw new BusinessException(400, "地址不存在");
        }

        // 第1步：清除当前用户所有地址的默认标记
        LambdaUpdateWrapper<Address> clearWrapper = new LambdaUpdateWrapper<>();
        clearWrapper.eq(Address::getUserId, userId)                      // 条件：当前用户
                .set(Address::getIsDefault, 0);                        // 设置 is_default = 0
        addressMapper.update(null, clearWrapper);                          // 执行批量更新

        // 第2步：设置目标地址为默认
        Address update = new Address();                                   // 创建临时对象
        update.setId(addressId);                                          // 设置要更新的 ID
        update.setIsDefault(1);                                           // 设置 is_default = 1
        addressMapper.updateById(update);                                  // 更新到数据库

        return true;
    }

    @Override
    public List<Address> getCurrentUserAddressList() {                      // 查询当前用户所有地址
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId)                            // 只查当前用户
                .orderByDesc(Address::getIsDefault)                        // 默认地址排最前面
                .orderByDesc(Address::getCreatedAt);                       // 再按创建时间倒排

        return addressMapper.selectList(wrapper);                           // 查询列表
    }
}