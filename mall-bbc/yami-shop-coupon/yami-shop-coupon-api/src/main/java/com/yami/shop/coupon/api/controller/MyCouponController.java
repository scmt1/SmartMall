/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.coupon.api.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Snowflake;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.yami.shop.bean.app.dto.CouponDto;
import com.yami.shop.bean.app.dto.CouponOrderNumberDto;
import com.yami.shop.bean.app.param.PayParam;
import com.yami.shop.bean.model.CouponOrder;
import com.yami.shop.bean.model.Dict;
import com.yami.shop.bean.model.DictData;
import com.yami.shop.bean.pay.PayInfoDto;
import com.yami.shop.card.common.model.Card;
import com.yami.shop.card.common.model.CardShop;
import com.yami.shop.card.common.model.CardUser;
import com.yami.shop.card.common.service.CardService;
import com.yami.shop.card.common.service.CardShopService;
import com.yami.shop.card.common.service.CardUserService;
import com.yami.shop.common.bean.BuyCardInfo;
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.coupon.api.params.ReceiveCouponParam;
import com.yami.shop.coupon.api.util.SnowflakeIdWorker;
import com.yami.shop.coupon.common.constants.ValidTimeTypeEnum;
import com.yami.shop.coupon.common.model.Coupon;
import com.yami.shop.coupon.common.model.CouponOnly;
import com.yami.shop.coupon.common.model.CouponQrcode;
import com.yami.shop.coupon.common.model.CouponUser;
import com.yami.shop.coupon.common.service.*;
import com.yami.shop.coupon.common.utils.PolygonUtil;
import com.yami.shop.manager.impl.PayManager;
import com.yami.shop.security.api.model.YamiUser;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 我的优惠券接口
 *
 * @author lanhai
 */
@RestController
@RequestMapping("/p/myCoupon")
@Api(tags = "我的优惠券接口")
@AllArgsConstructor
public class MyCouponController {

    private CouponService couponService;
    private CouponUserService couponUserService;
    private DictService dictService;
    private DictDataService dictDataService;
    private CouponQrcodeService couponQrcodeService;
    private CouponOrderService couponOrderService;
    private final PayInfoService payInfoService;
    private final PayManager payManager;
    private final SysConfigService sysConfigService;
    @Autowired
    private Snowflake snowflake;
    private CardService cardService;
    private CardUserService cardUserService;
    private CouponOnlyService couponOnlyService;
    private CardShopService cardShopService;
    private CouponShopService couponShopService;

    /**
     * 抢券限流前缀
     */
    private final static String SECKILL_LIMIT_PREFIX = "COUPON_LIMIT_";




    @GetMapping("/listCouponIds")
    @ApiOperation(value = "查看用户拥有的所有优惠券id", notes = "查看用户拥有的所有优惠券id")
    public ServerResponseEntity<List<CouponDto>> list() {
        String userId = SecurityUtils.getUser().getUserId();
        List<CouponDto> coupons = couponService.listCouponIdsByUserId(userId);
        return ServerResponseEntity.success(coupons);
    }

    @PostMapping("/receive")
    @ApiOperation(value = "领取优惠券接口", notes = "领取优惠券接口")
    @ApiImplicitParam(name = "couponId", value = "优惠券ID", required = true, dataType = "Long")
    public ServerResponseEntity<String> receive(@Valid @RequestBody ReceiveCouponParam receiveCouponParam) {
        String userId = SecurityUtils.getUser().getUserId();
        Coupon coupon = couponService.getById(receiveCouponParam.getCouponId());
        couponService.receive(coupon, userId, receiveCouponParam.getLat(), receiveCouponParam.getLng());
        // 领取优惠券成功
        return ServerResponseEntity.success(I18nMessage.getMessage("yami.coupon.receive.success"));

    }

