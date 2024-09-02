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
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.yami.shop.bean.bo.PayInfoResultBO;
import com.yami.shop.bean.enums.PayEntry;
import com.yami.shop.bean.enums.PayStatus;
import com.yami.shop.bean.model.*;
import com.yami.shop.bean.pay.CarPayInfoDto;
import com.yami.shop.card.common.model.Card;
import com.yami.shop.card.common.model.CardShop;
import com.yami.shop.card.common.model.CardUser;
import com.yami.shop.card.common.service.CardService;
import com.yami.shop.card.common.service.CardShopService;
import com.yami.shop.card.common.service.CardUserService;
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.enums.PayType;
import com.yami.shop.common.util.Arith;
import com.yami.shop.common.util.RequestKitBean;
import com.yami.shop.coupon.api.util.SnowflakeIdWorker;
import com.yami.shop.coupon.common.constants.ValidTimeTypeEnum;
import com.yami.shop.coupon.common.model.Coupon;
import com.yami.shop.coupon.common.model.CouponUser;
import com.yami.shop.coupon.common.service.CouponService;
import com.yami.shop.coupon.common.service.CouponShopService;
import com.yami.shop.coupon.common.service.CouponUserService;
import com.yami.shop.dao.PayInfoMapper;
import com.yami.shop.manager.impl.PayManager;
import com.yami.shop.mq.model.PayOrderMchNotifyMQ;
import com.yami.shop.mq.vender.IMQSender;
import com.yami.shop.service.*;
import com.yami.shop.user.common.service.UserBalanceService;
import com.yami.shop.user.common.service.UserLevelService;
import com.yami.shop.utils.OkHttpUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @author LGH
 */
@ApiIgnore
@RestController
@RequestMapping("/notice/pay")
@AllArgsConstructor
@Slf4j
public class PayNoticeController {

    private final PayManager payManager;

    private final PayInfoService payInfoService;

    private final UserLevelService userLevelService;

    private final UserBalanceService userBalanceService;

    private final OrderItemService orderItemService;

    private final ShopDetailService shopDetailService;

    private final PickupService pickupService;

    private final RoomsService roomsService;
    private final RoomsRecordService roomsRecordService;
    private final CardService cardService;

    private final CardMqService cardMqService;

    @Autowired
    private IMQSender mqSender;


    @Autowired
    private RequestKitBean requestKitBean;

    @Autowired
    private PayInfoMapper payInfoMapper;

    private PickCarController pickCarController;

    private final CouponService couponService;

    private final CouponOrderService couponOrderService;
    private final CardShopService cardShopService;
    private final CouponShopService couponShopService;
    private final CardUserService cardUserService;
    private final CouponUserService couponUserService;
    @Autowired
    private Snowflake snowflake;
    private final RedissonClient redissonClient;


