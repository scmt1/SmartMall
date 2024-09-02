package com.yami.shop.coupon.platform.controller;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.coupon.common.model.Coupon;
import com.yami.shop.coupon.common.model.CouponOnly;
import com.yami.shop.coupon.common.service.CouponOnlyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController("adminCouponOnlyController")
@RequestMapping("/platform/couponOnly")
@Api(tags = "平台端优惠券接口")
public class CouponOnlyController {
    @Autowired
    private CouponOnlyService couponOnlyService;
    @Autowired
    private Snowflake snowflake;

    @PostMapping("/save")
    @ApiOperation(value = "分页获取优惠券列表信息")
    public ServerResponseEntity<Object> save(@RequestBody List<CouponOnly> couponList) {
        List<Long> couponIdList = couponList.stream().map(item -> item.getCouponId()).collect(Collectors.toList());
        int count = couponOnlyService.count(new LambdaQueryWrapper<CouponOnly>().in(CouponOnly::getCouponId, couponIdList));
        if(count > 0) {
            ServerResponseEntity.showFailMsg("选择的优惠券里已有优惠券存在于其他组合了");
        }
        long groupNum = snowflake.nextId();
        couponList.forEach(item -> item.setGroupNum(groupNum));
        couponOnlyService.saveBatch(couponList);
        return ServerResponseEntity.success();
    }


    @DeleteMapping
    @ApiOperation(value = "删除绑定的优惠券组")
    public ServerResponseEntity<Void> delete(@RequestBody Long groupNum) {
        couponOnlyService.remove(new LambdaQueryWrapper<CouponOnly>().eq(CouponOnly::getGroupNum, groupNum));
        return ServerResponseEntity.success();
    }


    @GetMapping("/page")
    @ApiOperation(value = "分页获取优惠券列表信息")
    public ServerResponseEntity<IPage<CouponOnly>> page(PageParam<CouponOnly> page) {
        IPage<CouponOnly> couponPage = couponOnlyService.getPlatformPage(page);
        return ServerResponseEntity.success(couponPage);
    }

    @GetMapping("/getCouponByGroupNum")
    @ApiOperation(value = "分页获取优惠券列表信息")
    public ServerResponseEntity<List<Coupon>> getCouponByGroupNum(@RequestParam("groupNum") String groupNum) {
        List<Coupon> couponPage = couponOnlyService.getCouponByGroupNum(groupNum);
        return ServerResponseEntity.success(couponPage);
    }
}
