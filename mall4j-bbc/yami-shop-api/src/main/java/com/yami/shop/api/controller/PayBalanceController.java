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

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.jeequan.jeepay.Jeepay;
import com.yami.shop.bean.app.param.PayParam;
import com.yami.shop.bean.app.param.PayUserParam;
import com.yami.shop.bean.app.param.RechargeCardParam;
import com.yami.shop.bean.enums.PayEntry;
import com.yami.shop.bean.model.*;
import com.yami.shop.bean.param.UserUpdateParam;
import com.yami.shop.bean.pay.PayInfoDto;
import com.yami.shop.bean.vo.RechargeGiftVO;
import com.yami.shop.bean.vo.RechargeTimeRangeVO;
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.enums.PayType;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.Arith;
import com.yami.shop.card.common.utils.SnowflakeIdWorker;
import com.yami.shop.config.ShopConfig;
import com.yami.shop.coupon.common.model.Coupon;
import com.yami.shop.manager.impl.PayManager;
import com.yami.shop.security.api.model.YamiUser;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.*;
import com.yami.shop.user.common.dto.UserBalanceDto;
import com.yami.shop.user.common.model.UserBalance;
import com.yami.shop.user.common.model.UserBalanceLog;
import com.yami.shop.user.common.model.UserLevel;
import com.yami.shop.user.common.model.UserLevelLog;
import com.yami.shop.user.common.service.UserBalanceLogService;
import com.yami.shop.user.common.service.UserBalanceService;
import com.yami.shop.user.common.service.UserLevelLogService;
import com.yami.shop.user.common.service.UserLevelService;
import com.yami.shop.utils.HttpClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;

/**
 * @author LHD on 2020/03/02.
 */
@Api(tags = "余额支付接口")
@RestController
@RequestMapping("/p/balance")
@AllArgsConstructor
public class PayBalanceController {

    /**
     * 会员信息
     */
    private final UserBalanceService userBalanceService;
    private final UserBalanceLogService userBalanceLogService;
    private final UserExtensionService userExtensionService;
    private final Snowflake snowflake;
    private final PayManager payManager;
    private final PayInfoService payInfoService;
    private final RechargeCardService rechargeCardService;
    private final SysConfigService sysConfigService;
    private final ShopConfig shopConfig;
    private final ShopMchService shopMchService;
    private final UserLevelService userLevelService;
    private final UserLevelLogService userLevelLogService;

