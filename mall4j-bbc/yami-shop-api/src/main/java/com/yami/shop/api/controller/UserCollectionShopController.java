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


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.shop.bean.app.dto.UserCollectionShopDto;
import com.yami.shop.bean.enums.RenovationType;
import com.yami.shop.bean.model.ShopRenovation;
import com.yami.shop.bean.model.UserCollectionShop;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.ShopDetailService;
import com.yami.shop.service.ShopRenovationService;
import com.yami.shop.service.UserCollectionShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author LGH
 */
@RestController
@RequestMapping("/p/shop/collection")
@Api(tags = "店铺收藏接口")
@AllArgsConstructor
public class UserCollectionShopController {

    private final UserCollectionShopService userCollectionShopService;

    private final ShopDetailService shopDetailService;

    private final ShopRenovationService shopRenovationService;

    @GetMapping("/page")
    @ApiOperation(value = "分页返回收藏数据", notes = "根据用户id获取")
    @ApiImplicitParam(name = "shopName", value = "店铺名称", dataType = "String")
    public ServerResponseEntity<IPage<UserCollectionShopDto>> getUserCollectionDtoPageByUserId(PageParam page, String shopName) {
        Page<UserCollectionShopDto> userCollectionShopPageByUserId = userCollectionShopService.getUserCollectionShopPageByUserId(page, SecurityUtils.getUser().getUserId(), shopName);
        List<UserCollectionShopDto> records = userCollectionShopPageByUserId.getRecords();
        for (UserCollectionShopDto userCollectionShopDto:records) {
            ShopRenovation shopRenovation = shopRenovationService.getOne(new LambdaQueryWrapper<ShopRenovation>()
                    .eq(ShopRenovation::getShopId, userCollectionShopDto.getShopId()).eq(ShopRenovation::getHomeStatus, 1).eq(ShopRenovation::getRenovationType, RenovationType.H5.value()));
            userCollectionShopDto.setRenovationId(Objects.isNull(shopRenovation) ? null : shopRenovation.getRenovationId());
        }
        return ServerResponseEntity.success(userCollectionShopPageByUserId);
    }

    @GetMapping("isCollection")
    @ApiOperation(value = "根据店铺id获取该店铺是否在收藏夹中", notes = "传入收藏店铺id")
    @ApiImplicitParam(name = "shopId", value = "店铺id", dataType = "Long")
    public ServerResponseEntity<Boolean> isCollection(Long shopId) {
        return ServerResponseEntity.success(userCollectionShopService.count(new LambdaQueryWrapper<UserCollectionShop>()
                .eq(UserCollectionShop::getShopId, shopId)
                .eq(UserCollectionShop::getUserId, SecurityUtils.getUser().getUserId())) > 0);
    }

    @PostMapping("/addOrCancel")
    @ApiOperation(value = "添加/取消收藏", notes = "传入收藏店铺id,如果店铺未收藏则收藏店铺，已收藏则取消收藏")
    @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "Long")
    public ServerResponseEntity<Boolean> addOrCancel(@RequestBody Long shopId) {
        if (Objects.isNull(shopDetailService.getById(shopId))) {
            // 该店铺不存在
            throw new YamiShopBindException("yami.store.not.exist");
        }
        boolean isAdd = false;
        String userId = SecurityUtils.getUser().getUserId();
        if (userCollectionShopService.count(new LambdaQueryWrapper<UserCollectionShop>()
                .eq(UserCollectionShop::getShopId, shopId)
                .eq(UserCollectionShop::getUserId, userId)) > 0) {
            userCollectionShopService.remove(new LambdaQueryWrapper<UserCollectionShop>()
                    .eq(UserCollectionShop::getShopId, shopId)
                    .eq(UserCollectionShop::getUserId, userId));
        } else {
            UserCollectionShop userCollectionShop = new UserCollectionShop();
            userCollectionShop.setCreateTime(new Date());
            userCollectionShop.setUserId(userId);
            userCollectionShop.setShopId(shopId);
            userCollectionShopService.save(userCollectionShop);
            isAdd = true;
        }
        return ServerResponseEntity.success(isAdd);
    }

    @PostMapping("/batachCancel")
    @ApiOperation(value = "批量取消收藏", notes = "传入收藏店铺id")
    @ApiImplicitParam(name = "shopIds", value = "店铺Id", required = true, dataType = "Long")
    public ServerResponseEntity<Boolean> batchCancel(@RequestBody List<Long> shopIds) {
        String userId = SecurityUtils.getUser().getUserId();
        if (CollectionUtils.isEmpty(shopIds)) {
            return ServerResponseEntity.success(false);
        }
        boolean remove = false;
        for (Long shopId : shopIds) {
            if (Objects.isNull(shopDetailService.getById(shopId))) {
                continue;
            }
            int count = userCollectionShopService.count(new LambdaQueryWrapper<UserCollectionShop>()
                    .eq(UserCollectionShop::getShopId, shopId)
                    .eq(UserCollectionShop::getUserId, userId));
            if (count <= 0) {
                continue;
            }
            remove = userCollectionShopService.remove(new LambdaQueryWrapper<UserCollectionShop>()
                    .eq(UserCollectionShop::getShopId, shopId)
                    .eq(UserCollectionShop::getUserId, userId));
        }
        return ServerResponseEntity.success(remove);
    }

    @GetMapping("count")
    @ApiOperation(value = "查询用户收藏店铺数量", notes = "查询用户收藏店铺数量")
    public int findUserCollectionCount() {
        String userId = SecurityUtils.getUser().getUserId();
        return userCollectionShopService.count(new LambdaQueryWrapper<UserCollectionShop>().eq(UserCollectionShop::getUserId, userId));
    }
}
