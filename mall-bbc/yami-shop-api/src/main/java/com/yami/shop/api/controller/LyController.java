package com.yami.shop.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yami.shop.bean.model.LyConsumption;
import com.yami.shop.bean.model.User;
import com.yami.shop.bean.model.UserExtension;
import com.yami.shop.bean.param.GrowthParamConfig;
import com.yami.shop.bean.param.ScoreConfigParam;
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.Arith;
import com.yami.shop.dao.UserExtensionMapper;
import com.yami.shop.dao.UserMapper;
import com.yami.shop.security.common.service.AppConnectService;
import com.yami.shop.service.LyConsumptionService;
import com.yami.shop.service.SysConfigService;
import com.yami.shop.user.common.service.UserLevelService;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Date;
import java.util.Objects;

@ApiIgnore
@RestController
@RequestMapping("/lySystem")
@AllArgsConstructor
@Slf4j
public class LyController {
    @Autowired
    private LyConsumptionService lyConsumptionService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AppConnectService appConnectService;

    @Autowired
    private SysConfigService sysConfigService;

    @Autowired
    private UserExtensionMapper userExtensionMapper;

    @Autowired
    private UserLevelService userLevelService;


    @PostMapping("/addConsumption")
    @ApiOperation(value = "保存绿源超市消费消费信息", notes = "保存绿源超市消费消费信息")
    public ServerResponseEntity<?> addConsumption(@RequestBody LyConsumption consumption) {
        consumption.setCreateTime(new Date());
        lyConsumptionService.save(consumption);

        //保存成功后根据用户消费金额积分 如果用户不存在，默认注册成会员
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserMobile, consumption.getMobile()));
        // 没有就注册
        if (user == null) {
            // 注册并绑定用户
            user = appConnectService.registerAndBindUser(consumption.getMobile(), null, null);
        }

        try {
            UserExtension userExtension = userExtensionMapper.selectOne(new LambdaQueryWrapper<UserExtension>().eq(UserExtension::getUserId, user.getUserId()));
            //获取积分和成长值获取比例
            ScoreConfigParam scoreParam = sysConfigService.getSysConfigObject(Constant.SCORE_CONFIG, ScoreConfigParam.class);
            GrowthParamConfig growthParamConfig = sysConfigService.getSysConfigObject(Constant.GROWTH_CONFIG, GrowthParamConfig.class);
            if (Objects.isNull(scoreParam) && Objects.isNull(growthParamConfig)) {
                return ServerResponseEntity.success("保存成功");
            }
            double scoreLimit;
            double score = Arith.div(consumption.getMoney(), scoreParam.getShopGetScore());
            double growthPrice = growthParamConfig.getBuyOrder();

            double getDiscount = Objects.isNull(scoreParam.getGetDiscount()) ? 0.0 : scoreParam.getGetDiscount();
            scoreLimit = Arith.div(Arith.mul(consumption.getMoney(), getDiscount), 100);

            scoreLimit = Arith.div(scoreLimit, scoreParam.getShopGetScore());
            //上限取最小值
            score = Math.min(score, scoreLimit);
            //计算出成长值
            growthPrice = Arith.div(consumption.getMoney(), growthParamConfig.getBuyPrice());
            userLevelService.addGrowthAndScore(growthPrice, (long) score, user.getUserId(), "666666", userExtension, 1);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("会员积分异常：{}", e.getMessage());
        }
        return ServerResponseEntity.success("保存成功");
    }
}
