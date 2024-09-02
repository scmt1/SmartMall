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

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Maps;
import com.yami.shop.bean.app.param.SendSmsParam;
import com.yami.shop.bean.dto.ShopDetailDto;
import com.yami.shop.bean.enums.SendType;
import com.yami.shop.bean.model.ShopAuditing;
import com.yami.shop.bean.model.ShopDetail;
import com.yami.shop.bean.param.ShopDetailParam;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.security.common.model.UpdatePasswordDto;
import com.yami.shop.security.common.model.UsernameAndPasswordDto;
import com.yami.shop.service.ShopAuditingService;
import com.yami.shop.service.ShopDetailService;
import com.yami.shop.service.SmsLogService;
import com.yami.shop.sys.common.model.ShopEmployee;
import com.yami.shop.sys.common.service.ShopEmployeeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;


/**
 * @author lgh on 2018/08/29.
 */
@RestController
@RequestMapping("/p/shop")
@Api(tags = "店铺相关接口")
@AllArgsConstructor
public class MyShopDetailController {

    private final ShopDetailService shopDetailService;

    private final PasswordEncoder passwordEncoder;

    private final ShopAuditingService shopAuditingService;

    private final MapperFacade mapperFacade;

    private final SmsLogService smsLogService;

    private final ShopEmployeeService shopEmployeeService;

    @PostMapping("/apply")
    @ApiOperation(value = "申请店铺")
    public ServerResponseEntity<Void> apply(@Valid @RequestBody ShopDetailParam shopDetailParam) {
        String userId = SecurityUtils.getUser().getUserId();
        shopDetailService.applyShop(userId, shopDetailParam);
        return ServerResponseEntity.success();
    }

    @PostMapping("/saveUsernameAndPassword")
    @ApiOperation(value = "保存店铺账号密码")
    public ServerResponseEntity<Void> saveUsernameAndPassword(@Valid @RequestBody UsernameAndPasswordDto usernameAndPasswordDto) {
        String userId = SecurityUtils.getUser().getUserId();
        ShopDetail shopDetail = shopDetailService.getShopDetailByUserId(userId);
        if (shopDetail == null) {
            // 请先进行开店申请
            throw new YamiShopBindException("yami.open.store");
        }
        if (StrUtil.isNotBlank(shopDetail.getMobile())) {
            // 店铺账号已设置不能修改
            throw new YamiShopBindException("yami.stop.cannot.modified");
        }
        ShopDetail dbUsernameUser = shopDetailService.getShopByMobile(usernameAndPasswordDto.getUsername());
        ShopEmployee employee = shopEmployeeService.getByUserName(usernameAndPasswordDto.getUsername());
        if (dbUsernameUser != null || Objects.nonNull(employee)) {
            // 该账号已经开通过店铺，无法重复开通
            throw new YamiShopBindException("yami.stop.cannot.open");
        }
        if (!smsLogService.checkValidCode(usernameAndPasswordDto.getUsername(), usernameAndPasswordDto.getCode(), SendType.VALID)) {
            // 验证码有误或已过期
            throw new YamiShopBindException("yami.user.code.error");
        }
        String password = passwordEncoder.encode(usernameAndPasswordDto.getPassword());
        shopDetail.setPassword(password);
        shopDetail.setMobile(usernameAndPasswordDto.getUsername());
        // 更新店铺账号密码
        shopEmployeeService.updateUserNameAndPassword(shopDetail);
        shopDetailService.removeShopDetailCacheByShopId(shopDetail.getShopId());
        return ServerResponseEntity.success();
    }

    @PostMapping("/updatePassword")
    @ApiOperation(value = "更新店铺密码")
    public ServerResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordDto updatePasswordDto) {
        String userId = SecurityUtils.getUser().getUserId();
        ShopDetail shopDetail = shopDetailService.getShopDetailByUserId(userId);
        if (shopDetail == null) {
            // 请先进行开店申请
            throw new YamiShopBindException("yami.open.store");
        }
        if (!passwordEncoder.matches(updatePasswordDto.getPassword(), shopDetail.getPassword())) {
            // 原密码不正确
            throw new YamiShopBindException("yami.password.error");
        }
        //新密码
        String newPassword = passwordEncoder.encode(updatePasswordDto.getNewPassword());
        //更新密码
        shopEmployeeService.updatePasswordByUserName(shopDetail.getMobile(), newPassword);
        return ServerResponseEntity.success();
    }


    @GetMapping()
    @ApiOperation(value = "获取拥有的店铺信息")
    public ServerResponseEntity<ShopDetailDto> auditingDetail() {
        ShopDetail shopDetail = shopDetailService.getShopDetailByUserId(SecurityUtils.getUser().getUserId());
        ShopDetailDto shopDetailDto = mapperFacade.map(shopDetail, ShopDetailDto.class);
        return ServerResponseEntity.success(shopDetailDto);
    }

    @GetMapping("/shopAudit")
    @ApiOperation(value = "获取店铺的审核信息")
    public ServerResponseEntity<ShopAuditing> getShopAuditing() {
        ShopAuditing shopAuditing = shopAuditingService.getOne(new LambdaQueryWrapper<ShopAuditing>()
                .eq(ShopAuditing::getUserId, SecurityUtils.getUser().getUserId()));
        return ServerResponseEntity.success(shopAuditing);
    }

    @PostMapping("/sendCode")
    @ApiOperation(value = "发送验证码")
    public ServerResponseEntity<Void> sendLoginCode(@Valid @RequestBody SendSmsParam sendSmsParam) {
        String userId = SecurityUtils.getUser().getUserId();
        if (Objects.isNull(sendSmsParam.getMobile())) {
            // 手机号不能为空
            throw new YamiShopBindException("yami.shop.phone.no.exist");
        }
        if (Objects.nonNull(sendSmsParam.getShopAccount()) && Objects.equals(sendSmsParam.getShopAccount(), 1)) {
            ShopDetail dbUsernameUser = shopDetailService.getShopByMobile(sendSmsParam.getMobile());
            if (dbUsernameUser != null) {
                // 该账号已经开通过店铺，无法重复开通
                throw new YamiShopBindException("yami.stop.cannot.open");
            }
        }
        smsLogService.sendSms(SendType.VALID, userId, sendSmsParam.getMobile(), Maps.newHashMap());
        return ServerResponseEntity.success();
    }
}
