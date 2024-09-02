/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yami.shop.bean.app.dto.UserAddrDto;
import com.yami.shop.bean.app.param.AddrParam;
import com.yami.shop.bean.model.UserAddr;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.UserAddrService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

/**
 * @author LGH
 */
@RestController
@RequestMapping("/p/address")
@Api(tags = "地址接口")
public class AddrController {

    @Autowired
    private MapperFacade mapperFacade;

    @Autowired
    private UserAddrService userAddrService;

    @GetMapping("/list")
    @ApiOperation(value = "用户地址列表", notes = "获取用户的所有地址信息")
    public ServerResponseEntity<List<UserAddrDto>> dvyList() {
        String userId = SecurityUtils.getUser().getUserId();
        List<UserAddr> userAddrs = userAddrService.list(new LambdaQueryWrapper<UserAddr>().eq(UserAddr::getUserId, userId).orderByDesc(UserAddr::getCommonAddr).orderByDesc(UserAddr::getUpdateTime));
        return ServerResponseEntity.success(mapperFacade.mapAsList(userAddrs, UserAddrDto.class));
    }

    @PostMapping("/addAddr")
    @ApiOperation(value = "新增用户地址", notes = "新增用户地址")
    public ServerResponseEntity<String> addAddr(@Valid @RequestBody AddrParam addrParam) {
        String userId = SecurityUtils.getUser().getUserId();
        if (addrParam.getAddrId() != null && addrParam.getAddrId() != 0) {
            // 该地址已存在
            return ServerResponseEntity.showFailMsg(I18nMessage.getMessage("yami.address.exists"));
        }
        int receiverLengthLimit = 50;
        if (addrParam.getReceiver().length() > receiverLengthLimit) {
            // 收货人名称长度超过50个字符
            return ServerResponseEntity.showFailMsg(I18nMessage.getMessage("yami.address.receiver.length.limit"));
        }
        int addrCount = userAddrService.count(new LambdaQueryWrapper<UserAddr>().eq(UserAddr::getUserId, userId));
        UserAddr userAddr = mapperFacade.map(addrParam, UserAddr.class);
        int maxUserAddr = 10;
        if (addrCount >= maxUserAddr) {
            // 收货地址已达到上限，无法再新增地址
            throw new YamiShopBindException("yami.address.add.limit");
        } else if (addrCount == 0) {
            userAddr.setCommonAddr(1);
        } else {
            userAddr.setCommonAddr(0);
        }
        userAddr.setUserId(userId);
        userAddr.setStatus(1);
        userAddr.setCreateTime(new Date());
        userAddr.setUpdateTime(new Date());
        userAddrService.save(userAddr);
        if (userAddr.getCommonAddr() == 1) {
            // 清除默认地址缓存
            userAddrService.removeUserAddrByUserId(0L, userId);
        }
        // 添加地址成功
        return ServerResponseEntity.success(I18nMessage.getMessage("yami.address.added.successfully"));
    }

    @PutMapping("/updateAddr")
    @ApiOperation(value = "修改用户地址", notes = "修改用户地址")
    public ServerResponseEntity<String> updateAddr(@Valid @RequestBody AddrParam addrParam) {
        String userId = SecurityUtils.getUser().getUserId();
        UserAddr dbUserAddr = userAddrService.getUserAddrByUserId(addrParam.getAddrId(), userId);
        if (dbUserAddr == null) {
            // 该地址已被删除
            return ServerResponseEntity.showFailMsg(I18nMessage.getMessage("yami.user.address.delete"));
        }
        int receiverLengthLimit = 50;
        if (addrParam.getReceiver().length() > receiverLengthLimit) {
            // 收货人名称长度超过50个字符
            return ServerResponseEntity.showFailMsg(I18nMessage.getMessage("yami.address.receiver.length.limit"));
        }
        UserAddr userAddr = mapperFacade.map(addrParam, UserAddr.class);
        userAddr.setUserId(userId);
        userAddr.setUpdateTime(new Date());
        userAddrService.updateById(userAddr);
        // 清除当前地址缓存
        userAddrService.removeUserAddrByUserId(addrParam.getAddrId(), userId);
        // 清除默认地址缓存
        userAddrService.removeUserAddrByUserId(0L, userId);
        // 修改地址成功
        return ServerResponseEntity.success(I18nMessage.getMessage("yami.address.modified.successfully"));
    }

    @DeleteMapping("/deleteAddr/{addrId}")
    @ApiOperation(value = "删除订单用户地址", notes = "根据地址id，删除用户地址")
    @ApiImplicitParam(name = "addrId", value = "地址ID", required = true, dataType = "Long")
    public ServerResponseEntity<String> deleteDvy(@PathVariable("addrId") Long addrId) {
        String userId = SecurityUtils.getUser().getUserId();
        UserAddr userAddr = userAddrService.getUserAddrByUserId(addrId, userId);
        if (userAddr == null) {
            // 该地址已被删除
            return ServerResponseEntity.showFailMsg(I18nMessage.getMessage("yami.user.address.delete"));
        }
        if (userAddr.getCommonAddr() == 1) {
            // 默认地址无法删除
            return ServerResponseEntity.showFailMsg(I18nMessage.getMessage("yami.address.cannot.delete"));
        }
        userAddrService.removeById(addrId);
        userAddrService.removeUserAddrByUserId(addrId, userId);
        // 删除地址成功
        return ServerResponseEntity.success(I18nMessage.getMessage("yami.deleted.address.successfully"));
    }

    @PutMapping("/defaultAddr/{addrId}")
    @ApiOperation(value = "设置默认地址", notes = "根据地址id，设置默认地址")
    @ApiImplicitParam(name = "addrId", value = "地址ID", required = true, dataType = "Long")
    public ServerResponseEntity<String> defaultAddr(@PathVariable("addrId") Long addrId) {
        String userId = SecurityUtils.getUser().getUserId();
        userAddrService.updateDefaultUserAddr(addrId, userId);
        userAddrService.removeUserAddrByUserId(0L, userId);
        // 清楚掉该用户下所有使用地址使用的缓存,否则无法删除上一次的地址
        List<UserAddr> list = userAddrService.list(new LambdaQueryWrapper<UserAddr>().eq(UserAddr::getUserId, userId));
        for (UserAddr userAddr : list) {
            userAddrService.removeUserAddrByUserId(userAddr.getAddrId(), userId);
        }
        // 修改地址成功
        return ServerResponseEntity.success(I18nMessage.getMessage("yami.address.modified.successfully"));
    }

    @GetMapping("/addrInfo/{addrId}")
    @ApiOperation(value = "获取地址信息", notes = "根据地址id，获取订单配送地址信息")
    @ApiImplicitParam(name = "addrId", value = "地址ID", required = true, dataType = "Long")
    public ServerResponseEntity<UserAddrDto> addrInfo(@PathVariable("addrId") Long addrId) {
        String userId = SecurityUtils.getUser().getUserId();
        UserAddr userAddr = userAddrService.getUserAddrByUserId(addrId, userId);
        if (userAddr == null) {
            // 该地址已被删除
            throw new YamiShopBindException("yami.user.address.delete");
        }
        return ServerResponseEntity.success(mapperFacade.map(userAddr, UserAddrDto.class));
    }
}