    /**
     * 会员余额充值接口
     */
    @PostMapping("/pay")
    @ApiOperation(value = "会员余额充值", notes = "会员余额充值")
    @SneakyThrows
    public ServerResponseEntity<?> payVip(HttpServletResponse httpResponse, @RequestBody PayUserParam payCardParam) {
        YamiUser user = SecurityUtils.getUser();
        String balanceShopId = sysConfigService.getConfigValue("BALANCE_SHOP");
        if (!user.getEnabled()) {
            // 您已被禁用，不能购买，请联系平台客服
            throw new YamiShopBindException("yami.order.pay.user.disable");
        }
        if (payCardParam.getPayType().equals(PayType.SCOREPAY.value()) || payCardParam.getPayType().equals(PayType.BALANCE.value())){
            // 请使用微信或支付宝进行支付
            throw new YamiShopBindException("yami.user.payType.error");
        }
        UserExtension userExtension = userExtensionService.getOne(new LambdaQueryWrapper<UserExtension>().eq(UserExtension::getUserId, user.getUserId()));
        UserBalance userBalance = null;
        // 自定义金额
        double balance;
        if (Objects.equals(payCardParam.getId(), -1L)) {
            balance = Arith.add(userExtension.getTotalBalance(), payCardParam.getCustomRechargeAmount());
        }
        // 购买
        else {
            userBalance = userBalanceService.getById(payCardParam.getId());
            if (Objects.isNull(userBalance)) {
                // 当前余额充值选项已被删除
                throw new YamiShopBindException("yami.user.blance.not.exist");
            }
            balance = Arith.add(userExtension.getTotalBalance(), userBalance.getRechargeAmount());
        }
        if (balance > Constant.MAX_USER_BALANCE) {
            // 您的余额加充值的余额将大于最大余额，不能进行充值操作
            throw new YamiShopBindException("yami.balance.than.max.balance");
        }
        // 使用雪花算法生成支付单号
        String payNo = String.valueOf(snowflake.nextId());
        Date now = new Date();

        //生成余额充值日志
        UserBalanceLog userBalanceLog = new UserBalanceLog();
        userBalanceLog.setCreateTime(now);
        userBalanceLog.setIoType(1);
        userBalanceLog.setType(1);
        userBalanceLog.setIsPayed(0);
        userBalanceLog.setPayNo(payNo);
        userBalanceLog.setUserId(user.getUserId());
        userBalanceLog.setShopId(Long.valueOf(balanceShopId));
        if (Objects.equals(payCardParam.getId(), -1L)) {
            userBalanceLog.setBalanceId(-1L);
            userBalanceLog.setChangeBalance(payCardParam.getCustomRechargeAmount());
        } else {
            userBalanceLog.setBalanceId(userBalance.getBalanceId());
            userBalanceLog.setChangeBalance(userBalance.getRechargeAmount());
        }
        //用户实际支付金额
        userBalanceLog.setPayAmount(userBalanceLog.getChangeBalance());
        //实际支付金额
        payCardParam.setNeedAmount(userBalanceLog.getChangeBalance());

        //查询会员卡余额充值前多少名赠送配置
        String rechargeGift = sysConfigService.getConfigValue("MEMBER_RECHARAGE_GIFT_RATIO");
        if(StringUtils.isNotBlank(rechargeGift)){
            Gson gson = new Gson();
            RechargeGiftVO rechargeGiftVO = gson.fromJson(rechargeGift,RechargeGiftVO.class);
            //查询会员卡余额成功充值数量
            Integer paySuccessNum = userBalanceLogService.getPaySuccessNum();
            if(rechargeGiftVO.getOrderNum() > paySuccessNum){//配置数量是否大于实际充值成功数量
                //更新赠送金额
                double presAmount = Arith.mul(userBalance.getRechargeAmount(),rechargeGiftVO.getGiftRatio());
                userBalance.setPresAmount(presAmount);
            }
        }

        //获取时间范围内充值配置信息
        String rechargeTimeRange = sysConfigService.getConfigValue("RECHARGE_TIME_RANGE");
        if(StringUtils.isNotBlank(rechargeTimeRange)){
            double presAmount10 = 0.0;//赠送10%金额
            double presAmount5 = 0.0;//赠送5%金额
            Gson gson = new Gson();
            RechargeTimeRangeVO rechargeTimeRangeVO = gson.fromJson(rechargeTimeRange,RechargeTimeRangeVO.class);
            String[] rechargeTimeRangeSplit = rechargeTimeRangeVO.getTimeRange().split("~");
            boolean before = new Date().before(DateUtil.parse(rechargeTimeRangeSplit[1], "yyyy-MM-dd"));
            boolean after = new Date().after(DateUtil.parse(rechargeTimeRangeSplit[0],"yyyy-MM-dd"));
            if(before && after){//判断当前时间是否在区间内
                //查询充值用户当月充值总金额和赠送总金额
                UserBalanceLog userBalanceLogInfo = userBalanceLogService.getTotalPayAmount(user.getUserId());
                //赠送金额
                double presAmount = 0.0;
                //判断当前赠送金额是否大于等于300元
                if(userBalanceLogInfo.getTotalGiveAmount() >= rechargeTimeRangeVO.getMaxGiveAmount()){
                    //更新赠送金额
                    presAmount = Arith.mul(userBalance.getRechargeAmount(),rechargeTimeRangeVO.getExceedGiftRatio());
                    userBalance.setPresAmount(presAmount);
                }else{
                    //300元赠送金额中还能赠送多少
                    double maxPresAmount = Arith.sub(rechargeTimeRangeVO.getMaxGiveAmount(),userBalanceLogInfo.getTotalGiveAmount());
                    //这笔充值金额判断是否能算10%
                    presAmount10 = Arith.mul(userBalance.getRechargeAmount(),rechargeTimeRangeVO.getGiftRatio());
                    if(presAmount10 > maxPresAmount){
                        //10%赠送金额最多还能充值金额
                        double maxAmount10 = Arith.div(maxPresAmount,rechargeTimeRangeVO.getGiftRatio());
                        //剩余金额赠送5%
                        double maxAmount5 = Arith.sub(userBalance.getRechargeAmount(),maxAmount10);
                        presAmount5 = Arith.mul(maxAmount5,rechargeTimeRangeVO.getExceedGiftRatio());
                        //赠送金额
                        presAmount = Arith.add(maxPresAmount,presAmount5);
                        userBalance.setPresAmount(presAmount);
                    }else{
                        //更新赠送金额
                        presAmount = presAmount10;
                        userBalance.setPresAmount(presAmount);
                    }
                }
            }
        }

        //充值金额加赠送金额
        if(userBalance.getPresAmount() > 0){
            Double changeBalance = Arith.add(userBalance.getRechargeAmount(),userBalance.getPresAmount());
            userBalanceLog.setChangeBalance(changeBalance);
        }
        //赠送金额
        userBalanceLog.setGiveAmount(userBalance.getPresAmount());
        userBalanceLogService.save(userBalanceLog);

        //充值金额大于一定金额升级高级会员
        String balanceRechargeUpMemberAmount = sysConfigService.getConfigValue("BALANCE_RECHARGE_UP_MEMBER");
        if(StringUtils.isNotBlank(balanceRechargeUpMemberAmount)){
            Double aDouble = new Double(balanceRechargeUpMemberAmount);
            if(userBalance.getRechargeAmount() >= aDouble){
                String memberLevelId = sysConfigService.getConfigValue("MEMBER_LEVEL_ID");
                if(StringUtils.isNotBlank(memberLevelId)){
                    UserLevel userLevel = userLevelService.getById(memberLevelId);
                    if(userLevel != null){
                        //生成等级日志
                        UserLevelLog userLevelLog = new UserLevelLog();
                        userLevelLog.setGiftTime(now);
                        userLevelLog.setCreateTime(now);
                        userLevelLog.setLevel(userLevel.getLevel());
                        userLevelLog.setState(0);
                        userLevelLog.setPayNo(payNo);
                        userLevelLog.setIsPayed(0);
                        userLevelLog.setPayAmount(userBalance.getRechargeAmount());
//                        userLevelLog.setPayAmount(userLevel.getNeedAmount());
                        userLevelLog.setLevelName(userLevel.getLevelName());
                        userLevelLog.setTerm(99);
                        userLevelLog.setTermType(5);
//                        userLevelLog.setTerm(userLevel.getTerm());
//                        userLevelLog.setTermType(userLevel.getTermType());
                        userLevelLog.setPayType(payCardParam.getPayType());
                        userLevelLog.setUserId(userExtension.getUserId());
                        userLevelLog.setType(1);
                        //插入一条等级日志
                        userLevelLogService.save(userLevelLog);
                    }
                }
            }
        }

        payCardParam.setPayNo(payNo);
        payCardParam.setOrderIds(userBalanceLog.getBalanceLogId());

        PayInfoDto payInfo = payInfoService.recharge(user.getUserId(), payCardParam);
        payInfo.setBizUserId(user.getBizUserId());
        payInfo.setPayType(payCardParam.getPayType());
        payInfo.setApiNoticeUrl("/notice/pay/" + PayEntry.RECHARGE.value() + "/" + payCardParam.getPayType());
        payInfo.setReturnUrl(payCardParam.getReturnUrl());
        payInfo.setShopId(Long.valueOf(balanceShopId));

        return payManager.doPay(httpResponse, payInfo);
    }

