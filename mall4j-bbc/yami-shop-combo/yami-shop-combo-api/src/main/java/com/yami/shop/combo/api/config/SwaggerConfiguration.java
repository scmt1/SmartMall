/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.api.config;


import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * 套餐、赠品
 * @author lanhai
 */
@Configuration("comboSwaggerConfiguration")
@AllArgsConstructor
public class SwaggerConfiguration {

    private final ApiInfo apiInfo;

    @Bean
    public Docket comboRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .groupName("套餐、赠品接口")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.yami.shop.combo.api.controller"))
                .paths(PathSelectors.any())
                .build();
    }


}
