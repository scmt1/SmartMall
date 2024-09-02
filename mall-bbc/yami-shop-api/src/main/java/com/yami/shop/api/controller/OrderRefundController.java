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

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.app.dto.ApiOrderRefundDto;
import com.yami.shop.bean.app.dto.UpdateRefundDto;
import com.yami.shop.bean.app.param.OrderRefundExpressParam;
import com.yami.shop.bean.app.param.OrderRefundParam;
import com.yami.shop.bean.dto.OrderRefundDto;
import com.yami.shop.bean.enums.*;
import com.yami.shop.bean.model.*;
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.Arith;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author LGH
 */
@Slf4j
@RestController
@RequestMapping("/p/orderRefund")
@Api(tags = "订单退款接口")
public class OrderRefundController {

    @Autowired
    private OrderRefundService orderRefundService;
    @Autowired
    private OrderVirtualInfoService orderVirtualInfoService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private MapperFacade mapperFacade;
    @Autowired
    private Snowflake snowflake;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private RefundDeliveryService refundDeliveryService;

    @PostMapping("/apply")
    @ApiOperation(value = "申请退款", notes = "申请退款")
    public ServerResponseEntity<String> apply(@Valid @RequestBody OrderRefundParam orderRefundParam) {
        String userId = SecurityUtils.getUser().getUserId();
        // 获取订单信息
        Order order = orderService.getOrderByOrderNumberAndUserId(orderRefundParam.getOrderNumber(), userId, true);
        if (Objects.equals(order.getIsRefund(), 0)) {
            // 当前订单不能退款，请联系客服
            throw new YamiShopBindException("yami.order.virtual.refund.check1");
        }
        if (orderRefundParam.getRefundAmount() < 0.0) {
            // 退款金额有误，请重新输入
            throw new YamiShopBindException("yami.refund.amount.check1");
        }
        boolean isOrderStatus = Objects.equals(order.getStatus(), OrderStatus.CONSIGNMENT.value())
                || Objects.equals(order.getStatus(), OrderStatus.SUCCESS.value());

        checkAndGetOrderStatus(orderRefundParam, order,isOrderStatus);
        if(Objects.equals(order.getOrderMold(),1)) {
            // 虚拟商品订单相关退款校验
            checkVirtualOrder(order, orderRefundParam);
        }
        // 生成退款单信息
        OrderRefund newOrderRefund = new OrderRefund();

        // 获取所有正在进行中的退款订单
        List<OrderRefund> orderRefunds = orderRefundService.getProcessingOrderRefundByOrderId(order.getOrderId());

        for (OrderRefund orderRefund : orderRefunds) {
            if (Objects.equals(RefundType.ALL.value(), orderRefund.getRefundType())) {
                // 该订单正在进行整单退款，无法进行新的退款操作
                throw new YamiShopBindException("yami.order.all.refund.check");
            }
            if (Objects.equals(orderRefund.getOrderItemId(), orderRefundParam.getOrderItemId())) {
                // 该商品正在进行退款中，无法进行新的退款操作
                throw new YamiShopBindException("yami.order.refund.go.on");
            }
        }

        // 如果存在分销订单，则计算分销总金额
        List<OrderItem> orderItemList = orderItemService.getUnGiveawayOrderItemsByOrderNumber(order.getOrderNumber(), I18nMessage.getDbLang());
        // 获取所有的订单项总数
        int orderItemCount = orderItemService.count(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderNumber, order.getOrderNumber())
                .isNull(OrderItem::getGiveawayOrderItemId));
        // 判断退款单类型（1:整单退款,2:单个物品退款）
        if (orderRefundParam.getRefundType().equals(RefundType.ALL.value())) {
            // 全部物品退款
            // 计算该订单项的分销金额
            newOrderRefund.setDistributionTotalAmount(orderService.sumTotalDistributionAmountByOrderItem(orderItemList));
            // 计算平台退款金额（退款时将这部分钱退回给平台，所以商家要扣除从平台这里获取的金额）
            newOrderRefund.setPlatformRefundAmount(order.getPlatformAmount());
            newOrderRefund.setPlatformRefundCommission(order.getPlatformCommission());
        } else {
            // 单个订单退款
            singleRefund(orderRefundParam, order, isOrderStatus, newOrderRefund, orderRefunds, orderItemList, orderItemCount);
        }

        newOrderRefund.setShopId(order.getShopId());
        newOrderRefund.setUserId(order.getUserId());
        newOrderRefund.setOrderId(order.getOrderId());
        newOrderRefund.setRefundSn(String.valueOf(snowflake.nextId()));
        newOrderRefund.setRefundType(orderRefundParam.getRefundType());