    @PostMapping("/h5Pay")
    @ApiOperation(value = "H5会员余额充值", notes = "H5会员余额充值")
    @SneakyThrows
    public ServerResponseEntity<?> cashierPay(@Valid @RequestBody PayUserParam payCardParam) {
        YamiUser user = SecurityUtils.getUser();
        String balanceShopId = sysConfigService.getConfigValue("BALANCE_SHOP");
        if (!user.getEnabled()) {
            // 您已被禁用，不能购买，请联系平台客服
            throw new YamiShopBindException("yami.order.pay.user.disable");
        }
        if (payCardParam.getPayType().equals(PayType.SCOREPAY.value()) || payCardParam.getPayType().equals(PayType.BALANCE.value())){
            // 请使用微信或支付宝进行支付
            throw new YamiShopBindException("yami.user.payType.error");
        }
        UserExtension userExtension = userExtensionService.getOne(new LambdaQueryWrapper<UserExtension>().eq(UserExtension::getUserId, user.getUserId()));
        UserBalance userBalance = null;
        // 自定义金额
        double balance;
        if (Objects.equals(payCardParam.getId(), -1L)) {
            balance = Arith.add(userExtension.getTotalBalance(), payCardParam.getCustomRechargeAmount());
        }
        // 购买
        else {
            userBalance = userBalanceService.getById(payCardParam.getId());
            if (Objects.isNull(userBalance)) {
                // 当前余额充值选项已被删除
                throw new YamiShopBindException("yami.user.blance.not.exist");
            }
            balance = Arith.add(userExtension.getTotalBalance(), userBalance.getRechargeAmount());
        }
        if (balance > Constant.MAX_USER_BALANCE) {
            // 您的余额加充值的余额将大于最大余额，不能进行充值操作
            throw new YamiShopBindException("yami.balance.than.max.balance");
        }
        // 使用雪花算法生成支付单号
        String payNo = String.valueOf(snowflake.nextId());
        Date now = new Date();

        //生成余额充值日志
        UserBalanceLog userBalanceLog = new UserBalanceLog();
        userBalanceLog.setCreateTime(now);
        userBalanceLog.setIoType(1);
        userBalanceLog.setType(1);
        userBalanceLog.setIsPayed(0);
        userBalanceLog.setPayNo(payNo);
        userBalanceLog.setUserId(user.getUserId());
        userBalanceLog.setShopId(Long.valueOf(balanceShopId));
        if (Objects.equals(payCardParam.getId(), -1L)) {
            userBalanceLog.setBalanceId(-1L);
            userBalanceLog.setChangeBalance(payCardParam.getCustomRechargeAmount());
        } else {
            userBalanceLog.setBalanceId(userBalance.getBalanceId());
            userBalanceLog.setChangeBalance(userBalance.getRechargeAmount());
        }
        //用户实际支付金额
        userBalanceLog.setPayAmount(userBalanceLog.getChangeBalance());
        //实际支付金额
        payCardParam.setNeedAmount(userBalanceLog.getChangeBalance());
        //查询会员卡余额充值前多少名赠送配置
        String rechargeGift = sysConfigService.getConfigValue("MEMBER_RECHARAGE_GIFT_RATIO");
        if(StringUtils.isNotBlank(rechargeGift)){
            Gson gson = new Gson();
            RechargeGiftVO rechargeGiftVO = gson.fromJson(rechargeGift,RechargeGiftVO.class);
            //查询会员卡余额成功充值数量
            Integer paySuccessNum = userBalanceLogService.getPaySuccessNum();
            if(rechargeGiftVO.getOrderNum() > paySuccessNum){//配置数量是否大于实际充值成功数量
                //更新赠送金额
                double presAmount = Arith.mul(userBalance.getRechargeAmount(),rechargeGiftVO.getGiftRatio());
                userBalance.setPresAmount(presAmount);
            }
        }
        //获取时间范围内充值配置信息
        String rechargeTimeRange = sysConfigService.getConfigValue("RECHARGE_TIME_RANGE");
        if(StringUtils.isNotBlank(rechargeTimeRange)){
            double presAmount10 = 0.0;//赠送10%金额
            double presAmount5 = 0.0;//赠送5%金额
            Gson gson = new Gson();
            RechargeTimeRangeVO rechargeTimeRangeVO = gson.fromJson(rechargeTimeRange,RechargeTimeRangeVO.class);
            String[] rechargeTimeRangeSplit = rechargeTimeRangeVO.getTimeRange().split("~");
            boolean before = new Date().before(DateUtil.parse(rechargeTimeRangeSplit[1], "yyyy-MM-dd"));
            boolean after = new Date().after(DateUtil.parse(rechargeTimeRangeSplit[0],"yyyy-MM-dd"));
            if(before && after){//判断当前时间是否在区间内
                //查询充值用户当月充值总金额和赠送总金额
                UserBalanceLog userBalanceLogInfo = userBalanceLogService.getTotalPayAmount(user.getUserId());
                //赠送金额
                double presAmount = 0.0;
                //判断当前赠送金额是否大于等于300元
                if(userBalanceLogInfo.getTotalGiveAmount() >= rechargeTimeRangeVO.getMaxGiveAmount()){
                    //更新赠送金额
                    presAmount = Arith.mul(userBalance.getRechargeAmount(),rechargeTimeRangeVO.getExceedGiftRatio());
                    userBalance.setPresAmount(presAmount);
                }else{
                    //300元赠送金额中还能赠送多少
                    double maxPresAmount = Arith.sub(rechargeTimeRangeVO.getMaxGiveAmount(),userBalanceLogInfo.getTotalGiveAmount());
                    //这笔充值金额判断是否能算10%
                    presAmount10 = Arith.mul(userBalance.getRechargeAmount(),rechargeTimeRangeVO.getGiftRatio());
                    if(presAmount10 > maxPresAmount){
                        //10%赠送金额最多还能充值金额
                        double maxAmount10 = Arith.div(maxPresAmount,rechargeTimeRangeVO.getGiftRatio());
                        //剩余金额赠送5%
                        double maxAmount5 = Arith.sub(userBalance.getRechargeAmount(),maxAmount10);
                        presAmount5 = Arith.mul(maxAmount5,rechargeTimeRangeVO.getExceedGiftRatio());
                        //赠送金额
                        presAmount = Arith.add(maxPresAmount,presAmount5);
                        userBalance.setPresAmount(presAmount);
                    }else{
                        //更新赠送金额
                        presAmount = presAmount10;
                        userBalance.setPresAmount(presAmount);
                    }
                }
            }
        }

        //充值金额加赠送金额
        if(userBalance.getPresAmount() > 0){
            Double changeBalance = Arith.add(userBalance.getRechargeAmount(),userBalance.getPresAmount());
            userBalanceLog.setChangeBalance(changeBalance);
        }
        //赠送金额
        userBalanceLog.setGiveAmount(userBalance.getPresAmount());
        userBalanceLogService.save(userBalanceLog);

        //充值金额大于一定金额升级高级会员
        String balanceRechargeUpMemberAmount = sysConfigService.getConfigValue("BALANCE_RECHARGE_UP_MEMBER");
        if(StringUtils.isNotBlank(balanceRechargeUpMemberAmount)){
            Double aDouble = new Double(balanceRechargeUpMemberAmount);
            if(userBalance.getRechargeAmount() >= aDouble){
                String memberLevelId = sysConfigService.getConfigValue("MEMBER_LEVEL_ID");
                if(StringUtils.isNotBlank(memberLevelId)){
                    UserLevel userLevel = userLevelService.getById(memberLevelId);
                    if(userLevel != null){
                        //生成等级日志
                        UserLevelLog userLevelLog = new UserLevelLog();
                        userLevelLog.setGiftTime(now);
                        userLevelLog.setCreateTime(now);
                        userLevelLog.setLevel(userLevel.getLevel());
                        userLevelLog.setState(0);
                        userLevelLog.setPayNo(payNo);
                        userLevelLog.setIsPayed(0);
                        userLevelLog.setPayAmount(userBalance.getRechargeAmount());
//                        userLevelLog.setPayAmount(userLevel.getNeedAmount());
                        userLevelLog.setLevelName(userLevel.getLevelName());
                        userLevelLog.setTerm(99);
                        userLevelLog.setTermType(5);
//                        userLevelLog.setTerm(userLevel.getTerm());
//                        userLevelLog.setTermType(userLevel.getTermType());
                        userLevelLog.setPayType(payCardParam.getPayType());
                        userLevelLog.setUserId(userExtension.getUserId());
                        userLevelLog.setType(1);
                        //插入一条等级日志
                        userLevelLogService.save(userLevelLog);
                    }
                }
            }
        }

        payCardParam.setPayNo(payNo);
        payCardParam.setOrderIds(userBalanceLog.getBalanceLogId());

        PayInfoDto payInfo = payInfoService.recharge(user.getUserId(), payCardParam);
        payInfo.setShopId(Long.valueOf(balanceShopId));
        payInfo.setBizUserId(user.getBizUserId());
        payInfo.setPayType(payCardParam.getPayType());
        payInfo.setApiNoticeUrl("/notice/pay/" + PayEntry.RECHARGE.value() + "/" + payCardParam.getPayType());
        payInfo.setReturnUrl(payCardParam.getReturnUrl());

        if (StringUtils.isBlank(payCardParam.getReturnUrl())) {
            payCardParam.setReturnUrl("https://lhshop.lzjczl.com/lhH5");
        }
        String domainName = shopConfig.getDomain().getApiDomainName();

        QueryWrapper<ShopMch> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shop_id", payInfo.getShopId());
        ShopMch one = shopMchService.getOne(queryWrapper);
        if (one == null) {
            throw new YamiShopBindException("该商户没有配置支付信息，无法下单支付！");
        }
        //调用统一支付收银台界面，拉起支付
        HashMap<String, Object> params = new HashMap<>();
        params.put("mchNo", one.getMchNo());
        params.put("appId", one.getAppId());
        params.put("apiKey", one.getApiKey());

        params.put("orderNo", payInfo.getPayNo());
        params.put("clientIp", "192.168.4.95");
        params.put("body", payInfo.getBody());
        params.put("notifyUrl", domainName + payInfo.getApiNoticeUrl());
        params.put("amount", String.valueOf(payInfo.getPayAmount()));
        params.put("returnUrl", payCardParam.getReturnUrl());
        params.put("extParam", "");
        params.put("couponShow", "0");
        params.put("orderNumbers", payCardParam.getPayNo());
        params.put("payType", payCardParam.getPayType());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Accept", "application/json;charset=utf-8");
        String postResult = HttpClient.sendPostRequest("http://192.168.5.49:9220/api/common/payHome", params, httpHeaders); //线上
        return ServerResponseEntity.success(postResult);
    }