    /**
     * 支付异步回调
     */
    @RequestMapping("/{payEntry}/{payType}")
    public String notice(HttpServletRequest request,
                                               @PathVariable("payEntry") Integer payEntry,
                                               @PathVariable("payType") Integer payType,
                                               @RequestBody(required = false) String xmlData) throws WxPayException, UnsupportedEncodingException, AlipayApiException {

//        PayInfoResultBO payInfoResultBO = payManager.validateAndGetPayInfo(request, PayType.instance(payType), xmlData);

        JSONObject params = getReqParamJSON();
        log.info("接收支付回调参数=========={}", params);
        if (params == null || StrUtil.isBlank(params.toJSONString())) {
            return "false";
        }
        //支付状态不等于2 说明支付未成功
        if (!"2".equals(params.getString("state"))) {
            return "false";
        }
        PayInfoResultBO payInfoResultBO = new PayInfoResultBO();
        payInfoResultBO.setPayNo(params.getString("mchOrderNo"));
        payInfoResultBO.setBizPayNo(params.getString("channelOrderNo"));
        payInfoResultBO.setIsPaySuccess("2".equals(params.getString("state")));
        payInfoResultBO.setSuccessString("true");
        payInfoResultBO.setPayAmount(Arith.div(Double.parseDouble(params.getString("amount")), 100));
        payInfoResultBO.setCallbackContent(params.toJSONString());
        payInfoResultBO.setBizOrderNo(params.getString("payOrderId"));
        // 校验订单参数异常
        if (!payInfoResultBO.getIsPaySuccess()) {
            return "false";
        }

        PayInfo payInfo = payInfoService.getOne(new LambdaQueryWrapper<PayInfo>().eq(PayInfo::getPayNo, payInfoResultBO.getPayNo()));

        // 已经支付
        if (Objects.equals(payInfo.getPayStatus(), PayStatus.PAYED.value()) || Objects.equals(payInfo.getPayStatus(), PayStatus.REFUND.value())) {
            return "success";
        }
        // 支付金额不对
        if (!Objects.equals(payInfo.getPayAmount(), payInfoResultBO.getPayAmount()) && !Objects.equals(payInfo.getPayType(), PayType.PAYPAL.value())) {
            log.info("支付金额不对");
            return "false";
        }

        if (Objects.equals(payEntry, PayEntry.ORDER.value())) {
            return payInfoService.noticeOrder(payInfoResultBO, payInfo);
        } else if (Objects.equals(payEntry, PayEntry.RECHARGE.value())) {
            return userBalanceService.noticeRecharge(payInfoResultBO, payInfo);
        } else if (Objects.equals(payEntry, PayEntry.VIP.value())) {
            return userLevelService.noticeBuyVip(payInfoResultBO, payInfo);
        }
        return "false";
    }

    /**
     * 支付异步回调
     */
    @RequestMapping(path = "/carPay/{payType}", produces = {"text/plain", "application/json"})
    public String carPayNotice(HttpServletRequest request, @PathVariable("payType") Integer payType) throws Exception {
        JSONObject paramJSON = getReqParamJSON();
        String totalAmount = paramJSON.getString("totalAmount");
        CarPayInfoDto carPayInfoDto = new CarPayInfoDto();
        carPayInfoDto.setResult(0);
        carPayInfoDto.setMsg("success");
        carPayInfoDto.setTradeNo(paramJSON.getString("tradeno"));
        carPayInfoDto.setVersion(1);
        carPayInfoDto.setPayType(paramJSON.getString("carPayType"));
        carPayInfoDto.setTotalAmount(new Double(Double.valueOf(totalAmount) * 100).intValue());
        long time = System.currentTimeMillis()/1000L;
        carPayInfoDto.setPayTime(String.valueOf(time));
        Boolean aBoolean = pickCarController.aPIPayResult(carPayInfoDto);
        return aBoolean ? "success" : "false";
    }

    /**
     * 支付异步回调
     */
    @RequestMapping(path = "/couponPay/{payType}")
    public String couponPayNotice() {
        JSONObject params = getReqParamJSON();
        log.info("接收支付回调参数=========={}", params);
        if (params == null || StrUtil.isBlank(params.toJSONString())) {
            return "false";
        }
        //支付状态不等于2 说明支付未成功
        if (!"2".equals(params.getString("state"))) {
            return "false";
        }
        PayInfoResultBO payInfoResultBO = new PayInfoResultBO();
        payInfoResultBO.setPayNo(params.getString("mchOrderNo"));
        payInfoResultBO.setBizPayNo(params.getString("channelOrderNo"));
        payInfoResultBO.setIsPaySuccess("2".equals(params.getString("state")));
        payInfoResultBO.setSuccessString("true");
        payInfoResultBO.setPayAmount(Arith.div(Double.parseDouble(params.getString("amount")), 100));
        payInfoResultBO.setCallbackContent(params.toJSONString());

        PayInfo payInfo = new PayInfo();
        payInfo.setPayNo(params.getString("mchOrderNo"));
        payInfo.setBizPayNo(params.getString("channelOrderNo"));
        payInfo.setBizOrderNo(params.getString("payOrderId"));
        payInfo.setCallbackTime(new Date());
        payInfo.setCallbackContent(params.toJSONString());
        // 支付宝多次回调可能该支付单已经退款，但还是更新了回调时间导致对账查询有误
        payInfoMapper.update(payInfo, new LambdaUpdateWrapper<PayInfo>()
                .eq(PayInfo::getPayNo, payInfo.getPayNo())
                .eq(PayInfo::getPayStatus, PayStatus.UNPAY.value()));

        payInfo = payInfoService.getOne(new LambdaQueryWrapper<PayInfo>().eq(PayInfo::getPayNo, params.getString("mchOrderNo")));
        // 已经支付
        if (Objects.equals(payInfo.getPayStatus(), PayStatus.PAYED.value()) || Objects.equals(payInfo.getPayStatus(), PayStatus.REFUND.value())) {
            return "success";
        }
        payInfoService.noticeCouponOrder(payInfoResultBO, payInfo);
        CouponOrder couponOrder = couponOrderService.getCouponOrderByOrderNumber(payInfo.getOrderNumbers());
        couponService.bindCouponById(Collections.singletonList(couponOrder.getCouponId()), couponOrder.getUserId(), couponOrder.getShopId());
        return "success";
    }

