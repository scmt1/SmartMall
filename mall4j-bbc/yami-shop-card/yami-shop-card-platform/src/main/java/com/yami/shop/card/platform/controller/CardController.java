package com.yami.shop.card.platform.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.yami.shop.bean.model.ShopDetail;
import com.yami.shop.bean.model.StockChangeReason;
import com.yami.shop.card.common.model.Card;
import com.yami.shop.card.common.model.CardShop;
import com.yami.shop.card.common.model.CardUseRecord;
import com.yami.shop.card.common.model.CardUser;
import com.yami.shop.card.common.service.CardService;
import com.yami.shop.card.common.service.CardShopService;
import com.yami.shop.card.common.service.CardUseRecordService;
import com.yami.shop.card.common.service.CardUserService;
import com.yami.shop.card.common.utils.GenImgUtils;
import com.yami.shop.card.common.utils.SnowflakeIdWorker;
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.Arith;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.config.ShopConfig;
import com.yami.shop.service.ShopDetailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController("adminCardController")
@RequestMapping("/platform/card")
@Api(tags = "平台端会员卡接口")
public class CardController {
    @Autowired
    private CardService cardService;

    @Autowired
    private CardShopService cardShopService;

    @Autowired
    private CardUseRecordService cardUseRecordService;
    @Autowired
    private CardUserService cardUserService;
    @Autowired
    private Snowflake snowflake;

    @Autowired
    private ShopConfig shopConfig;
    @Autowired
    private ShopDetailService shopDetailService;


    @GetMapping("/page")
    @ApiOperation(value = "分页获取提货卡列表信息")
    public ServerResponseEntity<IPage<Card>> page(Card card, PageParam<Card> page) {
        IPage<Card> couponPage = cardService.getPlatformPage(page, card);
        for (Card cardInfo:couponPage.getRecords()) {
            CardUseRecord cardUseRecord = cardUseRecordService.queryCardUseTotalBalance(cardInfo.getCardCode());
            cardInfo.setRemainingAmount(Arith.sub(cardInfo.getBalance(),cardUseRecord.getAmount()));
            if(cardInfo.getShopId() == 0){
                cardInfo.setShopName("运营平台");
            }else{
                ShopDetail shopDetail = shopDetailService.getById(cardInfo.getShopId());
                cardInfo.setShopName(shopDetail.getShopName());
            }
        }
        return ServerResponseEntity.success(couponPage);
    }

    @GetMapping("/batchCardPage")
    @ApiOperation(value = "分页获取批次列表信息")
    public ServerResponseEntity<IPage<Card>> batchCardPage(Card card, PageParam<Card> page) {
        IPage<Card> couponPage = cardService.getPlatformBatchPage(page, card);
        for (Card cardInfo:couponPage.getRecords()) {
            List<Card> cards = cardService.queryCardSellRecord(cardInfo.getBatchNumber());
            cardInfo.setChildren(cards);
        }
        couponPage.setRecords(couponPage.getRecords());
        return ServerResponseEntity.success(couponPage);
    }

    @GetMapping("/batchCardDetailsPage")
    @ApiOperation(value = "分页获取批次明细列表信息")
    public ServerResponseEntity<IPage<Card>> batchCardDetailsPage(Card card, PageParam<Card> page) {
        IPage<Card> cards = cardService.batchCardDetailsPage(page, card);
        return ServerResponseEntity.success(cards);
    }

    @GetMapping("/batchInfoStatistic")
    @ApiOperation(value = "统计提货卡(券)批次信息")
    public ServerResponseEntity<Card> batchInfoStatistic(Card card) {
        Card cardInfo = cardService.batchInfoStatistic(card);
        return ServerResponseEntity.success(cardInfo);
    }

