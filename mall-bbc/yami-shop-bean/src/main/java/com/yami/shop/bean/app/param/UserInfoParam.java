/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.bean.app.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Yami
 */
@Data
@ApiModel(value= "设置用户信息")
public class UserInfoParam {

    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @ApiModelProperty(value = "用户头像")
    private String avatarUrl;

    @ApiModelProperty(value = "用户性别")
    private String sex;

    @ApiModelProperty(value = "邮箱")
    private String userMail;

    @ApiModelProperty(value = "用户生日")
    private String birthDate;

    @ApiModelProperty(value = "用户手机号")
    private String userMobile;


}