        newOrderRefund.setRefundAmount(orderRefundParam.getRefundAmount());
        if (Objects.equals(RefundType.ALL.value(), orderRefundParam.getRefundType())) {
            newOrderRefund.setOrderItemId(0L);
            // 如果是整单退款，则最大退款金额就为退款金额
            newOrderRefund.setMaxRefundAmount(newOrderRefund.getRefundAmount());
            newOrderRefund.setRefundActualTotal(order.getActualTotal());
        } else {
            newOrderRefund.setOrderItemId(orderRefundParam.getOrderItemId());
        }
        newOrderRefund.setGoodsNum(orderRefundParam.getGoodsNum());
        newOrderRefund.setApplyType(orderRefundParam.getApplyType());
        if (Objects.equals(orderRefundParam.getApplyType(), RefundApplyType.RETURN_REFUND.value())) {
            newOrderRefund.setIsReceiver(true);
        } else {
            newOrderRefund.setIsReceiver(orderRefundParam.getIsReceiver());
        }
        newOrderRefund.setBuyerReason(orderRefundParam.getBuyerReason());
        newOrderRefund.setBuyerDesc(orderRefundParam.getBuyerDesc());
        newOrderRefund.setBuyerMobile(orderRefundParam.getBuyerMobile());
        newOrderRefund.setPhotoFiles(orderRefundParam.getPhotoFiles());
        newOrderRefund.setReturnMoneySts(ReturnMoneyStsType.APPLY.value());
        newOrderRefund.setApplyTime(new Date());
        newOrderRefund.setUpdateTime(new Date());
        orderRefundService.applyRefund(newOrderRefund);

