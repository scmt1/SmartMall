package com.yami.shop.card.multishop.controller;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.card.common.model.Card;
import com.yami.shop.card.common.model.CardShop;
import com.yami.shop.card.common.model.CardUseRecord;
import com.yami.shop.card.common.model.CardUser;
import com.yami.shop.card.common.service.CardService;
import com.yami.shop.card.common.service.CardShopService;
import com.yami.shop.card.common.service.CardUseRecordService;
import com.yami.shop.card.common.service.CardUserService;
import com.yami.shop.card.common.utils.SnowflakeIdWorker;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.Arith;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.multishop.util.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Date;
import java.util.Objects;

@RestController("adminCardUserController")
@RequestMapping("/admin/cardUser")
@Api(tags = "商家端会员卡接口")
public class CardUserController {
    @Autowired
    private CardUserService cardUserService;
    @Autowired
    private CardService cardService;
    @Autowired
    private CardUseRecordService cardUseRecordService;

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

    @GetMapping("/getCardBalance")
    @ApiOperation(value = "根据会员卡编号获取卡余额信息")
    public ServerResponseEntity<CardUser> info(@RequestParam(value = "cardCode") String cardCode) {
        QueryWrapper<Card> cardQueryWrapper = new QueryWrapper<>();
        cardQueryWrapper.lambda().eq(Card::getCardCode,cardCode);
        Card one = cardService.getOne(cardQueryWrapper);
        if(one != null){
            if(one.getStatus() == 0 || one.getStatus() == 1 || one.getStatus() == 4){
                return ServerResponseEntity.showFailMsg("该卡暂无法使用！");
            }
            CardUser cardUser = null;
            if(one.getStatus() == 3){
                QueryWrapper<CardUser> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(CardUser::getCardNumber,cardCode);
                queryWrapper.lambda().eq(CardUser::getIsDelete,0);
                cardUser = cardUserService.getOne(queryWrapper);
            }
            if(cardUser == null){
                //未绑卡计算余额
                CardUseRecord cardUseRecord = cardUseRecordService.queryCardUseTotalBalance(cardCode);
                CardUser cardUser1 = new CardUser();
                double balance = Arith.sub(one.getBalance(), cardUseRecord.getAmount());
                cardUser1.setBalance(balance);
                return ServerResponseEntity.success(cardUser1);
            }else{
                return ServerResponseEntity.success(cardUser);
            }
        }else{
            return ServerResponseEntity.showFailMsg("未查询到信息，提货卡号有误！");
        }
    }
}