    @GetMapping("/isPay")
    @ApiOperation(value = "获取余额充值支付信息", notes = "获取余额充值支付信息")
    public ServerResponseEntity<Integer> isPay(){
        YamiUser user = SecurityUtils.getUser();
        UserBalanceLog userBalanceLog = userBalanceLogService.getMaxCrtTimeByUserId(user.getUserId());
        return ServerResponseEntity.success(userBalanceLog.getIsPayed());
    }

    /**
     * 会员余额充值接口
     */
    @PostMapping("/rechargeCard")
    @ApiOperation(value = "充值卡充值", notes = "充值卡充值")
    @Transactional(rollbackFor = Exception.class)
    public ServerResponseEntity<?> rechargeCard(@RequestBody RechargeCardParam rechargeCardParam) {
        YamiUser user = SecurityUtils.getUser();
        if (!user.getEnabled()) {
            // 您已被禁用，不能购买，请联系平台客服
            throw new YamiShopBindException("yami.order.pay.user.disable");
        }
        //判断卡号对应的数据是否还有效
        RechargeCard rechargeCard = rechargeCardService.getOne((new LambdaQueryWrapper<RechargeCard>().eq(RechargeCard::getCardNumber, rechargeCardParam.getCardNumber().trim())));
        if (rechargeCard == null) {
            throw new YamiShopBindException("充值卡号不正确，充值失败！");
        }
        if(rechargeCard.getExpirationTime().getTime() < new Date().getTime()) {
            throw new YamiShopBindException("充值卡已失效！");
        }
        if (rechargeCard.getState() != 1) {
            throw new YamiShopBindException("充值卡已失效！");
        }
        UserUpdateParam userUpdateParam = new UserUpdateParam();
        userUpdateParam.setBalanceValue(rechargeCard.getMoney());
        userUpdateParam.setUserIds(Arrays.asList(user.getUserId()));
        userUpdateParam.setScopeSource(7);
        Boolean aBoolean = userBalanceService.batchUpdateUserBalance(userUpdateParam);
        if (aBoolean) {
            //把充值卡状态设置为失效
            rechargeCard.setState(0);
            rechargeCard.setRechargeTime(new Date());
            rechargeCard.setRechargeUserId(user.getUserId());
            boolean b = rechargeCardService.updateById(rechargeCard);
            if (b) {
                return ServerResponseEntity.success("充值成功");
            }
        }
        throw new YamiShopBindException("充值失败");
    }