    /**
     * 功能描述：导出批次信息数据
     *
     * @param response      请求参数
     * @param card 查询参数
     * @return
     */
    @ApiOperation("导出批次信息数据")
    @GetMapping("/downloadBatchInfo")
    public void downloadBatchInfo(HttpServletResponse response, Card card) {
        cardService.downloadBatchInfo(card, response);
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取会员卡列表信息")
    public ServerResponseEntity<IPage<Card>> couponList(PageParam<Card> page, Card card) {
        PageParam<Card> couponPage = cardService.page(page, new LambdaQueryWrapper<Card>()
                .eq(Card::getShopId, Constant.PLATFORM_SHOP_ID)
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

        List<CardShop> list = cardShopService.queryCardShopList(id);
        byId.setCardShops(list);
        return ServerResponseEntity.success(byId);
    }


    @DeleteMapping
    @ApiOperation(value = "根据会员卡id删除会员卡")
    @ApiImplicitParam(name = "cardId", value = "会员卡id", required = true, dataType = "Long")
    public ServerResponseEntity<Void> delete(@RequestBody Long cardId) {
        cardService.deleteByCardId(cardId);
        return ServerResponseEntity.success();
    }


    @PostMapping
    @ApiOperation(value = "保存会员卡")
    public ServerResponseEntity<Void> save(@RequestBody @Valid Card card) {
        QueryWrapper<Card> cardQueryWrapper = new QueryWrapper<>();
        cardQueryWrapper.lambda().eq(Card::getCardPrefix,card.getCardPrefix());
        cardQueryWrapper.lambda().eq(Card::getIsDelete,0);
        cardQueryWrapper.last("LIMIT 1");
        Card cardInfo = cardService.getOne(cardQueryWrapper);
        if (cardInfo != null) {
            return ServerResponseEntity.showFailMsg("该卡前缀已存在请更换");
        }
        // 批次号第一部分：时间
        DateFormat dateFormat = new SimpleDateFormat("yyyyMM");
        String currentTimeStr = dateFormat.format(new Date());
        // 批次号第二部分：编号
        QueryWrapper<Card> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("batch_number", currentTimeStr);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("LIMIT 1");
        Card one = cardService.getOne(queryWrapper);
        String cusCodeStr = "";
        if (one != null) {
            String s = one.getBatchNumber().split(currentTimeStr)[1];
            int num = Integer.parseInt(s);
            cusCodeStr = String.format("%04d", num + 1);
        } else {
            cusCodeStr = "0001";
        }
        card.setBatchNumber(currentTimeStr + cusCodeStr);
        card.setBatchTime(new Date());
        card.setBatchStatus(0);
        long index = 1;
        for (int i = 0; i < card.getCardNum(); i++) {
            // 此处皆为平台通用
            card.setShopId(Constant.PLATFORM_SHOP_ID);
            card.setCreateTime(new Date());
            card.setIsDelete(0);
            card.setStatus(0);
            card.setCreateType(1);
            card.setCardCode(checkQrCodeExists().toString());
            card.setBuyCardType(0);

            String code = "";
            if (String.valueOf(index).length() == 1) {
                code = "000" + index;
            } else if (String.valueOf(index).length() == 2) {
                code = "00" + index;
            } else if (String.valueOf(index).length() == 3) {
                code = "0" + index;
            } else {
                code = String.valueOf(index);
            }
            card.setCardNumber(card.getCardPrefix() + code);
            card.setPassword(String.valueOf(SnowflakeIdWorker.generateIdNum6()));
            cardService.save(card);

            if (card.getSuitableProdType() == 2) {
                card.getCardShops().forEach(item -> {
                    item.setCardId(card.getCardId());
                    item.setCreateTime(new Date());
                });
                cardShopService.saveBatch(card.getCardShops());
            }

            index++;
        }
        return ServerResponseEntity.success();
    }

    @PutMapping
    @ApiOperation(value = "修改会员卡")
    public ServerResponseEntity<Void> update(@RequestBody @Valid Card card) {
        // 查询平台下会员卡
        Card dbCard = cardService.getById(card.getCardId());
        if (null == dbCard) {
            throw new YamiShopBindException("该会员卡不存在");
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
        card.setShopId(dbCard.getShopId());
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


    @GetMapping("/queryWriteOffCardList")
    @ApiOperation(value = "查询店铺所核销的提货卡记录")
    public ServerResponseEntity<Object> queryWriteOffCardList(CardUseRecord cardUseRecord, PageParam<Card> page) {
        IPage<CardUseRecord> dataList = cardUseRecordService.queryWriteOffCardList(cardUseRecord, page);
        return ServerResponseEntity.success(dataList);
    }


    /**
     * 功能描述：导出全部数据
     *
     * @param response      请求参数
     * @param cardUseRecord 查询参数
     * @return
     */
    @ApiOperation("导出提货卡核销数据")
    @GetMapping("/download")
    public void downloadAll(HttpServletResponse response, CardUseRecord cardUseRecord) {
        cardUseRecordService.download(cardUseRecord, response);
    }

    /**
     * 功能描述：查询批次号
     *
     * @return
     */
    @ApiOperation("查询批次号")
    @GetMapping("/queryBatchNumList")
    public ServerResponseEntity<List<String>> queryBatchNumList() {
        List<String> batchNums = cardService.queryBatchNumList(0L);
        return ServerResponseEntity.success(batchNums);
    }

    @PostMapping("/sellCardByNum")
    @ApiOperation(value = "批量出售并充值提货卡", notes = "批量出售并充值提货卡")
    @Transactional
    public ServerResponseEntity<Void> sellCardByNumAndBatchNumber(@RequestBody @Valid Card card) {
        QueryWrapper<Card> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Card::getBatchNumber,card.getBatchNumber());
        queryWrapper.last("LIMIT 1");
        Card one = cardService.getOne(queryWrapper);
        if(one != null){
            String str = one.getCardNumber().split(one.getCardPrefix())[1];
            int length = str.length();
            for (int i = card.getStartNum(); i <= card.getEndNum(); i++) {
                String number = one.getCardPrefix() + String.format("%0"+length+"d", i);
                cardService.sellCardByNumAndBatchNumber(card.getBatchNumber(),number,card.getShopId(),
                        card.getBalance(),card.getBuyUnit(),card.getBuyReason());
            }
            return ServerResponseEntity.success();
        }else{
            return ServerResponseEntity.showFailMsg("未查询到信息！");
        }
    }

    @PostMapping("/makeCardByNum")
    @ApiOperation(value = "批量制卡")
    @Transactional
    public ServerResponseEntity<Void> makeCardByNumAndBatchNumber(@RequestBody @Valid Card card) {
        cardService.makeCardByNumAndBatchNumber(card.getBatchNumbers(), card.getShopId());
        return ServerResponseEntity.success();
    }

    @PostMapping("/rechargeCardByNum")
    @ApiOperation(value = "批量充值提货卡")
    @Transactional
    public ServerResponseEntity<Void> rechargeCardByNumAndBatchNumber(@RequestBody @Valid Card card) {
        QueryWrapper<Card> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Card::getBatchNumber,card.getBatchNumber());
        queryWrapper.last("LIMIT 1");
        Card one = cardService.getOne(queryWrapper);
        if(one != null){
            String str = one.getCardNumber().split(one.getCardPrefix())[1];
            int length = str.length();
            for (int i = card.getStartNum(); i <= card.getEndNum(); i++) {
                String number = one.getCardPrefix() + String.format("%0"+length+"d", i);
                cardService.rechargeCardByNumAndBatchNumber(card.getBatchNumber(),number,card.getBalance(),card.getShopId());
            }
            return ServerResponseEntity.success();
        }else{
            return ServerResponseEntity.showFailMsg("未查询到信息！");
        }
    }

    @PostMapping("/freezeCardByNum")
    @ApiOperation(value = "批量冻结提货卡", notes = "批量冻结提货卡")
    @Transactional
    public ServerResponseEntity<Void> freezeCardByNumAndBatchNumber(@RequestBody @Valid Card card) {
        QueryWrapper<Card> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Card::getBatchNumber,card.getBatchNumber());
        queryWrapper.last("LIMIT 1");
        Card one = cardService.getOne(queryWrapper);
        if(one != null){
            String str = one.getCardNumber().split(one.getCardPrefix())[1];
            int length = str.length();
            for (int i = card.getStartNum(); i <= card.getEndNum(); i++) {
                String number = one.getCardPrefix() + String.format("%0"+length+"d", i);
                cardService.freezeCardByNumAndBatchNumber(card.getBatchNumber(),number,card.getShopId());
            }
            return ServerResponseEntity.success();
        }else{
            return ServerResponseEntity.showFailMsg("未查询到信息！");
        }
    }

    @PostMapping("/soldToUnsoldCardByNum")
    @ApiOperation(value = "批量出售转未售提货卡", notes = "批量出售转未售提货卡")
    public ServerResponseEntity<Void> soldToUnsoldCardByNum(@RequestBody @Valid Card card) {
        QueryWrapper<Card> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Card::getBatchNumber,card.getBatchNumber());
        queryWrapper.last("LIMIT 1");
        Card one = cardService.getOne(queryWrapper);
        if(one != null){
            String str = one.getCardNumber().split(one.getCardPrefix())[1];
            int length = str.length();
            for (int i = card.getStartNum(); i <= card.getEndNum(); i++) {
                String number = one.getCardPrefix() + String.format("%0"+length+"d", i);
                cardService.soldToUnsoldCardByNumAndBatchNumber(card.getBatchNumber(),number,card.getShopId());
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
        cardService.delBatchCard(card.getBatchNumber(), card.getShopId());
        return ServerResponseEntity.success();
    }

    @PostMapping("/freezeCardByCardIds")
    @ApiOperation(value = "通过cardId冻结提货卡")
    @Transactional
    public ServerResponseEntity<Void> freezeCardByCardIds(@RequestBody @Valid Card card) {
        cardService.freezeCardByCardIds(card.getCardIds(), card.getShopId());
        return ServerResponseEntity.success();
    }

    @PostMapping("/updateCardBybatchNumber")
    @ApiOperation(value = "更新批次信息")
    @Transactional
    public ServerResponseEntity<Void> updateCardBybatchNumber(@RequestBody @Valid Card card) {
        cardService.updateCardBybatchNumber(card);
        return ServerResponseEntity.success();
    }

    @PostMapping("/unfreezeCard")
    @ApiOperation(value = "解冻提货卡(券)")
    public ServerResponseEntity<Void> unfreezeCard(@RequestBody @Valid Card card) {
        // 查询平台下会员卡
        Card dbCard = cardService.getById(card.getCardId());
        if (null == dbCard) {
            throw new YamiShopBindException("该会员卡不存在");
        }
        QueryWrapper<CardUser> cardUserQueryWrapper = new QueryWrapper<>();
        cardUserQueryWrapper.lambda().eq(CardUser::getCardId,card.getCardId());
        CardUser one = cardUserService.getOne(cardUserQueryWrapper);
        if(one != null){
            card.setStatus(3);
        }else{
            card.setStatus(dbCard.getBatchStatus());
        }
        cardService.updateById(card);
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

    @GetMapping("/getMinNotSoldCardNumber")
    @ApiOperation(value = "获取未出售卡(券)的最小编号")
    public ServerResponseEntity<Card> getMinNotSoldCardNumber(@RequestParam(value = "batchNumber") String batchNumber) {
        Card minNotSoldCardNumber = cardService.getMinNotSoldCardNumber(batchNumber);
        return ServerResponseEntity.success(minNotSoldCardNumber);
    }

    @PostMapping("/getSellCardNum")
    @ApiOperation(value = "通过起始编号和批次号获取状态未未出售的出售卡(券)数量")
    public ServerResponseEntity<Integer> getSellCardNum(@RequestBody @Valid Card card) {
        QueryWrapper<Card> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Card::getBatchNumber,card.getBatchNumber());
        queryWrapper.last("LIMIT 1");
        Card one = cardService.getOne(queryWrapper);
        if(one != null){
            String str = one.getCardNumber().split(one.getCardPrefix())[1];
            int length = str.length();
            String startNumber = one.getCardPrefix() + String.format("%0"+length+"d", card.getStartNum());
            String endNumber = one.getCardPrefix() + String.format("%0"+length+"d", card.getEndNum());
            Integer sellCardNum = cardService.getSellCardNum(card.getBatchNumber(), startNumber, endNumber);
            return ServerResponseEntity.success(sellCardNum);
        }else{
            return ServerResponseEntity.showFailMsg("未查询到信息！");
        }
    }

    @PostMapping("/getCardNumByNumber")
    @ApiOperation(value = "通过起始编号和批次号获取数量")
    public ServerResponseEntity<Integer> getCardNumByNumber(@RequestBody @Valid Card card) {
        QueryWrapper<Card> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Card::getBatchNumber,card.getBatchNumber());
        queryWrapper.last("LIMIT 1");
        Card one = cardService.getOne(queryWrapper);
        if(one != null){
            String str = one.getCardNumber().split(one.getCardPrefix())[1];
            int length = str.length();
            String startNumber = one.getCardPrefix() + String.format("%0"+length+"d", card.getStartNum());
            String endNumber = one.getCardPrefix() + String.format("%0"+length+"d", card.getEndNum());
            Integer sellCardNum = cardService.getCardNumByNumber(card.getBatchNumber(), startNumber, endNumber);
            return ServerResponseEntity.success(sellCardNum);
        }else{
            return ServerResponseEntity.showFailMsg("未查询到信息！");
        }
    }

    @GetMapping("/getCouponCardInfo")
    @ApiOperation(value = "根据实物券号获取实物券卡信息")
    public ServerResponseEntity<Card> getCouponCardInfo(@RequestParam(value = "cardCode") String cardCode) {
        QueryWrapper<Card> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Card::getCardCode,cardCode);
        Card byId = cardService.getOne(queryWrapper);
        if(byId != null){
            QueryWrapper<CardUseRecord> cardUseRecordQueryWrapper = new QueryWrapper<>();
            cardUseRecordQueryWrapper.lambda().eq(CardUseRecord::getCardCode,cardCode);
            cardUseRecordQueryWrapper.last("LIMIT 1");
            CardUseRecord one = cardUseRecordService.getOne(cardUseRecordQueryWrapper);
            if(one != null){
                return ServerResponseEntity.showFailMsg("该实物券已使用，已无法置换");
            }
            if(byId.getStatus() == 0 || byId.getStatus() == 1){
                return ServerResponseEntity.showFailMsg("该实物券还未出售暂不能置换");
            }
            if(byId.getStatus() == 4){
                return ServerResponseEntity.showFailMsg("该实物券已冻结暂不能置换");
            }
            if(byId.getStatus() == 5){
                return ServerResponseEntity.showFailMsg("该实物券已置换");
            }
            if(byId.getStatus() == 6){
                return ServerResponseEntity.showFailMsg("该实物券已核销，已不能置换");
            }
            CardUseRecord cardUseRecord = cardUseRecordService.queryCardUseTotalBalance(cardCode);
            byId.setBalance(byId.getBalance() - cardUseRecord.getAmount());
            return ServerResponseEntity.success(byId);
        }else{
            return ServerResponseEntity.showFailMsg("未查询到实物券信息,请查看券号是否正确");
        }
    }

