package com.yami.shop.card.multishop.controller;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.model.ShopDetail;
import com.yami.shop.card.common.model.Card;
import com.yami.shop.card.common.model.CardShop;
import com.yami.shop.card.common.model.CardUseRecord;
import com.yami.shop.card.common.service.CardService;
import com.yami.shop.card.common.service.CardShopService;
import com.yami.shop.card.common.service.CardUseRecordService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@RestController("adminCardController")
@RequestMapping("/admin/card")
@Api(tags = "商家端提货卡接口")
public class CardController {
    @Autowired
    private CardService cardService;

    @Autowired
    private CardShopService cardShopService;

    @Autowired
    private CardUseRecordService cardUseRecordService;
    @Autowired
    private Snowflake snowflake;

    @GetMapping("/page")
    @ApiOperation(value = "分页获取提货卡列表信息")
    public ServerResponseEntity<IPage<Card>> page(Card card, PageParam<Card> page) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        card.setShopId(shopId);
        IPage<Card> couponPage = cardService.getPlatformPage(page, card);
        for (Card cardInfo:couponPage.getRecords()) {
            CardUseRecord cardUseRecord = cardUseRecordService.queryCardUseTotalBalance(cardInfo.getCardCode());
            cardInfo.setRemainingAmount(Arith.sub(cardInfo.getBalance(),cardUseRecord.getAmount()));
        }
        return ServerResponseEntity.success(couponPage);
    }

    @GetMapping("/batchCardPage")
    @ApiOperation(value = "分页获取批次列表信息")
    public ServerResponseEntity<IPage<Card>> batchCardPage(Card card, PageParam<Card> page) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        card.setShopId(shopId);
        IPage<Card> couponPage = cardService.getPlatformBatchPage(page, card);
        return ServerResponseEntity.success(couponPage);
    }

    @GetMapping("/info/{id}")
    @ApiOperation(value = "根据会员卡id获取会员卡信息")
    @ApiImplicitParam(name = "id", value = "会员卡id", required = true, dataType = "Long")
    public ServerResponseEntity<Card> info(@PathVariable("id") Long id) {
        Card byId = cardService.getById(id);
        if (!Objects.equals(SecurityUtils.getShopUser().getShopId(), byId.getShopId())) {
            throw new YamiShopBindException("yami.no.auth");
        }
        return ServerResponseEntity.success(byId);
    }


    @DeleteMapping
    @ApiOperation(value = "根据会员卡id删除会员卡")
    @ApiImplicitParam(name = "cardId", value = "优惠券id", required = true, dataType = "Long")
    public ServerResponseEntity<Void> delete(@RequestBody Long cardId) {
        cardService.deleteByCardId(cardId);
        return ServerResponseEntity.success();
    }


    @PostMapping
    @ApiOperation(value = "保存提货卡")
    public ServerResponseEntity<Void> save(@RequestBody @Valid Card card) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        // 批次号第一部分：时间
        DateFormat dateFormat = new SimpleDateFormat("yyyyMM");
        String currentTimeStr = dateFormat.format(new Date());
        // 批次号第二部分：编号
        QueryWrapper<Card> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("batch_number",currentTimeStr);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("LIMIT 1");
        Card one = cardService.getOne(queryWrapper);
        String cusCodeStr = "";
        if(one != null){
            String s = one.getBatchNumber().split(currentTimeStr)[1];
            int num = Integer.parseInt(s);
            cusCodeStr = String.format("%04d", num + 1);
        }else{
            cusCodeStr = "0001";
        }
        card.setBatchNumber(currentTimeStr + cusCodeStr);
        card.setBatchTime(new Date());
        card.setBatchStatus(0);
        long index = 1;
        for (int i = 0; i < card.getCardNum(); i++) {
            card.setShopId(shopId);
            card.setCreateTime(new Date());
            card.setIsDelete(0);
            card.setStatus(0);
            card.setSuitableProdType(2); //使用范围只能是本店铺
            card.setCreateType(1);
            card.setCardCode(checkQrCodeExists().toString());
            card.setBuyCardType(0);
            //设置卡号  卡密  随机生成
            String code = "";
            if(String.valueOf(index).length() == 1) {
                code = "000" + index;
            }else if(String.valueOf(index).length() == 2) {
                code = "00" + index;
            }else if(String.valueOf(index).length() == 3) {
                code = "0" + index;
            }else {
                code = String.valueOf(index);
            }
            card.setCardNumber(card.getCardPrefix() +  code);
            card.setPassword(String.valueOf(SnowflakeIdWorker.generateIdNum6()));
            cardService.save(card);
            CardShop cardShop = new CardShop();
            cardShop.setCardId(card.getCardId());
            cardShop.setCreateTime(new Date());
            cardShop.setShopId(shopId);
            cardShopService.save(cardShop);
            index ++;
        }
        return ServerResponseEntity.success();
    }

    @PutMapping
    @ApiOperation(value = "修改提货卡")
    public ServerResponseEntity<Void> update(@RequestBody @Valid Card card) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        // 查询平台下优惠券
        Card dbCard = cardService.getById(card.getCardId());
        if (null == dbCard) {
            throw new YamiShopBindException("该提货卡不存在");
        }
        if (!Objects.equals(dbCard.getShopId(), shopId)) {
            // 没有权限修改该优惠券信息
            throw new YamiShopBindException("yami.no.auth");
        }
        if(dbCard.getBatchStatus() == 0 || dbCard.getBatchStatus() == 1){
            if(card.getStatus() == 0){
                card.setBatchStatus(0);
            }
            if(card.getStatus() == 1){
                card.setBatchStatus(1);
            }
        }
        if(card.getStatus() > 1){
            card.setBatchStatus(2);
        }
        card.setShopId(shopId);
        cardService.updateById(card);
        return ServerResponseEntity.success();
    }


    private Long checkQrCodeExists() {
        //生成7位随机数
        Long integer = SnowflakeIdWorker.generateIdNum();
        QueryWrapper<Card> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_number", integer);
        queryWrapper.last("LIMIT 1");
        int count = cardService.count(queryWrapper);
        if (count > 0) {
            return checkQrCodeExists();
        }
        return integer;
    }


    /**
     * 查询当前店铺所核销的提货卡记录
     */

    @GetMapping("/queryWriteOffCardList")
    @ApiOperation(value = "查询当前店铺所核销的提货卡记录")
    public ServerResponseEntity<Object> queryWriteOffCardList(CardUseRecord cardUseRecord, PageParam<Card> page) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        cardUseRecord.setWriteOffShopIds(Collections.singletonList(shopId));
        IPage<CardUseRecord> dataList = cardUseRecordService.queryWriteOffCardList(cardUseRecord, page);
        return ServerResponseEntity.success(dataList);
    }

    @GetMapping("/statistic")
    @ApiOperation(value = "统计提货卡(券)消费信息")
    public ServerResponseEntity<CardUseRecord> statisticCoupon(CardUseRecord cardUseRecord) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        cardUseRecord.setWriteOffShopIds(Collections.singletonList(shopId));
        CardUseRecord statisticCardUseRecord = cardUseRecordService.statisticCardUseRecord(cardUseRecord);
        return ServerResponseEntity.success(statisticCardUseRecord);
    }

    /**
     * 功能描述：导出全部数据
     *
     * @param response 请求参数
     * @param cardUseRecord   查询参数
     * @return
     */
    @ApiOperation("导出提货卡核销数据")
    @GetMapping("/download")
    public void downloadAll(HttpServletResponse response, CardUseRecord cardUseRecord) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        cardUseRecord.setWriteOffShopIds(Collections.singletonList(shopId));
        cardUseRecordService.shopDownload(cardUseRecord,response);
    }

    /**
     * 功能描述：查询批次号
     *
     * @return
     */
    @ApiOperation("查询批次号")
    @GetMapping("/queryBatchNumList")
    public ServerResponseEntity<List<String>> queryBatchNumList() {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        List<String> batchNums = cardService.queryBatchNumList(shopId);
        return ServerResponseEntity.success(batchNums);
    }
    @PostMapping("/sellCardByNum")
    @ApiOperation(value = "批量出售提货卡")
    public ServerResponseEntity<Void> sellCardByNumAndBatchNumber(@RequestBody @Valid Card card) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        QueryWrapper<Card> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Card::getBatchNumber,card.getBatchNumber());
        queryWrapper.last("LIMIT 1");
        Card one = cardService.getOne(queryWrapper);
        if(one != null){
            String str = one.getCardNumber().split(one.getCardPrefix())[1];
            int length = str.length();
            for (int i = card.getStartNum(); i <= card.getEndNum(); i++) {
                String number = one.getCardPrefix() + String.format("%0"+length+"d", i);
                cardService.sellCardByNumAndBatchNumber(card.getBatchNumber(),number,shopId,card.getBalance(),card.getBuyUnit(),card.getBuyReason());
            }
            return ServerResponseEntity.success();
        }else{
            return ServerResponseEntity.showFailMsg("未查询到信息！");
        }
    }

    @PostMapping("/makeCardByNum")
    @ApiOperation(value = "批量制卡")
    public ServerResponseEntity<Void> makeCardByNumAndBatchNumber(@RequestBody @Valid Card card) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        cardService.makeCardByNumAndBatchNumber(card.getBatchNumbers(),shopId);
        return ServerResponseEntity.success();
    }

    @PostMapping("/rechargeCardByNum")
    @ApiOperation(value = "批量充值提货卡")
    public ServerResponseEntity<Void> rechargeCardByNumAndBatchNumber(@RequestBody @Valid Card card) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        QueryWrapper<Card> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Card::getBatchNumber,card.getBatchNumber());
        queryWrapper.last("LIMIT 1");
        Card one = cardService.getOne(queryWrapper);
        if(one != null){
            String str = one.getCardNumber().split(one.getCardPrefix())[1];
            int length = str.length();
            for (int i = card.getStartNum(); i <= card.getEndNum(); i++) {
                String number = one.getCardPrefix() + String.format("%0"+length+"d", i);
                cardService.rechargeCardByNumAndBatchNumber(card.getBatchNumber(),number,card.getBalance(),shopId);
            }
            return ServerResponseEntity.success();
        }else{
            return ServerResponseEntity.showFailMsg("未查询到信息！");
        }
    }

    @PostMapping("/freezeCardByNum")
    @ApiOperation(value = "批量冻结提货卡")
    public ServerResponseEntity<Void> freezeCardByNumAndBatchNumber(@RequestBody @Valid Card card) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        QueryWrapper<Card> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Card::getBatchNumber,card.getBatchNumber());
        queryWrapper.last("LIMIT 1");
        Card one = cardService.getOne(queryWrapper);
        if(one != null){
            String str = one.getCardNumber().split(one.getCardPrefix())[1];
            int length = str.length();
            for (int i = card.getStartNum(); i <= card.getEndNum(); i++) {
                String number = one.getCardPrefix() + String.format("%0"+length+"d", i);
                cardService.freezeCardByNumAndBatchNumber(card.getBatchNumber(),number,shopId);
            }
            return ServerResponseEntity.success();
        }else{
            return ServerResponseEntity.showFailMsg("未查询到信息！");
        }
    }

    @PostMapping("/delBatchCard")
    @ApiOperation(value = "通过批次号批量删除提货卡")
    @Transactional
    public ServerResponseEntity<Void> delBatchCard(@RequestBody @Valid Card card) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        cardService.delBatchCard(card.getBatchNumber(),shopId);
        return ServerResponseEntity.success();
    }

    @PostMapping("/freezeCardByCardIds")
    @ApiOperation(value = "通过cardId冻结提货卡")
    @Transactional
    public ServerResponseEntity<Void> freezeCardByCardIds(@RequestBody @Valid Card card) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        cardService.freezeCardByCardIds(card.getCardIds(),shopId);
        return ServerResponseEntity.success();
    }

    @PostMapping("/updateCardBybatchNumber")
    @ApiOperation(value = "更新批次信息")
    @Transactional
    public ServerResponseEntity<Void> updateCardBybatchNumber(@RequestBody @Valid Card card) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        card.setShopId(shopId);
        cardService.updateCardBybatchNumber(card);
        return ServerResponseEntity.success();
    }

    @GetMapping("/getCardPrefix")
    @ApiOperation(value = "查询是否有该前缀")
    public ServerResponseEntity<Void> getCardPrefix(@RequestParam(value = "cardPrefix") String cardPrefix) {
        QueryWrapper<Card> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Card::getCardPrefix,cardPrefix);
        queryWrapper.lambda().eq(Card::getIsDelete,0);
        queryWrapper.last("LIMIT 1");
        Card dbCard = cardService.getOne(queryWrapper);
        if (dbCard != null) {
            return ServerResponseEntity.showFailMsg("该卡前缀已存在请更换");
        }
        return ServerResponseEntity.success();
    }

    @GetMapping("/encrypCardNumber")
    @ApiOperation(value = "处理卡(券)号")
    public ServerResponseEntity<Card> encrypCardNumber(@RequestParam(value = "cardCode") String cardCode) {
        QueryWrapper<Card> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Card::getCardCode,cardCode);
        queryWrapper.last("LIMIT 1");
        Card one = cardService.getOne(queryWrapper);
        if(one != null){
            one.setEncrypCardNumber(cardCode.replaceAll("(\\d{3})\\d{5}(\\d{4})", "$1*****$2"));
            return ServerResponseEntity.success(one);
        }else{
            one = new Card();
            one.setEncrypCardNumber(cardCode.replaceAll("(\\d{3})\\d{5}(\\d{4})", "$1*****$2"));
            return ServerResponseEntity.success(one);
        }
    }
}

