/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.api.manager;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.StrUtil;
import com.yami.shop.bean.app.dto.*;
import com.yami.shop.bean.app.param.OrderShopParam;
import com.yami.shop.bean.app.param.SubmitOrderParam;
import com.yami.shop.bean.dto.SkuStockLockDTO;
import com.yami.shop.bean.dto.UserScoreLockDTO;
import com.yami.shop.bean.enums.DeliveryStatus;
import com.yami.shop.bean.enums.DvyType;
import com.yami.shop.bean.enums.OrderType;
import com.yami.shop.bean.enums.ShopCityStatus;
import com.yami.shop.bean.event.SubmitOrderActivityEvent;
import com.yami.shop.bean.event.TryLockStockEvent;
import com.yami.shop.bean.model.Order;
import com.yami.shop.bean.model.OrderItem;
import com.yami.shop.bean.vo.GiveawayProdVO;
import com.yami.shop.bean.vo.GiveawayVO;
import com.yami.shop.bean.vo.VirtualRemarkVO;
import com.yami.shop.common.constants.OrderCacheNames;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.response.ResponseEnum;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.CacheManagerUtil;
import com.yami.shop.common.util.RedisUtil;
import com.yami.shop.coupon.common.dto.CouponRecordDTO;
import com.yami.shop.coupon.common.service.CouponUseRecordService;
import com.yami.shop.manager.SubmitOrderManager;
import com.yami.shop.service.SkuStockLockService;
import com.yami.shop.user.common.service.UserScoreLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 * 提交订单适配
 * @author FrozenWatermelon
 * @date 2020/12/07
 */
@Component
public class SubmitOrderImplManager implements SubmitOrderManager {

    @Autowired
    private CacheManagerUtil cacheManagerUtil;

    @Autowired
    private UserScoreLockService userScoreLockService;

    @Autowired
    private SkuStockLockService skuStockLockService;

    @Autowired
    private Snowflake snowflake;

