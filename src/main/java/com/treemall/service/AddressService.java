package com.treemall.service;                                           // 服务接口包

import com.treemall.entity.Address;                                    // 地址实体

import java.util.List;

/**
 * 收货地址服务接口
 *
 * 功能：增删改查 + 设为默认地址
 * 设默认地址的业务逻辑：先清除当前用户所有地址的默认标记，再设置目标地址为默认
 */
public interface AddressService {

    /**
     * 新增收货地址
     * 如果这是用户的第一个地址，自动设为默认
     *
     * @param address 地址实体（前端只传 receiverName/phone/province/city/district/detailAddress）
     * @return 新增后的地址（含 ID）
     */
    Address addAddress(Address address);

    /**
     * 更新收货地址
     * 安全校验：只能更新自己的地址
     *
     * @param address 地址实体（只传要更新的字段，id 必传）
     * @return 更新后的地址
     */
    Address updateAddress(Address address);

    /**
     * 查看单个地址
     * */
    Address getAddressById(Long addressId);

    /**
     * 删除收货地址（逻辑删除）
     * 安全校验：只能删除自己的地址
     *
     * @param addressId 地址 ID
     * @return 成功返回 true
     */
    boolean deleteAddress(Long addressId);

    /**
     * 设为默认地址
     * 规则：一个用户只能有一个默认地址，设置新的默认地址前先清除旧的默认标记
     *
     * @param addressId 地址 ID
     * @return 成功返回 true
     */
    boolean setDefault(Long addressId);

    /**
     * 查询当前用户的所有收货地址
     * 默认地址排在最前面
     *
     * @return 地址列表
     */
    List<Address> getCurrentUserAddressList();
}