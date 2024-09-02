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
import cn.hutool.core.util.BooleanUtil;
import com.google.common.collect.Lists;
import com.yami.shop.bean.app.dto.*;
import com.yami.shop.bean.app.param.ChangeShopCartParam;
import com.yami.shop.bean.app.param.CheckShopCartItemParam;
import com.yami.shop.bean.enums.DeliveryType;
import com.yami.shop.bean.enums.DiscountRule;
import com.yami.shop.bean.enums.ProdStatusEnums;
import com.yami.shop.bean.enums.ProdType;
import com.yami.shop.bean.model.Basket;
import com.yami.shop.bean.model.Product;
import com.yami.shop.bean.model.Sku;
import com.yami.shop.bean.vo.ShopCartWithAmountVO;
import com.yami.shop.bean.vo.ShopTransFeeVO;
import com.yami.shop.bean.vo.UserDeliveryInfoVO;
import com.yami.shop.combo.multishop.dto.ComboSkuDto;
import com.yami.shop.combo.multishop.service.ComboService;
import com.yami.shop.common.enums.StatusEnum;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.Arith;
import com.yami.shop.delivery.api.manager.DeliveryOrderManager;
import com.yami.shop.manager.ComboShopCartManager;
import com.yami.shop.manager.DiscountShopCartManager;
import com.yami.shop.manager.impl.ShopCartAdapter;
import com.yami.shop.manager.impl.ShopCartItemAdapter;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.BasketService;
import com.yami.shop.service.ProductService;
import com.yami.shop.service.SkuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author LGH
 */
@RestController
@RequestMapping("/p/shopCart")
@Api(tags = "购物车接口")
public class ShopCartController {

    @Autowired
    private BasketService basketService;
    @Autowired
    private ProductService productService;
    @Autowired
    private SkuService skuService;
    @Autowired
    private DeliveryOrderManager deliveryOrderManager;
    @Autowired
    private ThreadPoolExecutor prodThreadPoolExecutor;
    @Autowired
    private ShopCartAdapter shopCartAdapter;
    @Autowired
    private ShopCartItemAdapter shopCartItemAdapter;
    @Autowired
    private DiscountShopCartManager discountShopCartManager;
    @Autowired
    private ComboShopCartManager comboShopCartManager;
    @Autowired
    private ComboService comboService;

    @PostMapping("/info")
    @ApiOperation(value = "获取用户购物车信息", notes = "获取用户购物车信息，参数为用户选中的活动项数组")
    public ServerResponseEntity<ShopCartWithAmountVO> info() throws ExecutionException, InterruptedException {
        String userId = SecurityUtils.getUser().getUserId();
        // 拿到购物车的所有item
        List<ShopCartItemDto> shopCartItems = shopCartItemAdapter.getShopCartItems(userId,0);
        List<ShopCartItemDto> filterShopCartItems = shopCartItems.stream()
                .filter(shopCartItemDto -> BooleanUtil.isTrue(shopCartItemDto.getIsChecked())).collect(Collectors.toList());
        // 组合每个店铺的购物车信息
        List<ShopCartDto> shopCarts = shopCartAdapter.getShopCarts(shopCartItems);
        ShopCartWithAmountVO shopCartWithAmount = new ShopCartWithAmountVO();
        shopCartWithAmount.setShopCarts(shopCarts);

        // 组装满减折，套餐，赠品的优惠数据
        if (discountShopCartManager != null) {
            discountShopCartManager.calculateDiscountAndMakeUpShopCartAndAmount(shopCartWithAmount);
        }
        if (comboShopCartManager != null) {
            comboShopCartManager.calculateComboAndMakeUpShopCartAndAmount(shopCartWithAmount);
        }
        // 重新计算下运费
        UserDeliveryInfoVO userDeliveryInfoVO = deliveryOrderManager.calculateAndGetDeliverInfo(userId, 0L, DeliveryType.EXPRESS.getValue(), filterShopCartItems);
        shopCartWithAmount.setUserDeliveryInfo(userDeliveryInfoVO);
        calculateComboAndMakeUpShopCartAndAmount(shopCartWithAmount);
        return ServerResponseEntity.success(shopCartWithAmount);
    }