    @Autowired
    private CouponUseRecordService couponLockService;
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public ServerResponseEntity<ShopCartOrderMergerDto> checkSubmitInfo(SubmitOrderParam submitOrderParam, String userId) {
        ShopCartOrderMergerDto mergerOrder = cacheManagerUtil.getCache(OrderCacheNames.ORDER_CONFIRM_KEY, String.valueOf(userId));
        // 看看订单有没有过期
        if (Objects.isNull(mergerOrder)) {
            // 订单已过期，请重新下单
            throw new YamiShopBindException("yami.order.expired");
        }

        // 防止重复、同时提交
        boolean cad = RedisUtil.cad(OrderCacheNames.ORDER_CONFIRM_UUID_KEY + OrderCacheNames.UNION + userId, userId);
        if (!cad) {
            // 订单状态已经发生改变，请重新下单
//            throw new YamiShopBindException("yami.order.status.check.change");
            OrderNumbersDto orderNumbersDto = new OrderNumbersDto(null);
            orderNumbersDto.setDuplicateError(1);
            return ServerResponseEntity.fail(ResponseEnum.REPEAT_ORDER);
        }
        // 看看订单的标记有没有过期
        if (cacheManagerUtil.getCache(OrderCacheNames.ORDER_CONFIRM_KEY, userId) == null) {
            // 订单已过期，请重新下单
            throw new YamiShopBindException("yami.order.expired");
        }
        // 检查下活动有没有过期
        applicationContext.publishEvent(new SubmitOrderActivityEvent(mergerOrder));
        if (CollectionUtil.isNotEmpty(submitOrderParam.getOrderInvoiceList())) {
            mergerOrder.setOrderInvoiceList(submitOrderParam.getOrderInvoiceList());
        }
        if (!Objects.equals(mergerOrder.getDvyType(), DvyType.DELIVERY.value()) && mergerOrder.getShopCartOrders().size() > 1) {
            // 多家店铺提交订单时，配送方式只能是快递
            throw new YamiShopBindException("yami.order.delivery.error");
        }
        if (Objects.equals(mergerOrder.getDvyType(), DvyType.DELIVERY.value())) {
            if (Objects.nonNull(mergerOrder.getShopDeliveryStatus()) && mergerOrder.getShopDeliveryStatus() < DeliveryStatus.USABLE.value()) {
                // 超出商家快递配送区域
                throw new YamiShopBindException("超出商家快递配送区域");
            }
        }
        if (Objects.equals(mergerOrder.getDvyType(), DvyType.SAME_CITY.value())) {
            if (Objects.nonNull(mergerOrder.getShopCityStatus()) && Objects.equals(mergerOrder.getShopCityStatus(), ShopCityStatus.NO_CONFIG.value())) {
                // 当前店铺未开启同城配送
                throw new YamiShopBindException("yami.order.same.city.error");
            }
            if (Objects.nonNull(mergerOrder.getShopCityStatus()) && mergerOrder.getShopCityStatus() < ShopCityStatus.USABLE.value()) {
                // 超出商家配送距离或起送费不够
                throw new YamiShopBindException("yami.order.same.city.error2");
            }
        }
        if (Objects.equals(mergerOrder.getDvyType(), DvyType.STATION.value()) && Objects.isNull(mergerOrder.getStationId()) && Objects.isNull(submitOrderParam.getOrderSelfStationDto())) {
            // 请选择自提点并填写完整的自提信息
            throw new YamiShopBindException("yami.station.detail.check");
        }
        // 检查下虚拟商品的所需留言是否填写完成
        if (Objects.equals(mergerOrder.getMold(), 1) && CollectionUtil.isNotEmpty(submitOrderParam.getVirtualRemarkList())) {
            List<VirtualRemarkVO> virtualRemarkList = submitOrderParam.getVirtualRemarkList();
            boolean isRequired = false;
            for (VirtualRemarkVO virtualRemarkVO : mergerOrder.getVirtualRemarkList()) {
                if (virtualRemarkVO.getIsRequired()) {
                    isRequired = true;
                    break;
                }
            }
            // 如果至少要输入一个备注信息，则不能为空
            if (isRequired && CollectionUtil.isEmpty(virtualRemarkList)) {
                // 不能为空
                throw new YamiShopBindException("yami.order.not.null");
            }
            for (VirtualRemarkVO virtualRemarkVO : virtualRemarkList) {
                if (virtualRemarkVO.getIsRequired() && StrUtil.isBlank(virtualRemarkVO.getValue())) {
                    // 不能为空
                    throw new YamiShopBindException("yami.order.not.null");
                }
            }
        }
        mergerOrder.setUserId(userId);
        mergerOrder.setVirtualRemarkList(submitOrderParam.getVirtualRemarkList());
        // 设置自提信息
        mergerOrder.setOrderSelfStationDto(submitOrderParam.getOrderSelfStationDto());
        // 设置备注
        List<OrderShopParam> orderParams = submitOrderParam.getOrderShopParams();
        List<ShopCartOrderDto> shopCartOrders = mergerOrder.getShopCartOrders();
        if (CollectionUtil.isNotEmpty(orderParams)) {
            for (ShopCartOrderDto shopCartOrder : shopCartOrders) {
                for (OrderShopParam orderParam : orderParams) {
                    if (!Objects.equals(shopCartOrder.getShopId(), orderParam.getShopId())) {
                        continue;
                    }
                    shopCartOrder.setRemarks(orderParam.getRemarks());
                }
            }
        }
        for (ShopCartOrderDto shopCartOrder : shopCartOrders) {
            // 使用雪花算法生成的订单号
            String orderNumber = String.valueOf(snowflake.nextId());
            shopCartOrder.setOrderNumber(orderNumber);
        }
        return ServerResponseEntity.success(mergerOrder);
    }

