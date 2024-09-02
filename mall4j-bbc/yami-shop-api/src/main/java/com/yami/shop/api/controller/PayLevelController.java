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

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeequan.jeepay.Jeepay;
import com.yami.shop.bean.app.param.PayUserParam;
import com.yami.shop.bean.enums.PayEntry;
import com.yami.shop.bean.model.ShopMch;
import com.yami.shop.bean.model.UserExtension;
import com.yami.shop.bean.pay.PayInfoDto;
import com.yami.shop.common.bean.SysPayConfig;
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.config.ShopConfig;
import com.yami.shop.manager.impl.PayManager;
import com.yami.shop.security.api.model.YamiUser;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.PayInfoService;
import com.yami.shop.service.ShopMchService;
import com.yami.shop.service.SysConfigService;
import com.yami.shop.service.UserExtensionService;
import com.yami.shop.user.common.model.UserLevel;
import com.yami.shop.user.common.model.UserLevelLog;
import com.yami.shop.user.common.service.UserLevelLogService;
import com.yami.shop.user.common.service.UserLevelService;
import com.yami.shop.utils.HttpClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author LHD on 2020/03/02.
 */
@Api(tags = "会员购买接口")
@RestController
@RequestMapping("/p/level")
@AllArgsConstructor
public class PayLevelController {

    private final UserLevelService userLevelService;
    private final UserExtensionService userExtensionService;
    private final UserLevelLogService userLevelLogService;
    private final PayManager payManager;
    private final PayInfoService payInfoService;
    private final Snowflake snowflake;
    private final SysConfigService sysConfigService;
    private final ShopMchService shopMchService;
    @Autowired
    private ShopConfig shopConfig;

    /**
     * 会员支付接口
     */
    @PostMapping("/payLevel")
    @ApiOperation(value = "根据会员等级进行支付", notes = "根据会员等级进行支付")
    @SneakyThrows
    public ServerResponseEntity<?> payVip(HttpServletResponse httpResponse, @RequestBody PayUserParam payCardParam) {
        YamiUser user = SecurityUtils.getUser();
        if (!user.getEnabled()) {
            // 您已被禁用，不能购买，请联系平台客服
            throw new YamiShopBindException("yami.order.pay.user.disable");
        }
        UserExtension userExtension = userExtensionService.getOne(new LambdaQueryWrapper<UserExtension>()
                                            .eq(UserExtension::getUserId, user.getUserId()));
        UserLevel userLevel = userLevelService.getById(payCardParam.getId());

        //如果会员等级为空
        if(userLevel == null || !Objects.equals(userLevel.getLevelType(),1)) {
            // 会员等级不存在或当前等级不为付费会员等级！
            throw new YamiShopBindException("yami.user.level.no.exist");
        }
        if(Objects.equals(userExtension.getLevelType(),1) && userExtension.getLevel() > userLevel.getLevel()){
            // 用户无法在会员期间购买低等级会员
            throw new YamiShopBindException("yami.user.pay.level.check");
        }
        if(userExtension.getGrowth() < userLevel.getNeedGrowth()){
            // 用户成长值不足，无法购买
            throw new YamiShopBindException("yami.user.growh.no.enough");
        }

        String payNo = String.valueOf(snowflake.nextId());

        Date now = new Date();
        //生成等级日志
        UserLevelLog userLevelLog = new UserLevelLog();
        userLevelLog.setGiftTime(now);
        userLevelLog.setCreateTime(now);
        userLevelLog.setLevel(userLevel.getLevel());
        userLevelLog.setState(0);
        userLevelLog.setPayNo(payNo);
        userLevelLog.setIsPayed(0);
        userLevelLog.setPayAmount(userLevel.getNeedAmount());
        userLevelLog.setLevelName(userLevel.getLevelName());
        userLevelLog.setTerm(userLevel.getTerm());
        userLevelLog.setTermType(userLevel.getTermType());
        userLevelLog.setPayType(payCardParam.getPayType());
        userLevelLog.setUserId(userExtension.getUserId());
        //插入一条等级日志
        userLevelLogService.save(userLevelLog);

        payCardParam.setPayNo(payNo);
        payCardParam.setOrderIds(userLevelLog.getLevelLogId());
        payCardParam.setNeedAmount(userLevel.getNeedAmount());

        PayInfoDto payInfo = payInfoService.buyVip(user.getUserId(), payCardParam);

        String shopId = sysConfigService.getConfigValue("BUY_MEMBER_SHOP");
        payInfo.setShopId(Long.valueOf(shopId));
        payInfo.setBizUserId(user.getBizUserId());
        payInfo.setPayType(payCardParam.getPayType());
        payInfo.setApiNoticeUrl("/notice/pay/" + PayEntry.VIP.value() + "/" + payCardParam.getPayType());
        payInfo.setReturnUrl(payCardParam.getReturnUrl());
        return payManager.doPay(httpResponse, payInfo);
    }