        return ServerResponseEntity.success(newOrderRefund.getRefundSn());
    }

    private void checkVirtualOrder(Order order, OrderRefundParam orderRefundParam) {
        if (Objects.equals(orderRefundParam.getApplyType(), RefundApplyType.RETURN_REFUND.value())) {
            // 虚拟商品订单不能退货退款
            throw new YamiShopBindException("yami.order.virtual.refund.check2");
        }

        if (Objects.equals(order.getWriteOffNum(), 0)) {
            return;
        }

        // 如果是长期核销且没有核销次数限制的，直接return
        if (Objects.isNull(order.getWriteOffEnd()) && Objects.equals(order.getWriteOffNum(), -1) && Objects.equals(order.getWriteOffMultipleCount(), -1)) {
            return;
        }

        // 1.如果是单次核销、多次核销的虚拟商品订单,核销时间没过期且还有待核销的券码才能退款,核销结束时间为空表示长期有效
        if (Objects.nonNull(order.getWriteOffEnd()) && DateUtil.compare(order.getWriteOffEnd(), new Date()) < 0) {
            // 虚拟商品核销时间已过期不能申请退款，请联系客服
            throw new YamiShopBindException("yami.order.virtual.refund.check3");
        }

        // 2.查询出核销信息
        List<OrderVirtualInfo> virtualInfoList = orderVirtualInfoService.list(new LambdaQueryWrapper<OrderVirtualInfo>()
                .eq(OrderVirtualInfo::getOrderNumber, order.getOrderNumber())
                .eq(OrderVirtualInfo::getIsWriteOff, 0));
        if (CollectionUtil.isEmpty(virtualInfoList)) {
            // 当前订单已经全部核销完成不能申请退款，请联系客服
            throw new YamiShopBindException("yami.order.virtual.refund.check4");
        }
        // 3.还需计算出已经核销过的商品数据对数量进行判断
        int goodsNum = orderRefundParam.getGoodsNum() == 0 ? order.getProductNums() : orderRefundParam.getGoodsNum();
        if (goodsNum > virtualInfoList.size()) {
            // 当前订单可以退款的数量超出，请重新输入
            throw new YamiShopBindException("yami.order.virtual.refund.check5");
        }
    }

    private void checkAndGetOrderStatus(OrderRefundParam orderRefundParam, Order order, boolean isOrderStatus) {
        if (!Objects.equals(order.getIsPayed(), 1)) {
            // 当前订单还未付款，无法申请
            throw new YamiShopBindException("yami.order.not.paid");
        }

        if (Objects.equals(order.getStatus(), OrderStatus.CLOSE.value())) {
            // 当前订单已失败，不允许退款
            throw new YamiShopBindException("yami.order.has.failed");
        }

        if (Objects.equals(order.getStatus(), OrderStatus.WAIT_GROUP.value())) {
            // 当前订单正在等待成团状态，需等待成团才能进行下一步操作
            throw new YamiShopBindException("yami.group.user.join.check");
        }

        if (orderRefundParam.getRefundAmount() > order.getActualTotal()) {
            // 退款金额已超出订单金额，无法申请
            throw new YamiShopBindException("yami.refund.exceeded.amount");
        }
        //待收货或者确认收货 -> 整单退款 -> 退款金额 < 订单金额 - 运费金额
        if (isOrderStatus && orderRefundParam.getRefundType().equals(RefundType.ALL.value())) {
            // 订单可退款金额 = 订单实际金额 - （订单运费金额 - 平台免运费金额）
            Double refundAmount = Arith.sub(order.getActualTotal(), Arith.sub(order.getFreightAmount(), order.getPlatformFreeFreightAmount()));
            if (orderRefundParam.getRefundAmount() > refundAmount) {
                // 退款金额已超出订单金额，无法申请
                throw new YamiShopBindException("yami.refund.exceeded.amount");
            }
        }

        if (!orderRefundService.checkRefundDate(order)) {
            // 该商品已经确认收货超过x天
            String dayMsg = I18nMessage.getMessage("yami.order.refund.day.check");
            String dayMsg2 = I18nMessage.getMessage("yami.order.refund.day.check2");
            throw new YamiShopBindException(dayMsg + Constant.MAX_FINALLY_REFUND_TIME + dayMsg2);
        }
        if (Objects.equals(order.getOrderType(), Constant.ORDER_TYPE_SCORE)) {
            // 积分商品，无法退款
            throw new YamiShopBindException("yami.order.refund.score");
        }
        boolean inRefund = !Objects.isNull(order.getRefundStatus())
                && !Objects.equals(order.getRefundStatus(), RefundStatusEnum.DISAGREE.value());
        if (Objects.equals(orderRefundParam.getRefundType(), RefundType.ALL.value())
                && inRefund) {
            // 该订单已有商品正在退款中，不能再进行整单退款
            throw new YamiShopBindException("yami.order.refund.go.on");
        }
    }

    private void singleRefund(OrderRefundParam orderRefundParam, Order order, boolean isOrderStatus, OrderRefund newOrderRefund, List<OrderRefund> orderRefunds, List<OrderItem> orderItemList, int orderItemCount) {
        // 部分物品退款
        OrderItem orderItem = orderItemService.getOne(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderItemId, orderRefundParam.getOrderItemId())
                .eq(OrderItem::getOrderNumber, orderRefundParam.getOrderNumber()));
        if (orderItem == null) {
            // 该物品在订单中不存在
            throw new YamiShopBindException("yami.item.not.exist");
        }

        boolean isCanRefund = false;
        //  查看是否有支付金额和积分都为空的订单，有则抛出异常
        for (OrderItem item : orderItemList) {
            if (item.getActualTotal() <= 0.0 && item.getUseScore() <= 0.0) {
                isCanRefund = true;
                break;
            }
        }
        if (isCanRefund) {
            // 该订单部分订单项支付金额和积分为0，无法使用部分退款
            throw new YamiShopBindException("yami.refund.amount.check");
        }

//        // 是否为最后一项
//        boolean isEndItem = Objects.equals(orderRefunds.size(), orderItemCount - 1);
        // 如果为最后一项并且不为确认收货或待收货状态，则进行加上运费判断是不是为部分退款
        double itemActualTotal = orderItem.getActualTotal();
        double giveawayRefundAmount = 0.0;
        StringBuilder returnGiveawayIds = new StringBuilder();
        // 赠品处理
        giveawayRefundAmount = handleGiveawayProd(orderRefundParam, newOrderRefund, orderItem, giveawayRefundAmount, returnGiveawayIds);
        // 最多可以退的实付金额需要减去不退回的赠品退款金额,最小值应该为0.01，理论上赠品价值不可能大于商品
        itemActualTotal = Arith.sub(itemActualTotal,giveawayRefundAmount);
