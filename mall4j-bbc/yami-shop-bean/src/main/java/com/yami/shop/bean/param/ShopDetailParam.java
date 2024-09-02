/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.bean.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Yami
 */
@Data
public class ShopDetailParam {

    @NotBlank(message = "{yami.shop.name.not.blank}")
    @ApiModelProperty(value = "店铺名称(数字、中文，英文(可混合，不可有特殊字符)，可修改)")
    @Size(max = 50, message = "{yami.shop.name.len.less}")
    private String shopName;

    @Size(max = 200, message = "{yami.shop.desc.len.less}")
    @ApiModelProperty(value = "店铺简介(可修改)")
    private String intro;

    @ApiModelProperty(value = "店铺绑定手机号")
    @Size(max = 20, message = "{yami.shop.mobile.len.less}")
    private String mobile;

    @ApiModelProperty(value = "店铺联系电话")
    @NotBlank(message = "{yami.shop.phone.not.blank}")
    @Size(max = 20, message = "{yami.shop.phone.len.less}")
    private String tel;

    @ApiModelProperty(value = "店铺详细地址")
    @NotBlank(message = "{yami.shop.address.not.blank}")
    @Size(max = 100, message = "{yami.shop.address.len.less}")
    private String shopAddress;

    @ApiModelProperty(value = "店铺所在省份（描述）")
    @NotBlank(message = "{yami.shop.province.not.blank}")
    @Size(max = 15, message = "{yami.shop.province.len.less}")
    private String province;

    @ApiModelProperty(value = "店铺所在省份Id")
    @NotNull(message = "{yami.shop.province.id.not.blank}")
    private Long provinceId;

    @ApiModelProperty(value = "店铺所在城市（描述）")
    @NotBlank(message = "{yami.shop.city.not.blank}")
    @Size(max = 15, message = "{yami.shop.city.len.less}")
    private String city;

    @ApiModelProperty(value = "店铺所在城市Id")
    private Long cityId;

    @ApiModelProperty(value = "店铺所在区域（描述）")
    @NotBlank(message = "{yami.shop.area.not.blank}")
    @Size(max = 15, message = "{yami.shop.area.len.less}")
    private String area;

    @ApiModelProperty(value = "店铺所在区域Id")
    private Long areaId;

    @ApiModelProperty(value = "店铺logo")
    @NotBlank(message = "{yami.shop.logo.not.blank}")
    @Size(max = 200, message = "{yami.shop.logo.len.less}")
    private String shopLogo;

    @ApiModelProperty(value = "店铺楼层")
    @NotBlank(message = "店铺楼层不为空")
    @Size(max = 50, message = "店铺楼层长度不能超过50")
    private String floor;

    @ApiModelProperty(value = "店铺房号")
    @NotBlank(message = "店铺房号不为空")
    @Size(max = 50, message = "店铺房号长度不能超过50")
    private String roomNumber;

    @ApiModelProperty(value = "店铺相册")
    @Size(max = 1000, message = "{yami.shop.photo.album.len.less}")
    private String shopPhotos;

    @ApiModelProperty(value = "营业执照")
    @Size(max = 200, message = "{yami.business.license.len.less}")
    private String businessLicense;

    @ApiModelProperty(value = "身份证正面")
    @Size(max = 200, message = "{yami.id.front.len.less}")
    private String identityCardFront;

    @ApiModelProperty(value = "店铺接收短信通知手机号")
    @Size(max = 11, message = "评店铺接收短信通知手机号应该为{max}")
    private String receiveMobile;

    @ApiModelProperty(value = "身份证反面")
    @Size(max = 200, message = "{yami.id.negative.len.less}")
    private String identityCardLater;

    @ApiModelProperty(value = "店铺所在纬度")
    @NotBlank(message = "{yami.latitude.not.blank}")
    private String shopLat;

    @ApiModelProperty(value = "店铺所在经度")
    @NotBlank(message = "{yami.longitude.not.blank}")
    private String shopLng;

    @ApiModelProperty("移动端背景图")
    private String mobileBackgroundPic;

    @ApiModelProperty("pc背景图")
    private String pcBackgroundPic;

    @ApiModelProperty("商家名称")
    private String merchantName;

    @ApiModelProperty(value = "邮箱", required = true)
    private String email;

    @ApiModelProperty("行业类型")
    private String industryType;

    @ApiModelProperty("店铺类型")
    private String storeType;

    @ApiModelProperty(value = "店铺顶部图")
    @Size(max = 200, message = "长度太长")
    private String shopTopImg;

    @ApiModelProperty("是否开启小票积分(0：不开启 1：开启)")
    private Integer isTicketScore;
}