    /**
     * 会员H5支付接口
     */
    @PostMapping("/h5PayLevel")
    @ApiOperation(value = "根据会员等级进行支付", notes = "根据会员等级进行支付")
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    public ServerResponseEntity<?> h5PayVip(@RequestBody PayUserParam payCardParam) {
        YamiUser user = SecurityUtils.getUser();
        if (!user.getEnabled()) {
            // 您已被禁用，不能购买，请联系平台客服
            throw new YamiShopBindException("yami.order.pay.user.disable");
        }
        UserExtension userExtension = userExtensionService.getOne(new LambdaQueryWrapper<UserExtension>()
                .eq(UserExtension::getUserId, user.getUserId()));
        UserLevel userLevel = userLevelService.getById(payCardParam.getId());

        //如果会员等级为空
        if(userLevel == null || !Objects.equals(userLevel.getLevelType(),1)) {
            // 会员等级不存在或当前等级不为付费会员等级！
            throw new YamiShopBindException("yami.user.level.no.exist");
        }
        if(Objects.equals(userExtension.getLevelType(),1) && userExtension.getLevel() > userLevel.getLevel()){
            // 用户无法在会员期间购买低等级会员
            throw new YamiShopBindException("yami.user.pay.level.check");
        }
        if(userExtension.getGrowth() < userLevel.getNeedGrowth()){
            // 用户成长值不足，无法购买
            throw new YamiShopBindException("yami.user.growh.no.enough");
        }

        String payNo = String.valueOf(snowflake.nextId());

        Date now = new Date();
        //生成等级日志
        UserLevelLog userLevelLog = new UserLevelLog();
        userLevelLog.setGiftTime(now);
        userLevelLog.setCreateTime(now);
        userLevelLog.setLevel(userLevel.getLevel());
        userLevelLog.setState(0);
        userLevelLog.setPayNo(payNo);
        userLevelLog.setIsPayed(0);
        userLevelLog.setPayAmount(userLevel.getNeedAmount());
        userLevelLog.setLevelName(userLevel.getLevelName());
        userLevelLog.setTerm(userLevel.getTerm());
        userLevelLog.setTermType(userLevel.getTermType());
        userLevelLog.setPayType(payCardParam.getPayType());
        userLevelLog.setUserId(userExtension.getUserId());
        //插入一条等级日志
        userLevelLogService.save(userLevelLog);

        payCardParam.setPayNo(payNo);
        payCardParam.setOrderIds(userLevelLog.getLevelLogId());
        payCardParam.setNeedAmount(userLevel.getNeedAmount());

        PayInfoDto payInfo = payInfoService.buyVip(user.getUserId(), payCardParam);

        String shopId = sysConfigService.getConfigValue("BUY_MEMBER_SHOP");

        payInfo.setShopId(Long.valueOf(shopId));
        payInfo.setBizUserId(user.getBizUserId());
        payInfo.setPayType(payCardParam.getPayType());
        payInfo.setApiNoticeUrl("/notice/pay/" + PayEntry.VIP.value() + "/" + payCardParam.getPayType());
        payInfo.setReturnUrl(payCardParam.getReturnUrl());

        QueryWrapper<ShopMch> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shop_id", shopId);
        ShopMch one = shopMchService.getOne(queryWrapper);
        if (one == null) {
            throw new YamiShopBindException("该商户没有配置支付信息，无法下单支付！");
        }
        String domainName = shopConfig.getDomain().getApiDomainName();
        //调用统一支付收银台界面，拉起支付
        HashMap<String, Object> params = new HashMap<>();
        params.put("mchNo", one.getMchNo());
        params.put("appId", one.getAppId());
        params.put("apiKey", one.getApiKey());

        params.put("orderNo", payCardParam.getPayNo());
        params.put("clientIp", "192.168.4.95");
        params.put("body", payInfo.getBody());
        params.put("notifyUrl", domainName + payInfo.getApiNoticeUrl());
        params.put("amount", String.valueOf(payInfo.getPayAmount()));
        params.put("returnUrl", payCardParam.getReturnUrl());
        params.put("extParam", "");
        params.put("couponShow", "0");
        params.put("orderNumbers", payCardParam.getOrderIds());
        params.put("payType", payCardParam.getPayType());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Accept", "application/json;charset=utf-8");
        String postResult = HttpClient.sendPostRequest("http://192.168.5.49:9220/api/common/payHome", params, httpHeaders); //线上
        return ServerResponseEntity.success(postResult);
    }

    @GetMapping("/isPay")
    public ServerResponseEntity<Integer> isPay(){
        YamiUser user = SecurityUtils.getUser();
        UserLevelLog uLog = userLevelLogService.getMaxCrtTimeByUserId(user.getUserId());
        return ServerResponseEntity.success(uLog.getIsPayed());
    }
}
