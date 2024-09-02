package com.yami.shop.api.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.yami.shop.bean.dto.TicketIdentifyResult;
import com.yami.shop.bean.enums.ScoreLogType;
import com.yami.shop.bean.model.ShopDetail;
import com.yami.shop.bean.model.TicketIdentifyRecord;
import com.yami.shop.bean.model.User;
import com.yami.shop.bean.model.UserExtension;
import com.yami.shop.bean.param.UserUpdateParam;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.Arith;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.*;
import com.yami.shop.user.common.model.UserScoreDetail;
import com.yami.shop.user.common.model.UserScoreLog;
import com.yami.shop.user.common.service.UserScoreDetailService;
import com.yami.shop.user.common.service.UserScoreLogService;
import com.yami.shop.utils.CharacterRecognitionUtil;
import com.yami.shop.utils.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author LGH
 */
@RestController
@RequestMapping("/p/ticketIdentify")
@Api(tags = "小票识别接口")
public class TicketIdentifyController {

    @Autowired
    private TicketIdentifyRecordService ticketIdentifyRecordService;
    @Autowired
    private UserScoreLogService userScoreLogService;
    @Autowired
    private UserScoreDetailService userScoreDetailService;
    @Autowired
    private UserExtensionService userExtensionService;
    @Autowired
    private SysConfigService sysConfigService;
    @Autowired
    private UserService userService;
    @Autowired
    private ShopDetailService shopDetailService;

