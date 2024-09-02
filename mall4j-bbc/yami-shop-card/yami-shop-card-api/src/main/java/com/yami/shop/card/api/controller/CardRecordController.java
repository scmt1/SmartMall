package com.yami.shop.card.api.controller;

import com.yami.shop.card.common.model.CardUseRecord;
import com.yami.shop.card.common.service.CardUseRecordService;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.security.api.util.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/p/cardRecord")
@Api(tags = "提货卡使用记录")
@AllArgsConstructor
public class CardRecordController {
    @Autowired
    private CardUseRecordService cardUseRecordService;


    @GetMapping("/getCardUseRecordList")
    @ApiOperation(value = "查询提货卡的使用记录", notes = "查询提货卡的使用记录")
    public ServerResponseEntity<Object> getCardUseRecordList(String cardNumber) {
        String userId = SecurityUtils.getUser().getUserId();
       List<CardUseRecord> list = cardUseRecordService.getCardUseRecordList(cardNumber, userId);
        return ServerResponseEntity.success(list);
    }
}
