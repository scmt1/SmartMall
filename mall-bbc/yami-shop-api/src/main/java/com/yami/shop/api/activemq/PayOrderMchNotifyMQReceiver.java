/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com & jeequan@126.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yami.shop.api.activemq;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.yami.shop.bean.bo.PayInfoResultBO;
import com.yami.shop.bean.enums.PayStatus;
import com.yami.shop.bean.model.CardMq;
import com.yami.shop.bean.model.CouponOrder;
import com.yami.shop.bean.model.PayInfo;
import com.yami.shop.card.common.model.Card;
import com.yami.shop.card.common.model.CardShop;
import com.yami.shop.card.common.model.CardUser;
import com.yami.shop.card.common.service.CardService;
import com.yami.shop.card.common.service.CardShopService;
import com.yami.shop.card.common.service.CardUserService;
import com.yami.shop.common.config.Constant;
import com.yami.shop.coupon.api.util.SnowflakeIdWorker;
import com.yami.shop.coupon.common.constants.ValidTimeTypeEnum;
import com.yami.shop.coupon.common.model.Coupon;
import com.yami.shop.coupon.common.model.CouponUser;
import com.yami.shop.coupon.common.service.CouponService;
import com.yami.shop.coupon.common.service.CouponShopService;
import com.yami.shop.coupon.common.service.CouponUserService;
import com.yami.shop.dao.PayInfoMapper;
import com.yami.shop.mq.model.PayOrderMchNotifyMQ;
import com.yami.shop.mq.vender.IMQSender;
import com.yami.shop.service.CardMqService;
import com.yami.shop.service.CouponOrderService;
import com.yami.shop.service.PayInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.*;

/**
 * 接收MQ消息
 * 业务： 支付订单商户通知
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/7/27 9:23
 */
@Slf4j
@Component
public class PayOrderMchNotifyMQReceiver implements PayOrderMchNotifyMQ.IMQReceiver {

    @Autowired
    private IMQSender mqSender;
    @Autowired
    private CardMqService cardMqService;
    @Autowired
    private PayInfoService payInfoService;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private CouponOrderService couponOrderService;
    @Autowired
    private CardShopService cardShopService;
    @Autowired
    private CouponShopService couponShopService;
    @Autowired
    private CardUserService cardUserService;
    @Autowired
    private CouponUserService couponUserService;
    @Autowired
    private CouponService couponService;
    @Autowired
    private CardService cardService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receive(PayOrderMchNotifyMQ.MsgPayload payload) {
        String notifyId = payload.getNotifyId();
        CardMq cardMq = cardMqService.getById(notifyId);
        Integer notifyCount = cardMq.getCount();
        if(notifyCount > 6) {
            return;
        }
        notifyCount +=1;
        try {
            log.info("接收提货卡支付回调通知MQ, msg={}", payload.toString());

            if(cardMq != null && (cardMq.getStatus() == 1 || cardMq.getStatus() == 3)){
                PayInfoResultBO payInfoResultBO = new PayInfoResultBO();
                payInfoResultBO.setPayNo(cardMq.getMchOrderNo());
                payInfoResultBO.setBizPayNo(cardMq.getChannelOrderNo());
                payInfoResultBO.setIsPaySuccess("2".equals(cardMq.getState()));
                payInfoResultBO.setSuccessString("true");
                payInfoResultBO.setPayAmount(cardMq.getAmount());
                payInfoResultBO.setCallbackContent(cardMq.getCallbackContent());

                PayInfo payInfo = new PayInfo();
                payInfo.setPayNo(cardMq.getMchOrderNo());
                payInfo.setBizPayNo(cardMq.getChannelOrderNo());
                payInfo.setBizOrderNo(cardMq.getPayOrderId());
                payInfo.setCallbackTime(new Date());
                payInfo.setCallbackContent(cardMq.getCallbackContent());
                // 支付宝多次回调可能该支付单已经退款，但还是更新了回调时间导致对账查询有误
                payInfoMapper.update(payInfo, new LambdaUpdateWrapper<PayInfo>()
                        .eq(PayInfo::getPayNo, payInfo.getPayNo())
                        .eq(PayInfo::getPayStatus, PayStatus.UNPAY.value()));

                payInfo = payInfoService.getOne(new LambdaQueryWrapper<PayInfo>().eq(PayInfo::getPayNo, cardMq.getMchOrderNo()));
                // 已经支付
                if (Objects.equals(payInfo.getPayStatus(), PayStatus.PAYED.value()) || Objects.equals(payInfo.getPayStatus(), PayStatus.REFUND.value())) {
                    return;
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
                card.setCardTitle(coupon.getCardName());
                card.setBalance(coupon.getReduceAmount());
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
                if(coupon.getGiveCouponId() != null){
                    giveCouponId = coupon.getGiveCouponId();
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
            }

            cardMq.setStatus(2);
            cardMqService.updateById(cardMq);
            return;
        }catch (Exception e) {

            log.error(e.getMessage(), e);
            cardMq.setStatus(3);
            cardMq.setCount(notifyCount);
            cardMqService.updateById(cardMq);
            mqSender.send(PayOrderMchNotifyMQ.build(String.valueOf(payload)), notifyCount * 20);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return;
        }
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
