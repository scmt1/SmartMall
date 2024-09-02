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


import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Maps;
import com.yami.shop.bean.app.param.CheckRegisterSmsParam;
import com.yami.shop.bean.app.param.SendSmsParam;
import com.yami.shop.bean.app.param.UserPwdUpdateParam;
import com.yami.shop.bean.app.param.UserRegisterParam;
import com.yami.shop.bean.enums.SendType;
import com.yami.shop.bean.model.User;
import com.yami.shop.common.enums.StatusEnum;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ResponseEnum;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PrincipalUtil;
import com.yami.shop.common.util.RedisUtil;
import com.yami.shop.security.common.bo.UserInfoInTokenBO;
import com.yami.shop.security.common.enums.SysTypeEnum;
import com.yami.shop.security.common.manager.PasswordManager;
import com.yami.shop.security.common.manager.TokenStore;
import com.yami.shop.security.common.model.AppConnect;
import com.yami.shop.security.common.service.AppConnectService;
import com.yami.shop.security.common.vo.TokenInfoVO;
import com.yami.shop.service.SmsLogService;
import com.yami.shop.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用户信息
 *
 * @author LGH
 */
@RestController
@RequestMapping("/user")
@Api(tags = "用户注册相关接口")
@AllArgsConstructor
public class UserRegisterController {

    private final UserService userService;
    private final MapperFacade mapperFacade;

    private final SmsLogService smsLogService;

    private final AppConnectService appConnectService;

    private final TokenStore tokenStore;

    private final PasswordEncoder passwordEncoder;

    private final PasswordManager passwordManager;

    public static final String CHECK_REGISTER_SMS_FLAG = "checkRegisterSmsFlag";

    public static final String CHECK_UPDATE_PWD_SMS_FLAG = "updatePwdSmsFlag";


    /**
     * 注册的头像昵称有几个值得注意的地方：
     * 1. 如果是微信公众号 or 小程序注册，在注册之前，也就是在进入页面之前，需要调用SocialLoginController 这里面的尝试登录的接口，如果可以登录就直接登录了。
     * 2. 关于发送验证码
     *    1) 手机号注册,要发送验证码
     * 3. 当注册成功的时候，已经返回token，相对来说，已经登录了
     */
    @ApiOperation(value="注册")
    @PostMapping("/register")
    public ServerResponseEntity<TokenInfoVO> register(@Valid @RequestBody UserRegisterParam userRegisterParam) {

        // 目前注册都是发验证码去注册的，看看有没有校验验证码成功的标识
        userService.validate(userRegisterParam, CHECK_REGISTER_SMS_FLAG + userRegisterParam.getCheckRegisterSmsFlag());
        // 正在进行申请注册
        if (userService.count(new LambdaQueryWrapper<User>().eq(User::getUserMobile, userRegisterParam.getMobile())) > 0) {
            // 该手机号已注册，无法重新注册
            throw new YamiShopBindException("yami.user.phone.exist");
        }

        User user = appConnectService.registerAndBindUser(userRegisterParam.getMobile(), userRegisterParam.getPassword(), null);
        // 2. 登录

        UserInfoInTokenBO userInfoInTokenBO = new UserInfoInTokenBO();
        userInfoInTokenBO.setUserId(user.getUserId());
        userInfoInTokenBO.setSysType(SysTypeEnum.ORDINARY.value());
        userInfoInTokenBO.setIsAdmin(0);
        userInfoInTokenBO.setEnabled(true);

        return ServerResponseEntity.success(tokenStore.storeAndGetVo(userInfoInTokenBO));
    }



    @PutMapping("/sendRegisterSms")
    @ApiOperation(value = "发送注册验证码", notes = "发送注册验证码")
    public ServerResponseEntity<Void> register(@Valid @RequestBody SendSmsParam sendSmsParam) {
        if (userService.count(new LambdaQueryWrapper<User>().eq(User::getUserMobile, sendSmsParam.getMobile())) > 0) {
            // 该手机号已注册，无法重新注册
            throw new YamiShopBindException("yami.user.phone.exist");
        }
        // 每个手机号每分钟只能发十个注册的验证码，免得接口被利用
        smsLogService.sendSms(SendType.REGISTER, sendSmsParam.getMobile(), sendSmsParam.getMobile(), Maps.newHashMap());
        return ServerResponseEntity.success();
    }