    @PostMapping("/checkAddress")
    @ApiOperation(value = "判断当前位置是否在配置里面", notes = "判断当前位置是否在配置里面")
    public ServerResponseEntity<String> couponAddress(@Valid @RequestBody ReceiveCouponParam receiveCouponParam) {
        //先判断位置是否在江安县范围内
        //boolean flag = checkAddress(receiveCouponParam);

//        判断是否在多边形内
        Dict dict = dictService.findByType("coupon_polygon");
        List<DictData> dictDataList = dictDataService.findByDictId(dict.getId());
        List<String> collect = dictDataList.stream().map(item -> item.getValue()).collect(Collectors.toList());

        Point2D.Double aDouble = new Point2D.Double();
        aDouble.x = receiveCouponParam.getLat();
        aDouble.y = receiveCouponParam.getLng();
        ArrayList<Point2D.Double> doubleArrayList = new ArrayList<>();
        for (String s : collect) {
            String[] split = s.split(",");
            Point2D.Double point = new Point2D.Double();
            point.x = Double.parseDouble(split[0]);
            point.y = Double.parseDouble(split[1]);
            doubleArrayList.add(point);
        }
        boolean flag = PolygonUtil.isInPolygon(aDouble, doubleArrayList);
        if (flag) {
            return ServerResponseEntity.success();
        } else {
            return ServerResponseEntity.showFailMsg("该券只能在剧荟广场定点范围内出示使用。");
        }
    }




    @GetMapping("/getMyCouponsStatusCount")
    @ApiOperation(value = "获取优惠券个数", notes = "获取各个状态下的优惠券个数")
    public ServerResponseEntity<Map<String, Long>> getCouponCountByStatus() {
        String userId = SecurityUtils.getUser().getUserId();
        Map<String, Long> couponCount = couponService.getCouponCountByStatus(userId);
        return ServerResponseEntity.success(couponCount);
    }

    @GetMapping("/getCouponList")
    @ApiOperation(value = "通过状态查看用户的优惠券列表信息", notes = "通过状态查看用户的优惠券列表信息,优惠券状态 0:已过期 1:未使用 2:使用过")
    @ApiImplicitParam(name = "status", value = "优惠券状态 0:失效 1:有效 2:使用过", required = true, dataType = "Integer")
    public ServerResponseEntity<IPage<CouponDto>> getCouponList(PageParam<CouponDto> page, @RequestParam("status") Integer status) {
        String userId = SecurityUtils.getUser().getUserId();
        IPage<CouponDto> couponDtoList = couponService.getCouponListByStatus(page, userId, status);
        return ServerResponseEntity.success(couponDtoList);
    }

    @Deprecated
    @DeleteMapping("/deleteCoupon/{couponId}")
    @ApiOperation(value = "删除用户优惠券", notes = "通过优惠券id删除用户优惠券")
    @ApiImplicitParam(name = "couponId", value = "优惠券Id", required = true, dataType = "Long")
    public ServerResponseEntity<String> deleteCoupon(@PathVariable("couponId") Long couponId) {
        String userId = SecurityUtils.getUser().getUserId();
        couponService.deleteUserCouponByCouponId(userId, couponId);
        // 删除成功
        return ServerResponseEntity.success(I18nMessage.getMessage("yami.delete.successfully"));
    }

    @DeleteMapping("/delCoupon/{couponUserId}")
    @ApiOperation(value = "删除用户优惠券", notes = "通过优惠券关联id删除用户优惠券")
    @ApiImplicitParam(name = "couponUserId", value = "优惠券关联id", required = true, dataType = "Long")
    public ServerResponseEntity<String> deleteCouponById(@PathVariable("couponUserId") Long couponUserId) {
        String userId = SecurityUtils.getUser().getUserId();
        CouponUser couponUser = new CouponUser();
        couponUser.setCouponUserId(couponUserId);
        couponUser.setIsDelete(1);
        couponUserService.update(couponUser, new LambdaUpdateWrapper<CouponUser>()
                .eq(CouponUser::getCouponUserId, couponUserId).eq(CouponUser::getUserId, userId));
        // 删除成功
        return ServerResponseEntity.success(I18nMessage.getMessage("yami.delete.successfully"));
    }