    @GetMapping("/genCardNumber")
    private void genCardNumber(int count1, int count2) {
        ArrayList<RechargeCard> rechargeCards = new ArrayList<>();
        for (int i = 1; i <=count1; i++) {
            RechargeCard rechargeCard = new RechargeCard();
            rechargeCard.setCardNumber(checkQrCodeExists().toString());
            rechargeCard.setCreateTime(new Date());
            rechargeCard.setType(1);
            rechargeCard.setMoney(500.0);
            rechargeCard.setState(1);
            rechargeCards.add(rechargeCard);
        }
        for (int i = 1; i <=count2; i++) {
            RechargeCard rechargeCard = new RechargeCard();
            rechargeCard.setCardNumber(checkQrCodeExists().toString());
            rechargeCard.setCreateTime(new Date());
            rechargeCard.setType(1);
            rechargeCard.setMoney(300.0);
            rechargeCard.setState(1);
            rechargeCards.add(rechargeCard);
        }
        rechargeCardService.saveBatch(rechargeCards);
    }


    private Integer checkQrCodeExists() {
        //生成7位随机数
        Integer integer = SnowflakeIdWorker.generateIdNum7();
        QueryWrapper<RechargeCard> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_number", integer);
        queryWrapper.last("LIMIT 1");
        RechargeCard one = rechargeCardService.getOne(queryWrapper);
        if (one != null) {
            return checkQrCodeExists();
        }
        return integer;
    }

}