    @Override
    @EventListener(TryLockStockEvent.class)
    public void tryLockStock(TryLockStockEvent event) {
        if(Objects.equals(event.getMergerOrder().getOrderType(),OrderType.SECKILL)){
            return;
        }
        List<ShopCartOrderDto> shopCartOrders = event.getMergerOrder().getShopCartOrders();
        List<Order> orders = new ArrayList<>();
        List<SkuStockLockDTO> skuStockLocks = new ArrayList<>();
        // 避免因赠品，套餐重复
        Map<Long,SkuStockLockDTO> skuStockMap = new HashMap<>(16);
        for (ShopCartOrderDto shopCartOrder : shopCartOrders) {
            Order order = new Order();
            order.setShopId(shopCartOrder.getShopId());
            order.setOrderNumber(shopCartOrder.getOrderNumber());
            List<ShopCartItemDiscountDto> shopCartItemDiscounts = shopCartOrder.getShopCartItemDiscounts();
            List<OrderItem> orderItems = new ArrayList<>();
            for (ShopCartItemDiscountDto shopCartItemDiscount : shopCartItemDiscounts) {
                List<ShopCartItemDto> shopCartItems = shopCartItemDiscount.getShopCartItems();
                for (ShopCartItemDto orderItem : shopCartItems) {
                    OrderItem item = new OrderItem();
                    item.setProdId(orderItem.getProdId());
                    item.setSkuId(orderItem.getSkuId());
                    item.setStatus(-1);
                    item.setProdCount(orderItem.getProdCount());
                    orderItems.add(item);
                    SkuStockLockDTO skuStockLockDTO = new SkuStockLockDTO(orderItem.getProdId(), orderItem.getSkuId(), shopCartOrder.getOrderNumber(), orderItem.getProdCount());
                    // 放入map
                    skuStockMap.put(orderItem.getSkuId(),skuStockLockDTO);
                    GiveawayVO giveaway = orderItem.getGiveaway();
                    if (Objects.isNull(giveaway)) {
                        continue;
                    }
                    // 赠品库存计算
                    int productNums = shopCartOrder.getTotalCount();
                    for (GiveawayProdVO giveawayProd : giveaway.getGiveawayProds()) {
                        // 出入库用到的信息
                        OrderItem giveawayItem = new OrderItem();
                        giveawayItem.setProdId(giveawayProd.getProdId());
                        giveawayItem.setSkuId(giveawayProd.getSkuId());
                        giveawayItem.setStatus(-1);
                        giveawayItem.setProdCount(giveawayProd.getGiveawayNum());
                        orderItems.add(giveawayItem);
                        productNums = productNums + orderItem.getProdCount();
                        SkuStockLockDTO comboSkuStockLock = new SkuStockLockDTO(giveawayProd.getProdId(), giveawayProd.getSkuId(), shopCartOrder.getOrderNumber(), giveawayProd.getGiveawayNum());
                        // 判断是否重复.重复重新put
                        if(skuStockMap.containsKey(giveawayProd.getSkuId())){
                            SkuStockLockDTO skuStockLock = skuStockMap.get(giveawayProd.getSkuId());
                            comboSkuStockLock.setCount(skuStockLock.getCount() + comboSkuStockLock.getCount());
                        }
                        skuStockMap.put(giveawayProd.getSkuId(), comboSkuStockLock);
                    }
                }
            }
            order.setOrderItems(orderItems);
            orders.add(order);
        }
        // 添加赠品库存扣减项
        for (Long skuId : skuStockMap.keySet()) {
            skuStockLocks.add(skuStockMap.get(skuId));
        }
        // 锁定库存
        ServerResponseEntity<?> lockStockResponse = skuStockLockService.lock(skuStockLocks,orders);
        // 提示具体哪个商品库存不足
        if (Objects.equals(ResponseEnum.NOT_STOCK.value(), lockStockResponse.getCode())) {
            for (ShopCartOrderDto shopCartOrder : shopCartOrders) {
                List<ShopCartItemDiscountDto> shopCartItemDiscounts = shopCartOrder.getShopCartItemDiscounts();
                for (ShopCartItemDiscountDto shopCartItemDiscount : shopCartItemDiscounts) {
                    List<ShopCartItemDto> shopCartItems = shopCartItemDiscount.getShopCartItems();
                    for (ShopCartItemDto orderItem : shopCartItems) {
                        if (Objects.equals(orderItem.getSkuId().toString(), lockStockResponse.getData().toString())) {
                            // 拼接库存不足商品的名称（有规格sku，要把规格也拼接上）
                            // split(" ")[0]是因为有些商品名已经拼接了‘prodName skuName’，只取prodName，避免skuName重复
                            String prodName;
                            if ((prodName=orderItem.getSkuName()) == null) {
                                prodName = orderItem.getProdName().split(" ")[0];
                            } else {
                                prodName = orderItem.getProdName().split(" ")[0] + "(" + prodName + ")";
                            }
                            throw new YamiShopBindException(prodName + I18nMessage.getMessage("yami.insufficient.inventory"));
                        }
                    }
                }
            }
        }
        if (!lockStockResponse.isSuccess()) {
            throw new YamiShopBindException(ResponseEnum.EXCEPTION);
        }
    }

