package com.yami.shop.card.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yami.shop.bean.model.ShopDetail;
import com.yami.shop.card.api.params.ReceiveCardParam;
import com.yami.shop.card.common.dto.CardDto;
import com.yami.shop.card.common.dto.CardUserDto;
import com.yami.shop.card.common.model.Card;
import com.yami.shop.card.common.model.CardShop;
import com.yami.shop.card.common.model.CardUseRecord;
import com.yami.shop.card.common.model.CardUser;
import com.yami.shop.card.common.service.CardService;
import com.yami.shop.card.common.service.CardShopService;
import com.yami.shop.card.common.service.CardUseRecordService;
import com.yami.shop.card.common.service.CardUserService;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.SysConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 我的会员卡接口
 *
 * @author lanhai
 */
@RestController
@RequestMapping("/p/myCard")
@Api(tags = "我的会员卡接口")
@AllArgsConstructor
public class MyCardController {

    private CardService cardService;
    private CardUserService cardUserService;
    private CardShopService cardShopService;
    private CardUseRecordService cardUseRecordService;
    private final SysConfigService sysConfigService;


    @PostMapping("/receive")
    @ApiOperation(value = "领取会员卡接口", notes = "领取会员卡接口")
    @ApiImplicitParam(name = "cardId", value = "会员卡ID", required = true, dataType = "Long")
    public ServerResponseEntity<String> receive(@Valid @RequestBody ReceiveCardParam receiveCardParam) {
        String userId = SecurityUtils.getUser().getUserId();
        Card card = cardService.getById(receiveCardParam.getCardId());
        if(card == null) {
            throw new YamiShopBindException("会员卡不存在！");
        }

        int count = cardUserService.count(new LambdaQueryWrapper<CardUser>().eq(CardUser::getCardId, card.getCardId()).eq(CardUser::getUserId, userId));
        if(count > 0) {
            throw new YamiShopBindException("您已经领取过该会员卡了！");
        }


        CardUser cardUser = new CardUser();
        cardUser.setUserId(userId);
        cardUser.setCardId(card.getCardId());
        cardUser.setReceiveTime(new Date());
        cardUser.setScore(0.0);
        cardUser.setBalance(0.0);
        cardUser.setStatus(0);
        cardUser.setIsDelete(0);
        cardUser.setCardNumber(card.getCardCode());
        cardUserService.save(cardUser);

        // 领取会员卡成功
        return ServerResponseEntity.success(I18nMessage.getMessage("yami.Card.receive.success"));
    }


    @Deprecated
    @DeleteMapping("/deleteCard/{cardId}")
    @ApiOperation(value = "删除用户会员卡", notes = "通过会员卡id删除用户会员卡")
    @ApiImplicitParam(name = "CardId", value = "会员卡Id", required = true, dataType = "Long")
    public ServerResponseEntity<String> deleteCard(@PathVariable("cardId") Long cardId) {
        String userId = SecurityUtils.getUser().getUserId();
        cardService.deleteUserCardByCardId(userId, cardId);
        // 删除成功
        return ServerResponseEntity.success(I18nMessage.getMessage("yami.delete.successfully"));
    }

    @DeleteMapping("/delCard/{cardUserId}")
    @ApiOperation(value = "删除用户会员卡", notes = "通过会员卡关联id删除用户会员卡")
    @ApiImplicitParam(name = "cardUserId", value = "会员卡关联id", required = true, dataType = "Long")
    public ServerResponseEntity<String> deleteCardById(@PathVariable("cardUserId") Long cardUserId) {
        String userId = SecurityUtils.getUser().getUserId();
        CardUser cardUser = new CardUser();
        cardUser.setCardUserId(cardUserId);
        cardUser.setIsDelete(1);
        cardUserService.update(cardUser, new LambdaUpdateWrapper<CardUser>()
                .eq(CardUser::getCardUserId, cardUserId).eq(CardUser::getUserId, userId));
        // 删除成功
        return ServerResponseEntity.success(I18nMessage.getMessage("yami.delete.successfully"));
    }


    @GetMapping("/getCardByCardUserId")
    @ApiOperation(value = "根据用户会员卡id获取信息")
    @ApiImplicitParam(name = "cardUserId", value = "用户会员卡id", dataType = "Long")
    public ServerResponseEntity<CardDto> info(@RequestParam(value = "cardUserId") Long cardUserId) {
        CardDto CardUser = cardService.getCardUserInfo(cardUserId);
        CardUser.setEncrypCardNumber(CardUser.getCardNumber().replaceAll("(\\d{3})\\d{5}(\\d{4})", "$1*****$2"));
        return ServerResponseEntity.success(CardUser);
    }

    @GetMapping("/getCardShopListByCardId")
    @ApiOperation(value = "根据卡id获取店铺信息")
    @ApiImplicitParam(name = "cardId", value = "卡id", dataType = "Long")
    public ServerResponseEntity<List<CardShop>> getCardShopListByCardId(@RequestParam(value = "cardId") Long cardId) {
        List<CardShop> cardShops = cardShopService.queryCardShopList(cardId);
        return ServerResponseEntity.success(cardShops);
    }