    @PutMapping("/checkRegisterSms")
    @ApiOperation(value = "校验验证码", notes = "校验验证码返回校验成功的标识")
    public ServerResponseEntity<String> register(@Valid @RequestBody CheckRegisterSmsParam checkRegisterSmsParam) {
        // 每个ip每分钟只能发十个注册的验证码，免得接口被利用
        if (!smsLogService.checkValidCode(checkRegisterSmsParam.getMobile(), checkRegisterSmsParam.getValidCode(), SendType.REGISTER)){
            // 验证码有误或已过期
            throw new YamiShopBindException("yami.user.code.error");
        }
        String checkRegisterSmsFlag = IdUtil.simpleUUID();
        RedisUtil.set(CHECK_REGISTER_SMS_FLAG + checkRegisterSmsFlag, checkRegisterSmsParam.getMobile(), 600);
        return ServerResponseEntity.success(checkRegisterSmsFlag);
    }

    @PutMapping("/sendBindSms")
    @ApiOperation(value = "发送绑定验证码", notes = "发送绑定验证码")
    public ServerResponseEntity<Void> bindSms(@Valid @RequestBody SendSmsParam sendSmsParam) {
        // 每个手机号每分钟只能发十个注册的验证码，免得接口被利用
        smsLogService.sendSms(SendType.VALID, sendSmsParam.getMobile(), sendSmsParam.getMobile(), Maps.newHashMap());
        return ServerResponseEntity.success();
    }


    @PutMapping("/checkUpdatePwdSms")
    @ApiOperation(value = "修改密码校验验证码", notes = "校验验证码返回校验成功的标识")
    public ServerResponseEntity<String> checkUpdatePwdSms(@RequestBody CheckRegisterSmsParam checkRegisterSmsParam) {
        boolean isCheckPass = false;
        if (Objects.nonNull(checkRegisterSmsParam) && Objects.nonNull(checkRegisterSmsParam.getMobile())) {
            Matcher m = Pattern.compile(PrincipalUtil.MOBILE_REGEXP).matcher(checkRegisterSmsParam.getMobile());
            isCheckPass = m.matches();
        }
        if (!isCheckPass) {
            throw new YamiShopBindException("yami.user.err.phone");
        }

        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUserMobile, checkRegisterSmsParam.getMobile()));
        if (user == null) {
            // 此用户不存在，请先注册
            throw new YamiShopBindException("yami.user.account.no.exist");
        }
        if (!smsLogService.checkValidCode(user.getUserMobile(), checkRegisterSmsParam.getValidCode(), SendType.UPDATE_PASSWORD)) {
            // 验证码有误或已过期
            throw new YamiShopBindException("yami.user.code.error");
        }
        String checkRegisterSmsFlag = IdUtil.simpleUUID();
        RedisUtil.set(CHECK_UPDATE_PWD_SMS_FLAG + checkRegisterSmsFlag, checkRegisterSmsParam.getMobile(), 600);
        return ServerResponseEntity.success(checkRegisterSmsFlag);
    }

    @PutMapping("/updatePwd")
    @ApiOperation(value = "修改密码", notes = "修改密码")
    public ServerResponseEntity<Void> updatePwd(@Valid @RequestBody UserPwdUpdateParam userPwdUpdateParam) {
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUserMobile, userPwdUpdateParam.getMobile()));
        if (user == null) {
            // 无法获取用户信息
            throw new YamiShopBindException("yami.user.no.exist");
        }
        UserRegisterParam registerParam = mapperFacade.map(userPwdUpdateParam, UserRegisterParam.class);
        // 看看有没有校验验证码成功的标识
        userService.validate(registerParam, CHECK_UPDATE_PWD_SMS_FLAG + userPwdUpdateParam.getCheckRegisterSmsFlag());
        String decryptPassword = passwordManager.decryptPassword(userPwdUpdateParam.getPassword());
        if (StrUtil.isBlank(decryptPassword)) {
            // 新密码不能为空
            throw new YamiShopBindException("yami.user.password.no.exist");
        }
        if (StrUtil.equals(passwordEncoder.encode(decryptPassword), user.getLoginPassword())) {
            // 新密码不能与原密码相同!
            throw new YamiShopBindException("yami.user.password.check");
        }
        user.setModifyTime(new Date());
        user.setLoginPassword(passwordEncoder.encode(decryptPassword));
        userService.updateById(user);
        tokenStore.deleteAllToken(SysTypeEnum.ORDINARY.value().toString(),user.getUserId());
        return ServerResponseEntity.success();
    }
}
