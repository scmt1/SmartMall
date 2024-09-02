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
import com.yami.shop.bean.app.dto.OrderCountData;
import com.yami.shop.bean.app.dto.UserCenterInfoDto;
import com.yami.shop.bean.app.dto.UserDto;
import com.yami.shop.bean.app.dto.UserInfoDto;
import com.yami.shop.bean.app.param.UserInfoParam;
import com.yami.shop.bean.event.UpdateDistributionUserEvent;
import com.yami.shop.bean.model.ShopAuditing;
import com.yami.shop.bean.model.ShopDetail;
import com.yami.shop.bean.model.User;
import com.yami.shop.bean.model.UserExtension;
import com.yami.shop.bean.param.UserParam;
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.i18n.LanguageEnum;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.config.WxConfig;
import com.yami.shop.distribution.common.dto.AchievementDataDto;
import com.yami.shop.distribution.common.dto.DistributionUserWalletDto;
import com.yami.shop.distribution.common.model.DistributionUser;
import com.yami.shop.distribution.common.service.DistributionUserService;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.util.Objects;

/**
 * 用户信息
 *
 * @author LGH
 */
@RestController
@RequestMapping("/p/user")
@Api(tags = "用户接口")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    private final ApplicationContext applicationContext;

    private final MapperFacade mapperFacade;

    private final OrderService orderService;

    private final ShopDetailService shopDetailService;

    private final ShopAuditingService shopAuditingService;

    private final WxConfig wxConfig;

    private final UserExtensionService userExtensionService;

    private DistributionUserService distributionUserService;

    @GetMapping("/userInfo")
    @ApiOperation(value = "查看用户信息", notes = "根据用户ID（userId）获取用户信息")
    public ServerResponseEntity<UserDto> userInfo() {
        String userId = SecurityUtils.getUser().getUserId();
        User user = userService.getUserByUserId(userId);
        if (Objects.isNull(user)) {
            return ServerResponseEntity.success();
        }
        //获取用户等级积分详细表
        UserExtension extension = userExtensionService.getOne(
                new LambdaQueryWrapper<UserExtension>().eq(UserExtension::getUserId, SecurityUtils.getUser().getUserId()));
        UserDto userDto = mapperFacade.map(user, UserDto.class);
        userDto.setLevel(extension.getLevel());
        userDto.setUsername(user.getUserName());
        userDto.setGrowth(extension.getGrowth());
        userDto.setScore(extension.getScore());
        userDto.setLevelType(extension.getLevelType());
        userDto.setBalance(extension.getBalance());
        if (userDto.getUserMobile() != null) {
            userDto.setMobile(userDto.getUserMobile().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        }
        return ServerResponseEntity.success(userDto);
    }

    @PutMapping("/setUserInfo")
    @ApiOperation(value = "设置用户信息", notes = "设置用户信息")
    public ServerResponseEntity<Void> setUserInfo(@RequestBody UserInfoParam userInfoParam) {
        String userId = SecurityUtils.getUser().getUserId();
        User user = new User();
        user.setUserId(userId);
        user.setPic(StrUtil.isBlank(userInfoParam.getAvatarUrl()) ? user.getPic() : userInfoParam.getAvatarUrl());
        user.setSex(userInfoParam.getSex() == null ? user.getSex() : userInfoParam.getSex());
        user.setUserMobile(userInfoParam.getUserMobile() == null ? user.getUserMobile() : userInfoParam.getUserMobile());
        user.setBirthDate(userInfoParam.getBirthDate() == null ? user.getBirthDate() : userInfoParam.getBirthDate());
        user.setUserMail(StrUtil.isNotBlank(userInfoParam.getUserMail()) ? userInfoParam.getUserMail() : user.getUserMail());
        if (StrUtil.isNotBlank(userInfoParam.getNickName())) {
            user.setNickName(userInfoParam.getNickName());
        }
        // 如果有一项不为空，则发送事件，修改分销员信息（如果是分销员）
        if (StrUtil.isNotBlank(userInfoParam.getAvatarUrl()) || StrUtil.isNotBlank(userInfoParam.getNickName())) {
            applicationContext.publishEvent(new UpdateDistributionUserEvent(userInfoParam, user.getUserId()));
        }
        userService.updateById(user);
        return ServerResponseEntity.success();
    }

    @GetMapping("/centerInfo")
    @ApiOperation(value = "个人中心信息", notes = "获取用户个人中心信息")
    public ServerResponseEntity<UserCenterInfoDto> centerInfo() {
        String userId = SecurityUtils.getUser().getUserId();
        UserCenterInfoDto userCenterInfoDto = new UserCenterInfoDto();
        userCenterInfoDto.setOrderCountData(orderService.getOrderCount(userId));
        ShopAuditing shopAuditing = shopAuditingService.getShopAuditingByUserId(userId);
        userCenterInfoDto.setShopAuditStatus(shopAuditing == null ? null : shopAuditing.getStatus());
        ShopDetail shopDetail = shopDetailService.getShopDetailByUserId(userId);
        userCenterInfoDto.setIsSetPassword(shopDetail != null && StrUtil.isNotBlank(shopDetail.getPassword()));
        userCenterInfoDto.setShopId(shopDetail == null ? null : shopDetail.getShopId());
        userCenterInfoDto.setShopStatus(shopDetail == null ? null : shopDetail.getShopStatus());
        return ServerResponseEntity.success(userCenterInfoDto);
    }


    @GetMapping("/getUserScore")
    @ApiOperation(value = "获取用户积分", notes = "返回用户的积分信息")
    public ServerResponseEntity<UserParam> getUserScore() {
        String userId = SecurityUtils.getUser().getUserId();
        User user = userService.getById(userId);
        UserParam userParam = mapperFacade.map(user, UserParam.class);
        System.out.println();
        return ServerResponseEntity.success(userParam);
    }

    @GetMapping("/getUserInfo")
    @ApiOperation(value = "获取用户信息", notes = "返回用户的信息")
    public ServerResponseEntity<UserInfoDto> getUserInfo() {
        String userId = SecurityUtils.getUser().getUserId();
        UserInfoDto userInfo = userExtensionService.getUserInfo(userId);
        return ServerResponseEntity.success(userInfo);
    }

    @ApiOperation(value = "注销账户", notes = "注销账户")
    @GetMapping("/destroy")
    @ApiImplicitParam(name = "forceDestroy", value = "强制注销")
    public ServerResponseEntity<Void> destroy(@RequestParam(value = "forceDestroy", required = false, defaultValue = "false") Boolean forceDestroy) {
        String userId = SecurityUtils.getUser().getUserId();
        // 检查此账户是否有未完成的订单
        OrderCountData orderCount = orderService.getOrderCount(userId);

        if (orderCount.getConsignment() + orderCount.getPayed() + orderCount.getRefund() + orderCount.getGrouping() > 0) {
            // 存在未完成订单不能注销
            throw new YamiShopBindException("yami.order.unfinished.cannot.destroy.account");
        }

        if (!forceDestroy) {
            //用户余额
            Double balance = 0.00;
            //分销可提现余额
            Double settledAmount = 0.00;
            // 检查余额
            UserInfoDto userInfo = userExtensionService.getUserInfo(userId);
            if (userInfo.getTotalBalance() > 0) {
                balance = userInfo.getTotalBalance();
            }
            // 检查分销账户余额
            DistributionUser distributionUser = distributionUserService.getByUserIdAndShopId(userId, Constant.PLATFORM_SHOP_ID);
            if (Objects.nonNull(distributionUser)) {
                AchievementDataDto achievementDataDto = distributionUserService.getAchievementDataDtoById(distributionUser.getDistributionUserId());
                DistributionUserWalletDto distributionUserWallet = achievementDataDto.getDistributionUserWallet();
                settledAmount=distributionUserWallet.getSettledAmount();
            }
            String warnMsg = "";
            DecimalFormat df = new DecimalFormat("#0.00");
            String formatBanlance = df.format(balance);
            String formatSettledAmount = df.format(settledAmount);
            if(balance>0&&settledAmount>0){
                if (Objects.equals(I18nMessage.getDbLang(), LanguageEnum.LANGUAGE_ZH_CN.getLang())) {
                    warnMsg = "您的账户当前仍有余额" + formatBanlance + "元及分销余额"+formatSettledAmount +"元，注销账户后所有余额将清零，请考虑清楚后再注销";
                }else {
                    warnMsg = "You still have" + formatBanlance +" dollars and settledAmount "+formatSettledAmount+" dollars in your account, all balance will be cleared after you destroy the account. Please consider carefully before operation";
                }
                return ServerResponseEntity.showFailMsg(warnMsg);
            }else if(balance>0){
                if (Objects.equals(I18nMessage.getDbLang(), LanguageEnum.LANGUAGE_ZH_CN.getLang())) {
                    warnMsg = "您的账户当前仍有余额" +formatBanlance+ "元，注销账户后所有余额将清零，请考虑清楚后再注销";
                }else {
                    warnMsg = "You still have" + formatBanlance +" dollars in your account, all balance will be cleared after you destroy the account. Please consider carefully before operation";
                }
                return ServerResponseEntity.showFailMsg(warnMsg);
            }else if(settledAmount>0){
                if (Objects.equals(I18nMessage.getDbLang(), LanguageEnum.LANGUAGE_ZH_CN.getLang())) {
                    warnMsg = "您的账户当前仍有分销余额" + formatSettledAmount + "元，注销账户后所有余额将清零，请考虑清楚后再注销";
                }else {
                    warnMsg = "You still have settledAmount " + formatSettledAmount +" dollars in your account, all balance will be cleared after you destroy the account. Please consider carefully before operation";
                }
                return ServerResponseEntity.showFailMsg(warnMsg);
            }
        }
        userService.destroyUser(userId);
        return ServerResponseEntity.success();
    }
}
