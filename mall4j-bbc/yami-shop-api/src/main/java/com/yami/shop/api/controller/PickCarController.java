package com.yami.shop.api.controller;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yami.shop.api.utils.Md5Util;
import com.yami.shop.bean.model.CarOrder;
import com.yami.shop.bean.model.User;
import com.yami.shop.bean.pay.CarPayInfoDto;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.security.api.model.YamiUser;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.SysConfigService;
import com.yami.shop.service.UserService;
import com.yami.shop.utils.JsonUtils;
import com.yami.shop.utils.OkHttpUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Year;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 车辆管理接口信息
 * @author yami
 */
@RestController
@Api(tags = "车辆管理接口信息")
@RequestMapping("/car/carManage")
public class PickCarController {
    @Autowired
    private SysConfigService sysConfigService;
    @Autowired
    private UserService userService;


    @ApiOperation(value = "车辆停车支付结果推送", notes = "车辆停车支付结果推送")
    @GetMapping("/carPutAPIPayResult")
    public ServerResponseEntity<Boolean> carPutAPIPayResult(CarPayInfoDto carPayInfoDto) throws Exception {
        carPayInfoDto.setResult(0);
        carPayInfoDto.setMsg("success");
        carPayInfoDto.setVersion(1);
        carPayInfoDto.setTotalAmount((int) (carPayInfoDto.getPayTotalAmount() * 100));
        long time = System.currentTimeMillis()/1000L;
        carPayInfoDto.setPayTime(String.valueOf(time));
        String secretkey = sysConfigService.getConfigValue("secretkey");
        String str = "pay_type=" + carPayInfoDto.getPayType() + "&paytime=" +
                carPayInfoDto.getPayTime() + "&total_amount=" + carPayInfoDto.getTotalAmount() +
                "&tradeno=" + carPayInfoDto.getTradeNo() + "&version=1&key=" + secretkey;
        String sign = Md5Util.md5(str);
        JSONObject requestObj = new JSONObject();
        requestObj.put("result", carPayInfoDto.getResult());
        if(carPayInfoDto.getResult() == 0){
            requestObj.put("msg", "success");
        }else{
            requestObj.put("msg", "支付失败");
        }
        requestObj.put("version",carPayInfoDto.getVersion());
        requestObj.put("paytime", carPayInfoDto.getPayTime());
        requestObj.put("sign", sign);
        requestObj.put("tradeno", carPayInfoDto.getTradeNo());
        requestObj.put("pay_type", carPayInfoDto.getPayType());
        requestObj.put("total_amount", carPayInfoDto.getTotalAmount());

        String postResult = OkHttpUtils.syncHttps("https://www.airenche.com/pub/APIPayResult?tk=5381&sid=9e2419f1"
                ,"POST", headers1(),requestObj.toJSONString(),"application/json");
        JSONObject jsonObject = JSONObject.parseObject(postResult);
        if ("success".equals(jsonObject.getString("code"))) {
            return ServerResponseEntity.success(true);
        }else{
            return ServerResponseEntity.showFailMsg("推送失败");
        }

    }

    @ApiOperation(value = "车辆停车支付结果推送", notes = "车辆停车支付结果推送")
    @GetMapping("/aPIPayResult")
    public Boolean aPIPayResult(@RequestBody CarPayInfoDto carPayInfoDto) throws Exception {
        String secretkey = sysConfigService.getConfigValue("secretkey");
        String str = "pay_type=" + carPayInfoDto.getPayType() + "&paytime=" +
                carPayInfoDto.getPayTime() + "&total_amount=" + carPayInfoDto.getTotalAmount() +
                "&tradeno=" + carPayInfoDto.getTradeNo() + "&version=1&key=" + secretkey;
        String sign = Md5Util.md5(str);
        JSONObject requestObj = new JSONObject();
        requestObj.put("result", carPayInfoDto.getResult());
        if(carPayInfoDto.getResult() == 0){
            requestObj.put("msg", "success");
        }else{
            requestObj.put("msg", "支付失败");
        }
        requestObj.put("version",carPayInfoDto.getVersion());
        requestObj.put("paytime", carPayInfoDto.getPayTime());
        requestObj.put("sign", sign);
        requestObj.put("tradeno", carPayInfoDto.getTradeNo());
        requestObj.put("pay_type", carPayInfoDto.getPayType());
        requestObj.put("total_amount", carPayInfoDto.getTotalAmount());

        String postResult = OkHttpUtils.syncHttps("https://www.airenche.com/pub/APIPayResult?tk=5381&sid=9e2419f1"
                ,"POST", headers1(),requestObj.toJSONString(),"application/json");
        JSONObject jsonObject = JSONObject.parseObject(postResult);
        if ("success".equals(jsonObject.getString("code"))) {
            return true;
        }else{
            return false;
        }

    }