    @GetMapping("/getNoCardShopListByCardId")
    @ApiOperation(value = "根据卡id获取不可用店铺信息")
    @ApiImplicitParam(name = "cardId", value = "卡id", dataType = "Long")
    public ServerResponseEntity<List<CardShop>> getNoCardShopListByCardId(@RequestParam(value = "cardId") Long cardId) {
        Card byId = cardService.getById(cardId);
        if(byId.getSuitableProdType() == 1){
            String cardNoUseShop = sysConfigService.getConfigValue("CARD_NO_USE_SHOP");
            List<Long> shops = Arrays.stream(cardNoUseShop.split(",")).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
            List<CardShop> noUseBalanceShopList = cardShopService.getConfigNoUseCardShopList(shops);
            return ServerResponseEntity.success(noUseBalanceShopList);
        }else{
            List<CardShop> cardShops = cardShopService.queryCardShopList(cardId);
            List<Long> shops = cardShops.stream().map(item -> item.getShopId()).collect(Collectors.toList());
            List<CardShop> noUseBalanceShopList = cardShopService.getNoUseCardShopList(shops);
            return ServerResponseEntity.success(noUseBalanceShopList);
        }
    }

    @GetMapping("/getCardListByUserId")
    @ApiOperation(value = "根据用户id获取卡列表")
    public ServerResponseEntity<List<CardUserDto>> getCardListByUserId(CardUser cardUser) {
        String userId = SecurityUtils.getUser().getUserId();
        cardUser.setUserId(userId);
        List<CardUserDto> cardUserList = cardUserService.getCardUserList(cardUser);
        return ServerResponseEntity.success(cardUserList);
    }

    @PostMapping("/bindCard")
    @ApiOperation(value = "用户绑定提货卡", notes = "用户绑定提货卡")
    public ServerResponseEntity<String> bindCard(@Valid @RequestBody ReceiveCardParam receiveCardParam) {
        String userId = SecurityUtils.getUser().getUserId();
        Card card = cardService.getOne(new LambdaQueryWrapper<Card>().eq(Card::getCardCode, receiveCardParam.getCardNumber()));
        if(card == null) {
            return ServerResponseEntity.showFailMsg("卡号不正确！");
        }
        if(card.getCardType() != 1) {
            return ServerResponseEntity.showFailMsg("该卡类型不支持绑定！");
        }

//        if(!Objects.equals(card.getPassword(), receiveCardParam.getPassword())) {
//            throw new YamiShopBindException("卡号密码不正确！");
//        }

        if(card.getStatus() == 0 || card.getStatus() == 1) {
            return ServerResponseEntity.showFailMsg("该卡暂无法绑定！");
        }
        if(card.getStatus() == 4) {
            return ServerResponseEntity.showFailMsg("该卡已被冻结，请联系相关人员处理！");
        }

        int count = cardUserService.count(new LambdaQueryWrapper<CardUser>().eq(CardUser::getCardNumber, card.getCardCode()));
        if(count > 0) {
            return ServerResponseEntity.showFailMsg("该卡已被绑定！");
        }

        Double balance = card.getBalance();
        //这里余额需要计算一下，去查询使用记录，减去已使用的
        CardUseRecord cardUseRecord = cardUseRecordService.queryCardUseTotalBalance(card.getCardCode());
        if(cardUseRecord != null) {
            balance = balance - cardUseRecord.getAmount();
        }
        CardUser cardUser = new CardUser();
        cardUser.setUserId(userId);
        cardUser.setCardId(card.getCardId());
        cardUser.setReceiveTime(new Date());
        cardUser.setScore(0.0);
        cardUser.setBalance(balance);
        cardUser.setStatus(1);
        cardUser.setIsDelete(0);
        cardUser.setCardNumber(card.getCardCode());
        cardUser.setUserStartTime(card.getUserStartTime());
        cardUser.setUserEndTime(card.getUserEndTime());
        cardUserService.save(cardUser);

        card.setStatus(3);
        cardService.updateById(card);
        // 领取会员卡成功
        return ServerResponseEntity.success(I18nMessage.getMessage("yami.Card.receive.success"));
    }

    @GetMapping("/unbindingCardByCardUserId")
    @ApiOperation(value = "解绑提货卡")
    @Transactional
    @ApiImplicitParam(name = "cardUserId", value = "用户卡id", dataType = "Long")
    public ServerResponseEntity<Void> unbindingCardByCardUserId(@RequestParam(value = "cardUserId") Long cardUserId) {
        CardUser byId = cardUserService.getById(cardUserId);
        boolean b = cardUserService.removeById(cardUserId);
        Card card = cardService.getById(byId.getCardId());
        boolean cardUpdate = cardService.update(Wrappers.lambdaUpdate(Card.class)
                .set(Card::getStatus, card.getStatus() == 4 ? card.getStatus() : 2)
                .eq(Card::getCardId, byId.getCardId()));
        if(b && cardUpdate){
            return ServerResponseEntity.success();
        }else{
            return ServerResponseEntity.showFailMsg("解绑失败");
        }
    }

    @GetMapping("/lossCardByCardUserId")
    @ApiOperation(value = "挂失提货卡")
    @Transactional
    @ApiImplicitParam(name = "cardUserId", value = "用户卡id", dataType = "Long")
    public ServerResponseEntity<Void> lossCardByCardUserId(@RequestParam(value = "cardUserId") Long cardUserId) {
        CardUser byId = cardUserService.getById(cardUserId);
        boolean cardUpdate = cardService.update(Wrappers.lambdaUpdate(Card.class)
                .set(Card::getStatus, 4)
                .eq(Card::getCardId, byId.getCardId()));
        if(cardUpdate){
            return ServerResponseEntity.success();
        }else{
            return ServerResponseEntity.showFailMsg("挂失失败");
        }
    }
}