//        // 平台运费减免金额等于0的时候才可以把运费算入退款金额
//        if (isEndItem && !isOrderStatus && order.getPlatformFreeFreightAmount() <= 0) {
//            itemActualTotal = Arith.add(itemActualTotal, order.getFreightAmount());
//        }
//        if (order.getPlatformAmount() > 0 && !Arith.isEquals(orderRefundParam.getRefundAmount(), itemActualTotal)) {
//            // 该订单有使用平台优惠，无法使用部分退款
//            throw new YamiShopBindException("yami.refund.platform.amount.check");
//        }
        // 计算平台退款金额（退款时将这部分钱退回给平台，所以商家要扣除从平台这里获取的金额）
        double changePlatformAmount;
        double changePlatformCommission;
        if (orderRefundParam.getRefundAmount() != itemActualTotal && orderRefundParam.getRefundAmount() != 0.0) {
            // 平台退款金额 = 平台优惠金额 *（退款金额 / 实付金额）
            changePlatformAmount = Arith.div(Arith.mul(orderItem.getPlatformShareReduce(), orderRefundParam.getRefundAmount()), itemActualTotal);
            // 平台佣金应退 = 平台佣金 *（退款金额 / 实付金额）
            changePlatformCommission = Arith.div(Arith.mul(orderItem.getPlatformCommission(), orderRefundParam.getRefundAmount()), itemActualTotal);
        } else {
            changePlatformAmount = orderItem.getPlatformShareReduce();
            changePlatformCommission = orderItem.getPlatformCommission();
        }

        //此处设置实际平台抵现金额
        newOrderRefund.setPlatformRefundAmount(changePlatformAmount);

        newOrderRefund.setPlatformRefundCommission(changePlatformCommission);


        // 退款物品数量为null或者0时，则为退款全部数量
        if (orderRefundParam.getGoodsNum() <= 0) {
            orderRefundParam.setGoodsNum(orderItem.getProdCount());
        }

        // 判断退款数量是否溢出
        if (orderRefundParam.getGoodsNum() > orderItem.getProdCount()) {
            // 退款物品数量已超出订单中的数量，不允许申请
            throw new YamiShopBindException("yami.refund.num.check");
        }

        // 判断退款金额是否超出订单金额两种情况
        // 计算单件退款金额有无超出时，用户输入的单件退款金额 = （用户输入的退款金额 + 赠品金额）/ 件数 - 赠品金额
        double refundSingleAmount = Arith.div(Arith.add(orderRefundParam.getRefundAmount(),giveawayRefundAmount), orderRefundParam.getGoodsNum(), 3);
        refundSingleAmount = Arith.sub(refundSingleAmount,giveawayRefundAmount);

        // 单件退款金额
        double singleAmount = Arith.div(orderItem.getActualTotal(), orderItem.getProdCount(), 3);

        // 当前最大可退款金额为 当前退款数量 * singleAmount(单个最大退款金额) - 赠品金额,并且不能超过订单项的实付金额
        double maxRefundAmount = Arith.sub(Arith.mul(singleAmount,orderRefundParam.getGoodsNum()) , giveawayRefundAmount);
        maxRefundAmount = Math.min(maxRefundAmount, orderItem.getActualTotal());

        // 单件最大退款金额应为 订单项实付金额  / 件数 - 赠品金额，前端没限制时可能为负数
        double singleMaxAmount = Arith.sub(singleAmount,giveawayRefundAmount);

        // 商品最大退款金额应为 订单项实付金额 - 赠品金额
        double productTotalAmount = Arith.sub(orderItem.getProductTotalAmount(),giveawayRefundAmount);
