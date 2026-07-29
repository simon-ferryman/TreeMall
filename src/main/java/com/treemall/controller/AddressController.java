package com.treemall.controller;                                         // 控制器包

import com.treemall.common.Result;                                      // 统一响应格式
import com.treemall.entity.Address;                                    // 地址实体
import com.treemall.service.AddressService;                            // 地址服务
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址控制器
 *
 * 所有接口都需要 Token：地址是用户私人数据，必须登录
 * 接口：增删改查 + 设为默认（5 个接口）
 */
@RestController
@RequestMapping("/api/v1/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * 新增收货地址
     *
     * 请求方式：POST /api/v1/address
     * 请求体：  { "receiverName": "张三", "receiverPhone": "13800138000",
     *            "province": "广东省", "city": "广州市", "district": "天河区",
     *            "detailAddress": "体育西路100号" }
     * 响应体：  { "code": 200, "message": "成功", "data": { "id": 1, ... } }
     */
    @PostMapping
    public Result<Address> add(@RequestBody Address address) {
        Address result = addressService.addAddress(address);
        return Result.success(result);
    }

    @GetMapping("/{addressId}")
    public Result<Address> getById(@PathVariable Long addressId) {
        Address address = addressService.getAddressById(addressId);
        return Result.success(address);
    }


    /**
     * 更新收货地址
     *
     * 请求方式：PUT /api/v1/address
     * 请求体：  { "id": 1, "receiverName": "李四", "receiverPhone": "13900139000" }
     * 只传要更新的字段，不传的字段保持不变
     */
    @PutMapping
    public Result<Address> update(@RequestBody Address address) {
        Address result = addressService.updateAddress(address);
        return Result.success(result);
    }

    /**
     * 删除收货地址
     *
     * 请求方式：DELETE /api/v1/address/{addressId}
     */
    @DeleteMapping("/{addressId}")
    public Result<Void> delete(@PathVariable Long addressId) {
        addressService.deleteAddress(addressId);
        return Result.success();
    }

    /**
     * 设为默认地址
     *
     * 请求方式：PUT /api/v1/address/default/{addressId}
     * 业务逻辑：先清除当前用户所有地址的默认标记，再设置目标地址为默认
     */
    @PutMapping("/default/{addressId}")
    public Result<Void> setDefault(@PathVariable Long addressId) {
        addressService.setDefault(addressId);
        return Result.success();
    }

    /**
     * 获取当前用户所有收货地址
     *
     * 请求方式：GET /api/v1/address/list
     * 默认地址排在最前面
     */
    @GetMapping("/list")
    public Result<List<Address>> list() {
        List<Address> addresses = addressService.getCurrentUserAddressList();
        return Result.success(addresses);
    }

}