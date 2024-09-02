/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.bean.app.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Yami
 */
@Data
public class ResourcesInfoDto {

    @ApiModelProperty(value = "服务器域名url",required=true)
    private String resourcesUrl;

    @ApiModelProperty(value = "文件上传的路径",required=true)
    private String filePath;
}
