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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.shop.bean.app.dto.ShopHeadInfoDto;
import com.yami.shop.bean.enums.ProdStatusEnums;
import com.yami.shop.bean.enums.RenovationType;
import com.yami.shop.bean.model.*;
import com.yami.shop.bean.param.ShopSearchParam;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * @author lgh on 2018/08/29.
 */
@RestController
@RequestMapping("/shop")
@Api(tags = "店铺相关接口")
@AllArgsConstructor
public class ShopDetailController {

    private final ShopDetailService shopDetailService;

    private final UserCollectionShopService userCollectionShopService;

    private final ShopRenovationService shopRenovationService;

    private final MapperFacade mapperFacade;

    private final ProductService productService;

    private final ShopMchService shopMchService;

    private final DictService dictService;

    private final DictDataService dictDataService;

    private final SysConfigService sysConfigService;

    @GetMapping("/headInfo")
    @ApiOperation(value = "店铺头部信息", notes = "获取的店铺头部信息")
    @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "Long")
    public ServerResponseEntity<ShopHeadInfoDto> getShopHeadInfo(Long shopId) {
        ShopHeadInfoDto shopHeadInfoDto = new ShopHeadInfoDto();
        // 粉丝数量
        int fansCount = userCollectionShopService.count(new LambdaQueryWrapper<UserCollectionShop>().eq(UserCollectionShop::getShopId, shopId));
        // TODO 先默认是移动端的装修页面，后续删除此处装修相关的代码
        ShopRenovation shopRenovation = shopRenovationService.getOne(new LambdaQueryWrapper<ShopRenovation>()
                .eq(ShopRenovation::getShopId, shopId).eq(ShopRenovation::getHomeStatus, 1).eq(ShopRenovation::getRenovationType, RenovationType.H5.value()));
        ShopDetail shopDetail = shopDetailService.getShopDetailByShopId(shopId);
        shopHeadInfoDto.setShopStatus(shopDetail.getShopStatus());
        if (!Objects.equals(shopDetail.getShopStatus(), 1)) {
            return ServerResponseEntity.success(shopHeadInfoDto);
        }
        shopHeadInfoDto.setRenovationId(Objects.isNull(shopRenovation) ? null : shopRenovation.getRenovationId());
        shopHeadInfoDto.setShopId(shopId);
        shopHeadInfoDto.setShopLogo(shopDetail.getShopLogo());
        shopHeadInfoDto.setShopName(shopDetail.getShopName());
        shopHeadInfoDto.setIntro(shopDetail.getIntro());
        shopHeadInfoDto.setFansCount(fansCount);
        shopHeadInfoDto.setTel(shopDetail.getTel());
        shopHeadInfoDto.setType(shopDetail.getType());
        shopHeadInfoDto.setContractStartTime(shopDetail.getContractStartTime());
        shopHeadInfoDto.setContractEndTime(shopDetail.getContractEndTime());
        shopHeadInfoDto.setShopTopImg(shopDetail.getShopTopImg());
        shopHeadInfoDto.setStoreType(shopDetail.getStoreType());
        shopHeadInfoDto.setIndustryType(shopDetail.getIndustryType());
        return ServerResponseEntity.success(shopHeadInfoDto);
    }

    @GetMapping("/getShopInfo")
    @ApiOperation(value = "店铺信息（装修）", notes = "获取的店铺信息（装修）")
    @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "Long")
    public ServerResponseEntity<ShopHeadInfoDto> getShopInfo(Long shopId) {
        ShopDetail shopDetail = shopDetailService.getShopDetailByShopId(shopId);
        ShopHeadInfoDto shopHeadInfoDto = mapperFacade.map(shopDetail, ShopHeadInfoDto.class);
        return ServerResponseEntity.success(shopHeadInfoDto);
    }

    @GetMapping("/hotShops")
    @ApiOperation(value = "热门店铺", notes = "热门店铺")
    public ServerResponseEntity<List<ShopHeadInfoDto>> hotShops() {
        List<ShopHeadInfoDto> hotShopsHead = shopDetailService.listHotShopsHead();
        return ServerResponseEntity.success(hotShopsHead);
    }

    @GetMapping("/searchShops")
    @ApiOperation(value = "搜索店铺", notes = "根据店铺名称搜索店铺")
    public ServerResponseEntity<Page<ShopHeadInfoDto>> searchShops(PageParam<ShopHeadInfoDto> page, ShopHeadInfoDto shopHeadInfoDto) {
        Page<ShopHeadInfoDto> hotShopsHeadPage = shopDetailService.searchShops(page, shopHeadInfoDto);
        return ServerResponseEntity.success(hotShopsHeadPage);
    }

    @GetMapping("/listRenovationShop")
    @ApiOperation(value = "获取装修店铺列表信息", notes = "获取装修店铺列表信息")
    public ServerResponseEntity<List<ShopHeadInfoDto>> listRenovationShop(ShopSearchParam shopSearchParam) {
        List<ShopHeadInfoDto> shopPage = shopDetailService.listRenovationShop(shopSearchParam);
        return ServerResponseEntity.success(shopPage);
    }

    @GetMapping("/listShop")
    @ApiOperation(value = "获取店铺列表", notes = "获取店铺列表")
    public ServerResponseEntity<List<ShopDetail>> listShop(ShopSearchParam shopSearchParam) {
        List<ShopDetail> shopPage = shopDetailService.listShop(shopSearchParam);
        for (ShopDetail shopDetail:shopPage) {
            ShopRenovation shopRenovation = shopRenovationService.getOne(new LambdaQueryWrapper<ShopRenovation>()
                    .eq(ShopRenovation::getShopId, shopDetail.getShopId()).eq(ShopRenovation::getHomeStatus, 1).eq(ShopRenovation::getRenovationType, RenovationType.H5.value()));
            shopDetail.setRenovationId(Objects.isNull(shopRenovation) ? null : shopRenovation.getRenovationId());

            shopDetail.setProdNum(productService.getProdNum(shopDetail.getShopId(), ProdStatusEnums.NORMAL.getValue()));
        }
        return ServerResponseEntity.success(shopPage);
    }

    @GetMapping("/getFloorList")
    @ApiOperation(value = "获取楼层", notes = "获取楼层")
    public ServerResponseEntity<List<String>> getFloorList() {
        List<String> shopPage = shopDetailService.getFloorList();
        return ServerResponseEntity.success(shopPage);
    }

    @GetMapping("/getIndustryType/{type}")
    @ApiOperation(value = "获取行业类型", notes = "获取行业类型")
    public ServerResponseEntity<List<DictData>> getIndustryType(@PathVariable String type) {
        Dict dict = dictService.findByType(type);
        if (dict == null) {
            return ServerResponseEntity.showFailMsg("字典类型 " + type + " 不存在");
        }
        List<DictData> dictDataList = dictDataService.findByDictId(dict.getId());
        return ServerResponseEntity.success(dictDataList);
    }

    @GetMapping("/getShopMchInfo")
    @ApiOperation(value = "获取行业类型", notes = "获取行业类型")
    public ServerResponseEntity<ShopMch> getShopMchInfo(Long shopId) {
        if (shopId == null) {
            return ServerResponseEntity.showFailMsg("参数有误");
        }
        QueryWrapper<ShopMch> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ShopMch::getShopId,shopId);
        ShopMch shopMch = shopMchService.getOne(queryWrapper);
        return ServerResponseEntity.success(shopMch);
    }

    @GetMapping("/getShopInfoByShopIds")
    @ApiOperation(value = "获取服饰立减店铺", notes = "获取服饰立减店铺")
    public ServerResponseEntity<List<ShopDetail>> getShopInfoByShopIds() {
        String orderShop = sysConfigService.getConfigValue("ORDER_SHOP");
        List<Long> shops = Arrays.stream(orderShop.split(",")).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
        List<ShopDetail> shopDetailByShopIds = shopDetailService.getShopDetailByShopIds(shops);
        return ServerResponseEntity.success(shopDetailByShopIds);
    }
}