    @GetMapping("/generalCouponList")
    @ApiOperation(value = "通用券列表(平台优惠券)", notes = "获取通用券列表")
    public ServerResponseEntity<List<CouponDto>> generalCouponList(Coupon coupon) {
        String userId = SecurityUtils.getUser().getUserId();
        List<CouponDto> couponList = couponService.generalCouponList(userId,coupon);
        return ServerResponseEntity.success(couponList);
    }

//    @GetMapping("/prodCouponList")
//    @ApiOperation(value = "商品券列表", notes = "获取商品券列表(指定商品可用优惠券)")
//    public ServerResponseEntity<IPage<CouponDto>> prodCouponList(PageParam<CouponDto> page) {
//        String userId = SecurityUtils.getUser().getUserId();
//        return ServerResponseEntity.success(couponService.pageProdCoupon(page, userId));
//    }

    @GetMapping("/getCouponPage")
    @ApiOperation(value = "商品券列表(商家优惠券)", notes = "获取商品券列表(指定商品可用优惠券)")
    public ServerResponseEntity<IPage<CouponDto>> getCouponList(PageParam<CouponDto> page) {
        String userId = SecurityUtils.getUser().getUserId();
        IPage<CouponDto> couponDto = couponService.getCouponList(page, userId);
        return ServerResponseEntity.success(couponDto);
    }

    @GetMapping("/getCouponByCouponUserId")
    @ApiOperation(value = "根据用户优惠券id获取信息")
    @ApiImplicitParam(name = "couponUserId", value = "用户优惠券id", dataType = "Long")
    public ServerResponseEntity<CouponDto> info(@RequestParam(value = "couponUserId") Long couponUserId) {
        CouponDto couponUser = couponService.getCouponUserInfo(couponUserId);
        return ServerResponseEntity.success(couponUser);
    }


    @GetMapping("/genCouponUserQrcode")
    @ApiOperation(value = "生成对应的用户优惠券条码")
    @ApiImplicitParam(name = "couponUserId", value = "用户优惠券id", dataType = "Long")
    public ServerResponseEntity<CouponQrcode> genCouponUserQrcode(@RequestParam(value = "couponUserId") Long couponUserId) {
        CouponQrcode one = couponQrcodeService.getOne(new LambdaQueryWrapper<CouponQrcode>().eq(CouponQrcode::getCouponUserId, couponUserId).last("LIMIT 1"));
        if (one == null) {
            Integer integer = checkQrCodeExists();
            one = new CouponQrcode();
            one.setQrCode(integer);
            one.setCouponUserId(couponUserId);
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());//设置起时间
//            cal.add(Calendar.MINUTE, 5);
            String time = sysConfigService.getConfigValue("COUPON_CODE_EXPIRE_TIME");
            cal.add(Calendar.SECOND, Integer.parseInt(time));
            one.setExpiredTime(cal.getTime()); //设置过期时间
            couponQrcodeService.save(one);
        }else{
            Integer integer = checkQrCodeExists();
            one.setQrCode(integer);
            one.setCouponUserId(couponUserId);
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());//设置起时间
            String time = sysConfigService.getConfigValue("COUPON_CODE_EXPIRE_TIME");
            cal.add(Calendar.SECOND, Integer.parseInt(time));
            one.setExpiredTime(cal.getTime()); //设置过期时间
            couponQrcodeService.updateById(one);
        }