    /**
     * 支付异步回调
     */
    @RequestMapping(path = "/cardPay/{payType}")
    public String cardPayNotice() {
        // 加锁
//        RLock rLock = redissonClient.getLock("redisson_lock::buyCard");
//        rLock.lock();
        try {
            JSONObject params = getReqParamJSON();
            log.info("接收支付回调参数=========={}", params);
            if (params == null || StrUtil.isBlank(params.toJSONString())) {
                return "false";
            }
            //支付状态不等于2 说明支付未成功
            if (!"2".equals(params.getString("state"))) {
                return "false";
            }

//            PayInfo payInfo = payInfoService.getOne(new LambdaQueryWrapper<PayInfo>().eq(PayInfo::getPayNo, params.getString("mchOrderNo")));
//            CouponOrder couponOrder = couponOrderService.getCouponOrderByOrderNumber(payInfo.getOrderNumbers());

//            //存支付信息
//            CardMq cardMq = new CardMq();
//            cardMq.setMchOrderNo(params.getString("mchOrderNo"));
//            cardMq.setChannelOrderNo(params.getString("channelOrderNo"));
//            cardMq.setPayOrderId(params.getString("payOrderId"));
//            cardMq.setStatus(1);
//            cardMq.setCount(1);
//            cardMq.setState(params.getString("state"));
//            cardMq.setAmount(Arith.div(Double.parseDouble(params.getString("amount")), 100));
//            cardMq.setCallbackContent(params.toJSONString());
//            cardMq.setUserId(couponOrder.getUserId());
//            boolean save = cardMqService.save(cardMq);
//            if(save){
//                mqSender.send(PayOrderMchNotifyMQ.build(cardMq.getId().toString()));
//            }


            PayInfoResultBO payInfoResultBO = new PayInfoResultBO();
            payInfoResultBO.setPayNo(params.getString("mchOrderNo"));
            payInfoResultBO.setBizPayNo(params.getString("channelOrderNo"));
            payInfoResultBO.setIsPaySuccess("2".equals(params.getString("state")));
            payInfoResultBO.setSuccessString("true");
            payInfoResultBO.setPayAmount(Arith.div(Double.parseDouble(params.getString("amount")), 100));
            payInfoResultBO.setCallbackContent(params.toJSONString());

            PayInfo payInfo = new PayInfo();
            payInfo.setPayNo(params.getString("mchOrderNo"));
            payInfo.setBizPayNo(params.getString("channelOrderNo"));
            payInfo.setBizOrderNo(params.getString("payOrderId"));
            payInfo.setCallbackTime(new Date());
            payInfo.setCallbackContent(params.toJSONString());
            // 支付宝多次回调可能该支付单已经退款，但还是更新了回调时间导致对账查询有误
            payInfoMapper.update(payInfo, new LambdaUpdateWrapper<PayInfo>()
                    .eq(PayInfo::getPayNo, payInfo.getPayNo())
                    .eq(PayInfo::getPayStatus, PayStatus.UNPAY.value()));

            payInfo = payInfoService.getOne(new LambdaQueryWrapper<PayInfo>().eq(PayInfo::getPayNo, params.getString("mchOrderNo")));
            // 已经支付
            if (Objects.equals(payInfo.getPayStatus(), PayStatus.PAYED.value()) || Objects.equals(payInfo.getPayStatus(), PayStatus.REFUND.value())) {
                return "success";
            }
            payInfoService.noticeCouponOrder(payInfoResultBO, payInfo);
            CouponOrder couponOrder = couponOrderService.getCouponOrderByOrderNumber(payInfo.getOrderNumbers());
            //生成提货卡
            Card card = new Card();
            List<Long> couponShopIds = null;
            Coupon coupon = couponService.getById(couponOrder.getCouponId());
            card.setSuitableProdType(2);
            if(coupon.getShopId() == 0 && coupon.getSuitableProdType() == 3){//优惠券是否指定店铺使用
                couponShopIds = couponShopService.getShopIdByCouponId(couponOrder.getCouponId());
            } else if(coupon.getShopId() != 0) {
                couponShopIds = Arrays.asList(coupon.getShopId());
            }
            if(couponOrder.getShopId() == 0){
                card.setShopId(Constant.PLATFORM_SHOP_ID);
            }else{
                card.setShopId(couponOrder.getShopId());
            }
            card.setCardTitle(params.getString("cardName"));
            card.setBalance(Double.parseDouble(params.getString("cardBalance")));
            card.setCreateTime(new Date());
            card.setIsDelete(0);
            card.setCardType(1);
            card.setCreateType(2);
            card.setCardCode(checkCardQrCodeExists().toString());
            card.setStatus(0);
            card.setCardPrefix("BUY");
            card.setBuyUnit(couponOrder.getUserId());
            card.setBuyCardType(1);
            // 生效时间类型为固定时间
            if (coupon.getValidTimeType() == 1) {
                card.setUserStartTime(coupon.getStartTime());
                card.setUserEndTime(DateUtils.addDays(coupon.getEndTime(), -1));
            }
            // 生效时间类型为领取后生效
            if (coupon.getValidTimeType() == ValidTimeTypeEnum.RECEIVE.getValue()) {
                Date nowTime = new Date();
                if (coupon.getAfterReceiveDays() == null) {
                    coupon.setAfterReceiveDays(0);
                }
                if (coupon.getValidDays() == null) {
                    coupon.setValidDays(0);
                }
                card.setUserStartTime(DateUtils.addDays(DateUtil.beginOfDay(nowTime), coupon.getAfterReceiveDays()));
                card.setUserEndTime(DateUtils.addDays(card.getUserStartTime(), coupon.getValidDays() - 1));
            }
            //设置卡号  卡密  随机生成
            String code = "";
            //获取买卡的最大编号
            Card one = cardService.getBuyCardMaxInfo();
            if(one != null){
                String s = one.getCardNumber().split(one.getCardPrefix())[1];
                int num = Integer.parseInt(s) + 1;
                code = String.valueOf(num);
            }else{
                code = "1";
            }
            card.setCardNumber(card.getCardPrefix() + code);
            card.setPassword(String.valueOf(SnowflakeIdWorker.generateIdNum6()));
            cardService.save(card);

            if(card.getSuitableProdType() == 2) {//指定店铺
                List<CardShop> cardShops = new ArrayList<>();
                for (Long shopId:couponShopIds) {
                    CardShop cardShop = new CardShop();
                    cardShop.setCardId(card.getCardId());
                    cardShop.setCreateTime(new Date());
                    cardShop.setShopId(shopId);
                    cardShops.add(cardShop);
                }
                if(cardShops.size() > 0){
                    cardShopService.saveBatch(cardShops);
                }
            }
            //用户绑定提货卡
            CardUser cardUser = new CardUser();
            cardUser.setUserId(couponOrder.getUserId());
            cardUser.setCardId(card.getCardId());
            cardUser.setReceiveTime(new Date());
            cardUser.setScore(0.0);
            cardUser.setBalance(card.getBalance());
            cardUser.setStatus(1);
            cardUser.setIsDelete(0);
            cardUser.setCardNumber(card.getCardCode());
            cardUser.setCouponId(couponOrder.getCouponId());
            cardUser.setUserStartTime(card.getUserStartTime());
            cardUser.setUserEndTime(card.getUserEndTime());
            cardUserService.save(cardUser);

            card.setStatus(3);
            cardService.updateById(card);

            Long giveCouponId = null;
            if(StringUtils.isNotBlank(params.getString("giveCouponId")) && !"null".equals(params.getString("giveCouponId"))){
                giveCouponId = Long.valueOf(params.getString("giveCouponId"));
            }
            //获取购买提货卡赠送的券
            if(giveCouponId != null){
                Coupon couonInfo = couponService.getById(giveCouponId);
                //判断赠送优惠券是否有效
                if(couonInfo.getOverdueStatus() == 1 && couonInfo.getPutonStatus() == 1 && couonInfo.getStocks() > 0){
                    //该券用户已有数量
                    int count = couponUserService.count(new LambdaQueryWrapper<CouponUser>().eq(CouponUser::getUserId, couponOrder.getUserId()).eq(CouponUser::getCouponId, couonInfo.getCouponId()));
                    if (count < couonInfo.getLimitNum()) {
                        //送买提货卡券赠送券并更新库存
                        couponService.batchBindCouponByIds(Collections.singletonList(giveCouponId), couponOrder.getUserId(), couonInfo.getShopId());
                    }
                }
            }
            return "success";
        } catch (Exception e) {
            log.error("buyCard:", e);
            return "false";
        } finally {
//            rLock.unlock();
        }
    }

