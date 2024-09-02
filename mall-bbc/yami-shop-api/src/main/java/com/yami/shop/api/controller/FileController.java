/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.api.controller;

import com.yami.shop.bean.app.dto.ResourcesInfoDto;
import com.yami.shop.bean.model.AttachFile;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.config.ShopConfig;
import com.yami.shop.service.AttachFileService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件上传 controller
 *
 * @author lgh
 */

@RestController
@RequestMapping("/p/file")
public class FileController {

    @Autowired
    private AttachFileService attachFileService;

    @Autowired
    private ShopConfig shopConfig;

    @PostMapping("/upload")
    @ApiOperation(value = "文件上传接口", notes = "上传文件，返回文件路径与域名")
    public ServerResponseEntity<ResourcesInfoDto> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ServerResponseEntity.success();
        }
        String fileName = attachFileService.uploadFile(file.getBytes(), file.getOriginalFilename());
        String resourcesUrl = shopConfig.getDomain().getResourcesDomainName() + "/";
        ResourcesInfoDto resourcesInfoDto = new ResourcesInfoDto();
        resourcesInfoDto.setResourcesUrl(resourcesUrl);
        resourcesInfoDto.setFilePath(fileName);
        return ServerResponseEntity.success(resourcesInfoDto);
    }

    @PostMapping("/uploadImFile")
    @ApiOperation(value = "聊天文件上传接口", notes = "上传文件，返回文件路径与域名")
    public ServerResponseEntity<ResourcesInfoDto> uploadImFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ServerResponseEntity.success();
        }
        String fileName = attachFileService.uploadImFile(file.getBytes(), file.getOriginalFilename());
        String resourcesUrl = shopConfig.getDomain().getResourcesDomainName() + "/";
        ResourcesInfoDto resourcesInfoDto = new ResourcesInfoDto();
        resourcesInfoDto.setResourcesUrl(resourcesUrl);
        resourcesInfoDto.setFilePath(fileName);
        return ServerResponseEntity.success(resourcesInfoDto);
    }

    @GetMapping("/get_file_by_id")
    @ApiOperation(value = "根据文件id获取文件信息")
    public ServerResponseEntity<AttachFile> getFileById(@RequestParam("fileId") Long fileId) {
        AttachFile attachFile = attachFileService.getById(fileId);
        return ServerResponseEntity.success(attachFile);
    }
}
