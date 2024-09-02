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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.app.dto.NoticeDto;
import com.yami.shop.bean.model.Notice;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author LGH
 */
@RestController
@RequestMapping("/shop/notice")
@Api(tags = "公告管理接口")
@AllArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    private final MapperFacade mapperFacade;

    @GetMapping("/topNoticeList/{shopId}")
    @ApiOperation(value = "置顶公告列表信息", notes = "获取所有置顶公告列表信息")
    @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "Long")
    public ServerResponseEntity<List<NoticeDto>> getTopNoticeListByShopId(@PathVariable("shopId") Long shopId) {
        List<Notice> noticeList = noticeService.listTopNoticeByShopId(shopId);
        List<NoticeDto> noticeDtoList = mapperFacade.mapAsList(noticeList, NoticeDto.class);
        return ServerResponseEntity.success(noticeDtoList);
    }

    @GetMapping("/info/{id}")
    @ApiOperation(value = "公告详情", notes = "获取公告id公告详情")
    @ApiImplicitParam(name = "id", value = "公告id", required = true, dataType = "Long")
    public ServerResponseEntity<NoticeDto> getNoticeById(@PathVariable("id") Long id) {
        Notice notice = noticeService.getNoticeById(id);
        NoticeDto noticeDto = mapperFacade.map(notice, NoticeDto.class);
        return ServerResponseEntity.success(noticeDto);
    }

    /**
     * 公告列表
     */
    @GetMapping("/noticeList/{shopId}")
    @ApiOperation(value = "公告列表信息", notes = "获取所有公告列表信息")
    @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "Long")
    public ServerResponseEntity<IPage<NoticeDto>> pageNotice(@PathVariable("shopId") Long shopId, PageParam<NoticeDto> page) {
        Notice notice = new Notice();
        notice.setShopId(shopId);
        return ServerResponseEntity.success(noticeService.pageNotice(page, notice));
    }
}