    @PostMapping("/addScore")
    @ApiOperation(value = "识别文字并增加积分", notes = "识别文字并增加积分")
    @Transactional(rollbackFor = Exception.class)
    public ServerResponseEntity<Boolean> addScore(@RequestBody TicketIdentifyRecord identifyRecord) throws Exception {
        try {
            //积分兑换比例
            String moneyScore = "";
            ShopDetail shopInfo = shopDetailService.getById(identifyRecord.getShopId());
            //店铺配置了积分兑换金额就用店铺配置，否则用系统配置
            if(shopInfo.getTicketRatio() != null){
                moneyScore = shopInfo.getTicketRatio().toString();
            }else{
                moneyScore = sysConfigService.getConfigValue("MONEY_SCORE");
            }
            //积分倍数
            String scoreMultiple = sysConfigService.getConfigValue("SCORE_MULTIPLE");
            if(Objects.isNull(identifyRecord.getShopId())){
                return ServerResponseEntity.showFailMsg("请选择积分店铺");
            }
            ShopDetail shopDetail = shopDetailService.getById(identifyRecord.getShopId());
            //店铺小票关键字段JSON
            String ticketField = "";
            if(shopDetail != null){
                ticketField = shopDetail.getTicketField();
            }else{
                return ServerResponseEntity.showFailMsg("未查询到积分店铺");
            }
            if(StringUtils.isBlank(ticketField)){
                return ServerResponseEntity.showFailMsg("小票识别失败");
            }
            JSONObject ticketFieldObject = JSONObject.parseObject(ticketField);
            String userId = SecurityUtils.getUser().getUserId();
            User userInfo = userService.getUserByUserId(userId);
            UserExtension userExtension = userExtensionService.getByUserId(userId);
            String str = CharacterRecognitionUtil.ticketIdentifyByUrl(identifyRecord.getImgUrl());
            if(StringUtils.isNotBlank(str)){
                TicketIdentifyResult ticketIdentifyData = JsonUtils.fromJson(str, TicketIdentifyResult.class);
                List<TicketIdentifyResult.wordsResult> wordsResults = ticketIdentifyData.getWords_result();
                TicketIdentifyRecord ticketIdentifyRecord = new TicketIdentifyRecord();
                int index = 0;
                for (int i = 0; i < wordsResults.size(); i++) {
                    //订单号
                    if (wordsResults.get(i).getWords().contains(ticketFieldObject.getString("orderNumber"))){
                        if(shopDetail.getShopName().contains("鸿星尔克")){
                            ticketIdentifyRecord.setOrderNumber(wordsResults.get(i).getWords().split("日期")[0].split(ticketFieldObject.getString("orderNumber"))[1].trim());
                        }else if(shopDetail.getShopName().contains("红蜻蜓")){
                            ticketIdentifyRecord.setOrderNumber(wordsResults.get(i).getWords().split(ticketFieldObject.getString("orderNumber"))[1].split(ticketFieldObject.getString("saleTime"))[0].trim());
                        }else if(shopDetail.getShopName().contains("蓦努男装") || shopDetail.getShopName().contains("杰尼威尼")
                                || shopDetail.getShopName().contains("衫戈男装") || shopDetail.getShopName().contains("塔斯汀")
                                || shopDetail.getShopName().contains("意尔康")){
                            index = i + 1;
                            ticketIdentifyRecord.setOrderNumber(wordsResults.get(index).getWords().trim());
                        }else if(shopDetail.getShopName().contains("名创优品")){
                            ticketIdentifyRecord.setOrderNumber(wordsResults.get(i).getWords().split(ticketFieldObject.getString("orderNumber"))[1].split("机号")[0].trim());
                        }else if(shopDetail.getShopName().contains("奢玛莉")){
                            index = i + 2;
                            ticketIdentifyRecord.setOrderNumber(wordsResults.get(index).getWords().trim());
                        }else{
                            ticketIdentifyRecord.setOrderNumber(wordsResults.get(i).getWords().split(ticketFieldObject.getString("orderNumber"))[1].trim());
                        }

                        if(shopDetail.getShopName().contains("邻食魔珐")){
                            index = i + 1;
                            ticketIdentifyRecord.setSaleTime(wordsResults.get(index).getWords().trim());
                        }
                        if(shopDetail.getShopName().contains("小物社")){
                            index = i + 2;
                            ticketIdentifyRecord.setSaleTime(wordsResults.get(index).getWords().trim());
                        }
                        if(shopDetail.getShopName().contains("奢玛莉")){
                            index = i + 3;
                            ticketIdentifyRecord.setSaleTime(wordsResults.get(index).getWords().trim() + " " + wordsResults.get(index+1).getWords().trim());
                        }
                    }
                    //销售时间
                    if (wordsResults.get(i).getWords().contains(ticketFieldObject.getString("saleTime"))){
                        if(shopDetail.getShopName().contains("鸿星尔克")){
                            ticketIdentifyRecord.setSaleTime(wordsResults.get(i).getWords().split(ticketFieldObject.getString("saleTime"))[1]);
                        }else if(shopDetail.getShopName().contains("男生女生童装") || shopDetail.getShopName().contains("蓦努男装")
                                || shopDetail.getShopName().contains("杰尼威尼") || shopDetail.getShopName().contains("金甲虫")
                                || shopDetail.getShopName().contains("塔斯汀") || shopDetail.getShopName().contains("意尔康")
                                || shopDetail.getShopName().contains("N多寿司") || shopDetail.getShopName().contains("乡村基")){
                            index = i + 1;
                            ticketIdentifyRecord.setSaleTime(wordsResults.get(index).getWords());
                        }else if(shopDetail.getShopName().contains("小咖茶餐厅")){
                            ticketIdentifyRecord.setSaleTime(wordsResults.get(i-1).getWords());
                        }else{
                            ticketIdentifyRecord.setSaleTime(wordsResults.get(i).getWords().split(ticketFieldObject.getString("saleTime"))[1]);
                        }
                    }
                    //付款金额
                    if (wordsResults.get(i).getWords().contains(ticketFieldObject.getString("payment"))){
                        if(shopDetail.getShopName().contains("鸿星尔克") || shopDetail.getShopName().contains("男生女生童装")
                                || shopDetail.getShopName().contains("嘎婆家柴火鸡") || shopDetail.getShopName().contains("美丽衣橱")
                                || shopDetail.getShopName().contains("杰尼威尼") || shopDetail.getShopName().contains("小咖茶餐厅")
                                || shopDetail.getShopName().contains("茶屿水果茶") || shopDetail.getShopName().contains("布尔烤肉")
                                || shopDetail.getShopName().contains("蔡大胖炸洋芋") || shopDetail.getShopName().contains("果然嗨")
                                || shopDetail.getShopName().contains("塔斯汀") || shopDetail.getShopName().contains("香佰里火锅")
                                || shopDetail.getShopName().contains("徐鼎盛私房菜") || shopDetail.getShopName().contains("奢玛莉")
                                || shopDetail.getShopName().contains("乡村基") || shopDetail.getShopName().contains("慧欣女装")){
                            index = i + 1;
                            ticketIdentifyRecord.setPayment(Double.parseDouble(wordsResults.get(index).getWords().trim()));
                        }else if(shopDetail.getShopName().contains("邻食魔珐")){
                            ticketIdentifyRecord.setPayment(Double.parseDouble(wordsResults.get(i).getWords().split(ticketFieldObject.getString("payment"))[1].split("元")[0].trim()));
                        }else if(shopDetail.getShopName().contains("麻辣工坊") || shopDetail.getShopName().contains("衫戈男装")){
                            index = i + 2;
                            ticketIdentifyRecord.setPayment(Double.parseDouble(wordsResults.get(index).getWords().trim()));
                        }else if(shopDetail.getShopName().contains("名创优品") || shopDetail.getShopName().contains("龙记香酥鸡锁骨")
                                || shopDetail.getShopName().contains("意尔康") || shopDetail.getShopName().contains("茶歪歪")
                                || shopDetail.getShopName().contains("街调女装")){
                            index = i + 1;
                            if(wordsResults.get(index).getWords().contains("元")){
                                ticketIdentifyRecord.setPayment(Double.parseDouble(wordsResults.get(index).getWords().split("元")[0].trim()));
                            }else if(wordsResults.get(index).getWords().contains("￥")){
                                ticketIdentifyRecord.setPayment(Double.parseDouble(wordsResults.get(index).getWords().split("￥")[1].trim()));
                            }
                        }else{
                            if(wordsResults.get(i).getWords().split(ticketFieldObject.getString("payment"))[1].contains("￥")){
                                double payment = Double.parseDouble(wordsResults.get(i).getWords().split(ticketFieldObject.getString("payment"))[1].split("￥")[1].trim());
                                if(payment > 0){
                                    ticketIdentifyRecord.setPayment(payment);
                                }
                            }else{
                                ticketIdentifyRecord.setPayment(Double.parseDouble(wordsResults.get(i).getWords().split(ticketFieldObject.getString("payment"))[1].trim()));
                            }
                        }
                    }
                }
                if(StringUtils.isBlank(ticketIdentifyRecord.getOrderNumber())){
                    return ServerResponseEntity.showFailMsg("小票识别不完整，无法积分");
                }
                if(ticketIdentifyRecord.getPayment() == null){
                    return ServerResponseEntity.showFailMsg("小票识别不完整，无法积分");
                }
                //3天内小票可积分
                if(StringUtils.isNotBlank(ticketIdentifyRecord.getSaleTime())){
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    String s = ticketIdentifyRecord.getSaleTime();
                    if(s.contains("/")){
                        s = s.replaceAll("/","-");
                    }
                    if(s.contains("-")){
                        char[] c = s.toCharArray();
                        int num = 0;// 记录b出现的次数
                        for (int i = 0; i < c.length; i++) {
                            if ("-".equals((c[i]) + "")) {
                                num++;
                            }
                        }
                        if(num == 1){
                            s = df.format(new Date()).substring(0,4) + "-" + s;
                            if(s.length() >= 10){
                                s = s.substring(0,s.lastIndexOf("-")+3);
                            }else{
                                s = df.format(df.parse(s));
                                String dateStr = s;
                                s = s.substring(0,dateStr.length());
                            }
                            Date dt1 = df.parse(s);
                            Date newDate = df.parse(df.format(new Date()));
                            DateTime dateTime = DateUtil.offsetDay(dt1, 3);
                            if(dateTime.getTime() < newDate.getTime()){
                                return ServerResponseEntity.showFailMsg("该小票积分已超出3天有效期！");
                            }
                        }else if(num == 2){
                            if(s.length() >= 10){
                                s = s.substring(0,s.lastIndexOf("-")+3);
                            }else{
                                s = df.format(df.parse(s));
                                String dateStr = s;
                                s = s.substring(0,dateStr.length());
                            }
                            Date dt1 = df.parse(s);
                            Date newDate = df.parse(df.format(new Date()));
                            DateTime dateTime = DateUtil.offsetDay(dt1, 3);
                            if(dateTime.getTime() < newDate.getTime()){
                                return ServerResponseEntity.showFailMsg("该小票积分已超出3天有效期！");
                            }
                        }
                    }
                }
                //查询小票是否已积分
                QueryWrapper<TicketIdentifyRecord> ticketIdentifyRecordQueryWrapper = new QueryWrapper<>();
                if(StringUtils.isNotBlank(ticketIdentifyRecord.getOrderNumber())){//有订单号
                    ticketIdentifyRecordQueryWrapper.lambda().eq(TicketIdentifyRecord::getOrderNumber,ticketIdentifyRecord.getOrderNumber());
                }else{//无订单号用订单时间
                    ticketIdentifyRecordQueryWrapper.lambda().eq(TicketIdentifyRecord::getSaleTime,ticketIdentifyRecord.getSaleTime());
                }
                ticketIdentifyRecordQueryWrapper.lambda().eq(TicketIdentifyRecord::getShopId,identifyRecord.getShopId());
                ticketIdentifyRecordQueryWrapper.last("LIMIT 1");
                TicketIdentifyRecord one = ticketIdentifyRecordService.getOne(ticketIdentifyRecordQueryWrapper);
                if(one != null){
                    return ServerResponseEntity.showFailMsg("该小票已积分！");
                }
                int score = new Double(Arith.div(ticketIdentifyRecord.getPayment(), Double.parseDouble(moneyScore),0)).intValue();
                //翻倍后的积分
                score = Math.round(score * Float.parseFloat(scoreMultiple));
                ticketIdentifyRecord.setImgUrl(identifyRecord.getImgUrl());
                ticketIdentifyRecord.setShopId(identifyRecord.getShopId());
                ticketIdentifyRecord.setShopName(identifyRecord.getShopName());
                ticketIdentifyRecord.setUserMobile(userInfo.getUserMobile());
                ticketIdentifyRecord.setCreateTime(new Date());
                ticketIdentifyRecord.setScore((long) score);
                ticketIdentifyRecordService.save(ticketIdentifyRecord);

                UserUpdateParam userUpdateScore = new UserUpdateParam();
                List<String> userIds = new ArrayList<>();
                userIds.add(userId);
                userUpdateScore.setUserIds(userIds);

                // 添加积分日志
                UserScoreLog userScoreLog = new UserScoreLog();
                userScoreLog.setScore((long) score);
                userScoreLog.setUserId(userId);
                userScoreLog.setSource(ScoreLogType.TIXKET_POINTS.value());
                userScoreLog.setCreateTime(new Date());
                userScoreLog.setIoType(score > 0 ? 1 : 0);
                userScoreLog.setShopId(identifyRecord.getShopId());
                userScoreLogService.save(userScoreLog);

                // 添加积分明细
                UserScoreDetail addDetail = new UserScoreDetail();
                addDetail.setStatus(1);
                addDetail.setUserId(userId);
                addDetail.setUsableScore((long) score);
                addDetail.setCreateTime(new Date());
                userScoreDetailService.saveUserScoreDetail(addDetail);

                long nowScore = userExtension.getScore() + (long) score;
                userExtension.setScore(nowScore);
                userExtension.setUpdateTime(new Date());
                userExtensionService.updateById(userExtension);
            }else{
                return ServerResponseEntity.showFailMsg("小票识别失败，积分增加失败！");
            }
            return ServerResponseEntity.success(true);
        } catch (Exception e) {
            // 查询出错
            throw new YamiShopBindException("小票识别失败");
        }
    }