    @ApiOperation(value = "查询车辆停车信息", notes = "查询车辆停车信息")
    @GetMapping("/queryCarPrice")
    public ServerResponseEntity<CarOrder> carGetItem(CarOrder carOrder) throws Exception {
        String url = "";
        url = "&carno=" + carOrder.getCarno() + "&rid=3170";

        CarOrder carPriceInfo = null;
        Integer parktime = 0;
        String pricePostResult = OkHttpUtils.okHttpGet("https://www.airenche.com/carshop/APIQueryPrice?tk=5381&sid=9e2419f1" + url, headers());
        JSONObject jsonObject1 = JSONObject.parseObject(pricePostResult);
        if("0".equals(jsonObject1.getString("result"))){
            carPriceInfo = JsonUtils.fromJson(jsonObject1.getString("data"), CarOrder.class);
            parktime = carPriceInfo.getParktime();
        }else if("1004".equals(jsonObject1.getString("result"))){
            return ServerResponseEntity.showFailMsg("未查询到车辆信息！");
        }else if("2602".equals(jsonObject1.getString("result"))){
            return ServerResponseEntity.showFailMsg("未查询到车辆入场记录！");
        }else{
            return ServerResponseEntity.showFailMsg("车辆信息查询失败！");
        }
        String postResult = OkHttpUtils.okHttpGet("https://www.airenche.com/carshop/APICreateTmpOrder?tk=5381&sid=9e2419f1" + url, headers());
        JSONObject jsonObject = JSONObject.parseObject(postResult);
        if ("2904".equals(jsonObject.getString("result")) && "price=0".equals(jsonObject.getString("msg"))) {
            carPriceInfo = JsonUtils.fromJson(jsonObject.getString("data"), CarOrder.class);
            if(carPriceInfo == null){
                carPriceInfo = new CarOrder();
                carPriceInfo.setParktime(parktime);
                carPriceInfo.setPrice(Double.valueOf(0));
                return ServerResponseEntity.success(carPriceInfo);
            }else{
                return ServerResponseEntity.success(carPriceInfo);
            }
        }else if("0".equals(jsonObject.getString("result"))){
            carPriceInfo = JsonUtils.fromJson(jsonObject.getString("data"), CarOrder.class);
            return ServerResponseEntity.success(carPriceInfo);
        }else{
            return ServerResponseEntity.showFailMsg("车辆信息查询失败！");
        }
    }

    @ApiOperation(value = "查询当前道闸设别车辆信息", notes = "查询车辆信息")
    @GetMapping("/queryCarInfo")
    public ServerResponseEntity<String> queryCarInfo() throws Exception {
        String url = "";
        url = "&rid=3170&cid=9387";

        CarOrder carPriceInfo = null;
        String pricePostResult = OkHttpUtils.okHttpGet("https://www.airenche.com/carshop/APIQueryPrice?tk=5381&sid=9e2419f1" + url, headers());
        JSONObject jsonObject1 = JSONObject.parseObject(pricePostResult);
        if("0".equals(jsonObject1.getString("result"))){
            carPriceInfo = JsonUtils.fromJson(jsonObject1.getString("data"), CarOrder.class);
            return ServerResponseEntity.success(carPriceInfo.getCarno());
        }else if("1004".equals(jsonObject1.getString("result"))){
            return ServerResponseEntity.success();
        }else if("2602".equals(jsonObject1.getString("result"))){
            return ServerResponseEntity.success();
        }else{
            return ServerResponseEntity.success();
        }
    }