    /** 获取json格式的请求参数 **/
    protected JSONObject getReqParamJSON(){
        return requestKitBean.getReqParamJSON();
    }

    @RequestMapping("/order/{payType}")
    public String payNotice() {
        JSONObject params = getReqParamJSON();
        log.info("接收支付回调参数=========={}", params);

        if (params == null || StrUtil.isBlank(params.toJSONString())) {
            return "false";
        }


        //支付状态不等于2 说明支付未成功
        if (!"2".equals(params.getString("state"))) {
            return "false";
        }

        PayInfoResultBO payInfoResultBO = new PayInfoResultBO();
        payInfoResultBO.setPayNo(params.getString("mchOrderNo"));
        payInfoResultBO.setBizPayNo(params.getString("channelOrderNo"));
        payInfoResultBO.setIsPaySuccess("2".equals(params.getString("state")));
        payInfoResultBO.setSuccessString("true");
        payInfoResultBO.setPayAmount(Arith.div(Double.parseDouble(params.getString("amount")), 100));
        payInfoResultBO.setCallbackContent(params.toJSONString());

        PayInfo payInfo = new PayInfo();
        payInfo.setPayNo(params.getString("mchOrderNo"));
        payInfo.setBizPayNo(params.getString("channelOrderNo"));
        payInfo.setBizOrderNo(params.getString("payOrderId"));
        payInfo.setCallbackTime(new Date());
        payInfo.setCallbackContent(params.toJSONString());
        // 支付宝多次回调可能该支付单已经退款，但还是更新了回调时间导致对账查询有误
        payInfoMapper.update(payInfo, new LambdaUpdateWrapper<PayInfo>()
                .eq(PayInfo::getPayNo, payInfo.getPayNo())
                .eq(PayInfo::getPayStatus, PayStatus.UNPAY.value()));

        payInfo = payInfoService.getOne(new LambdaQueryWrapper<PayInfo>().eq(PayInfo::getPayNo, params.getString("mchOrderNo")));
        // 已经支付
        if (Objects.equals(payInfo.getPayStatus(), PayStatus.PAYED.value()) || Objects.equals(payInfo.getPayStatus(), PayStatus.REFUND.value())) {
            return "success";
        }

        payInfoService.noticeOrder(payInfoResultBO, payInfo);

        try {
            log.info("支付成功，开始保存取餐码------------------------------");
            String orderNumbers = payInfo.getOrderNumbers();
            String[] split = orderNumbers.split(",");
            for (int i = 0; i < split.length; i++) {
                List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderNumber(split[i], 0);
                OrderItem orderItem = orderItems.get(0);
                ShopDetail shopDetail = shopDetailService.getById(orderItem.getShopId());
                if(StringUtils.isNotBlank(shopDetail.getStoreType()) && shopDetail.getStoreType().contains("自提")) {
                    genPickupData(split[i], orderItem.getShopId(), orderItem.getPic());
                }

                if(StringUtils.isNotBlank(shopDetail.getStoreType()) && shopDetail.getStoreType().contains("堂食")) {
                    //把餐桌状态修改回来
                    LambdaQueryWrapper<RoomsRecord> queryWrapper = new QueryWrapper<RoomsRecord>().lambda().eq(RoomsRecord::getOrderNumber, orderItem.getOrderNumber())
                            .last("LIMIT 1");
                    RoomsRecord roomsRecord = roomsRecordService.getOne(queryWrapper);
                    if(roomsRecord != null) {
                        roomsRecord.setEndTime(new Date());
                        roomsRecordService.updateById(roomsRecord);


                        Rooms rooms = new Rooms();
                        rooms.setRoomsId(roomsRecord.getRoomsId());
                        rooms.setRoomsStatus(0);
                        roomsService.updateById(rooms);
                    }
                }
            }
        } catch (Exception e) {
            log.info("保存取餐码失败：" + e.getMessage());
        }
        return "success";
    }



//    //这里需要判断店铺类型，对餐桌、取餐进行处理
//    ShopDetail shopDetail = shopDetailService.getById(item.getShopId());
//            if(shopDetail.getStoreType().contains("外送") || shopDetail.getStoreType().contains("堂食") || shopDetail.getStoreType().contains("自提")) {
//        genPickupData(item.getOrderNumber(), shopDetail.getShopId(), orderItems.get(0).getPic());
//    }

