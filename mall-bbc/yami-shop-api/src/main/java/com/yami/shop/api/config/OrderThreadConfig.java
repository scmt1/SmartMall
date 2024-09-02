package com.yami.shop.api.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author FrozenWatermelon
 * @date 2020/12/15
 */
@Configuration
@EnableConfigurationProperties(ThreadPoolConfigProperties.class)
public class OrderThreadConfig {

    /**
     * 因为每台机器不同，所以线程数量应该是通过配置文件进行配置的
     * @param pool 线程池配置信息
     * @return 订单线程池
     */
    @Bean
    public ThreadPoolExecutor orderThreadPoolExecutor(ThreadPoolConfigProperties pool) {
        return new ThreadPoolExecutor(
                pool.getCoreSize(),
                pool.getMaxSize(),
                pool.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100000),
                new ThreadFactoryBuilder()
                        .setNameFormat("Order-Thread-Pool-%d").build()
        );
    }
}