//        // 1.如果是此笔订单最后一件并且不为确认收货或待收货状态，则订单项加上运费进行判断。(已不需要，现在前端退款金额不加上运费)
//        if (isEndItem && !isOrderStatus && order.getPlatformFreeFreightAmount() <= 0) {
//            productTotalAmount = Arith.add(orderItem.getProductTotalAmount(), order.getFreightAmount());
//            singleMaxAmount = Arith.add(singleMaxAmount, order.getFreightAmount());
//            maxRefundAmount =  Arith.add(maxRefundAmount, order.getFreightAmount());
//        }
        newOrderRefund.setDistributionTotalAmount(orderService.sumTotalDistributionAmountByOrderItem(Collections.singletonList(orderItem)));
        newOrderRefund.setMaxRefundAmount(maxRefundAmount);
        newOrderRefund.setRefundActualTotal(itemActualTotal);

        // 2.如果不是直接跟订单项进行判断
        if (refundSingleAmount > productTotalAmount || refundSingleAmount > singleMaxAmount) {
            // 退款金额已超出订单金额，无法申请
            throw new YamiShopBindException("yami.refund.exceeded.amount");
        }
        // 当前退款金额  +  已申请退款金额 > 订单实际支付总额， 就不能退款了
        double refundAmount = orderRefundParam.getRefundAmount();
        // 已退款总金额
        double alreadyRefundTotal = orderRefunds.stream().mapToDouble(OrderRefund::getRefundAmount).sum();
        if (Arith.add(refundAmount, alreadyRefundTotal) > order.getActualTotal()) {
            // 退款金额已超出订单金额，无法申请
            throw new YamiShopBindException("yami.refund.exceeded.amount");
        }
        // 一个订单项只能申请一次退款
        for (OrderRefund orderRefund : orderRefunds) {
            if (Objects.equals(orderRefund.getOrderId(), orderItem.getOrderItemId())) {
                // 退款订单项已处理，请勿重复申请
                throw new YamiShopBindException("yami.order.processed");
            }
        }
    }

    private double handleGiveawayProd(OrderRefundParam orderRefundParam, OrderRefund newOrderRefund, OrderItem orderItem, double giveawayRefundAmount, StringBuilder returnGiveawayIds) {
        if(Objects.equals(orderItem.getIsGiveaway(),1)){
            boolean isReturnGiveaway = true;
            List<OrderItem> giveawayItemList = orderItemService.list(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getGiveawayOrderItemId, orderItem.getOrderItemId()));
            List<Long> giveawayItemIds = orderRefundParam.getGiveawayItemIds();
            for (OrderItem giveawayItem : giveawayItemList) {
                if(giveawayItemIds.contains(giveawayItem.getOrderItemId())){
                    returnGiveawayIds.append(giveawayItem.getOrderItemId()).append(StrUtil.COMMA);
                    continue;
                }
                isReturnGiveaway = false;
                // 不退回判断金额
                giveawayRefundAmount = Arith.add(giveawayRefundAmount,giveawayItem.getGiveawayAmount());
            }
            // 如果是全部退回则不需要判断退款金额
            if(isReturnGiveaway){
                giveawayRefundAmount = 0.0;
            }
        }
        if(returnGiveawayIds.length() > 0){
            returnGiveawayIds.deleteCharAt(returnGiveawayIds.length() - 1);
            newOrderRefund.setReturnGiveawayIds(returnGiveawayIds.toString());
        }else{
            newOrderRefund.setReturnGiveawayIds(null);
        }
        return giveawayRefundAmount;
    }

    @PostMapping("/submitExpress")
    @ApiOperation(value = "提交退款订单物流填写信息")
    public ServerResponseEntity<String> submitExpress(@Valid @RequestBody OrderRefundExpressParam orderRefundExpressParam) {
        OrderRefund orderRefund = orderRefundService.getOrderRefundByRefundSn(orderRefundExpressParam.getRefundSn());
        if (Objects.isNull(orderRefund)) {
            // 查询不到退款信息
            throw new YamiShopBindException("yami.cannot.find");
        }

        String userId = SecurityUtils.getUser().getUserId();
        if (!Objects.equals(orderRefund.getUserId(), userId)) {
            // 申请失败，您没有该权限
            throw new YamiShopBindException("yami.not.permission");
        }

        if (!Objects.equals(orderRefund.getApplyType(), RefundApplyType.RETURN_REFUND.value())) {
            // 当前申请类型不允许提交物流信息操作
            throw new YamiShopBindException("yami.not.submission");
        }

        if (!Objects.equals(orderRefund.getReturnMoneySts(), ReturnMoneyStsType.PROCESSING.value())) {
            // 当前状态不允许提交物流信息操作
            throw new YamiShopBindException("yami.not.submission.delivery");
        }

        // 填写物流信息
        RefundDelivery refundDelivery = refundDeliveryService.getOne(new LambdaQueryWrapper<RefundDelivery>()
                .eq(RefundDelivery::getRefundSn, orderRefundExpressParam.getRefundSn()));
        refundDelivery.setSenderMobile(orderRefundExpressParam.getMobile());
        refundDelivery.setSenderRemarks(orderRefundExpressParam.getSenderRemarks());
        refundDelivery.setDeyId(orderRefundExpressParam.getExpressId());
        refundDelivery.setDeyName(orderRefundExpressParam.getExpressName());
        refundDelivery.setDeyNu(orderRefundExpressParam.getExpressNo());
        refundDelivery.setImgs(orderRefundExpressParam.getImgs());
        refundDelivery.setCreateTime(new Date());

        // 更新退款单信息
        orderRefund.setReturnMoneySts(ReturnMoneyStsType.CONSIGNMENT.value());
        orderRefund.setShipTime(new Date());
        orderRefund.setUpdateTime(new Date());

        orderRefundService.submitExpress(orderRefund, refundDelivery);
        // 提交成功
        return ServerResponseEntity.success(I18nMessage.getMessage("yami.submitted.successfully"));
    }

    @PutMapping("/reSubmitExpress")
    @ApiOperation(value = "修改退款订单物流填写信息", notes = "修改物流公司信息")
    public ServerResponseEntity<String> updateExpress(@Valid @RequestBody OrderRefundExpressParam orderRefundExpressParam) {
        String userId = SecurityUtils.getUser().getUserId();
        OrderRefund orderRefund = orderRefundService.getOrderRefundByRefundSn(orderRefundExpressParam.getRefundSn());
        if (Objects.isNull(orderRefund)) {
            // 查询不到退款信息
            throw new YamiShopBindException("yami.cannot.find");
        }

        if (!Objects.equals(orderRefund.getUserId(), userId)) {
            // 申请失败，您没有此权限
            throw new YamiShopBindException("yami.not.permission");
        }

        if (!Objects.equals(orderRefund.getReturnMoneySts(), ReturnMoneyStsType.CONSIGNMENT.value())) {
            // 当前状态不允许更新物流信息操作
            throw new YamiShopBindException("yami.not.update.delivery");
        }

        if (StrUtil.isNotBlank(orderRefundExpressParam.getImgs()) && orderRefundExpressParam.getImgs().length() > Constant.MAX_MYSQL_STRING_LENGTH) {
            // imgs数据错误：长度大于255
            throw new YamiShopBindException("yami.imgs.length.then.max");
        }

        // 填写物流信息
        RefundDelivery refundDelivery = refundDeliveryService.getOne(new LambdaQueryWrapper<RefundDelivery>()
                .eq(RefundDelivery::getRefundSn, orderRefundExpressParam.getRefundSn()));
        refundDelivery.setSenderMobile(orderRefundExpressParam.getMobile());
        refundDelivery.setDeyId(orderRefundExpressParam.getExpressId());
        refundDelivery.setSenderRemarks(orderRefundExpressParam.getSenderRemarks());
        refundDelivery.setDeyNu(orderRefundExpressParam.getExpressNo());
        refundDelivery.setImgs(orderRefundExpressParam.getImgs());
        refundDelivery.setReceiverMobile(orderRefundExpressParam.getMobile());
        refundDelivery.setDeyName(orderRefundExpressParam.getExpressName());

        // 更新退款单信息
        orderRefund.setUpdateTime(new Date());

        orderRefundService.submitExpress(orderRefund, refundDelivery);
        // 修改成功
        return ServerResponseEntity.success(I18nMessage.getMessage("yami.activity.update.success"));
    }

    @PutMapping("/cancel")
    @ApiOperation(value = "撤销退货退款申请")
    public ServerResponseEntity<String> cancel(@RequestBody String refundSn) {
        OrderRefundDto orderRefund = orderRefundService.getOrderRefundByRefundSn(refundSn);
        if (Objects.isNull(orderRefund)) {
            // 撤销失败 退款订单不存在
            throw new YamiShopBindException("yami.revocation.failed");
        }
        if (Objects.equals(orderRefund.getReturnMoneySts(), ReturnMoneyStsType.PROCESSING.value())) {
            // 卖家正在处理退款，不能撤销退款申请
            throw new YamiShopBindException("yami.shop.processing.check");
        }
        if (Objects.equals(orderRefund.getReturnMoneySts(), ReturnMoneyStsType.CONSIGNMENT.value())) {
            // 买家已发货，不能撤销退款申请
            throw new YamiShopBindException("yami.user.delivery.check");
        }
        if (Objects.equals(orderRefund.getReturnMoneySts(), ReturnMoneyStsType.RECEIVE.value())) {
            // 卖家已收货，不能撤销退款申请
            throw new YamiShopBindException("yami.user.receipt.check");
        }
        if (Objects.equals(orderRefund.getReturnMoneySts(), ReturnMoneyStsType.SUCCESS.value())) {
            throw new YamiShopBindException("yami.refund.success.check");
        }
        String userId = SecurityUtils.getUser().getUserId();

        // 查看订单是否还有处于处理中的退款单，如果没有则修改订单退款状态为关闭状态
        Order order = orderService.getOrderByOrderNumberAndUserId(orderRefund.getOrderNumber(), userId, true);

        //如果订单状态为待发货、包含运费、单个商品退款，且所有订单项都进行退款，则不能再取消退款（取消退款后再退款会导致重复退运费bug）
        if (Objects.equals(order.getStatus(), OrderStatus.PADYED.value()) && Objects.equals(orderRefund.getRefundType(), RefundType.SINGLE.value()) && order.getFreightAmount() > 0) {
            // 退款数量
            int refundCount = orderRefundService.count(new LambdaQueryWrapper<OrderRefund>()
                    .gt(OrderRefund::getReturnMoneySts, 1)
                    .lt(OrderRefund::getReturnMoneySts, 6)
                    .eq(OrderRefund::getOrderId, order.getOrderId())
            );
            // 订单项数量
            int orderItemCount = orderItemService.count(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderNumber, order.getOrderNumber()));
            if (refundCount == orderItemCount) {
                // 该订单所有商品都进行退款，已无法取消退款
                throw new YamiShopBindException("yami.refund.finish.check");
            }
        }

        if (Objects.equals(orderRefund.getReturnMoneySts(), ReturnMoneyStsType.SUCCESS.value()) ||
                Objects.equals(orderRefund.getReturnMoneySts(), ReturnMoneyStsType.FAIL.value()) ||
                Objects.equals(orderRefund.getReturnMoneySts(), ReturnMoneyStsType.REJECT.value()) ||
                Objects.equals(orderRefund.getReturnMoneySts(), ReturnMoneyStsType.CANCEL.value())) {
            // 撤销失败 当前状态不允许此操作
            throw new YamiShopBindException("yami.refund.status.check");
        }

        Date now = new Date();
        orderRefund.setReturnMoneySts(ReturnMoneyStsType.FAIL.value());
        orderRefund.setCancelTime(now);
        orderRefund.setUpdateTime(now);
        orderRefundService.updateById(orderRefund);


        List<OrderRefund> orderRefundList = orderRefundService.list(new LambdaQueryWrapper<OrderRefund>()
                .eq(OrderRefund::getOrderId, order.getOrderId()));
        long count = orderRefundList.stream().filter(item -> Objects.equals(item.getReturnMoneySts(), ReturnMoneyStsType.APPLY.value())
                || Objects.equals(item.getReturnMoneySts(), ReturnMoneyStsType.PROCESSING.value())
                || Objects.equals(item.getReturnMoneySts(), ReturnMoneyStsType.CONSIGNMENT.value())
                || Objects.equals(item.getReturnMoneySts(), ReturnMoneyStsType.RECEIVE.value())).count();
        if (count == 0) {
            order.setRefundStatus(RefundStatusEnum.DISAGREE.value());
            orderService.updateById(order);
        }
        // 撤销成功
        return ServerResponseEntity.success(I18nMessage.getMessage("yami.undo.successfully"));
    }

    @GetMapping("/info")
    @ApiOperation(value = "查看退款订单详情")
    public ServerResponseEntity<ApiOrderRefundDto> info(String refundSn) {
        // 查询详情
        OrderRefundDto orderRefundDto = orderRefundService.getOrderRefundByRefundSn(refundSn);
        if (orderRefundDto == null) {
            // 查看失败 该退款订单不存在
            throw new YamiShopBindException("yami.failed.not.exist");
        }
        if (!Objects.equals(orderRefundDto.getUserId(), SecurityUtils.getUser().getUserId())) {
            // 查看失败 您没有此权限
            throw new YamiShopBindException("yami.failed.not.permiso");
        }
        ApiOrderRefundDto apiOrderRefundDto = mapperFacade.map(orderRefundDto, ApiOrderRefundDto.class);
        if (Objects.equals(orderRefundDto.getApplyType(), RefundApplyType.RETURN_REFUND.value())) {
            apiOrderRefundDto.setDeliveryDto(orderRefundService.getDeliverInfoByRefundSn(refundSn));
        }
        return ServerResponseEntity.success(apiOrderRefundDto);
    }

    @GetMapping("/list")
    @ApiOperation(value = "我的退款订单列表", notes = "我的退款订单列表，显示数量时候")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "申请退款开始时间", dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "申请退款结束时间", dataType = "String"),
            @ApiImplicitParam(name = "applyType", value = "申请类型: 0/null 全部  1仅退款  2退款退货", dataType = "Integer")
    })
    public ServerResponseEntity<IPage<ApiOrderRefundDto>> list(PageParam<OrderRefundDto> page,
                                                         @RequestParam(required = false) String startTime,
                                                         @RequestParam(required = false) String endTime,
                                                         @RequestParam(required = false) Integer applyType) {
        OrderRefundDto orderRefundDto = new OrderRefundDto();
        orderRefundDto.setUserId(SecurityUtils.getUser().getUserId());
        // 0/null全部  1仅退款 2退款退货
        if (Objects.equals(0, applyType)) {
            applyType = null;
        }
        orderRefundDto.setApplyType(applyType);
        IPage<OrderRefundDto> pageList = orderRefundService.getPage(page, orderRefundDto, startTime, endTime, 1);
        // 克隆对象
        @SuppressWarnings("unchecked") IPage<ApiOrderRefundDto> apiPageList = mapperFacade.map(pageList, IPage.class);
        apiPageList.setRecords(mapperFacade.mapAsList(pageList.getRecords(), ApiOrderRefundDto.class));
        return ServerResponseEntity.success(apiPageList);
    }


    @PutMapping("/updateRefundAmount")
    @ApiOperation(value = "修改订单金额", notes = "修改订单金额")
    public ServerResponseEntity<Void> updateRefundAmount(@Valid @RequestBody UpdateRefundDto updateRefundDto) {
        // 查询详情
        OrderRefundDto orderRefundDto = orderRefundService.getOrderRefundByRefundSn(updateRefundDto.getRefundSn());
        if (orderRefundDto == null) {
            // 该退款订单不存在
            throw new YamiShopBindException("yami.order.refund.exist");
        }
        String userId = SecurityUtils.getUser().getUserId();
        if (!Objects.equals(orderRefundDto.getUserId(), userId)) {
            // 没有权限进行操作
            throw new YamiShopBindException("yami.no.auth");
        }
        // 仅退款只有申请状态下可以改退款金额，退货退货可以再申请、卖家处理、买家发货状态下修改退款金额
        boolean checkStatus =  (Objects.equals(orderRefundDto.getReturnMoneySts(),ReturnMoneyStsType.APPLY.value())
                || Objects.equals(orderRefundDto.getReturnMoneySts(),ReturnMoneyStsType.PROCESSING.value()) || Objects.equals(orderRefundDto.getReturnMoneySts(),ReturnMoneyStsType.CONSIGNMENT.value()));
        if (Objects.equals(orderRefundDto.getApplyType(),1) && !Objects.equals(orderRefundDto.getReturnMoneySts(),ReturnMoneyStsType.APPLY.value())) {
            // 当前退款单状态不能修改退款金额，请稍后重试
            throw new YamiShopBindException("yami.refund.amount.change.check");
        }
        if (Objects.equals(orderRefundDto.getApplyType(),RefundApplyType.RETURN_REFUND.value()) && !checkStatus) {
            // 当前退款单状态不能修改退款金额，请稍后重试
            throw new YamiShopBindException("yami.refund.amount.change.check");
        }
        if (updateRefundDto.getRefundAmount() > orderRefundDto.getMaxRefundAmount()) {
            // 退款金额已超出订单金额，无法申请
            throw new YamiShopBindException("yami.refund.exceeded.amount");
        }
        if (Objects.equals(orderRefundDto.getRefundType(),RefundType.ALL.value())) {
            // 整单退款暂不支持修改退款金额
            throw new YamiShopBindException("yami.refund.amount.change.check2");
        }
        // 部分物品退款
        OrderItem orderItem = orderItemService.getOne(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderItemId, orderRefundDto.getOrderItemId())
                .eq(OrderItem::getOrderNumber, orderRefundDto.getOrderNumber()));
        if (orderItem == null) {
            // 该物品在订单中不存在
            throw new YamiShopBindException("yami.item.not.exist");
        }
        if (orderItem.getPlatformShareReduce() > 0.0) {
            //使用平台优惠的订单不能修改退款金额
            throw new YamiShopBindException("yami.refund.amount.change.check3");
        }
        // 生成退款单信息
        OrderRefund newOrderRefund = new OrderRefund();
        double actualTotal = orderRefundDto.getRefundActualTotal();
        // 计算平台退款金额（退款时将这部分钱退回给平台，所以商家要扣除从平台这里获取的金额）
        // 平台退款金额 = 平台优惠金额 *（退款金额 / 实付金额）
        double changePlatformAmount = Arith.div(Arith.mul(orderItem.getPlatformShareReduce(), updateRefundDto.getRefundAmount()), actualTotal);
        newOrderRefund.setRefundSn(updateRefundDto.getRefundSn());
        newOrderRefund.setRefundAmount(updateRefundDto.getRefundAmount());
        //此处设置实际平台抵现金额
        newOrderRefund.setPlatformRefundAmount(changePlatformAmount);

        // 平台佣金应退 = 平台佣金 *（退款金额 / 实付金额）
        double changePlatformCommission = Arith.div(Arith.mul(orderItem.getPlatformCommission(), updateRefundDto.getRefundAmount()), actualTotal);
        newOrderRefund.setPlatformRefundCommission(changePlatformCommission);
        orderRefundService.updateByRefundSn(newOrderRefund);
        return ServerResponseEntity.success();
    }
}