    @Override
    @EventListener(TryLockStockEvent.class)
    public void tryLockCoupon(TryLockStockEvent event) {
        ShopCartOrderMergerDto mergerOrder = event.getMergerOrder();
        String userId = event.getUserId();
        if (!Objects.equals(mergerOrder.getOrderType(), OrderType.ORDINARY)) {
            return;
        }
        List<ShopCartOrderDto> shopCartOrders = mergerOrder.getShopCartOrders();
        List<String> orderNumbers = new ArrayList<>();

        for (ShopCartOrderDto shopCartOrder : shopCartOrders) {
            String orderNumber = shopCartOrder.getOrderNumber();
            List<CouponOrderDto> coupons = shopCartOrder.getCoupons();
            if (CollectionUtil.isNotEmpty(coupons)) {
                for (CouponOrderDto coupon : coupons) {
                    if (Objects.equals(Boolean.TRUE, coupon.isChoose())) {
                        coupon.setOrderNumber(orderNumber);
                    }
                }
            }
            orderNumbers.add(orderNumber);
        }

        // 锁优惠券
        List<CouponRecordDTO> lockCouponParams = getLockCouponDto(mergerOrder, orderNumbers);
        if (CollectionUtil.isNotEmpty(lockCouponParams)) {
            couponLockService.lockCoupon(lockCouponParams,userId);
        }
    }

    /**
     * 尝试锁定优惠券
     * @param mergerOrder
     * @param orderIds
     */
    private List<CouponRecordDTO> getLockCouponDto(ShopCartOrderMergerDto mergerOrder, List<String> orderIds) {
        // 锁定优惠券
        // 平台优惠券
        List<CouponRecordDTO> lockCouponParams = new ArrayList<>();
        CouponRecordDTO platformLockCouponParam = getLockCouponDTO(mergerOrder.getCoupons(),mergerOrder.getShopCartOrders().get(0).getShopId());
        if (platformLockCouponParam != null) {
            // 平台优惠券涉及多个订单，所以设置订单id为多个订单id以逗号分割
            platformLockCouponParam.setOrderNumbers(StrUtil.join(StrUtil.COMMA, orderIds));
            lockCouponParams.add(platformLockCouponParam);
        }
        // 店铺优惠券
        for (ShopCartOrderDto shopCartOrder : mergerOrder.getShopCartOrders()) {
            CouponRecordDTO shopLockCouponParam = getLockCouponDTO(shopCartOrder.getCoupons(),shopCartOrder.getShopId());
            if (shopLockCouponParam != null) {
                lockCouponParams.add(shopLockCouponParam);
            }
        }

        return lockCouponParams;
    }

    private CouponRecordDTO getLockCouponDTO(List<CouponOrderDto> couponOrders, Long writeOffShopId) {
        if (CollectionUtil.isEmpty(couponOrders)) {
            return null;
        }
        for (CouponOrderDto couponOrder : couponOrders) {
            if ((Objects.equals(Boolean.TRUE, couponOrder.isChoose())) && couponOrder.isCanUse()) {
                CouponRecordDTO param = new CouponRecordDTO();
                param.setOrderNumbers(couponOrder.getOrderNumber());
                param.setCouponId(couponOrder.getCouponId());
                param.setCouponUserId(couponOrder.getCouponUserId());
                param.setReduceAmount(couponOrder.getReduceAmount());
                param.setWriteOffShopId(writeOffShopId);
                return param;
            }
        }
        return null;
    }

    @Override
    @EventListener(TryLockStockEvent.class)
    public void tryLockScore(TryLockStockEvent event) {
        String userId = event.getUserId();
        ShopCartOrderMergerDto mergerOrder = event.getMergerOrder();
        // 如果没有使用积分，就不用锁定积分啦
        if (Objects.isNull(mergerOrder.getUsableScore()) || Objects.equals(mergerOrder.getUsableScore(), 0L)) {
            return;
        }
        List<ShopCartOrderDto> shopCartOrders = mergerOrder.getShopCartOrders();
        List<UserScoreLockDTO> userScoreLocks = new ArrayList<>();
        for (ShopCartOrderDto shopCartOrder : shopCartOrders) {
            if (Objects.equals(shopCartOrder.getUseScore(), 0L)) {
                continue;
            }
            userScoreLocks.add(new UserScoreLockDTO(shopCartOrder.getOrderNumber(), shopCartOrder.getUseScore(), mergerOrder.getOrderType().value()));
        }

        // 锁定积分
        userScoreLockService.lock(userScoreLocks,userId);
    }

}