    @GetMapping("/getCardInfoByCardCode")
    @ApiOperation(value = "根据卡号获取提货卡信息")
    public ServerResponseEntity<Card> getCardInfoByCardCode(@RequestParam(value = "cardCode") String cardCode) {
        QueryWrapper<Card> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Card::getCardCode,cardCode);
        Card byId = cardService.getOne(queryWrapper);
        if(byId != null){
            CardUseRecord cardUseRecord = cardUseRecordService.queryCardUseTotalBalance(cardCode);
            byId.setBalance(Arith.sub(byId.getBalance(),cardUseRecord.getAmount()));
            return ServerResponseEntity.success(byId);
        }else{
            return ServerResponseEntity.showFailMsg("未查询到提货卡/券信息,请查看卡号是否正确");
        }
    }

    @PostMapping("/rechargeCardBalance")
    @ApiOperation(value = "根据卡号充值金额")
    public ServerResponseEntity<Card> rechargeCardBalance(@RequestBody @Valid Card card) {
        QueryWrapper<Card> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Card::getCardCode,card.getCardCode());
        Card one = cardService.getOne(queryWrapper);
        if(one != null){
            String str = "";
            if(StringUtils.isNotBlank(one.getDisplaceCardCode())){
                str = one.getDisplaceCardCode() + "," + card.getCouponCardCodes();
            }else{
                str = card.getCouponCardCodes();
            }
            //更新原提货卡金额
            cardService.update(Wrappers.lambdaUpdate(Card.class)
                    .set(Card::getBalance, Arith.add(one.getBalance(),card.getBalance()))
                    .set(Card::getDisplaceCardCode,str)
                    .eq(Card::getCardCode, card.getCardCode())
            );
            //原卡已绑定
            CardUser cardUser = cardUserService.getOne(Wrappers.lambdaUpdate(CardUser.class)
                    .eq(CardUser::getCardNumber, card.getCardCode())
            );
            if(cardUser != null){
                //更新原提货卡用户绑定表金额信息
                cardUserService.update(Wrappers.lambdaUpdate(CardUser.class)
                        .set(CardUser::getBalance, Arith.add(cardUser.getBalance(),card.getBalance()))
                        .eq(CardUser::getCardNumber, card.getCardCode())
                );
            }
            String[] cardCodes = card.getCouponCardCodes().split(",");
            for (String cardCode:cardCodes) {
                cardService.update(Wrappers.lambdaUpdate(Card.class)
                        .set(Card::getStatus, 5)
                        .eq(Card::getCardCode, cardCode)
                );
            }
            return ServerResponseEntity.success();
        }else{
            return ServerResponseEntity.showFailMsg("未查询到原提货卡信息！");
        }
    }

    @GetMapping("/getCouponCardInfoByCardCodes")
    @ApiOperation(value = "根据实物券号获取实物券卡信息")
    public ServerResponseEntity<Card> getCouponCardInfoByCardCodes(@RequestParam(value = "cardCodeList") List<String> cardCodeList) {

        Double amount = 0.0;
        for (String cardCode:cardCodeList) {
            QueryWrapper<CardUseRecord> cardUseRecordQueryWrapper = new QueryWrapper<>();
            cardUseRecordQueryWrapper.lambda().eq(CardUseRecord::getCardCode,cardCode);
            cardUseRecordQueryWrapper.last("LIMIT 1");
            CardUseRecord one = cardUseRecordService.getOne(cardUseRecordQueryWrapper);
            if(one != null){
                return ServerResponseEntity.showFailMsg(cardCode + "实物券已使用，已无法置换");
            }
            Card byId = cardService.getCardInfoByCardCode(cardCode);
            if(byId == null){
                return ServerResponseEntity.showFailMsg("未查询到券号为" + cardCode + "的实物券信息,请查看券号是否正确");
            }else{
                if(byId.getStatus() == 0 || byId.getStatus() == 1){
                    return ServerResponseEntity.showFailMsg(cardCode + "实物券还未出售暂不能置换");
                }
                if(byId.getStatus() == 4){
                    return ServerResponseEntity.showFailMsg(cardCode + "实物券已冻结暂不能置换");
                }
                if(byId.getStatus() == 5){
                    return ServerResponseEntity.showFailMsg(cardCode + "实物券已置换");
                }
                if(byId.getStatus() == 6){
                    return ServerResponseEntity.showFailMsg(cardCode + "实物券已核销，已不能置换");
                }
                if(byId.getCardType() == 1){
                    return ServerResponseEntity.showFailMsg(cardCode + "类型不为实物券，无法置换");
                }
                amount = Arith.add(amount,byId.getBalance());
            }
        }
        Card card = new Card();
        card.setBalance(amount);
        return ServerResponseEntity.success(card);
    }

    @PostMapping("/replacementCard")
    @ApiOperation(value = "置换单卡", notes = "置换单卡")
    @Transactional(rollbackFor = Exception.class)
    public ServerResponseEntity<Void> replacementCard(@RequestBody @Valid Card card) {
        QueryWrapper<Card> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Card::getBatchNumber,card.getBatchNumber());
        queryWrapper.last("LIMIT 1");
        Card one = cardService.getOne(queryWrapper);
        if(one != null){
            String str = one.getCardNumber().split(one.getCardPrefix())[1];
            int length = str.length();
            String startNumber = one.getCardPrefix() + String.format("%0"+length+"d", card.getStartNum());
            //查询卡状态是否可置换
            Card cardInfo = cardService.getOne(new LambdaQueryWrapper<Card>()
                    .eq(Card::getCardNumber, startNumber)
                    .eq(Card::getBatchNumber, card.getBatchNumber())
            );
            if(cardInfo == null){
                return ServerResponseEntity.showFailMsg("未查询到" + startNumber + "该编号提货卡");
            }
            if(cardInfo.getStatus() != 1){
                return ServerResponseEntity.showFailMsg(startNumber + "该编号提货卡不能被置换");
            }
            cardService.update(Wrappers.lambdaUpdate(Card.class)
                    .set(Card::getStatus, 2)
                    .set(Card::getBalance, card.getBalance())
                    .set(Card::getBatchStatus, 2)
                    .set(Card::getDisplaceCardCode,card.getCouponCardCodes())
                    .eq(Card::getCardNumber, startNumber)
                    .eq(Card::getBatchNumber, card.getBatchNumber())
            );
            String[] cardCodes = card.getCouponCardCodes().split(",");
            for (String cardCode:cardCodes) {
                cardService.update(Wrappers.lambdaUpdate(Card.class)
                        .set(Card::getStatus, 5)
                        .eq(Card::getCardCode, cardCode)
                );
            }
            return ServerResponseEntity.success();
        }else{
            return ServerResponseEntity.showFailMsg("未查询到信息！");
        }
    }

    @PostMapping("/replacementManyCard")
    @ApiOperation(value = "置换多卡", notes = "置换多卡")
    @Transactional(rollbackFor = Exception.class)
    public ServerResponseEntity<Void> replacementManyCard(@RequestBody @Valid Card card) {
        QueryWrapper<Card> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Card::getBatchNumber,card.getBatchNumber());
        queryWrapper.last("LIMIT 1");
        Card one = cardService.getOne(queryWrapper);
        if(one != null){
            String str = one.getCardNumber().split(one.getCardPrefix())[1];
            int length = str.length();
            //分卡信息
            for (Card cardInfo:card.getCardInfos()) {
                if(cardInfo.getStartNum() != null){
                    String startNumber = one.getCardPrefix() + String.format("%0"+length+"d", cardInfo.getStartNum());
                    //查询卡状态是否可置换
                    Card info = cardService.getOne(new LambdaQueryWrapper<Card>()
                            .eq(Card::getCardNumber, startNumber)
                            .eq(Card::getBatchNumber, card.getBatchNumber())
                    );
                    if(info == null){
                        return ServerResponseEntity.showFailMsg("未查询到" + startNumber + "该编号提货卡");
                    }
                    if(info.getStatus() != 1){
                        return ServerResponseEntity.showFailMsg(startNumber + "该编号提货卡不能被置换");
                    }
                    cardService.update(Wrappers.lambdaUpdate(Card.class)
                            .set(Card::getStatus, 2)
                            .set(Card::getBalance, cardInfo.getBalance())
                            .set(Card::getBatchStatus, 2)
                            .set(Card::getDisplaceCardCode,card.getCouponCardCodes())
                            .eq(Card::getCardNumber, startNumber)
                            .eq(Card::getBatchNumber, card.getBatchNumber())
                    );
                }
            }
            String[] cardCodes = card.getCouponCardCodes().split(",");
            for (String cardCode:cardCodes) {
                cardService.update(Wrappers.lambdaUpdate(Card.class)
                        .set(Card::getStatus, 5)
                        .eq(Card::getCardCode, cardCode)
                );
            }
            return ServerResponseEntity.success();
        }else{
            return ServerResponseEntity.showFailMsg("未查询到信息！");
        }
    }

    /**
     * 功能描述：导出提货卡(券)数据
     *
     * @param response 请求参数
     * @param card   查询参数
     * @return
     */
    @ApiOperation("导出提货卡(券)数据")
    @GetMapping("/downLoadCardRecord")
    public void downLoadCardRecord(HttpServletResponse response,Card card){
        cardService.downLoadCardRecord(card,response);
    }

    @GetMapping("/downLoadCardImg")
    public void downLoadCardImg(HttpServletResponse response, Card card) throws Exception {
        ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());

        List<Card> cardList = cardService.getCardList(card);
        String imgUrl = shopConfig.getDomain().getResourcesDomainName();

        String downloadFilename = "card_" + DateUtil.today() + ".zip";
        //设置格式
        response.setCharacterEncoding("UTF-8");
        response.setHeader("content-Type", "application/x-msdownload");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(downloadFilename, "UTF-8"));
        for (Card item : cardList) {
            String startTime = DateUtil.format(item.getUserStartTime(), "yyyy/MM/dd");
            String endTime = DateUtil.format(item.getUserEndTime(), "yyyy/MM/dd");
            BufferedImage bufferedImage = GenImgUtils.genImg(imgUrl + item.getCardImg(), item.getCardCode(), item.getCardNumber(),
                    String.valueOf(item.getBalance().intValue()), startTime + "至" + endTime);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", out);
            InputStream is = new ByteArrayInputStream(out.toByteArray());
            zos.putNextEntry(new ZipEntry(item.getCardNumber() + ".png"));
            byte[] buffer = new byte[1024];
            int r = 0;
            while ((r = is.read(buffer)) != -1) {
                zos.write(buffer, 0, r);
            }
            is.close();
            is = null;
            out.close();
            out = null;
            zos.flush();
        }
        zos.flush();
        zos.close();
    }


    @GetMapping("/downLoadCardData")
    public void downLoadCardData(HttpServletResponse response, Card card) throws Exception {
        List<Card> cardList = cardService.getCardList(card);
        // 2-1、创建 工作簿 对象.
        Workbook workbook = new XSSFWorkbook();
        // 2-2、创建工作表对象
        Sheet sheet = workbook.createSheet();
        // 设置标题行，第0行.
        Row headerRow = sheet.createRow(0);

        Cell cell = headerRow.createCell(0);
        cell.setCellValue("编号");
        cell = headerRow.createCell(1);
        cell.setCellValue("金额");
        cell = headerRow.createCell(2);
        cell.setCellValue("卡号");
        cell = headerRow.createCell(3);
        cell.setCellValue("条形码");

        for (int i = 0; i < cardList.size(); i++) {
            Card item = cardList.get(i);
            // 创建行对象，从第1行开始。
            int rowNum = i + 1;
            Row row = sheet.createRow(rowNum);
            cell = row.createCell(0);
            cell.setCellValue(item.getCardNumber());

            cell = row.createCell(1);
            cell.setCellValue(item.getBalance());

            cell = row.createCell(2);
            cell.setCellValue(item.getCardCode());
            sheet.setColumnWidth(cell.getColumnIndex(), 30*256);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
            hints.put(EncodeHintType.MARGIN, 0);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            MatrixToImageWriter.writeToStream(
                    new MultiFormatWriter().encode(item.getCardCode(), BarcodeFormat.CODE_128, 180, 60, hints), "PNG",
                    outputStream);

            @SuppressWarnings("static-access")
            int pictureIdx = workbook.addPicture(outputStream.toByteArray(), workbook.PICTURE_TYPE_PNG);  // 添加图片到工作簿中

            CreationHelper helper = workbook.getCreationHelper();  // 获取创建帮助类
            Drawing drawing = sheet.createDrawingPatriarch();   // 获取绘图父对象
            ClientAnchor anchor = helper.createClientAnchor();  // 创建客户端锚点
            // 图片插入坐标
            anchor.setCol1(3);  // 设置图片左上角的列数
            anchor.setRow1(rowNum);  // 设置图片左上角的行数
            anchor.setDx1(1000);
            // 插入图片
            Picture pict = drawing.createPicture(anchor, pictureIdx);  // 在指定位置插入图片
            // 设置 单元格高度
            cell.getRow().setHeight((short) 800);  // 将单元格所在行的高度设置为1000
            // 设置 单元格宽度
            // 设置图片宽、高放缩比例
            pict.resize(0.9, 0.9);  // 这行代码，可以将值设置成 pict.resize(0.5, 0.5)、pict.resize(2, 2)，看看效果如何。
        }
        // 第六步，将文件存到指定位置
        try {
            response.setHeader("content-Type", "application/ms-excel");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            response.setHeader("Content-disposition", "attachment;filename=aa;filename*=utf-8''");
            OutputStream out = response.getOutputStream();
            workbook.write(out);
            IoUtil.close(out);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public InputStream bufferedImageToInputStream(BufferedImage image) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        try (InputStream input = new ByteArrayInputStream(os.toByteArray())) {
            return input;
        }
    }


    @GetMapping("/cardToImg")
    @ApiOperation(value = "生成卡图片")
    public ServerResponseEntity<Object> cardToImg() throws Exception {
        BufferedImage bufferedImage = GenImgUtils.genImg("https://mall.lzjczl.com/imgApi/2023/09/cab3e77ca73244b1982b58f81b0167aa.jpg",
                "123456789123", "101", "104", "2023/09/01至2025/09/01");
        return ServerResponseEntity.success("data:image/jpg;base64," + GenImgUtils.convertImgBase64(bufferedImage));
    }
}