    @PostMapping("/lyAddScore")
    @ApiOperation(value = "绿源小票积分", notes = "绿源小票积分")
    @Transactional(rollbackFor = Exception.class)
    public ServerResponseEntity<Boolean> lyAddScore(@RequestBody TicketIdentifyRecord ticketIdentify) throws Exception {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        if(StringUtils.isNotBlank(ticketIdentify.getSaleTime())){
            Date saleTime = df.parse(df.format(df.parse(ticketIdentify.getSaleTime())));
            DateTime dateTime = DateUtil.offsetDay(saleTime, 3);
            Date newDate = df.parse(df.format(new Date()));
            if(dateTime.getTime() < newDate.getTime()){
                return ServerResponseEntity.showFailMsg("该小票积分已超出3天有效期！");
            }
        }
        //积分倍数
        String scoreMultiple = sysConfigService.getConfigValue("SCORE_MULTIPLE");
        //积分兑换比例
        String moneyScore = "";
        ShopDetail shopInfo = shopDetailService.getById(ticketIdentify.getShopId());
        //店铺配置了积分兑换金额就用店铺配置，否则用系统配置
        if(shopInfo.getTicketRatio() != null){
            moneyScore = shopInfo.getTicketRatio().toString();
        }else{
            moneyScore = sysConfigService.getConfigValue("MONEY_SCORE");
        }
        String userId = SecurityUtils.getUser().getUserId();
        User userInfo = userService.getUserByUserId(userId);
        ticketIdentify.setUserMobile(userInfo.getUserMobile());
        UserExtension userExtension = userExtensionService.getByUserId(userId);
        //查询小票是否已积分
        QueryWrapper<TicketIdentifyRecord> ticketIdentifyRecordQueryWrapper = new QueryWrapper<>();
        ticketIdentifyRecordQueryWrapper.lambda().eq(TicketIdentifyRecord::getOrderNumber,ticketIdentify.getOrderNumber());
        TicketIdentifyRecord one = ticketIdentifyRecordService.getOne(ticketIdentifyRecordQueryWrapper);
        if(one != null){
            return ServerResponseEntity.showFailMsg("该小票已积分！");
        }
        if(StringUtils.isBlank(ticketIdentify.getOrderNumber()) || ticketIdentify.getPayment() == null){
            return ServerResponseEntity.showFailMsg("请扫描绿源小票二维码");
        }
        int score = new Double(Arith.div(ticketIdentify.getPayment(), Double.parseDouble(moneyScore),0)).intValue();
        //翻倍后的积分
        score = Math.round(score * Float.parseFloat(scoreMultiple));
        ticketIdentify.setScore((long) score);
        ticketIdentify.setCreateTime(new Date());
        ticketIdentifyRecordService.save(ticketIdentify);

        UserUpdateParam userUpdateScore = new UserUpdateParam();
        List<String> userIds = new ArrayList<>();
        userIds.add(userId);
        userUpdateScore.setUserIds(userIds);

        // 添加积分日志
        UserScoreLog userScoreLog = new UserScoreLog();
        userScoreLog.setScore((long) score);
        userScoreLog.setUserId(userId);
        userScoreLog.setSource(ScoreLogType.TIXKET_POINTS.value());
        userScoreLog.setCreateTime(new Date());
        if(ticketIdentify.getShopId() != null){
            userScoreLog.setShopId(ticketIdentify.getShopId());
        }
        userScoreLog.setIoType(score > 0 ? 1 : 0);
        userScoreLogService.save(userScoreLog);

        // 添加积分明细
        UserScoreDetail addDetail = new UserScoreDetail();
        addDetail.setStatus(1);
        addDetail.setUserId(userId);
        addDetail.setUsableScore((long) score);
        addDetail.setCreateTime(new Date());
        userScoreDetailService.saveUserScoreDetail(addDetail);

        long nowScore = userExtension.getScore() + score;
        userExtension.setScore(nowScore);
        userExtension.setUpdateTime(new Date());
        userExtensionService.updateById(userExtension);
        return ServerResponseEntity.success(true);
    }
}