    private void calculateComboAndMakeUpShopCartAndAmount(ShopCartWithAmountVO shopCartWithAmount) {
        List<ShopCartDto> shopCarts = shopCartWithAmount.getShopCarts();

        double totalTransFee = 0L;
        double totalFreeTransFee = 0L;
        for (ShopCartDto shopCart : shopCarts) {
            // 计算运费
            Map<Long, ShopTransFeeVO> shopIdWithShopTransFee = null;
            if (Objects.nonNull(shopCartWithAmount.getUserDeliveryInfo())) {
                shopIdWithShopTransFee = shopCartWithAmount.getUserDeliveryInfo().getShopIdWithShopTransFee();
            }
            if (Objects.nonNull(shopIdWithShopTransFee) && shopIdWithShopTransFee.containsKey(shopCart.getShopId())) {
                ShopTransFeeVO shopTransFeeVO = shopIdWithShopTransFee.get(shopCart.getShopId());
                // 店铺的实付 = 购物车实付 + 运费
                shopCart.setActualTotal(Arith.add(shopCart.getActualTotal(), shopTransFeeVO.getTransFee()));
                // 店铺免运费金额
                shopCart.setFreeTransFee(shopTransFeeVO.getFreeTransFee());
                // 店铺优惠金额
                shopCart.setShopReduce(shopCart.getShopReduce());
                // 运费
                shopCart.setTransFee(shopTransFeeVO.getTransFee());
            } else {
                shopCart.setTransFee(0.0);
                shopCart.setFreeTransFee(0.0);
            }
            totalFreeTransFee = Arith.add(totalFreeTransFee, shopCart.getFreeTransFee());
            totalTransFee = Arith.add(totalTransFee, shopCart.getTransFee());
        }
        shopCartWithAmount.setFreightAmount(totalTransFee);
        shopCartWithAmount.setFreeTransFee(totalFreeTransFee);
        shopCartWithAmount.setSubtractMoney(shopCartWithAmount.getSubtractMoney());
        shopCartWithAmount.setFinalMoney(Arith.sub(Arith.add(shopCartWithAmount.getTotalMoney(), totalTransFee), shopCartWithAmount.getSubtractMoney()));
    }


    @PostMapping("/changeItem")
    @ApiOperation(value = "添加、修改用户购物车物品", notes = "通过商品id(prodId)、skuId、店铺Id(shopId),添加/修改用户购物车商品，并传入改变的商品个数(count)，" +
            "当count为正值时，增加商品数量，当count为负值时，将减去商品的数量，当最终count值小于0时，会将商品从购物车里面删除")
    public ServerResponseEntity<String> changeItem(@Valid @RequestBody ChangeShopCartParam param) {
        String userId = SecurityUtils.getUser().getUserId();
        if(param.getIsMall() != null && param.getIsMall() == 1){
            param.setIsMall(1);
        }else{
            param.setIsMall(0);
        }
        List<ShopCartItemDto> shopCartItems = shopCartItemAdapter.getShopCartItems(userId,param.getIsMall());

        //新增/修改套餐商品
        if (Objects.nonNull(param.getComboId()) && !Objects.equals(param.getComboId(), 0L)) {
            if (Objects.nonNull(param.getOldSkuId())) {
                return comboShopCartManager.comboChangeCart(param, userId, shopCartItems);
            }
            return comboShopCartManager.comboAddCart(param, userId, shopCartItems);
        }

        Product product = productService.getProductByProdId(param.getProdId(), I18nMessage.getDbLang());
        Sku sku = skuService.getSkuBySkuId(param.getSkuId(), I18nMessage.getDbLang());

        // 当商品状态不正常时，不能添加到购物车
        boolean noTakeOff = !Objects.equals(product.getStatus(), ProdStatusEnums.NORMAL.getValue()) || !Objects.equals(sku.getStatus(), StatusEnum.ENABLE.value())
                // 当商品为预售商品时，不能添加到购物车
                || (Objects.nonNull(product.getPreSellStatus()) && Objects.equals(product.getPreSellStatus(), 1))
                // 当商品为虚拟商品时，不能添加到购物车
                || Objects.equals(product.getMold(), 1)
                // 当商品为活动商品时，不能添加到购物车
                || Objects.equals(product.getProdType(), ProdType.PROD_TYPE_ACTIVE.value());
        if (noTakeOff) {
            // 预售/虚拟/活动商品不能加入购物车！
            return ServerResponseEntity.showFailMsg(I18nMessage.getMessage("yami.shopCart.prod.error"));
        }

        // 获取加购的sku库存
        Integer skuStock = sku.getStocks();

        Integer oldCount = 0;
        Long oldBasketId = null;
        for (ShopCartItemDto shopCartItemDto : shopCartItems) {
            if (Objects.nonNull(shopCartItemDto.getComboId()) && shopCartItemDto.getComboId() != 0) {
                continue;
            }
            if (Objects.equals(param.getSkuId(), shopCartItemDto.getSkuId())) {
                oldCount = shopCartItemDto.getProdCount();
                oldBasketId = shopCartItemDto.getBasketId();
                Basket basket = new Basket();
                basket.setUserId(userId);
                basket.setBasketCount(param.getCount() + shopCartItemDto.getProdCount());
                if (skuStock < basket.getBasketCount() && param.getCount() > 0) {
                    // 商品库存不足
                    return ServerResponseEntity.showFailMsg(I18nMessage.getMessage("yami.insufficient.inventory"));
                }
                basket.setBasketId(shopCartItemDto.getBasketId());
                basket.setIsChecked(shopCartItemDto.getIsChecked());
                basket.setDiscountId(param.getDiscountId());
                if (Objects.nonNull(param.getOldSkuId())) {
                    // 如果有个旧的sku，就说明是在切换sku
                    continue;
                }
                // 防止购物车变成负数
                if (basket.getBasketCount() <= 0) {
                    basketService.deleteShopCartItemsByBasketIds(userId, Collections.singletonList(basket.getBasketId()));
                    return ServerResponseEntity.success();
                }
                basketService.updateShopCartItem(basket);
                return ServerResponseEntity.success();
            }
        }

        // 切换sku
        if (Objects.nonNull(param.getOldSkuId())) {
            for (ShopCartItemDto oldShopCartItem : shopCartItems) {
                if (Objects.equals(param.getOldSkuId(), oldShopCartItem.getSkuId())) {
                    Basket basket = new Basket();
                    basket.setUserId(userId);
                    basket.setBasketId(oldShopCartItem.getBasketId());
                    // 如果以前就存在这个商品，要把以前的商品数量累加
                    basket.setBasketCount(param.getCount() + oldCount);
                    if (skuStock < basket.getBasketCount() && param.getCount() > 0) {
                        // 商品库存不足
                        return ServerResponseEntity.showFailMsg(I18nMessage.getMessage("yami.insufficient.inventory"));
                    }
                    basket.setSkuId(param.getSkuId());
                    if (Objects.nonNull(oldBasketId)) {
                        // 删除旧的购物项
                        basketService.deleteShopCartItemsByBasketIds(userId, Collections.singletonList(oldBasketId));
                    }
                    // 更新购物车
                    basketService.updateShopCartItem(basket);
                    return ServerResponseEntity.success();
                }
            }
        }
        // 所有都正常时
        if (skuStock > 0) {
            basketService.addShopCartItem(param, userId);
        } else {
            // 商品库存不足
            return ServerResponseEntity.showFailMsg(I18nMessage.getMessage("yami.insufficient.inventory"));
        }
        // 添加成功
        return ServerResponseEntity.success(I18nMessage.getMessage("yami.activity.add.success"));
    }

