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


import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yami.shop.bean.model.QrcodeTicket;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.IdUtil;
import com.yami.shop.config.WxConfig;
import com.yami.shop.service.QrcodeTicketService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Date;
import java.util.Objects;


/**
 * 二维码数据信息
 *
 * @author LGH
 * @date 2020-03-12 16:29:39
 */
@RestController
@AllArgsConstructor
@RequestMapping("/qrcodeTicket" )
@Api(tags="二维码数据")
public class QrcodeTicketController {

    private final QrcodeTicketService qrcodeTicketService;

    private final WxConfig wxConfig;

    private final IdUtil idUtil;

    @GetMapping("/miniQrCode")
    @ApiOperation(value="获取小程序二维码", notes="获取小程序二维码，返回二维码图片流，小程序跳到二维码的页面之后，" +
            "需要根据获取的scene请求获取线上保存的content，为什么要这么麻烦，以为scene的内容有限，只能在数据库保存")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "content", value = "要保存的内容", required = true, dataType = "String"),
            @ApiImplicitParam(name = "type", value = "类型：1. 小程序团购商品 2.小程序分销商品二维码", required = true, dataType = "Integer")
    })
    public ResponseEntity<FileSystemResource> save(String content, Integer type) throws WxErrorException {
        String page;
        if(Objects.equals(type, 1) || Objects.equals(type, 2)) {
            page = "pages/prod/prod";
        } else {
            // 无法获取页面信息
            throw new YamiShopBindException("yami.unable.get.page");
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("type", type);
        jsonObject.set("content", content);

        String ticket = idUtil.nextShortId();
        QrcodeTicket qrcodeTicket = new QrcodeTicket();
        qrcodeTicket.setContent(jsonObject.toString());
        qrcodeTicket.setCreateTime(new Date());
        qrcodeTicket.setType(type);
        qrcodeTicket.setTicket(ticket);
        qrcodeTicket.setTicketUrl(page);
        qrcodeTicketService.save(qrcodeTicket);
        File file = wxConfig.getWxMaService().getQrcodeService().createWxaCodeUnlimit(ticket, page);
        return ResponseEntity
                .ok()
                .contentLength(file.length())
                .contentType(MediaType.IMAGE_PNG)
                .body(new FileSystemResource(file));
    }

    @GetMapping("/getContent")
    @ApiOperation(value="根据Ticket获取保存的内容", notes="小程序里的scene就是你要的Ticket")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ticket", value = "小程序里的scene就是你要的Ticket", required = true, dataType = "String")
    })
    public ServerResponseEntity<QrcodeTicket> getContent(String ticket) {
        QrcodeTicket qrcodeTicket = qrcodeTicketService.getOne(new LambdaQueryWrapper<QrcodeTicket>().eq(QrcodeTicket::getTicket, ticket));
        if (qrcodeTicket == null) {
            // 二维码已过期
            throw new YamiShopBindException("yami.constant.code.expire");
        }
        if (qrcodeTicket.getExpireTime() !=null
                && qrcodeTicket.getExpireTime().getTime() < System.currentTimeMillis()) {
            throw new YamiShopBindException("yami.constant.code.expire");
        }
        return ServerResponseEntity.success(qrcodeTicket);
    }
}
