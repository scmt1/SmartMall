package com.yami.shop.card.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.card.common.model.CardUseRecord;
import com.yami.shop.card.common.service.CardUseRecordService;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@RestController("adminCardUseRecordController")
@RequestMapping("/platform/cardUseRecord")
@Api(tags = "商家端会员卡接口")
public class CardUseRecordController {
    @Autowired
    private CardUseRecordService cardUseRecordService;

    @GetMapping("/statistic")
    @ApiOperation(value = "统计提货卡(券)消费信息")
    public ServerResponseEntity<CardUseRecord> statisticCoupon(CardUseRecord cardUseRecord) {
        CardUseRecord statisticCardUseRecord = cardUseRecordService.statisticCardUseRecord(cardUseRecord);
        return ServerResponseEntity.success(statisticCardUseRecord);
    }

    @GetMapping("/writeOffDetailStatistic")
    @ApiOperation(value = "获取提货卡(券)核销明细统计信息")
    public ServerResponseEntity<List<CardUseRecord>> writeOffDetailStatistic(CardUseRecord cardUseRecord) {
        List<CardUseRecord> statisticCardUseRecord = cardUseRecordService.writeOffDetailStatistic(cardUseRecord);
        return ServerResponseEntity.success(statisticCardUseRecord);
    }

    @GetMapping("/shopWriteOffDetail")
    @ApiOperation(value = "获取店铺核销提货卡(券)明细信息")
    public ServerResponseEntity<IPage<CardUseRecord>> shopWriteOffDetail(PageParam<CardUseRecord> page, CardUseRecord cardUseRecord) {
        IPage<CardUseRecord> statisticCardUseRecord = cardUseRecordService.shopWriteOffDetail(page,cardUseRecord);
        return ServerResponseEntity.success(statisticCardUseRecord);
    }

    /**
     * 功能描述：导出店铺核销提货卡(券)数据
     *
     * @param response 请求参数
     * @param cardUseRecord   查询参数
     * @return
     */
    @ApiOperation("导出店铺核销提货卡(券)数据")
    @GetMapping("/shopDownloadWriteOff")
    public void shopDownloadByShopId(HttpServletResponse response,CardUseRecord cardUseRecord){
        cardUseRecordService.shopDownloadByShopId(cardUseRecord,response);
    }

    @PostMapping("/updateCardUseRecordSettlementStatusByIds")
    @ApiOperation(value = "批量更新记录结算状态")
    @Transactional(rollbackFor = Exception.class)
    public ServerResponseEntity<Void> updateCardUseRecordSettlementStatusByIds(@RequestBody @Valid CardUseRecord cardUseRecord){
        cardUseRecordService.updateCardUseRecordSettlementStatusByIds(cardUseRecord.getCardUseRecordIds());
        return ServerResponseEntity.success();
    }
}