    @ApiOperation(value = "用户绑定车辆", notes = "用户绑定车辆")
    @GetMapping("/bindCard")
    @Transactional(rollbackFor = Exception.class)
    public ServerResponseEntity<String> bindCard(CarOrder carOrder) throws Exception {
        User one = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUserMobile, carOrder.getPhone()));
        if(one != null){
            one.setCarNo(carOrder.getCarno());
            userService.updateById(one);
            return ServerResponseEntity.success("车牌号绑定成功！");
        }else{
            return ServerResponseEntity.showFailMsg("车牌号绑定失败！");
        }
    }

    @ApiOperation(value = "绑定黄金会员车辆为临时车", notes = "绑定黄金会员车辆为临时车")
    @GetMapping("/bindTemporaryCar")
    @Transactional(rollbackFor = Exception.class)
    public ServerResponseEntity<String> bindTemporaryCar(CarOrder carOrder) throws Exception {
        User one = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUserMobile, carOrder.getPhone()));
        if(one != null){
            if(StringUtils.isNotBlank(one.getCarNo())){
                return ServerResponseEntity.showFailMsg("您已绑定车牌号，无法再次绑定！");
            }
        }else{
            return ServerResponseEntity.showFailMsg("未查询到用户信息，车牌号绑定失败！");
        }
        Boolean flag = false;
        //查询车辆在停车系统是否已有绑定记录
        String postResult = OkHttpUtils.okHttpGet("https://www.airenche.com/carshop/CarGetItem?tk=5381&sid=9e2419f1&rid=[3170]" + "&carno=" + carOrder.getCarno(), headers());
        JSONObject jsonObject = JSONObject.parseObject(postResult);
        if("0".equals(jsonObject.getString("result"))){
            return ServerResponseEntity.showFailMsg("已存在车辆信息，无法再次绑定！");
        }else if("1004".equals(jsonObject.getString("result"))){
            flag = true;
        }
        if(flag){
            //广场绑定用户车牌
            one.setCarNo(carOrder.getCarno());
            userService.updateById(one);
            //绑定车辆为限时车
            String url = "";
            long starttime = new Date().getTime() / 1000;
            long endtime = DateUtil.offset(new Date(), DateField.YEAR, 99).getTime() / 1000;
            url = "&phone=" + carOrder.getPhone() + "&carno=" + carOrder.getCarno() +
                    "&starttime=" + starttime + "&endtime=" + endtime +
                    "&regtype=11&endofday=79200&startofday=25200&limit_can_in=1";
            String blueCarPayId = sysConfigService.getConfigValue("BLUE_CAR_PAY_ID");
            String greenCarPayId = sysConfigService.getConfigValue("GREEN_CAR_PAY_ID");
            if (StringUtils.isNotBlank(carOrder.getCarno())) {
                int length = carOrder.getCarno().length();
                if (length == 7) {
                    url = url + "&tchid=" + blueCarPayId;
                } else if (length == 8) {
                    url = url + "&tchid=" + greenCarPayId;
                }
            }
            String pricePostResult = OkHttpUtils.okHttpGet("https://www.airenche.com/carshop/CarAdd?tk=5381&sid=9e2419f1&rid=[3170]" + url, headers());
            JSONObject jsonObject1 = JSONObject.parseObject(pricePostResult);
            if("0".equals(jsonObject1.getString("result"))){
                return ServerResponseEntity.success("车牌号绑定成功！");
            }else{
                return ServerResponseEntity.showFailMsg("车牌号绑定失败！");
            }
        }
        return ServerResponseEntity.success();
    }

//    @ApiOperation(value = "开闸", notes = "开闸")
//    @GetMapping("/monitorChannelOpen")
//    public ServerResponseEntity<Boolean> monitorChannelOpen(CarOrder carOrder) throws Exception {
//        String url = "";
//        url = "&carno=" + carOrder.getCarno() + "&cid=9387";
//        String postResult = OkHttpUtils.okHttpGet("https://www.airenche.com/carshop/MonitorChannelOpen?tk=5381&sid=9e2419f1" + url, headers());
//        JSONObject jsonObject = JSONObject.parseObject(postResult);
//        if ("0".equals(jsonObject.getString("result"))) {
//            return ServerResponseEntity.success(true);
//        }else{
//            return ServerResponseEntity.showFailMsg("查询异常！");
//        }
//    }

    @ApiOperation(value = "停车系统报文封装", notes = "停车系统报文封装")
    public Map<String, String> headers() throws Exception {
        String secretkey = sysConfigService.getConfigValue("secretkey");
        long stamp = System.currentTimeMillis()/1000L;
        String sign = Md5Util.md5((int)stamp + secretkey);
        Map<String, String> headers = new HashMap<>();
        String stampStr = String.valueOf(stamp);
        String str = "stamp=" + stampStr + ";sign=" + sign;
        headers.put("Cookie", str);
        return headers;
    }

    @ApiOperation(value = "停车系统报文封装", notes = "停车系统报文封装")
    public Map<String, Object> headers1() throws Exception {
        String secretkey = sysConfigService.getConfigValue("secretkey");
        long stamp = System.currentTimeMillis()/1000L;
        String sign = Md5Util.md5((int)stamp + secretkey);
        Map<String, Object> headers = new HashMap<>();
        String stampStr = String.valueOf(stamp);
        String str = "stamp=" + stampStr;
        headers.put("Cookie", str);
        return headers;
    }
}
