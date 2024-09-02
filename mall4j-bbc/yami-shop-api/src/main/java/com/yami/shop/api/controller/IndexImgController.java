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

import com.yami.shop.bean.app.dto.IndexImgDto;
import com.yami.shop.bean.model.IndexImg;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.service.IndexImgService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author LGH
 */
@RestController
@Api(tags = "首页轮播图接口")
public class IndexImgController {

    @Autowired
    private MapperFacade mapperFacade;

    @Autowired
    private IndexImgService indexImgService;

    @GetMapping("/indexImgs/{shopId}")
    @ApiOperation(value = "首页轮播图", notes = "获取首页轮播图列表信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "imgType", value = "图片类型", dataType = "Integer")
    })
    public ServerResponseEntity<List<IndexImgDto>> indexImgs(@PathVariable("shopId") Long shopId,
                                                       @RequestParam(required = false, defaultValue = "0") Integer imgType) {
        List<IndexImg> indexImgList = indexImgService.listIndexImgsByShopId(shopId, imgType);
        List<IndexImgDto> indexImgDtos = mapperFacade.mapAsList(indexImgList, IndexImgDto.class);
        return ServerResponseEntity.success(indexImgDtos);
    }
}