    @PostMapping("/checkItems")
    @ApiOperation(value = "勾选购物车")
    public ServerResponseEntity<Void> checkItems(@Valid @RequestBody List<CheckShopCartItemParam> params) {
        if (CollectionUtil.isEmpty(params)) {
            return ServerResponseEntity.success();
        }
        String userId = SecurityUtils.getUser().getUserId();
        basketService.checkShopCartItems(userId, params);
        return ServerResponseEntity.success();
    }

    @GetMapping("/prodCount")
    @ApiOperation(value = "获取购物车商品数量", notes = "获取所有购物车商品数量")
    public ServerResponseEntity<Integer> prodCount() {
        String userId = SecurityUtils.getUser().getUserId();
        return ServerResponseEntity.success(basketService.getShopCartProdNum(userId));
    }

    @GetMapping("/expiryProdList")
    @ApiOperation(value = "获取购物车失效商品信息", notes = "获取购物车失效商品列表")
    public ServerResponseEntity<List<ShopCartExpiryItemDto>> expiryProdList() {
        String userId = SecurityUtils.getUser().getUserId();
        List<ShopCartItemDto> shopCartItems = basketService.getShopCartExpiryItems(userId);
        //根据店铺ID划分item
        Map<Long, List<ShopCartItemDto>> shopCartItemDtoMap = shopCartItems.stream().collect(Collectors.groupingBy(ShopCartItemDto::getShopId));

        // 返回一个店铺对应的所有信息
        List<ShopCartExpiryItemDto> shopCartExpiryItems = Lists.newArrayList();

        for (Long key : shopCartItemDtoMap.keySet()) {
            ShopCartExpiryItemDto shopCartExpiryItemDto = new ShopCartExpiryItemDto();
            shopCartExpiryItemDto.setShopId(key);
            List<ShopCartItemDto> shopCartItemList = Lists.newArrayList();
            for (ShopCartItemDto tempShopCartItemDto : shopCartItemDtoMap.get(key)) {
                shopCartExpiryItemDto.setShopName(tempShopCartItemDto.getShopName());
                shopCartItemList.add(tempShopCartItemDto);
            }
            shopCartExpiryItemDto.setShopCartItemDtoList(shopCartItemList);
            shopCartExpiryItems.add(shopCartExpiryItemDto);
        }
        return ServerResponseEntity.success(shopCartExpiryItems);
    }

    @DeleteMapping("/cleanExpiryProdList")
    @ApiOperation(value = "清空用户失效商品", notes = "清空用户失效商品")
    public ServerResponseEntity<Void> cleanExpiryProdList() {
        String userId = SecurityUtils.getUser().getUserId();
        basketService.cleanExpiryProdList(userId);
        return ServerResponseEntity.success();
    }

    @PutMapping("/deleteItem")
    @ApiOperation(value = "删除用户购物车物品", notes = "通过购物车id删除用户购物车物品")
    public ServerResponseEntity<Void> deleteItem(@RequestBody List<Long> basketIds) {
        String userId = SecurityUtils.getUser().getUserId();
        basketService.deleteShopCartItemsByBasketIds(userId, basketIds);
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/deleteAll")
    @ApiOperation(value = "清空用户购物车所有物品", notes = "清空用户购物车所有物品(暂无用)")
    public ServerResponseEntity<String> deleteAll() {
        String userId = SecurityUtils.getUser().getUserId();
        basketService.deleteAllShopCartItems(userId);
        // 删除成功
        return ServerResponseEntity.success(I18nMessage.getMessage("yami.delete.successfully"));
    }
}