//        if (new Date().getTime() > one.getExpiredTime().getTime()) {
//            //过期了。重新生成
//            Integer integer = checkQrCodeExists();
//            one.setQrCode(integer);
//            one.setCouponUserId(couponUserId);
//            Calendar cal = Calendar.getInstance();
//            cal.setTime(new Date());//设置起时间
//            String time = sysConfigService.getConfigValue("COUPON_CODE_EXPIRE_TIME");
//            cal.add(Calendar.SECOND, Integer.parseInt(time));
//            one.setExpiredTime(cal.getTime()); //设置过期时间
//            couponQrcodeService.updateById(one);
//        }
        return ServerResponseEntity.success(one);
    }

    private Integer checkQrCodeExists() {
        //生成7位随机数
        Integer integer = SnowflakeIdWorker.generateIdNum();
        QueryWrapper<CouponQrcode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("qr_code", integer);
        queryWrapper.last("LIMIT 1");
        CouponQrcode one = couponQrcodeService.getOne(queryWrapper);
        if (one != null) {
            return checkQrCodeExists();
        }
        return integer;
    }

    @GetMapping("/queryCouponUserDetail")
    @ApiOperation(value = "查询优惠券信息", notes = "查询优惠券信息")
    public ServerResponseEntity<Object> queryCouponUserDetail(Long couponId, Long couponUserId) {
        String userId = SecurityUtils.getUser().getUserId();
        List<CouponDto> couponUser = couponService.queryCouponUserDetail(couponId, userId,couponUserId);
        return ServerResponseEntity.success(couponUser);
    }

    @PostMapping("/couponOrderSubmit")
    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "提交订单，返回支付流水号")
    public ServerResponseEntity<CouponOrderNumberDto> submitCouponOrder(@Valid @RequestBody CouponOrder couponOrder) {
        String userId = SecurityUtils.getUser().getUserId();

        //优惠券信息
        Coupon coupon = couponService.getById(couponOrder.getCouponId());
        Boolean isBuyCard = false;
        if(coupon.getCouponType() == 4){//是提货卡/券
            isBuyCard = true;
        }
        List<CouponOnly> groupList = couponOnlyService.queryGroupListByCouponId(couponOrder.getCouponId());
        //如果groupList==0 说明购买的优惠券不在分组里面，不管 ，否则需要判断已购买的优惠券里面是否包含了分组里面的
        if (groupList != null && groupList.size() > 0) {
            List<Long> couponIdList = null;
            if(isBuyCard){//是提货卡券
                List<CardUser> list = cardUserService.list(new LambdaQueryWrapper<CardUser>()
                        .eq(CardUser::getUserId,userId)
                        .eq(CardUser::getIsDelete,0)
                        .ne(CardUser::getCouponId,"null")
                        .select(CardUser::getCouponId)
                );
                couponIdList = list.stream().map(item -> item.getCouponId()).collect(Collectors.toList());
            }else{//不是提货卡券
                //查询当前用户已领取的优惠券
                List<CouponUser> list = couponUserService.list(new LambdaQueryWrapper<CouponUser>()
                        .eq(CouponUser::getUserId, userId)
                        .eq(CouponUser::getIsDelete, 0)
                        .select(CouponUser::getCouponId)
                );
                couponIdList = list.stream().map(item -> item.getCouponId()).collect(Collectors.toList());
            }
            //判断已购买的优惠券里面是否有存在于分组里面的优惠券并且当前购买的券id不在已购买券id数组里面(可能存在购买多张)，有则提示不能重复领取
            boolean isContains = false;
            for (CouponOnly couponOnly : groupList) {
                for (Long aLong : couponIdList) {
                    if (Objects.equals(aLong, couponOnly.getCouponId()) && !couponOrder.getCouponId().equals(aLong)) {
                        isContains = true;
                        break;
                    }
                }
                if (isContains) {
                    break;
                }
            }
            if (isContains) {
                throw new YamiShopBindException("您已经购买过该类型的优惠券，无法继续购买！");
            }
        }
        // 当优惠券状态不为投放时
        if (coupon.getOverdueStatus() == 0 || coupon.getPutonStatus() != 1 || coupon.getStocks() == 0) {
            // 该券无法被领取或者该券领完了!
            throw new YamiShopBindException("该券无法被购买或者该券已抢完了!");
        }
        if(isBuyCard){//是提货卡券
            QueryWrapper<CardUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(CardUser::getUserId,userId);
            queryWrapper.lambda().eq(CardUser::getCouponId,couponOrder.getCouponId());
            List<CardUser> list = cardUserService.list(queryWrapper);
            if (list.size() >= coupon.getLimitNum()) {
                // 该券已达个人领取上限，无法继续领取！
                throw new YamiShopBindException("该券已达个人购买上限，无法继续购买!");
            }
        }else{//不是提货卡券
            //该券用户购买数量
            int count = couponUserService.count(new LambdaQueryWrapper<CouponUser>().eq(CouponUser::getUserId, userId).eq(CouponUser::getCouponId, coupon.getCouponId()));
            if (count >= coupon.getLimitNum()) {
                // 该券已达个人领取上限，无法继续领取！
                throw new YamiShopBindException("该券已达个人购买上限，无法继续购买!");
            }
        }
        // cancelTime 取消时间处理
        Date cancelTime = null;
        cancelTime = DateUtil.offsetMinute(new Date(), 5);
        couponOrder.setCancelTime(cancelTime);
        // 使用雪花算法生成的订单号
        String orderNumber = String.valueOf(snowflake.nextId());
        couponOrder.setCouponOrderNumber(orderNumber);
        couponOrder.setUserId(userId);
        couponOrder.setCreateTime(new Date());
        couponOrder.setUpdateTime(new Date());
        couponOrder.setIsPayed(0);
        couponOrder.setStatus(1);
        couponOrder.setIsRefund(1);
        couponOrderService.save(couponOrder);
        //更新团券库存
        couponService.updateCouponStocksAndVersion(couponOrder.getCouponId());
        return ServerResponseEntity.success(new CouponOrderNumberDto(couponOrder.getCouponOrderNumber()));
    }

    /**
     * 团购券购买接口
     */
    @PostMapping("/groupCouponPay")
    @ApiOperation(value = "团购券购买", notes = "团购券购买")
    @SneakyThrows
    @Transactional
    public ServerResponseEntity<?> groupCouponPay(HttpServletResponse httpResponse, @Valid @RequestBody PayParam payParam){
        YamiUser user = SecurityUtils.getUser();
        String userId = user.getUserId();
        if (!user.getEnabled()) {
            // 您已被禁用，不能购买，请联系平台客服
            throw new YamiShopBindException("yami.order.pay.user.disable");
        }
        //团购券订单信息
        CouponOrder couponOrder = couponOrderService.getCouponOrderByOrderNumber(payParam.getOrderNumbers());
        //获取优惠券信息
        Coupon couponInfo = couponService.getById(couponOrder.getCouponId());

        Boolean isBuyCard = false;
        if(couponInfo.getCouponType() == 4){
            payParam.setCardName(couponInfo.getCardName());
            payParam.setCardBalance(couponInfo.getReduceAmount());
            payParam.setGiveCouponId(couponInfo.getGiveCouponId());
            isBuyCard = true;
        }
        PayInfoDto payInfo = payInfoService.couponPay(userId, payParam);
        if(isBuyCard){//是提货卡券
            payInfo.setApiNoticeUrl("/notice/pay/cardPay/" + payParam.getPayType() + "?cardBalance=" + payParam.getCardBalance()
                    + "&cardName=" + payParam.getCardName() + "&giveCouponId=" + payParam.getGiveCouponId());
            if(couponOrder.getShopId() == 0){
                String shopId = sysConfigService.getConfigValue("BUY_CARD_SHOP");
                payInfo.setShopId(Long.valueOf(shopId));
            }else{
                payInfo.setShopId(couponOrder.getShopId());
            }
        }else{//不是提货卡券
            payInfo.setApiNoticeUrl("/notice/pay/couponPay/" + payParam.getPayType());
            if(couponOrder.getShopId() == 0){
                String shopId = sysConfigService.getConfigValue("PT_GROUP_COUPON_SHOP");
                payInfo.setShopId(Long.valueOf(shopId));
            }else{
                payInfo.setShopId(couponOrder.getShopId());
            }
        }

        payInfo.setBizUserId(user.getBizUserId());
        payInfo.setPayType(payParam.getPayType());
        payInfo.setReturnUrl(payParam.getReturnUrl());
        ServerResponseEntity<?> responseEntity = payManager.doPay(httpResponse, payInfo);
        return responseEntity;
    }

    private Long checkCardQrCodeExists() {
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
