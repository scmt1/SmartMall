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
import com.yami.shop.bean.dto.HotSearchDto;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.service.HotSearchService;
import com.yami.shop.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * @author LGH
 */
@RestController
@RequestMapping("/search")
@Api(tags = "搜索接口")
public class SearchController {

    @Autowired
    private HotSearchService hotSearchService;

    @Autowired
    private ProductService productService;

    @GetMapping("/hotSearchByShopId")
    @ApiOperation(value = "查看店铺热搜", notes = "根据店铺id,热搜数量获取热搜")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "number", value = "取数", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "sort", value = "是否按照顺序(0 否 1是)", dataType = "Integer"),
    })
    public ServerResponseEntity<List<HotSearchDto>> hotSearchByShopId(Long shopId, Integer number, Integer sort) {
        List<HotSearchDto> list = hotSearchService.getHotSearchDtoByshopId(shopId);
        return getListResponseEntity(number, sort, list);
    }

    @GetMapping("/hotSearch")
    @ApiOperation(value = "查看全局热搜", notes = "根据店铺id,热搜数量获取热搜")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "number", value = "取数", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "sort", value = "是否按照顺序(0 否 1是)", dataType = "Integer"),
    })
    public ServerResponseEntity<List<HotSearchDto>> hotSearch(Integer number, Integer sort) {
        List<HotSearchDto> list = hotSearchService.getHotSearchDtoByshopId(0L);
        return getListResponseEntity(number, sort, list);
    }

    private ServerResponseEntity<List<HotSearchDto>> getListResponseEntity(Integer number, Integer sort, List<HotSearchDto> list) {
        if (sort == null || sort == 0) {
            Collections.shuffle(list);
        }
/*        if(sort==1){
            Collections.sort(list, new SeqComparator()); // 根据排序号排序
        }*/
        if (!CollectionUtil.isNotEmpty(list) || list.size() < number) {
            return ServerResponseEntity.success(list);
        }

        return ServerResponseEntity.success(list.subList(0, number));
    }
}
