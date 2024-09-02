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
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeequan.jeepay.Jeepay;
import com.yami.shop.bean.app.param.PayInfoParam;
import com.yami.shop.bean.app.param.PayParam;
import com.yami.shop.bean.enums.PayEntry;
import com.yami.shop.bean.model.CouponOrder;
import com.yami.shop.bean.model.Order;
import com.yami.shop.bean.model.ShopMch;
import com.yami.shop.bean.model.UserExtension;
import com.yami.shop.bean.pay.CarPayInfoDto;
import com.yami.shop.bean.pay.PayInfoDto;
import com.yami.shop.common.enums.PayType;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.Arith;
import com.yami.shop.config.ShopConfig;
import com.yami.shop.coupon.common.model.Coupon;
import com.yami.shop.coupon.common.service.CouponService;
import com.yami.shop.manager.impl.PayManager;
import com.yami.shop.security.api.model.YamiUser;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.security.common.model.AppConnect;
import com.yami.shop.security.common.service.AppConnectService;
import com.yami.shop.service.*;
import com.yami.shop.user.common.model.UserBalanceLog;
import com.yami.shop.user.common.model.UserBalanceUseLog;
import com.yami.shop.user.common.service.UserBalanceLogService;
import com.yami.shop.user.common.service.UserBalanceUseLogService;
import com.yami.shop.utils.HttpClient;
import com.yami.shop.utils.OkHttpUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author LGH
 */
@RestController
@RequestMapping("/p/order")
@Api(tags = "订单支付接口")
@AllArgsConstructor
public class PayController {

    private final PayInfoService payInfoService;
    private final PayManager payManager;
    private final OrderService orderService;
    private final AppConnectService appConnectService;

    @Autowired
    private Snowflake snowflake;
    @Autowired
    private SysConfigService sysConfigService;

    @Autowired
    private ShopMchService shopMchService;
    @Autowired
    private CouponOrderService couponOrderService;
    @Autowired
    private CouponService couponService;
    @Autowired
    private ShopConfig shopConfig;
    @Autowired
    private UserExtensionService userExtensionService;
    @Autowired
    private UserBalanceLogService userBalanceLogService;
    @Autowired
    private UserBalanceUseLogService userBalanceUseLogService;
    private PickCarController pickCarController;


