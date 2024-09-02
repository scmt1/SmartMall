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


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.app.dto.ProductDto;
import com.yami.shop.bean.app.dto.UserCollectionDto;
import com.yami.shop.bean.enums.ProdType;
import com.yami.shop.bean.model.Product;
import com.yami.shop.bean.model.UserCollection;
import com.yami.shop.bean.model.UserCollectionShop;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.BasketService;
import com.yami.shop.service.ProductService;
import com.yami.shop.service.UserCollectionService;
import com.yami.shop.service.UserCollectionShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author LGH
 */
@RestController
@RequestMapping("/p/user/collection")
@Api(tags = "商品收藏接口")
@AllArgsConstructor
public class UserCollectionController {

    private final UserCollectionService userCollectionService;
    private final UserCollectionShopService userCollectionShopService;

    private final BasketService basketService;

    private final ProductService productService;

    @GetMapping("/page")
    @ApiOperation(value = "分页返回收藏数据", notes = "根据用户id获取")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "prodName", value = "商品名称", required = true, dataType = "String"),
            @ApiImplicitParam(name = "prodType", value = "商品类型", required = true, dataType = "Integer")
    })
    public ServerResponseEntity<IPage<UserCollectionDto>> getUserCollectionDtoPageByUserId(PageParam page, String prodName, Integer prodType) {
        return ServerResponseEntity.success(userCollectionService.getUserCollectionDtoPageByUserId(page, SecurityUtils.getUser().getUserId(), prodName, prodType, I18nMessage.getDbLang()));
    }

    @GetMapping("/isCollection")
    @ApiOperation(value = "根据商品id获取该商品是否在收藏夹中", notes = "传入收藏商品id")
    @ApiImplicitParam(name = "prodId", value = "商品id", required = true, dataType = "String")
    public ServerResponseEntity<Boolean> isCollection(String prodId) {
        String[] prodIds = prodId.split(StrUtil.COMMA);
        if (ArrayUtil.isEmpty(prodId)) {
            throw new YamiShopBindException("商品id不能为空");
        }
        int count = 0;
        for (String pLong : prodIds) {
            if (productService.count(new LambdaQueryWrapper<Product>()
                    .eq(Product::getProdId, pLong)) < 1) {
                // 该商品不存在
                throw new YamiShopBindException("yami.product.no.exist");
            }
            int i = userCollectionService.count(new LambdaQueryWrapper<UserCollection>()
                    .eq(UserCollection::getProdId, pLong)
                    .eq(UserCollection::getUserId, SecurityUtils.getUser().getUserId()));
            count += i;
        }
        return ServerResponseEntity.success(count == prodIds.length);
    }

    @PostMapping("/addOrCancel")
    @ApiOperation(value = "添加/取消收藏", notes = "传入收藏商品id,如果商品未收藏则收藏商品，已收藏则取消收藏")
    @ApiImplicitParam(name = "prodId", value = "商品id", required = true, dataType = "Long")
    public ServerResponseEntity<Boolean> addOrCancel(@RequestBody Long prodId) {
        Product product = productService.getProductByProdId(prodId, I18nMessage.getDbLang());
        if (Objects.isNull(product)) {
            // 该商品不存在
            throw new YamiShopBindException("yami.product.no.exist");
        }
        boolean isAdd = false;
        String userId = SecurityUtils.getUser().getUserId();
        //pc端收藏商品时，清除购物车缓存
        basketService.removeCacheByUserIds(Collections.singletonList(userId));
        if (userCollectionService.count(new LambdaQueryWrapper<UserCollection>()
                .eq(UserCollection::getProdId, prodId)
                .eq(UserCollection::getUserId, userId)) > 0) {
            userCollectionService.remove(new LambdaQueryWrapper<UserCollection>()
                    .eq(UserCollection::getProdId, prodId)
                    .eq(UserCollection::getUserId, userId));
        } else {
            if (Objects.equals(product.getProdType(), ProdType.PROD_TYPE_ACTIVE.value())) {
                // 活动商品不能添加收藏
                throw new YamiShopBindException("yami.active.prod.cannot.add.collection");
            }
            UserCollection userCollection = new UserCollection();
            userCollection.setCreateTime(new Date());
            userCollection.setUserId(userId);
            userCollection.setProdId(prodId);
            userCollectionService.save(userCollection);
            isAdd = true;
        }
        return ServerResponseEntity.success(isAdd);
    }

    @PostMapping("/batachCancel")
    @ApiOperation(value = "批量取消收藏", notes = "传入收藏商品id")
    @ApiImplicitParam(name = "prodIds", value = "商品id", required = true, dataType = "Long")
    public ServerResponseEntity<Boolean> batachCancel(@RequestBody List<Long> prodIds) {
        String userId = SecurityUtils.getUser().getUserId();
        if (CollectionUtils.isEmpty(prodIds)) {
            return ServerResponseEntity.success(false);
        }
        boolean remove = false;
        for (Long prodId : prodIds) {
            if (Objects.isNull(productService.getProductByProdId(prodId, I18nMessage.getDbLang()))) {
                continue;
            }
            int count = userCollectionService.count(new LambdaQueryWrapper<UserCollection>()
                    .eq(UserCollection::getProdId, prodId)
                    .eq(UserCollection::getUserId, userId));
            if (count <= 0) {
                continue;
            }
            remove = userCollectionService.remove(new LambdaQueryWrapper<UserCollection>()
                    .eq(UserCollection::getProdId, prodId)
                    .eq(UserCollection::getUserId, userId));
        }
        return ServerResponseEntity.success(remove);
    }

    @GetMapping("count")
    @ApiOperation(value = "查询用户收藏商品数量", notes = "查询用户收藏商品数量")
    public int findProdCollectionCount() {
        String userId = SecurityUtils.getUser().getUserId();
        return userCollectionService.count(new LambdaQueryWrapper<UserCollection>().eq(UserCollection::getUserId, userId));
    }

    @GetMapping("collectionCount")
    @ApiOperation(value = "查询用户收藏数量(商品+店铺收藏数量)", notes = "查询用户收藏数量")
    public ServerResponseEntity<Integer> userCollectionCount() {
        String userId = SecurityUtils.getUser().getUserId();
        int prodCollectionCount = userCollectionService.count(new LambdaQueryWrapper<UserCollection>().eq(UserCollection::getUserId, userId));
        int shopCollectionCount = userCollectionShopService.count(new LambdaQueryWrapper<UserCollectionShop>().eq(UserCollectionShop::getUserId, userId));
        return ServerResponseEntity.success(prodCollectionCount + shopCollectionCount);
    }

    @GetMapping("/prods")
    @ApiOperation(value = "获取用户收藏商品列表", notes = "获取用户收藏商品列表")
    public ServerResponseEntity<IPage<ProductDto>> collectionProds(PageParam page) {
        String userId = SecurityUtils.getUser().getUserId();
        IPage<ProductDto> productDtoPage = productService.collectionProds(page, userId, I18nMessage.getDbLang());
        return ServerResponseEntity.success(productDtoPage);
    }

    @PostMapping("/orderProdCollectionAll")
    @ApiOperation(value = "订单商品收藏", notes = "传入商品id拼接字符串")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "prodIds", value = "商品id", required = true, dataType = "Long"),
    })
    public ServerResponseEntity<Boolean> orderProdCollectionAll(@RequestBody String prodIds) {
        String userId = SecurityUtils.getUser().getUserId();
        //去重
        HashSet<String> prodSet = CollUtil.newHashSet(prodIds.split(StrUtil.COMMA));
        List<Product> productList = productService.listByIds(prodSet);
        List<Long> prodIdList = productList.stream()
                .filter(product -> !Objects.equals(product.getProdType(), ProdType.PROD_TYPE_ACTIVE.value()))
                .map(Product::getProdId)
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(prodIdList)) {
            userCollectionService.orderProdCollectionAll(prodIdList, userId);
        }
        return ServerResponseEntity.success();
    }
}
