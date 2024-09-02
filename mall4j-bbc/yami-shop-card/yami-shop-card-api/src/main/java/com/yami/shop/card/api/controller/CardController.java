package com.yami.shop.card.api.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.card.common.model.Card;
import com.yami.shop.card.common.service.CardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminCardController")
@RequestMapping("/card")
@Api(tags = "平台端会员卡接口")
public class CardController {
    @Autowired
    private CardService cardService;

    @GetMapping("/page")
    @ApiOperation(value = "分页获取会员卡列表信息")
    public ServerResponseEntity<IPage<Card>> page(Card card, PageParam<Card> page) {
        IPage<Card> couponPage = cardService.getPlatformPage(page, card);
        return ServerResponseEntity.success(couponPage);
    }


    @GetMapping("/list")
    @ApiOperation(value = "获取会员卡列表信息")
    public ServerResponseEntity<IPage<Card>> couponList(PageParam<Card> page, Card card) {
        PageParam<Card> couponPage = cardService.page(page, new LambdaQueryWrapper<Card>()
                .eq(card.getShopId() != null, Card::getShopId, card.getShopId())
                .like(StrUtil.isNotBlank(card.getCardTitle()), Card::getCardTitle, card.getCardTitle())
                .orderByDesc(Card::getCardId)
        );
        return ServerResponseEntity.success(couponPage);
    }

    @GetMapping("/info/{id}")
    @ApiOperation(value = "根据会员卡id获取会员卡信息")
    @ApiImplicitParam(name = "id", value = "会员卡id", required = true, dataType = "Long")
    public ServerResponseEntity<Card> info(@PathVariable("id") Long id) {
        Card byId = cardService.getById(id);
        return ServerResponseEntity.success(byId);
    }
}