    @PostMapping("/pay")
    @ApiOperation(value = "根据订单号进行支付", notes = "根据订单号进行支付")
    @SneakyThrows
    public ServerResponseEntity<?> pay(HttpServletResponse httpResponse, @Valid @RequestBody PayParam payParam) {
        YamiUser user = SecurityUtils.getUser();
        String userId = user.getUserId();
        if (!user.getEnabled()) {
            // 您已被禁用，不能购买，请联系平台客服
            throw new YamiShopBindException("yami.order.pay.user.disable");
        }

        Order orderByOrderNumber = orderService.getOrderByOrderNumber(payParam.getOrderNumbers());

        //使用余额支付时判断不可使用余额支付店铺
        if(Objects.equals(payParam.getPayType(), PayType.BALANCE.value())){
            String unableUseBalanceShops = sysConfigService.getConfigValue("UNABLE_USE_BALANCE_SHOP");
            if(StringUtils.isNotBlank(unableUseBalanceShops)){
                String[] shopIdList = unableUseBalanceShops.split(",");
                for (String shopId : shopIdList) {
                    if (orderByOrderNumber.getShopId().equals(Long.valueOf(shopId))) {
                        throw new YamiShopBindException("该店铺暂不支持使用会员卡余额支付");
                    }
                }
            }
        }

        PayInfoDto payInfo = payInfoService.pay(userId, payParam);
        payInfo.setBizUserId(user.getBizUserId());

        if (Objects.equals(payParam.getPayType(), PayType.HY_PAY.value())) {
            LambdaQueryWrapper<AppConnect> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AppConnect::getUserId, user.getUserId());
            queryWrapper.eq(AppConnect::getAppId, 3);
            queryWrapper.orderByDesc(AppConnect::getId);
            queryWrapper.last("LIMIT 1");
            AppConnect one = appConnectService.getOne(queryWrapper);
            payInfo.setBizUserId(one.getBizUserId());
            payInfo.setPayPassword(payParam.getPayPassword());
        }
        if (Objects.equals(payParam.getPayType(), PayType.CARD_PAY.value())) {
            payInfo.setCardCode(payParam.getCardCode());
        }
        payInfo.setPayType(payParam.getPayType());
        payInfo.setApiNoticeUrl("/notice/pay/" + PayEntry.ORDER.value() + "/" + payParam.getPayType());
        payInfo.setReturnUrl(payParam.getReturnUrl());
        payInfo.setShopId(orderByOrderNumber.getShopId());
        ServerResponseEntity<?> responseEntity = payManager.doPay(httpResponse, payInfo);
        return responseEntity;
    }


    @PostMapping("/cashierPay")
    @ApiOperation(value = "收银台根据订单号进行支付", notes = "收银台根据订单号进行支付")
    @SneakyThrows
    public ServerResponseEntity<?> cashierPay(@Valid @RequestBody PayParam payParam) {
        YamiUser user = SecurityUtils.getUser();
        String userId = user.getUserId();
        if (!user.getEnabled()) {
            // 您已被禁用，不能购买，请联系平台客服
            throw new YamiShopBindException("yami.order.pay.user.disable");
        }
        CouponOrder couponOrder = null;
        Coupon couponInfo = null;
        if(payParam.getBuyType() == 2){
            //团购券订单信息
            couponOrder = couponOrderService.getCouponOrderByOrderNumber(payParam.getOrderNumbers());
            //获取优惠券信息
            couponInfo = couponService.getById(couponOrder.getCouponId());
            if(couponInfo.getCouponType() == 4){
                payParam.setCardName(couponInfo.getCardName());
                payParam.setCardBalance(couponInfo.getReduceAmount());
                payParam.setGiveCouponId(couponInfo.getGiveCouponId());
            }
        }
        PayInfoDto payInfo = null;
        if(payParam.getBuyType() == 1){
            payInfo = payInfoService.pay(userId, payParam);
        }else if(payParam.getBuyType() == 2){
            payInfo = payInfoService.couponPay(userId, payParam);
        }
        payInfo.setBizUserId(user.getBizUserId());
        payInfo.setPayType(payParam.getPayType());
        if(payParam.getBuyType() == 1){ //商品
            payInfo.setApiNoticeUrl("/notice/pay/order/" + payParam.getPayType());
        }else if(payParam.getBuyType() == 2){ //买券/提货卡
            if(couponInfo.getCouponType() == 4){ //买卡
                payInfo.setApiNoticeUrl("/notice/pay/cardPay/" + payParam.getPayType() + "?cardBalance=" + payParam.getCardBalance()
                        + "&cardName=" + payParam.getCardName() + "&giveCouponId=" + payParam.getGiveCouponId());
            }else{//买券
                payInfo.setApiNoticeUrl("/notice/pay/couponPay/" + payParam.getPayType());
            }
        }
        payInfo.setReturnUrl(payParam.getReturnUrl());

        if (StringUtils.isBlank(payParam.getReturnUrl())) {
            payParam.setReturnUrl("https://lhshop.lzjczl.com/lhH5");
        }
        String domainName = shopConfig.getDomain().getApiDomainName();
        if(payParam.getBuyType() == 1){ //商品
            Order orderByOrderNumber = orderService.getOrderByOrderNumber(payParam.getOrderNumbers());
            payInfo.setShopId(orderByOrderNumber.getShopId());
        }else if(payParam.getBuyType() == 2){ //买券/提货卡
            if(couponOrder != null && couponInfo != null){
                if(couponInfo.getCouponType() == 4){ //买提货卡
                    if(couponOrder.getShopId() == 0){
                        String shopId = sysConfigService.getConfigValue("BUY_CARD_SHOP");
                        payInfo.setShopId(Long.valueOf(shopId));
                    }else{
                        payInfo.setShopId(couponOrder.getShopId());
                    }
                }else{
                    if(couponOrder.getShopId() == 0){
                        String shopId = sysConfigService.getConfigValue("PT_GROUP_COUPON_SHOP");
                        payInfo.setShopId(Long.valueOf(shopId));
                    }else{
                        payInfo.setShopId(couponOrder.getShopId());
                    }
                }
            }else{
                throw new YamiShopBindException("未查询到订单");
            }
        }
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
        params.put("returnUrl", payParam.getReturnUrl());
        params.put("extParam", "");
        params.put("couponShow", "0");
        params.put("orderNumbers", payParam.getOrderNumbers());
        params.put("payType", payParam.getPayType());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Accept", "application/json;charset=utf-8");
        String postResult = HttpClient.sendPostRequest("http://192.168.5.49:9220/api/common/payHome", params, httpHeaders); //线上
        return ServerResponseEntity.success(postResult);
    }


    @PostMapping("/carPay")
    @ApiOperation(value = "根据订单号进行支付", notes = "根据订单号进行支付")
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    public ServerResponseEntity<?> carPay(HttpServletResponse httpResponse, @Valid @RequestBody PayParam payParam) {
        String carShopId = sysConfigService.getConfigValue("CAR_SHOP");
        YamiUser user = SecurityUtils.getUser();
        LambdaQueryWrapper<AppConnect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppConnect::getUserId, user.getUserId());
        queryWrapper.eq(AppConnect::getAppId, payParam.getPayType() == 5 ? 2 : 1);
        queryWrapper.orderByDesc(AppConnect::getId);
        queryWrapper.last("LIMIT 1");
        AppConnect one = appConnectService.getOne(queryWrapper);

        if (one == null) {
            return ServerResponseEntity.showFailMsg("请先关注公众号");
        }
        JSONObject jsonObject = new JSONObject();
        payParam.setCarNo(URLDecoder.decode(payParam.getCarNo(), "UTF-8"));
        payParam.setEntranceTime(URLDecoder.decode(payParam.getEntranceTime(), "UTF-8"));
        payParam.setParkTime(URLDecoder.decode(payParam.getParkTime(), "UTF-8"));
        jsonObject.put("carNo", StringUtils.isNotBlank(payParam.getCarNo()) ? payParam.getCarNo() : "");
        String carType = "";
        if (StringUtils.isNotBlank(payParam.getCarNo())) {
            int length = payParam.getCarNo().length();
            if (length == 7) {
                carType = "蓝牌车";
            } else if (length == 8) {
                carType = "新能源车";
            }
        }
        jsonObject.put("carType", carType);
        jsonObject.put("totalAmount", payParam.getTotalAmount() != null ? payParam.getTotalAmount() : "");
        jsonObject.put("actualAmount", payParam.getActualAmount() != null ? payParam.getActualAmount() : "");
        jsonObject.put("entranceTime", StringUtils.isNotBlank(payParam.getEntranceTime()) ? payParam.getEntranceTime() : "");
        jsonObject.put("parkTime", StringUtils.isNotBlank(payParam.getParkTime()) ? payParam.getParkTime() : "");
        // 支付单号
        String payNo = String.valueOf(snowflake.nextId());
        PayInfoDto payInfoDto = new PayInfoDto();
        payInfoDto.setExtParam(jsonObject.toJSONString());
        payInfoDto.setBody("停车订单");
        payInfoDto.setPayAmount(Double.parseDouble(String.valueOf(payParam.getActualAmount())));
        payInfoDto.setPayNo(payNo);
        payInfoDto.setBizUserId(one.getBizUserId());
        payInfoDto.setPayType(payParam.getPayType());
        payInfoDto.setApiNoticeUrl("/notice/pay/carPay/" + payParam.getPayType() +
                "?tradeno=" + payParam.getTradeno() + "&carPayType=" + payParam.getCarPayType() + "&totalAmount=" + payParam.getTotalAmount());
        payInfoDto.setReturnUrl(payParam.getReturnUrl());
        payInfoDto.setShopId(Long.valueOf(carShopId));

        if(Objects.equals(payParam.getPayType(), PayType.BALANCE.value())){//判断是余额支付
            PayInfoParam payInfoParam = new PayInfoParam();
            UserExtension userExtension = userExtensionService.getOne(new LambdaQueryWrapper<UserExtension>().eq(UserExtension::getUserId, user.getUserId()));
            if (Objects.isNull(userExtension)) {
                payInfoParam.setType(2);
                // 您的信息有误，请尝试刷新后再进行操作
                payInfoParam.setMessage(I18nMessage.getMessage("yami.information.is.wrong"));
                return ServerResponseEntity.success(payInfoParam);
            }
            double payAmount = Double.parseDouble(String.valueOf(payParam.getActualAmount()));
            if(Objects.isNull(userExtension.getTotalBalance()) || userExtension.getTotalBalance() < payAmount) {
                payInfoParam.setType(3);
                // 您的余额不足，请先充值余额
                payInfoParam.setMessage(I18nMessage.getMessage("yami.balance.is.insufficient"));
                return ServerResponseEntity.success(payInfoParam);
            }
            userExtension.setTotalBalance(Arith.sub(userExtension.getTotalBalance(), payAmount));
            userExtensionService.updateBalanceByVersion(userExtension);

            Date now = new Date();

            //生成余额使用日志
            UserBalanceLog userBalanceLog = new UserBalanceLog();
            userBalanceLog.setCreateTime(now);
            userBalanceLog.setIoType(0);
            userBalanceLog.setType(8);
            userBalanceLog.setPayNo(payNo);
            userBalanceLog.setUserId(user.getUserId());
            userBalanceLog.setShopId(Long.valueOf(carShopId));
            userBalanceLog.setChangeBalance(new Double(Double.valueOf(payParam.getTotalAmount())));
            //用户实际支付金额
            userBalanceLog.setPayAmount(userBalanceLog.getChangeBalance());
            userBalanceLogService.save(userBalanceLog);
            //余额使用记录
            UserBalanceUseLog userBalanceUseLog = new UserBalanceUseLog();
            userBalanceUseLog.setPayNo(userBalanceLog.getPayNo());
            userBalanceUseLog.setUserId(userBalanceLog.getUserId());
            userBalanceUseLog.setChangeBalance(userBalanceLog.getChangeBalance());
            userBalanceUseLog.setCreateTime(new Date());
            userBalanceUseLog.setShopId(Long.valueOf(carShopId));
            userBalanceUseLog.setCarNo(URLDecoder.decode(payParam.getCarNo(), "UTF-8"));
            int carLength = userBalanceUseLog.getCarNo().length();
            if (carLength == 7) {
                carType = "蓝牌车";
            } else if (carLength == 8) {
                carType = "新能源车";
            }
            userBalanceUseLog.setCarType(carType);
            userBalanceUseLog.setEntranceTime(URLDecoder.decode(payParam.getEntranceTime(), "UTF-8"));
            userBalanceUseLog.setParkTime(URLDecoder.decode(payParam.getParkTime(), "UTF-8"));
            //1代表停车缴费
            userBalanceUseLog.setPayType(1);
            //获取会员卡消费店铺费率
            String memberCardRate = sysConfigService.getConfigValue("MEMBER_CARD_RATE");
            double rateAmount = 0.0;
            if(StringUtils.isNotBlank(memberCardRate)){
                rateAmount = Arith.mul(userBalanceLog.getChangeBalance(),Double.valueOf(memberCardRate));
            }else{
                rateAmount = Arith.mul(userBalanceLog.getChangeBalance(),0.05);
            }
            //费率扣除金额
            userBalanceUseLog.setRateAmount(new BigDecimal(String.valueOf(rateAmount)).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
            //店铺实得金额
            userBalanceUseLog.setShopAmount(Arith.sub(userBalanceLog.getChangeBalance(),userBalanceUseLog.getRateAmount()));
            //首诚承担金额
            double scAmount = Arith.mul(userBalanceUseLog.getRateAmount(),0.45);
            userBalanceUseLog.setScAmount(new BigDecimal(String.valueOf(scAmount)).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
            //物业承担金额
            userBalanceUseLog.setWyAmount(Arith.sub(userBalanceUseLog.getRateAmount(),userBalanceUseLog.getScAmount()));
            userBalanceUseLogService.save(userBalanceUseLog);
            //开闸
            CarPayInfoDto carPayInfoDto = new CarPayInfoDto();
            carPayInfoDto.setResult(0);
            carPayInfoDto.setMsg("success");
            carPayInfoDto.setTradeNo(payParam.getTradeno());
            carPayInfoDto.setVersion(1);
            carPayInfoDto.setPayType(payParam.getCarPayType());
            carPayInfoDto.setTotalAmount(new Double(Double.valueOf(payParam.getTotalAmount()) * 100).intValue());
            long time = System.currentTimeMillis()/1000L;
            carPayInfoDto.setPayTime(String.valueOf(time));
            Boolean aBoolean = pickCarController.aPIPayResult(carPayInfoDto);
            payInfoParam.setType(1);
            return ServerResponseEntity.success(payInfoParam);
        }else{
            ServerResponseEntity<?> responseEntity = payManager.doPay(httpResponse, payInfoDto);
            return responseEntity;
        }
    }

    @GetMapping("/isPay/{payEntry}/{orderNumbers}")
    @ApiOperation(value = "根据订单号查询该订单是否已经支付", notes = "根据订单号查询该订单是否已经支付")
    @ApiImplicitParam(name = "orderNumbers", value = "多个订单号拼接", required = true, dataType = "String")
    public ServerResponseEntity<Boolean> isPay(@PathVariable Integer payEntry, @PathVariable String orderNumbers) {
        YamiUser user = SecurityUtils.getUser();
        Integer count = payInfoService.queryPay(orderNumbers, user.getUserId(), payEntry);
        boolean res = count != null && count == 1;
        return ServerResponseEntity.success(res);
    }
}