    /**
     * 生成取餐码数据和对应的餐桌状态修改
     *
     * @param orderNumber
     * @param shopId
     * @param pic
     */
    private void genPickupData(String orderNumber, Long shopId, String pic) {
        Date currentDate = new Date();
        Pickup pickup = new Pickup();
        pickup.setStatus(0);
        pickup.setCreateTime(currentDate);
        pickup.setShopId(shopId);
        pickup.setOrderNumber(orderNumber);
        pickup.setProdPic(pic);
        pickup.setPickupCode(genPickupCode(shopId));
        pickupService.save(pickup);
    }


    private String genPickupCode(Long shopId) {
        String pickupCode = "";
        QueryWrapper<Pickup> queryWrapper = new QueryWrapper<Pickup>()
                .eq("shop_id", shopId)
                .eq("DATE_FORMAT(now(),'%Y-%m-%d')", "DATE_FORMAT(create_time,'%Y-%m-%d')")
                .orderByDesc("create_time")
                .last("LIMIT 1");
        Pickup one = pickupService.getOne(queryWrapper);
        if (one == null) {
            pickupCode = "0001";
        } else {
            String pickupCodeOld = one.getPickupCode();
            int i = Integer.parseInt(pickupCodeOld);
            i += 1;
            String code = String.valueOf(i);
            if (code.length() == 1) {
                code = "000" + code;
            } else if (code.length() == 2) {
                code = "00" + code;
            } else if (code.length() == 3) {
                code = "0" + code;
            }
            pickupCode = code;
        }
        return pickupCode;
    }

    private Long checkCardQrCodeExists() {
        //生成12位随机数
        Long integer = com.yami.shop.card.common.utils.SnowflakeIdWorker.generateIdNum();
        QueryWrapper<Card> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_number", integer);
        queryWrapper.last("LIMIT 1");
        int count = cardService.count(queryWrapper);
        if (count > 0) {
            return checkCardQrCodeExists();
        }
        return integer;
    }



}
