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

import com.google.common.collect.Maps;
import com.yami.shop.bean.app.param.SendSmsParam;
import com.yami.shop.bean.enums.SendType;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.service.SmsLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author LGH
 */
@RestController
@RequestMapping("/sms")
@Api(tags = "发送验证码接口")
public class SmsController {

    @Autowired
    private SmsLogService smsLogService;

    @PostMapping("/sendLoginCode")
    @ApiOperation(value = "发送登录验证码", notes = "用户发送登录验证码")
    public ServerResponseEntity<Void> sendLoginCode(@RequestBody SendSmsParam sendSmsParam) {
        // 每个手机号每分钟只能发十个注册的验证码，免得接口被利用
        smsLogService.sendSms(SendType.LOGIN, sendSmsParam.getMobile(), sendSmsParam.getMobile(), Maps.newHashMap());
        return ServerResponseEntity.success();
    }

    @PostMapping("/sendUpdatePwdCode")
    @ApiOperation(value = "发送修改密码验证码接口", notes = "发送修改密码验证码接口")
    public ServerResponseEntity<Void> sendUpdatePwdCode(@RequestBody SendSmsParam sendSmsParam) {
        // 每个手机号每分钟只能发十个注册的验证码，免得接口被利用
        smsLogService.sendSms(SendType.UPDATE_PASSWORD, sendSmsParam.getMobile(), sendSmsParam.getMobile(), Maps.newHashMap());
        return ServerResponseEntity.success();
    }
}
