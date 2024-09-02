package com.yami.shop.card.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.card.common.model.CardUser;
import com.yami.shop.card.common.service.CardUserService;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.Arith;
import com.yami.shop.common.util.PageParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController("adminCardUserController")
@RequestMapping("/platform/cardUser")
@Api(tags = "平台端用户提货卡接口")
public class CardUserController {
    @Autowired
    private CardUserService cardUserService;

    @GetMapping("/getCardUserPage")
    @ApiOperation(value = "分页获取提货卡购买信息", notes = "分页获取提货卡购买信息")
    public ServerResponseEntity<IPage<CardUser>> getCardUserPage(PageParam<CardUser> page,CardUser cardUser){
        IPage<CardUser> cardUsers = cardUserService.getCardUserPage(page,cardUser);
        return ServerResponseEntity.success(cardUsers);
    }

    @GetMapping("/downloadBuyRecord")
    @ApiOperation(value = "导出提货卡购买记录", notes = "导出提货卡购买记录")
    public void downloadBuyRecord(HttpServletResponse response, CardUser cardUser){
        cardUserService.downloadBuyRecord(cardUser,response);
    }
}

